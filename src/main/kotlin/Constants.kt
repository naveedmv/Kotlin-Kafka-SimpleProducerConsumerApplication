import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

val jsonPackagePath = "${System.getProperty("user.home")}/IdeaProjects/Kotlin-Kafka-SimpleProducerConsumerApplication/src/main/resources/packages.json"
val alarmsTopic = "alarms"

val jsonMapper = ObjectMapper().apply {
    registerKotlinModule()
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    setDateFormat(StdDateFormat())
}