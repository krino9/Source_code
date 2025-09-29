package com.spark.sql.five.Index_calculation

import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.{col, count, row_number, unix_timestamp}

import java.util.Properties

object spark3 {
  def main(args: Array[String]): Unit = {
    System.setProperty("HADOOP_USER_NAME", "root")

    val spark = SparkSession.builder()
      .appName("指标计算2")
      .master("local[*]")
      .enableHiveSupport()
      .config("spark.testing.memory", "2147480000")
      .getOrCreate()
    spark.sql("use dwd")

    val dataFrame = spark.sql(
      """
        |select
        | changemachineid machine_id,
        | machinefactory machine_factory,
        | sum(unix_timestamp(changeendtime)-unix_timestamp(changestarttime)) total_running_time
        |from fact_change_record f
        | join dim_machine d on f.changemachineid = d.basemachineid
        |where f.changerecordstate = '运行' and changeendtime is not null
        |group by changemachineid,machinefactory
        |""".stripMargin)
    dataFrame.show()

    val dataFrame1 = dataFrame.withColumn("num", row_number() over Window.partitionBy("machine_factory").orderBy("total_running_time"))
      .withColumn("count_data", count("machine_id") over Window.partitionBy("machine_factory"))
      .where(
        """
          |(count_data % 2 = 0 and (num = count_data/2 or num = count_data/2+1) or
          |   (count_data % 2 != 0 and num = count_data/2+1))
          |""".stripMargin)
      .drop("num", "count_data")
    dataFrame1.show()

    val jdbcUrl = "jdbc:mysql://192.168.1.121:3306/shtd_industry"
    val tableName = "machine_running_median"
    val properties = new Properties()
    properties.setProperty("user", "root")
    properties.setProperty("password", "123456")
    properties.setProperty("driver", "com.mysql.jdbc.Driver")

    dataFrame1.write.mode(SaveMode.Overwrite).jdbc(jdbcUrl,tableName,properties)

    spark.stop()
  }
}
