package co.tjcelaya.smack.stream

import com.redislabs.provider.redis.{RedisContext, RedisStreamingContext}
import com.typesafe.config.ConfigFactory
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by tj on 1/28/17.
  *
  */
object SparkContextFactory {

//  private val logger = LoggerFactory.getLogger(SparkContextFactory.getClass)

  private var sc: Option[SparkContext] = None
  private var ssc: Option[StreamingContext] = None

  def buildSparkConfig(): SparkConf = {
    val appConf = ConfigFactory.load()
    val sparkConf = new SparkConf(true)

    sparkConf setAppName "smack"

    if (appConf getBoolean "spark.local") {
      sparkConf setMaster "local[*, 3]"
    }

    for ((k, defaultConfig) <- Map(
      "spark.cassandra.connection.host" -> "cassandra",
      "redis.host" -> "redis",
      "redis.port" -> "6379"
      // "redis.auth" -> ""
    )) {
      sparkConf.set(k, if (appConf hasPath k) appConf getString k else defaultConfig)
    }

    sparkConf
  }

  def buildSparkContext(sConf: SparkConf = buildSparkConfig()): SparkContext = {
    if (sc.isEmpty) {
      sc = Some(new SparkContext(sConf))
    }

    sc.get
  }

  def buildStreamContext(sCxt: SparkContext = buildSparkContext(), secs: Int = 10): StreamingContext = {
    if (ssc.isEmpty) {
      ssc = Some(new StreamingContext(sCxt, Seconds(secs)))
      sys.ShutdownHookThread {
//        logger.info("Gracefully stopping Spark Streaming Application")
        ssc.get.stop(stopSparkContext = true, stopGracefully = true)
//        logger.info("Application stopped")
      }
    }

    ssc.get
  }

  def buildRedisContext(sCxt: SparkContext = buildSparkContext()): RedisContext = {
    new RedisContext(sCxt)
  }

  def buildRedisStreamContext(ssCxt: StreamingContext = buildStreamContext()): RedisStreamingContext = {
    new RedisStreamingContext(ssCxt)
  }

}
