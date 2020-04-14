import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.log4j.LogManager
import java.time.Duration
import java.util.*
import kotlin.system.exitProcess
import org.nield.kotlinstatistics.countBy
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun main() {
    SimpleProcessor("localhost:9092").process()
}

class SimpleProcessor(brokers: String) {

    private val logger = LogManager.getLogger(javaClass)
    private val consumer = createConsumer(brokers)

    private fun createConsumer(brokers: String): Consumer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["group.id"] = "alarm-processor-${UUID.randomUUID()}"
        /*
        // Use of unique group.id for each run is so that the consumer start reading from beginning always
        // https://stackoverflow.com/questions/28561147/how-to-read-data-using-kafka-consumer-api-from-beginning
        // https://stackoverflow.com/questions/33676266/how-to-make-kafka-consumer-to-read-from-last-consumed-offset-but-not-from-beginn
        */
        props["key.deserializer"] = StringDeserializer::class.java
        props["value.deserializer"] = StringDeserializer::class.java
        props["auto.offset.reset"] = "earliest"
        return KafkaConsumer<String, String>(props)
    }

    fun process() {
        consumer.subscribe(listOf(alarmsTopic))

        logger.info("Consuming and processing data")

        val alarmsList: MutableList<Alarm> = mutableListOf()
        val vnocAlarmIDsList: MutableList<String> = mutableListOf()

        while(alarmsList.size < 1000) {
            val alarmRecords = consumer.poll(Duration.ofSeconds(1))

            logger.info("Received ${alarmRecords.count()} records")

            val alarmMapper = jacksonObjectMapper()

            alarmRecords.iterator().forEach {
                val alarmJsonString = it.value()
                logger.debug("JSON data: $alarmJsonString")

                val alarm: Alarm = alarmMapper.readValue<Alarm>(alarmJsonString)
                logger.info("Received an alarm: $alarm")

                alarmsList.add(alarm)

                alarm.vnocAlarmID?.let { it1 -> vnocAlarmIDsList.add(it1) }

            }

            logger.info("alarmsList size: ${alarmsList.size}")
            logger.info("vnocAlarmIDsList size: ${vnocAlarmIDsList.size}")

        }

        val alarmsCountByVnocAlarmID = alarmsList.countBy() { it.vnocAlarmID }
        println("1. Histogram data about the most frequent alarms:")
        alarmsCountByVnocAlarmID.forEach { (vnocAlarmID, count) ->
            println("$vnocAlarmID: $count")
        }
        println("")

        val alarmsCountByAffectedNode = alarmsList.countBy() { it.affectedNode }
        val sortedAlarmsCountByAffectedNode = alarmsCountByAffectedNode.toList().sortedBy {(_, value) -> value}.reversed().toMap()
        println("2. Histogram data about the nodes that got the most alarms:")
        sortedAlarmsCountByAffectedNode.forEach { (affectedNode, count) ->
            println("$affectedNode: $count")
        }
        println("")

        /*
        println("Before sort: $alarmsList")
        run outForeach1@ {
            var i: Int = 0
            alarmsList.forEach {
                println(it.alarmEventTime)
                if (i == 10) return@outForeach1
                i++
            }
        }*/

        alarmsList.sortBy { it.alarmEventTime }

        /*
        println("After sort by event time: $alarmsList")
        run outForeach2@ {
            var i: Int = 0
            alarmsList.forEach {
                println(it.alarmEventTime)
                if (i == 10) return@outForeach2
                i++
            }
        }*/

        println("3. Timeline about the type of alarms per hour:")
        /*
        // Method 1: Hard-coded date range
        for (year in listOf<String>("2020")){
            for (monthInYear in listOf<String>("Jan")){
                // "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
                for (dayInMonth in 21..25){
                    // 1..31
                    println("---$year $monthInYear $dayInMonth---")
                    for (hourInDay in 0..23){
                        println(" $hourInDay:00 - $hourInDay:59 Hrs")
                        var era015AlarmHits: Int = 0
                        var era021AlarmHits: Int = 0
                        var era005AlarmHits: Int = 0
                        for (alarm in alarmsList){
                            var alarmYear = SimpleDateFormat("YYYY").format(alarm.alarmEventTime)
                            var alarmMonthInYear = SimpleDateFormat("MMM").format(alarm.alarmEventTime)
                            var alarmDayInMonth = SimpleDateFormat("DD").format(alarm.alarmEventTime)
                            var alarmHourInDay = SimpleDateFormat("H").format(alarm.alarmEventTime)
                            if (alarmYear == year && alarmMonthInYear == monthInYear && alarmDayInMonth == dayInMonth.toString() && alarmHourInDay == hourInDay.toString()){
                                when(alarm.vnocAlarmID){
                                    "ERA015" -> era015AlarmHits++
                                    "ERA021" -> era021AlarmHits++
                                    "ERA005" -> era005AlarmHits++
                                }
                            }
                        }
                        println("  ERA015 alarm hits: $era015AlarmHits")
                        println("  ERA021 alarm hits: $era021AlarmHits")
                        println("  ERA005 alarm hits: $era005AlarmHits")
                    }
                    print("\n") // Or println("")
                }
            }
        }*/

        // Method 2 : Supports dynamic date range
        val timelineStartDate = LocalDate.of(2020, 1, 21)
        val timelineEndDate = LocalDate.of(2020, 1, 25)

        for (date in timelineStartDate..timelineEndDate step 1) {
            val year = date.year.toString()
            val monthInYear = date.format(DateTimeFormatter.ofPattern("MMM"))
            val dayInMonth = date.dayOfMonth.toString()
            println("---$year $monthInYear $dayInMonth---")
            for (hourInDay in 0..23){
                println(" $hourInDay:00 - $hourInDay:59 Hrs")
                var era015AlarmHits: Int = 0
                var era021AlarmHits: Int = 0
                var era005AlarmHits: Int = 0
                for (alarm in alarmsList){
                    val alarmYear = SimpleDateFormat("YYYY").format(alarm.alarmEventTime)
                    val alarmMonthInYear = SimpleDateFormat("MMM").format(alarm.alarmEventTime)
                    val alarmDayInMonth = SimpleDateFormat("DD").format(alarm.alarmEventTime)
                    val alarmHourInDay = SimpleDateFormat("H").format(alarm.alarmEventTime)
                    if (alarmYear == year && alarmMonthInYear == monthInYear && alarmDayInMonth == dayInMonth && alarmHourInDay == hourInDay.toString()){
                        when(alarm.vnocAlarmID){
                            "ERA015" -> era015AlarmHits++
                            "ERA021" -> era021AlarmHits++
                            "ERA005" -> era005AlarmHits++
                        }
                    }
                }
                println("  ERA015 alarm hits: $era015AlarmHits")
                println("  ERA021 alarm hits: $era021AlarmHits")
                println("  ERA005 alarm hits: $era005AlarmHits")
            }
            print("\n") // Or println("")
        }
    }
}