import jetbrains.datalore.base.datetime.DateTime
import jetbrains.datalore.base.datetime.Time
import jetbrains.datalore.base.datetime.tz.TimeZone
import java.util.*

data class Alarm(
    val affectedNode: String?,
    val affectedEquipment: String?,
    val affectedSite: String?,
    val alarmCategory: String?,
    val alarmGroup: String?,
    val alarmCSN: String?,
    val alarmID: String?,
    val alarmMO: String?,
    val alarmNotificationType: String?,
    val alarmLastSeqNo: String?,
    val alarmEventTime: Date?,
    val vnocAlarmID: String?
)