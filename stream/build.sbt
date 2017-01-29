import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "co.tjcelaya.stream",
      scalaVersion := "2.11.1",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Stream",
    libraryDependencies ++= List(
      scalaTest,
      "RedisLabs" % "spark-redis" % "0.3.2",
      "com.typesafe" % "config" % "1.3.1",
      "datastax" % "spark-cassandra-connector" % "1.6.0-s_2.10",
      "org.apache.spark" %% "spark-core" %  "2.1.0",
      "org.apache.spark" %% "spark-streaming" %  "2.1.0"
    ),
    resolvers ++= List(
      "Spark Packages Repo" at "https://dl.bintray.com/spark-packages/maven"
    )
  )

enablePlugins(JavaAppPackaging)
