package org.telegram.tgcrm.features.alarm_screen.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.telegram.tgcrm.features.alarm_screen.MyAlarmManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            MyAlarmManager.getAlarms().forEach { alarm ->
                MyAlarmManager.setAlarm(context, alarm)
            }
        }
    }
}
