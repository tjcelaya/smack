import sbt._

object Version {
  final val Akka = "2.4.17"
  final val AkkaHttp = "10.0.3"
  final val AkkaHttpJson = "1.7.0"
  final val AkkaPersistenceCassandra = "0.17"
  final val HikariCP = "2.4.5"
  final val Scala = "2.11.8"
  final val ScalaCheck = "1.13.0"
  final val ScalaTest = "3.0.1"
  final val Sl4j = "1.7.22"
  final val Slick = "3.1.1"
}

object Library {
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % Version.Akka
  val akkaAgent = "com.typesafe.akka" %% "akka-agent" % Version.Akka
  val akkaCamel = "com.typesafe.akka" %% "akka-camel" % Version.Akka
  val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % Version.Akka
  val akkaClusterMetrics = "com.typesafe.akka" %% "akka-cluster-metrics" % Version.Akka
  val akkaClusterSharding = "com.typesafe.akka" %% "akka-cluster-sharding" % Version.Akka
  val akkaClusterTools = "com.typesafe.akka" %% "akka-cluster-tools" % Version.Akka
  val akkaContrib = "com.typesafe.akka" %% "akka-contrib" % Version.Akka
  val akkaDistributedDataExperimental = "com.typesafe.akka" %% "akka-distributed-data-experimental" % Version.Akka
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % Version.AkkaHttp
  val akkaHttpCore = "com.typesafe.akka" %% "akka-http-core" % Version.AkkaHttp
  val akkaHttpExperimental = "com.typesafe.akka" %% "akka-http-experimental" % Version.AkkaHttp
  val akkaHttpJackson = "com.typesafe.akka" %% "akka-http-jackson" % Version.AkkaHttp
  val akkaHttpSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % Version.AkkaHttp
  val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit" % Version.AkkaHttp
  val akkaHttpXml = "com.typesafe.akka" %% "akka-http-xml" % Version.AkkaHttp

  val scalaOauth2Core = "com.nulab-inc" %% "scala-oauth2-core" % "1.3.0"
  val akkaHttpOauth2Provider = "com.nulab-inc" %% "akka-http-oauth2-provider" % "1.3.0"

  val akkaMultiNodeTestkit = "com.typesafe.akka" %% "akka-multi-node-testkit" % Version.Akka
  val akkaOsgi = "com.typesafe.akka" %% "akka-osgi" % Version.Akka
  val akkaPersistence = "com.typesafe.akka" %% "akka-persistence" % Version.Akka

  // consider https://index.scala-lang.org/krasserm/akka-analytics

  // @link https://index.scala-lang.org/akka/akka-persistence-cassandra
  val akkaPersistenceCassandra = "com.typesafe.akka" %% "akka-persistence-cassandra" % Version.AkkaPersistenceCassandra

  // @link https://index.scala-lang.org/dnvriend/akka-persistence-jdbc
  val akkaPersistenceJdbc = "com.github.dnvriend" %% "akka-persistence-jdbc" % "2.4.17.0"

  // @link https://index.scala-lang.org/krasserm/akka-persistence-kafka
  val akkaPersistenceKafka = "com.github.krasserm" %% "akka-persistence-kafka" % "0.4"

  // @link https://index.scala-lang.org/okumin/akka-persistence-sql-async
  val akkaPersistenceSqlAsync = "com.okumin" %% "akka-persistence-sql-async" % "0.4.0"

  // @link https://index.scala-lang.org/hootsuite/akka-persistence-redis
  val akkaPersistenceRedis = "com.hootsuite" %% "akka-persistence-redis" % "0.6.0"

  val akkaPersistenceQueryExperimental = "com.typesafe.akka" %% "akka-persistence-query-experimental" % Version.Akka
  val akkaRemote = "com.typesafe.akka" %% "akka-remote" % Version.Akka
  val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % Version.Akka
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % Version.Akka
  val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" % Version.Akka
  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % Version.Akka
  val akkaTypedExperimental = "com.typesafe.akka" %% "akka-typed-experimental" % Version.Akka
  val hikariCP = "com.zaxxer" % "HikariCP" % Version.HikariCP
  val logback = "ch.qos.logback" % "logback-classic" % "1.1.7"
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
  val scalaTest = "org.scalatest" %% "scalatest" % Version.ScalaTest
  val slick = "com.typesafe.slick" %% "slick" % Version.Slick
  val sparkCassandra = "datastax" % "spark-cassandra-connector" % "1.6.0-s_2.10"
  val sparkJava = "com.sparkjava" % "spark-core" % "2.5.4"
  val sparkRedis = "RedisLabs" % "spark-redis" % "0.3.2"
  val typesafeConfig = "com.typesafe" % "config" % "1.3.1"
}

resolvers ++= Seq(
  "krasserm at bintray" at "http://dl.bintray.com/krasserm/maven",
  Resolver.jcenterRepo // Adds Bintray to resolvers for akka-persistence-redis and rediscala
)
