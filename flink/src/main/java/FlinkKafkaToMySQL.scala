import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.connector.jdbc.{JdbcConnectionOptions, JdbcSink}

import java.sql.PreparedStatement
import java.text.SimpleDateFormat
import java.util.Properties

object ChangeRecordProcessor {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    // Kafka配置
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "kafka-server:9092")
    properties.setProperty("group.id", "flink-change-record")

    // 创建Kafka消费者
    val kafkaConsumer = new FlinkKafkaConsumer[String](
      "ChangeRecord",
      new SimpleStringSchema(),
      properties
    )

    // 使用字符串处理解析JSON
    val dataStream = env.addSource(kafkaConsumer)
      .map { record =>
        try {
          // 简单的JSON解析，根据实际字段调整
          val fields = record.split(",")
          val machineId = fields(0).split(":")(1).replaceAll("\"", "").trim.toInt
          val currentState = fields(1).split(":")(1).replaceAll("\"", "").trim
          val previousState = fields(2).split(":")(1).replaceAll("\"", "").trim
          (machineId, currentState, previousState)
        } catch {
          case e: Exception =>
            println(s"Error parsing record: $record, error: ${e.getMessage}")
            (0, "", "")
        }
      }
      .filter(_._1 != 0) // 过滤解析失败的记录

    // 过滤出从其他状态转变为"运行"状态的记录
    val runStateStream = dataStream
      .filter { case (_, currentState, previousState) =>
        currentState == "运行" && previousState != "运行"
      }

    // 统计每个设备从其他状态转变为运行状态的总次数
    val resultStream = runStateStream
      .map { case (machineId, _, previousState) =>
        ((machineId, previousState), 1)
      }
      .keyBy(_._1)
      .reduce((a, b) => (a._1, a._2 + b._2))
      .map { case ((machineId, previousState), count) =>
        (machineId, previousState, count, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()))
      }

    // 写入MySQL
    resultStream.addSink(
      JdbcSink.sink(
        "INSERT INTO change_state_other_to_run_agg (change_machine_id, last_machine_state, total_change_torun, in_time) VALUES (?, ?, ?, ?) " +
          "ON DUPLICATE KEY UPDATE last_machine_state = VALUES(last_machine_state), total_change_torun = VALUES(total_change_torun), in_time = VALUES(in_time)",
        (statement: PreparedStatement, record: (Int, String, Int, String)) => {
          statement.setInt(1, record._1)
          statement.setString(2, record._2)
          statement.setInt(3, record._3)
          statement.setString(4, record._4)
        },
        new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
          .withUrl("jdbc:mysql://mysql-server:3306/shtd_industry")
          .withDriverName("com.mysql.cj.jdbc.Driver")
          .withUsername("your_username")
          .withPassword("your_password")
          .build()
      )
    )

    resultStream.print()
    env.execute("Change Record State Transition Aggregation")
  }
}