import Dependencies.Versions._

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.3"

name := "transferrer"

libraryDependencies ++=Seq(
  "net.cakesolutions" %% "scala-kafka-client" % simpleKafkaClientVersion,
  "com.typesafe" % "config" % configVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % akkaStreamKafkaVersion,
  "ch.qos.logback" % "logback-classic" % logBackVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.github.seratch" %% "awscala" % s3ScalaVersion,
  "com.github.pathikrit" %% "better-files" % betterFilesVersion
)

resolvers += Resolver.bintrayRepo("cakesolutions", "maven")

mainClass in assembly := Some("com.clicktale.kafka.Main")
assemblyJarName in assembly := "topicToTopic_transferer.jar"
