package co.tjcelaya.stream

import com.typesafe.config.ConfigFactory
import org.apache.log4j
import org.apache.spark.streaming._
import org.apache.spark._
import org.apache.spark.storage.StorageLevel
import org.apache.spark.util.Utils
import org.slf4j._
import com.redislabs.provider.redis._

import scala.concurrent.duration._
import scala.concurrent.duration.DurationConversions._


object Stream {
  private val logger = LoggerFactory.getLogger(Stream.getClass)

  def main(args: Array[String]) {
    logger.info("stream booting")

    // only one at a time
    val sc = SparkContextFactory.buildSparkContext
    sc.setLogLevel("WARN")

    val ssc = SparkContextFactory.buildStreamContext(sc, 2)
    val rsc = new RedisStreamingContext(ssc)

    val cmdStream = rsc.createRedisStream(Array("cmd"), storageLevel = StorageLevel.MEMORY_ONLY)

    val lines = ssc.socketTextStream("localhost", 9999)
    val words = cmdStream.flatMap(_._2.split(" "))

    val pairs = words.map(word => (word, 1))
    val wordCounts = pairs.reduceByKey(_ + _)

    // Print the first ten elements of each RDD generated in this DStream to the console
    wordCounts.print()

    ssc.start() // Start the computation
    ssc.awaitTermination() // Wait for the computation to terminate
  }
}

