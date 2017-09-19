package com.clicktale.kafkastreams

import akka.actor.ActorSystem
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import better.files.{File => ScalaFile}
import com.clicktale.kafkastreams.offsetstorage.DbMock
import com.clicktale.kafkastreams.s3endpoint.S3Client
import com.clicktale.kafkastreams.settings.Settings
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition

import scala.concurrent.Future
import scala.util.Success

object PocEngine extends App{
  private val db = new DbMock[String,Array[Byte]]()
  private val actorSystem = ActorSystem("poc-default-system")
  private val configs = ConfigFactory.load()
  implicit val mat = ActorMaterializer()(actorSystem)
  implicit val ex = actorSystem.dispatcher
  private val logger = Logger[PocEngine.type]

  def streamFilesToKafka() = {
    val folder = ScalaFile("src/main/resources/input_files")
    val source = Source.fromIterator(() => folder.children.map{f =>
      logger.debug(s"Streaming file: ${f.name} to kafka")
      f.name -> f.byteArray
    })
    source.map{case (k,v) => new ProducerRecord[String, Array[Byte]](configs.getString("transferer.kafka.poc_topic"), k,v)}.
      runWith(Producer.plainSink(Settings.prodSettings(actorSystem)))
  }

  def streamFromKafkaToS3() = {
    val s3 = S3Client()
    db.loadOffset().flatMap { fromOffset =>
      val partition = 0
      val subscription = Subscriptions.assignmentWithOffset(
        new TopicPartition(configs.getString("transferer.kafka.poc_topic"), partition) -> fromOffset
      )
      Consumer.plainSource(Settings.consSettings(actorSystem), subscription).mapAsync(1){
          rec => s3.putContentInS3(rec.key(),rec.value())
          Future(rec)
      }.mapAsync(1)(db.save _).runWith(Sink.ignore)
    }
  }

  def streamAutoCommmitableFromKafkaToS3() = {
    val s3 = S3Client()
    Consumer.committableSource(Settings.consSettings(actorSystem),
      Subscriptions.topics(configs.getString("transferer.kafka.poc_topic")))
      .mapAsync(1) { msg =>
        logger.debug(s"Streaming ${msg.record.key()} to S3")
        s3.putContentInS3(msg.record.key(),msg.record.value())
        Future(msg)
      }
      .mapAsync(1) { msg =>
        msg.committableOffset.commitScaladsl()
      }
      .runWith(Sink.ignore)
  }

  streamFilesToKafka andThen {case Success(_) => streamAutoCommmitableFromKafkaToS3()}
}

