package co.tjcelaya.smack.stream

import org.apache.spark.storage.StorageLevel
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by tj on 1/29/17.
  *
  */
object Stream {
  private val logger: Logger = LoggerFactory.getLogger(Stream.getClass)

  def main(args: Array[String]) {
    logger.info("stream booting")

    val sc = SparkContextFactory.buildSparkContext()
    val ssc = SparkContextFactory.buildStreamContext(sc)
    val rsc = SparkContextFactory.buildRedisStreamContext(ssc)
    sc.setLogLevel("WARN")

    val cmdStream = rsc.createRedisStream(Array("cmd"), storageLevel = StorageLevel.MEMORY_ONLY)

    val words = cmdStream.flatMap(_._2.split(" "))

    val pairs = words.map(word => (word, 1))
    val wordCounts = pairs.reduceByKey(_ + _)

    // Print the first ten elements of each RDD generated in this DStream to the console
    wordCounts.print()

    ssc.start() // Start the computation
    ssc.awaitTermination() // Wait for the computation to terminate
  }
}
