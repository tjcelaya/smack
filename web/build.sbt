import sbt.Keys.version

enablePlugins(JavaServerAppPackaging)

lazy val web = (project in file("."))
  .settings(
    name := "Web",
    scalaVersion := Version.Scala,
    packageName in Docker := "tjcelaya/smack-web",
    packageName := "tjcelaya/smack-web",
    version in Docker := "latest",
    maintainer in Docker := "Tomas Celaya <tjcelaya@gmail.com>",
    dockerUpdateLatest in Docker := true,
    dockerExposedPorts := Seq(8080),
    mainClass in Compile := Some("co.tjcelaya.smack.web.Web"),
    libraryDependencies ++= List(
      Library.scalaTest,
      Library.typesafeConfig,
      Library.akkaHttpCore,
      Library.akkaHttpExperimental,
      Library.scalaLogging,
      Library.logback
    )
  )
