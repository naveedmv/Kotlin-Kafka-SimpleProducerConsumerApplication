import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.log4j.LogManager
import com.fasterxml.jackson.module.kotlin.*
import java.io.File
import java.util.*
import kotlin.system.exitProcess

// $ sudo docker-compose -f kafka.yaml run --rm kafka-server /opt/bitnami/kafka/bin/kafka-topics.sh --create --zookeeper elisaautomateassignment_zookeeper-server_1:2181 --replication-factor 1 --partitions 1 --topic alarms

fun main() {
    SimpleProducer("localhost:9092").produce(2)
}

class SimpleProducer(brokers: String) {

    private val logger = LogManager.getLogger(javaClass)
    private val producer = createProducer(brokers)

    private fun createProducer(brokers: String): Producer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["key.serializer"] = StringSerializer::class.java
        props["value.serializer"] = StringSerializer::class.java
        return KafkaProducer<String, String>(props)
    }

    fun produce(ratePerSecond: Int) {
        val waitTimeBetweenIterationsMs = 1000L / ratePerSecond
        logger.info("Producing $ratePerSecond records per second (1 every ${waitTimeBetweenIterationsMs}ms)")

        val metadataMapper = jacksonObjectMapper()
        val metadataList: List<AlarmMetadata> = metadataMapper.readValue(File(jsonPackagePath))
        logger.info("metadataList size: ${metadataList.size}")
        //println(metadataList[0].metadata)

        //exitProcess(1)

        val alarmMapper = jacksonObjectMapper()
        for (m in metadataList) {
            val alarmJsonString = m.metadata
            logger.debug("JSON data: $alarmJsonString")

            val alarm : Alarm = alarmMapper.readValue<Alarm>(alarmJsonString)
            logger.info("Generated an alarm: $alarm")

            /*
            val alarmJson = jsonMapper.writeValueAsString(alarm)
            println("JSON data: $alarmJson")
            logger.debug("JSON data: $alarmJson")
            */

            val produceResult = producer.send(ProducerRecord(alarmsTopic, alarmJsonString))
            logger.debug("Sent an alarm record")

            Thread.sleep(waitTimeBetweenIterationsMs)

            // wait for the write acknowledgment
            produceResult.get()
        }
    }
}

