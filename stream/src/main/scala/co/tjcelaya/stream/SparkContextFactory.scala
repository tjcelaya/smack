package co.tjcelaya.stream

import com.typesafe.config.ConfigFactory
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by tj on 1/28/17.
  *
  */
object SparkContextFactory {
  def buildSparkConfig: SparkConf = {
    val appConf = ConfigFactory.load()
    val sparkConf = new SparkConf(true)

    sparkConf
      .setAppName("smack")
      .set("spark.cassandra.connection.host", "cassandra")

    if (appConf.getBoolean("spark.local")) {
      sparkConf.setMaster("local[*, 3]")
    }
    sparkConf
  }

  def buildSparkContext: SparkContext = {
    new SparkContext(buildSparkConfig)
  }

  def buildStreamContext(sc: SparkContext, secs: Int = 10): StreamingContext = {
    new StreamingContext(sc, Seconds(1))
  }
}
