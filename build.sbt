

val logging = "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
val logback = "ch.qos.logback" % "logback-classic" % "1.1.3"
val actor = "com.typesafe.akka" %% "akka-actor" % "2.3.14"
val liftJson = "net.liftweb" % "lift-json_2.11" % "2.6"
val dispatchLiftJson = "net.databinder.dispatch" % "dispatch-lift-json_2.11" % "0.11.3"
val typesafeConfig = "com.typesafe" % "config" % "1.3.0"
val kafka = "org.apache.kafka" % "kafka_2.11" % "0.8.2.2"

val slick =     "com.typesafe.slick" %% "slick" % "3.0.0"
val sqlite = "org.xerial" % "sqlite-jdbc" % "3.8.10.1"
val commonsPool = "org.apache.commons" % "commons-pool2" % "2.3"


val scalaTest = "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
val akkaTestKit = "com.typesafe.akka" % "akka-testkit_2.11" % "2.4.0" % "test"

val sprayCan = "io.spray" % "spray-can_2.11" % "1.3.3"
val sprayRouting =  "io.spray" % "spray-routing_2.11" % "1.3.3"
val sprayTestKit = "io.spray" % "spray-testkit_2.11" % "1.3.3"

lazy val commonSettings = Seq(
    organization := "botkop",
    version := "0.1.0",
    scalaVersion := "2.11.6"
)

lazy val root = (project in file(".")).
    settings(commonSettings: _*).
    settings(
        name := "actor-traffic",
        libraryDependencies ++= Seq(logging, logback,
            actor,
            liftJson, dispatchLiftJson,
            typesafeConfig,
            kafka,
            slick, sqlite, commonsPool,
            scalaTest, akkaTestKit,
            sprayCan, sprayRouting, sprayTestKit
        )
    )
