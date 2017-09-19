package com.clicktale.kafkastreams.s3endpoint

import awscala.{Credentials, Region}
import awscala.s3.{PutObjectResult, S3}
import com.amazonaws.services.s3.model.ObjectMetadata
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Promise}

class S3Client {
import S3Client._

  def putContentInS3(key:String,content:Array[Byte])(implicit ex:ExecutionContext) = {
    val p = Promise[PutObjectResult]
    try {
      p.success {
        val bucket = s3.createBucket(bucketName)
        bucket.putObject(s"eli_poc/$key",content,new ObjectMetadata())
      }
    } catch {
      case ex:Exception => p.failure(ex)
    }
    p.future
  }
}

object S3Client {
  private val bucketName = ConfigFactory.load().getString("transferer.S3.poc_bucket")
  val (accKey,secKey) = {
    val conf = ConfigFactory.load()
    conf.getString("transferer.S3.access_key") -> conf.getString("transferer.S3.secret_key")
  }
  private implicit val region: Region = Region.US_EAST_1
  private implicit  val s3 = S3(Credentials(accKey,secKey))

  def apply() = new S3Client()
}