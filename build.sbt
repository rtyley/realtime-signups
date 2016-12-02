name := "realtime-signups root project"

scalaVersion in ThisBuild := "2.11.8"

lazy val root = project.in(file(".")).
  aggregate(realtimeSignupsJS, realtimeSignupsJVM).
  settings(
    publish := {},
    publishLocal := {}
  )

val enumeratumVersion = "1.5.2"

lazy val realtimeSignups = crossProject.in(file(".")).
  settings(
    name := "realtime",
    version := "0.1-SNAPSHOT"
  ).jvmConfigure(_.enablePlugins(PlayScala)).jvmSettings(
  libraryDependencies ++= Seq(
    "com.vmunier" %% "scalajs-scripts" % "1.0.0",
    "com.beachape" %% "enumeratum" % enumeratumVersion,
    "com.lihaoyi" %% "upickle" % "0.4.4",
    "com.lihaoyi" %% "autowire" % "0.2.6",
    // "com.amazonaws" % "aws-java-sdk-kinesis" % "1.11.60",
    "com.amazonaws" % "amazon-kinesis-producer" % "0.12.3",
    "com.amazonaws" % "amazon-kinesis-client" % "1.7.2"
  )
).jsSettings(
  libraryDependencies ++= Seq(
    "com.beachape" %%% "enumeratum" % enumeratumVersion,
    "org.scala-js" %%% "scalajs-dom" % "0.9.1",
    "com.lihaoyi" %%% "upickle" % "0.4.4",
    "com.lihaoyi" %%% "autowire" % "0.2.6"
  )
).jsConfigure(_.enablePlugins(ScalaJSWeb))

lazy val realtimeSignupsJS = realtimeSignups.js

lazy val realtimeSignupsJVM = realtimeSignups.jvm.settings(
  scalaJSProjects := Seq(realtimeSignups.js),
  pipelineStages in Assets := Seq(scalaJSPipeline)
)
