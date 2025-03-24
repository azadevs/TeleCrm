package org.telegram.tgcrm.features.alarm_screen

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.telegram.messenger.ApplicationLoader
import org.telegram.tgcrm.features.alarm_screen.utils.AlarmReceiver

object MyAlarmManager {

    private val sharedPreferences =
        ApplicationLoader.applicationContext.getSharedPreferences("db", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()
    private var gson = Gson()

    fun createAlarm(context: Context, alarm: AlarmEntity){
        var alarms = getAlarms()
        val oldAlarm = alarms.find { it.dialogId == alarm.dialogId }
        if (oldAlarm != null) {
            var indexOf = alarms.indexOf(oldAlarm)
            cancelAlarm(context, alarms[indexOf])
            alarms[indexOf] = alarm
        }else{
            alarms.add(alarm)
        }
        setAlarm(context, alarm)
        saveCache(alarms)
    }

    fun removeAlarmFromCache(context: Context, alarm: AlarmEntity) {
        var alarms = getAlarms()
        var oldAlarm = alarms.find { it.dialogId == alarm.dialogId }
        if (oldAlarm != null) {
            cancelAlarm(context, oldAlarm)
            alarms.remove(oldAlarm)
            saveCache(alarms)
        }
    }

    fun saveCache(alarms: ArrayList<AlarmEntity>) {
        val str = gson.toJson(alarms)
        editor.putString("alarms", str)
        editor.commit()
    }

    fun getAlarms(): ArrayList<AlarmEntity> {
        val list = ArrayList<AlarmEntity>()
        val str = sharedPreferences.getString("alarms", "")
        if (str !== "") {
            val type: TypeToken<*> = object : TypeToken<List<AlarmEntity?>?>() {}
            val fromJson = gson.fromJson<ArrayList<AlarmEntity>>(str, type.type)
            for (item in fromJson) {
                list.add(item)
            }
        }

        return list
    }

    fun getAlarmByDialogId(dialogId: Long): AlarmEntity? {
        return getAlarms().find { it.dialogId == dialogId }
    }

    fun getByAlarmId(alarmId: Int): AlarmEntity? {
        return getAlarms().find { it.alarmId == alarmId }
    }

    fun setAlarm(context: Context?, alarm: AlarmEntity) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("alarm_id", alarm.alarmId)
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarm.alarmId, intent,
            PendingIntent.FLAG_IMMUTABLE // <-- BU YERDA BAYROQ QO‘SHILDI
        )

        // Budilnikni belgilangan vaqtga qo‘yish
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarm.time ?: 0L,
            pendingIntent
        )
    }

    fun cancelAlarm(context: Context, alarm: AlarmEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // <-- BAYROQ QO‘SHILDI
        )
        alarmManager.cancel(pendingIntent)
    }

}

class AlarmEntity {
    var workerId: String? = null
    var dialogId: Long? = null
    var dialogName: String? = null
    var message: String? = null
    var time: Long? = null
    var alarmId: Int = 0

    constructor()

    constructor(
        workerId: String?,
        dialogId: Long?,
        dialogName: String?,
        message: String?,
        time: Long?
    ) {
        this.workerId = workerId
        this.dialogId = dialogId
        this.dialogName = dialogName
        this.message = message
        this.time = time
        this.alarmId = time.hashCode()
    }
}