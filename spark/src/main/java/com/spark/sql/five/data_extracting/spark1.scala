package com.spark.sql.five.data_extracting

import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.functions.{current_date, date_format, date_sub}

import java.util.Properties

object spark1 {
  def main(args: Array[String]): Unit = {
    System.setProperty("HADOOP_USER_NAME", "root")
    System.setProperty("hadoop.home.dir","D://dummy-hadoop")

    val spark = SparkSession.builder()
      .appName("数据抽取")
      .master("local[*]")
      .enableHiveSupport()
      .config("spark.testing.memory", "2147480000")
      .config("spark.hadoop.fs.defaultFS", "hdfs://192.168.35.130:8020")
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")
    val jdbcUrl = "jdbc:mysql://localhost:3306/bigdata"
    val tableName = Array("changerecord", "basemachine", "environmentdata", "machinedata")
    val properties = new Properties()
    properties.setProperty("user", "root")
    properties.setProperty("password", "lv142536")
    properties.setProperty("driver", "com.mysql.cj.jdbc.Driver")

    //    spark.sql("use ods5")
    tableName.foreach(x =>{
      val dataFrame = spark.read.jdbc(jdbcUrl, x, properties)
        .withColumn("etldate", date_format(date_sub(current_date(), 1), "yyyyMMdd"))
      //      dataFrame.show()
      if (x == "environmentdata"){
        spark.sql("use ods")
        val dataFrame1 = dataFrame.drop("ProducePrgCode")
        val dataFrame2 = dataFrame1.toDF(dataFrame1.columns.map(_.toLowerCase): _*)
        dataFrame2.write.mode(SaveMode.Overwrite)
          .format("orc")
          .partitionBy("etldate")
          .saveAsTable(x)
        dataFrame2.show()
      }else{
        spark.sql("use ods")
        val dataFrame1 = dataFrame.toDF(dataFrame.columns.map(_.toLowerCase): _*)
        dataFrame1.write.mode(SaveMode.Overwrite)
          .format("orc")
          .partitionBy("etldate")
          .saveAsTable(x)
        dataFrame1.show()
      }
      println(s"${x}表写入完成！")
    })
    spark.stop()
  }
}
