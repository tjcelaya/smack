
def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + ((t1 - t0)/ 1000000000.0) + "s")
    result
}


simple things can be turned into other simple things easily
    scala> spark.range(100).toJSON.collect.foreach(println)
    {"id":0}
    {"id":1}
    {"id":2}
    ...


time(sqlCxt.read.format("com.databricks.spark.csv").option("header", "true").option("inferSchema", "true").load("/tmp/cc.csv") && csvCcDf.count)

> sqlCxt
|   .read
|   .format("com.databricks.spark.csv")
|   .option("header", "true")
|   .option("inferSchema", "true")
|   .load("/tmp/cc.csv")

scala> csvCcDf.createOrReplaceTempView("csv")

scala> time(spark.sql("select * from csv limit 1").show)
+-------------+-------------+------------+--------------------+---------+----------------------------+-----------------------+--------------------+-----+--------+----+--------------------------+-------------+--------------------+----------------------------+----------------+------------------+------------+
|Date received|      Product| Sub-product|               Issue|Sub-issue|Consumer complaint narrative|Company public response|             Company|State|ZIP code|Tags|Consumer consent provided?|Submitted via|Date sent to company|Company response to consumer|Timely response?|Consumer disputed?|Complaint ID|
+-------------+-------------+------------+--------------------+---------+----------------------------+-----------------------+--------------------+-----+--------+----+--------------------------+-------------+--------------------+----------------------------+----------------+------------------+------------+
|   07/29/2013|Consumer Loan|Vehicle loan|Managing the loan...|     null|                        null|                   null|Wells Fargo & Com...|   VA|   24540|null|                       N/A|        Phone|          07/30/2013|        Closed with expla...|             Yes|                No|      468882|
+-------------+-------------+------------+--------------------+---------+----------------------------+-----------------------+--------------------+-----+--------+----+--------------------------+-------------+--------------------+----------------------------+----------------+------------------+------------+

Elapsed time: 0.870086585s

scala> time(spark.sql("select * from csv where `Date received` = '07/29/2013' limit 1").show)
+-------------+-------------+------------+--------------------+---------+----------------------------+-----------------------+--------------------+-----+--------+----+--------------------------+-------------+--------------------+----------------------------+----------------+------------------+------------+
|Date received|      Product| Sub-product|               Issue|Sub-issue|Consumer complaint narrative|Company public response|             Company|State|ZIP code|Tags|Consumer consent provided?|Submitted via|Date sent to company|Company response to consumer|Timely response?|Consumer disputed?|Complaint ID|
+-------------+-------------+------------+--------------------+---------+----------------------------+-----------------------+--------------------+-----+--------+----+--------------------------+-------------+--------------------+----------------------------+----------------+------------------+------------+
|   07/29/2013|Consumer Loan|Vehicle loan|Managing the loan...|     null|                        null|                   null|Wells Fargo & Com...|   VA|   24540|null|                       N/A|        Phone|          07/30/2013|        Closed with expla...|             Yes|                No|      468882|
+-------------+-------------+------------+--------------------+---------+----------------------------+-----------------------+--------------------+-----+--------+----+--------------------------+-------------+--------------------+----------------------------+----------------+------------------+------------+

Elapsed time: 0.667656888s

scala> time(spark.sql("select * from csv where `Date received` not like '%/%/%' limit 1").show)
+--------------------+-------+-----------+-----+---------+----------------------------+-----------------------+----------+--------------------+--------+----+--------------------------+-------------+--------------------+----------------------------+----------------+------------------+------------+
|       Date received|Product|Sub-product|Issue|Sub-issue|Consumer complaint narrative|Company public response|   Company|               State|ZIP code|Tags|Consumer consent provided?|Submitted via|Date sent to company|Company response to consumer|Timely response?|Consumer disputed?|Complaint ID|
+--------------------+-------+-----------+-----+---------+----------------------------+-----------------------+----------+--------------------+--------+----+--------------------------+-------------+--------------------+----------------------------+----------------+------------------+------------+
|",,"Citizens Fina...|  Inc."|         MI|482XX|     null|            Consent provided|                    Web|03/23/2015|Closed with expla...|     Yes| Yes|                   1296693|         null|                null|                        null|            null|              null|        null|
+--------------------+-------+-----------+-----+---------+----------------------------+-----------------------+----------+--------------------+--------+----+--------------------------+-------------+--------------------+----------------------------+----------------+------------------+------------+

Elapsed time: 1.006033481s

scala>