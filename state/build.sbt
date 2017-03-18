import sbt.Keys.version

enablePlugins(JavaServerAppPackaging)

lazy val state = (project in file("."))
  .settings(
    name := "State",
    scalaVersion := Version.Scala,
    packageName in Docker := "tjcelaya/smack-state",
    packageName := "tjcelaya/smack-state",
    version in Docker := "latest",
    maintainer in Docker := "Tomas Celaya <tjcelaya@gmail.com>",
    dockerUpdateLatest in Docker := true,
    dockerExposedPorts := Seq(8080),
    initialCommands in console += "import akka.actor._; import akka.event._; import co.tjcelaya.smack.state.example.traffic._; import akka.actor.{Actor,ActorSystem,Props}; import akka.stream.ActorMaterializer; import akka.pattern.ask; import scala.util.Random;import akka.util.Timeout;import scala.concurrent.duration._; implicit val timeout = Timeout(2.seconds); ",
    mainClass in Compile := Some("co.tjcelaya.smack.state.Web"),
    libraryDependencies ++= List(
      Library.scalaTest,
      Library.scalaLogging,
      Library.logback,
      Library.typesafeConfig,
      Library.akkaActor,
      Library.akkaAgent,
      Library.akkaCamel,
      Library.akkaCluster,
      Library.akkaClusterMetrics,
      Library.akkaClusterSharding,
      Library.akkaClusterTools,
      Library.akkaMultiNodeTestkit,
      Library.akkaPersistence,
      Library.akkaPersistenceCassandra,
      Library.akkaRemote,
      Library.akkaSlf4j,
      Library.akkaStream,
      Library.akkaStreamTestkit,
      Library.akkaTestkit,
      Library.akkaPersistenceQueryExperimental,
      "de.aktey.akka.visualmailbox" %% "collector" % "1.1.0"
    )
  )
