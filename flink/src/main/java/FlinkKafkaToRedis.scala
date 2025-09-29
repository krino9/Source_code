import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import redis.clients.jedis.Jedis

import java.text.SimpleDateFormat
import java.util.Properties

object WarningRecordProcessor {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    // Kafka配置
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "localhost:9092")
    properties.setProperty("group.id", "flink-warning-record-" + System.currentTimeMillis())  // 唯一消费组

    // 创建Kafka消费者
    val kafkaConsumer: FlinkKafkaConsumer[String] = new FlinkKafkaConsumer[String](
      "ChangeRecord",
      new SimpleStringSchema(),
      properties
    )

    // 解析Kafka数据，过滤预警记录
    val warningStream: DataStream[String] = env.addSource(kafkaConsumer)
      .map { record =>
        try {
          // 输入JSON
          val cleanRecord = record.replaceAll("[{}]", "")  // 去除前后大括号
          val fields = cleanRecord.split(",")
            .map(_.trim)  // 分割并清理空格

          // 按字段名提取
          val machineId = fields.find(_.startsWith("\"machine_id\""))
            .map(_.split(":")(1).replace("\"", ""))
            .getOrElse("")

          val isWarning = fields.find(_.startsWith("\"is_warning\""))
            .map(_.split(":")(1).toInt)
            .getOrElse(0)

          (machineId, isWarning)
        } catch {
          case e: Exception =>
            println(s"解析错误: $record, 原因: ${e.getMessage}")
            ("", 0)  // 错误数据标记
        }
      }
      .filter(_._2 == 1)  // 过滤出预警记录
      .map(_._1)          // 只保留设备ID（输出类型：String）

    // 滑动窗口统计：每隔1分钟，统计最近3分钟的预警次数
    val resultStream: DataStream[(String, String, Int)] = warningStream
      .map((_, 1))  // 转换为(设备ID, 计数1)
      .keyBy(_._1)  // 按设备ID分组
      .window(SlidingProcessingTimeWindows.of(Time.minutes(3), Time.minutes(1)))  // 3分钟窗口，1分钟滑动步长
      .reduce((a, b) => (a._1, a._2 + b._2))  // 累加预警次数
      .map(new RichMapFunction[(String, Int), (String, String, Int)] {
        // 格式化窗口结束时间（
        private val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        override def map(value: (String, Int)): (String, String, Int) = {
          // 注意：System.currentTimeMillis()是当前处理时间，非窗口实际结束时间
          val windowEndStr = dateFormat.format(System.currentTimeMillis())
          (windowEndStr, value._1, value._2)
        }
      })

    resultStream
      .keyBy(_._1)  // 按窗口结束时间分组，找出每个窗口预警最多的设备
      .reduce((a, b) => if (a._3 > b._3) a else b)  // 取预警次数最多的设备
      .addSink(new RichSinkFunction[(String, String, Int)] {
        private var jedis: Jedis = _

        // 初始化Redis连接
        override def open(parameters: Configuration): Unit = {
          super.open(parameters)
          jedis = new Jedis("localhost", 6379)
          if (!jedis.ping().equals("PONG")) {
            throw new RuntimeException("Redis连接失败，请检查地址和端口")
          }
        }

        // 处理每条数据
        override def invoke(
                             value: (String, String, Int),
                             context: SinkFunction.Context  // 明确使用SinkFunction.Context
                           ): Unit = {
          val (windowEnd, machineId, count) = value
          // 写入Redis：Hash结构
          jedis.hset("warning_last3min_everymin_out", windowEnd, s"$machineId,$count")
        }

        // 关闭Redis连接
        override def close(): Unit = {
          super.close()
          if (jedis != null && jedis.isConnected) {
            jedis.close()
          }
        }
      })

    resultStream.print()  // 控制台输出，用于调试
    env.execute("设备预警3分钟滑动窗口统计")
  }
}