package org.telegram.tgcrm.features.alarm_screen.utils

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import org.telegram.messenger.databinding.AlarmActivityBinding
import org.telegram.tgcrm.features.alarm_screen.MyAlarmManager

class AlarmActivity : AppCompatActivity() {

    lateinit var alarmActivityBinding: AlarmActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alarmActivityBinding = AlarmActivityBinding.inflate(layoutInflater)
        setContentView(alarmActivityBinding.root)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val alarmId = intent.getIntExtra("alarm_id", -1)
        var alarm = MyAlarmManager.getByAlarmId(alarmId)


        alarmActivityBinding.tv.setText(alarm?.dialogName)
        alarmActivityBinding.message.setText(alarm?.message)
    }
}
