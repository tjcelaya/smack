import sbt.Keys.libraryDependencies

///////////////////////////////////
// deps
///////////////////////////////////

val slickVersion = "3.2.0"

///////////////////////////////////
// everything else
///////////////////////////////////

organization in ThisBuild := "co.tjcelaya.smack.service"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test

val usingEmbeddedCassandra = sys.env.getOrElse("EMBED_CASSANDRA", "1") == "1"

lagomCassandraEnabled in ThisBuild := usingEmbeddedCassandra
lagomKafkaEnabled in ThisBuild := sys.env.getOrElse("EMBED_KAFKA", "1") == "1"

val cassandraHost = sys.env.getOrElse("CASSANDRA_HOST", "localhost")
val cassandraPort = sys.env.getOrElse("CASSANDRA_PORT", 4000).toString.toInt
lagomCassandraPort in ThisBuild := cassandraPort

lagomCassandraCleanOnStart in ThisBuild := usingEmbeddedCassandra

lagomUnmanagedServices in ThisBuild := Map(
  if (usingEmbeddedCassandra)
    "null" -> "http://localhost:0"
  else
    "cas_native" -> s"http://$cassandraHost:$cassandraPort"
)

lagomCassandraJvmOptions in ThisBuild := Seq(
  "-Xms256m", "-Xmx1024m", "-Dcassandra.jmx.local.port=4099",
  "-DCassandraLauncher.configResource=dev-embedded-cassandra.yaml"
)


val serviceApiDefaultDeps = Seq(
  "com.nulab-inc" %% "scala-oauth2-core" % "1.3.0",
  lagomScaladslApi
)

val serviceImplDefaultDeps = Seq(
  lagomScaladslTestKit,
  macwire,
  scalaTest,
  "com.nulab-inc" %% "scala-oauth2-core" % "1.3.0",
  "org.zalando" %% "scala-jsonapi" % "0.6.2",
  "io.spray" %% "spray-httpx" % "1.3.3",
  "com.pauldijou" %% "jwt-play-json" % "0.12.0"
)

lazy val `service` = (project in file("."))
  .aggregate(
    `common`
    , `auth-api`
    , `auth-impl`
//    , `user-api`
//    , `user-impl`
  )

lazy val `common` = (project in file("common"))
  .settings(
    libraryDependencies ++= Seq(
      "com.google.guava" % "guava" % "16.0.1",
      "org.zalando" %% "scala-jsonapi" % "0.6.2",
      "io.spray" %% "spray-httpx" % "1.3.3",
      "com.fasterxml.uuid" % "java-uuid-generator" % "3.1.3",
      lagomScaladslApi,
      // TODO: find a way to depend on whatever version lagom depends?
      "com.typesafe.scala-logging" %% "scala-logging-api" % "2.1.2",
      "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2"

      // list of unused lagom packages for reference
      // lagomScaladslClient,
      // lagomScaladslServer,
      // lagomScaladslDevMode,
      // lagomScaladslCluster,
      // lagomScaladslBroker,
      // lagomScaladslKafkaClient,
      // lagomScaladslKafkaBroker,
      // lagomScaladslPersistence,
      // lagomScaladslPersistenceCassandra,
      // lagomScaladslPersistenceJdbc,
      // lagomScaladslPubSub,
    )
  )

lazy val `user-api` = (project in file("user-api"))
  .settings(
    libraryDependencies ++= serviceApiDefaultDeps
  )
  .dependsOn(`common`)

lazy val `user-impl` = (project in file("user-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= serviceImplDefaultDeps ++ Seq(
      lagomScaladslPersistenceCassandra
    ),
    lagomCassandraKeyspace := "user"
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`user-api`, `common`)


lazy val `auth-api` = (project in file("auth-api"))
  .settings(
    libraryDependencies ++= serviceApiDefaultDeps
  )
  .dependsOn(`common`)

lazy val `auth-impl` = (project in file("auth-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= (serviceImplDefaultDeps ++ Seq(
      "com.livestream" %% "scredis" % "2.0.6",
      "mysql" % "mysql-connector-java" % "6.0.5",
      lagomScaladslPersistenceJdbc,
      "com.lambdaworks" % "scrypt" % "1.4.0"
    ))
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`auth-api`, `common`)