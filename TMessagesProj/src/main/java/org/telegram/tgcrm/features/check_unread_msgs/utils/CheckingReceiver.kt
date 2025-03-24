package org.telegram.tgcrm.features.check_unread_msgs.utils

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
import org.telegram.messenger.R
import org.telegram.tgcrm.TgCrmUtilities
import org.telegram.tgcrm.features.check_unread_msgs.CheckUnreadMessages

class CheckingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        CheckUnreadMessages.replyChecker(context)

        CheckUnreadMessages.startChecking{
            if (it){
                CheckUnreadMessages.setWarning()
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

                val fullScreenIntent = Intent(context, WarningActivity::class.java)
                fullScreenIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                val fullScreenPendingIntent = PendingIntent.getActivity(
                    context, 0, fullScreenIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
                    .setContentTitle("Ogoxlantirish")
                    .setContentText("Siz mijozga 5 daqiqa ichida javob bermadingiz sizga jarima yozildi!!!")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setFullScreenIntent(fullScreenPendingIntent, true)  // 📌 **FULL-SCREEN INTENT**
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(1, notification)
            }
        }
    }
}