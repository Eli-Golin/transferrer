package com.clicktale.kafkastreams.settings

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, ProducerSettings}
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringDeserializer, StringSerializer}

object Settings {

  def prodSettings(system:ActorSystem) = {
    ProducerSettings(system, new StringSerializer, new ByteArraySerializer)
  }

  def consSettings(system:ActorSystem) = {
    ConsumerSettings(system, new StringDeserializer, new ByteArrayDeserializer)
  }
}
