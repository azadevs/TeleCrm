package org.telegram.tgcrm.features.alarm_screen.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import androidx.core.app.NotificationCompat
import org.telegram.messenger.ApplicationLoader.applicationContext
import org.telegram.messenger.R
import org.telegram.tgcrm.features.alarm_screen.AlarmEntity
import org.telegram.tgcrm.features.alarm_screen.MyAlarmManager

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        var alarmId = intent?.getIntExtra("alarm_id", -1)
        var alarm = if (alarmId != -1) {
            MyAlarmManager.getByAlarmId(alarmId ?: 0)
        } else {
            AlarmEntity("unknown", 0, "unnamed", "no message", 0)
        }

        val audioManager = context?.getSystemService(AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_SHOW_UI)
        val mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sund)
        mediaPlayer?.let {
            it.start()
            it.setOnCompletionListener { player ->
                player.release()
            }
        }

        val fullScreenIntent = Intent(context, AlarmActivity::class.java)
        fullScreenIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        fullScreenIntent.putExtra("alarm_id", alarmId)

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, alarm?.alarmId?:0, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "alarm_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Alarm Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(alarm?.dialogName)
            .setContentText(alarm?.message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)  // 📌 **FULL-SCREEN INTENT**
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)

    }
}
