package com.spark.sql.five.data_cleansing

import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.functions.{current_timestamp, date_format, date_sub, lit}
import org.apache.spark.sql.types.TimestampType

object spark2 {
  def main(args: Array[String]): Unit = {
    System.setProperty("HADOOP_USER_NAME", "root")
    System.setProperty("hadoop.home.dir","D://dummy-hadoop")

    val spark = SparkSession.builder()
      .appName("数据清洗")
      .master("local[*]")
      .enableHiveSupport()
      .config("spark.testing.memory", "2147480000")
      .getOrCreate()
    // 定义源表与目标表的映射关系 (源表名, 目标表名, 去重主键)
    val tableMappings = Array(
      ("changerecord", "fact_change_record", "changemachineid"),
      ("basemachine", "dim_machine", "machineno"),
      ("environmentdata", "fact_produce_record", "environmentid"),
      ("machinedata", "fact_machine_data", "machinerecordid")
    )

    // 处理每张表
    tableMappings.foreach { case (sourceTable, targetTable, primaryKey) =>
      // 切换到ODS库读取源数据
      spark.sql("use ods")

      // 读取数据并执行去重
      val rawData = spark.sql(s"select * from $sourceTable")
      val dedupedData = rawData.dropDuplicates(Seq(primaryKey))

      // 添加数据治理字段
      val processedData = dedupedData
        .withColumn("dwd_insert_user", lit("user1"))
        .withColumn("dwd_insert_time", current_timestamp().cast(TimestampType))
        .withColumn("dwd_modify_user", lit("user1"))
        .withColumn("dwd_modify_time", current_timestamp().cast(TimestampType))

      // 写入DWD层
      spark.sql("use dwd")
      processedData.write
        .mode(SaveMode.Overwrite)
        .format("orc")
        .partitionBy("etldate")
        .saveAsTable(targetTable)

      // 打印处理结果日志
      println(s"表 $sourceTable 清洗完成，共处理 ${processedData.count()} 条记录，已写入 $targetTable")
    }

    // 停止SparkSession
    spark.stop()
  }
}