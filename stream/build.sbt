enablePlugins(JavaServerAppPackaging)

lazy val stream = (project in file("."))
  .settings(
    name := "Stream",
    scalaVersion := Version.Scala,
    mainClass in Compile := Some("co.tjcelaya.smack.stream.Stream"),
    libraryDependencies ++= List(
      Library.scalaTest,
      Library.sparkRedis,
      Library.typesafeConfig,
      Library.sparkCassandra,
      "org.apache.spark" %% "spark-core" % "2.1.0",
      "org.apache.spark" %% "spark-streaming" % "2.1.0",
      Library.scalaLogging,
      Library.logback
    ),
    resolvers ++= List(
      "Spark Packages Repo" at "https://dl.bintray.com/spark-packages/maven"
    )
  )