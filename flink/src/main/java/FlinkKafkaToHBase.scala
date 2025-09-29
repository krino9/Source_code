import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Put}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.configuration.Configuration

import java.text.SimpleDateFormat
import java.util.Properties

object ProduceRecordProcessor {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    // Kafka配置
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "kafka-server:9092")
    properties.setProperty("group.id", "flink-produce-record")

    // 创建Kafka消费者
    val kafkaConsumer = new FlinkKafkaConsumer[String](
      "ProduceRecord",
      new SimpleStringSchema(),
      properties
    )

    // 使用Flink自带的JSON处理方式
    val dataStream = env.addSource(kafkaConsumer)
      .map { record =>
        try {
          // 简单的JSON解析，根据实际字段调整
          val fields = record.split(",")
          val machineId = fields(0).split(":")(1).replaceAll("\"", "").trim
          val changeHandleState = fields(1).split(":")(1).replaceAll("\"", "").trim.toInt
          (machineId, changeHandleState)
        } catch {
          case e: Exception =>
            println(s"Error parsing record: $record, error: ${e.getMessage}")
            ("", 0)
        }
      }
      .filter(_._2 == 1) // 过滤已检验的产品
      .map(_._1) // 只保留设备ID

    // 每5分钟统计各设备生产总数
    val resultStream = dataStream
      .map((_, 1))
      .keyBy(_._1)
      .window(TumblingProcessingTimeWindows.of(Time.minutes(5)))
      .reduce((a, b) => (a._1, a._2 + b._2))
      .map(new RichMapFunction[(String, Int), (String, String, String)] {
        private var connection: Connection = _
        private val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

        override def open(parameters: Configuration): Unit = {
          val config = HBaseConfiguration.create()
          config.set("hbase.zookeeper.quorum", "hbase-server:2181")
          connection = ConnectionFactory.createConnection(config)
        }

        override def map(value: (String, Int)): (String, String, String) = {
          val (machineId, count) = value
          val currentTime = dateFormat.format(System.currentTimeMillis())
          val rowKey = s"$machineId-$currentTime"

          // 写入HBase
          val table = connection.getTable(TableName.valueOf("gyflinkresult:Produce5minAgg"))
          val put = new Put(Bytes.toBytes(rowKey))
          put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("machine_id"), Bytes.toBytes(machineId))
          put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("total_produce"), Bytes.toBytes(count.toString))
          put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("system_time"), Bytes.toBytes(currentTime))

          table.put(put)
          table.close()

          (rowKey, machineId, count.toString)
        }

        override def close(): Unit = {
          if (connection != null) connection.close()
        }
      })

    resultStream.print()
    env.execute("Produce Record 5min Aggregation")
  }
}