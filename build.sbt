import com.typesafe.sbt.packager.archetypes.JavaAppPackaging

enablePlugins(JavaAppPackaging)

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "3.0.1"


lazy val root = (project in file(".")).
  aggregate(stream, web)

lazy val stream = (project in file("stream")).
  settings(
    inThisBuild(List(
      organization := "co.tjcelaya.smack.stream",
      scalaVersion := "2.11.8",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Stream",
    libraryDependencies ++= List(
      "org.scalatest" %% "scalatest" % "3.0.1",
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

lazy val web = (project in file("web")).
  settings(
    inThisBuild(List(
      organization := "co.tjcelaya.smack.web",
      scalaVersion := "2.11.8",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Web",
    libraryDependencies ++= List(
      "org.scalatest" %% "scalatest" % "3.0.1",
      "com.typesafe" % "config" % "1.3.1",
      "com.sparkjava" % "spark-core" % "2.5.4"
    )
  )
