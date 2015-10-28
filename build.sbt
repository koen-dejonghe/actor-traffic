
val logging = "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
val logback = "ch.qos.logback" % "logback-classic" % "1.1.3"
val actor = "com.typesafe.akka" %% "akka-actor" % "2.3.14"
val liftJson = "net.liftweb" % "lift-json_2.11" % "2.6"
val dispatchLiftJson = "net.databinder.dispatch" % "dispatch-lift-json_2.11" % "0.11.3"
val typesafeConfig = "com.typesafe" % "config" % "1.3.0"


val scalaTest = "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
val akkaTestKit = "com.typesafe.akka" % "akka-testkit_2.11" % "2.4.0" % "test"


lazy val commonSettings = Seq(
    organization := "botkop",
    version := "0.1.0",
    scalaVersion := "2.11.6"
)

lazy val root = (project in file(".")).
    settings(commonSettings: _*).
    settings(
        name := "actor-traffic",
        libraryDependencies ++= Seq(logging, logback, actor, liftJson, dispatchLiftJson, typesafeConfig, scalaTest, akkaTestKit)
    )
