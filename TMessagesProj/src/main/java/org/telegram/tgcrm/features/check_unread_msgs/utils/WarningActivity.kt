package org.telegram.tgcrm.features.check_unread_msgs.utils

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import org.telegram.messenger.databinding.WarningActivityBinding

class WarningActivity: AppCompatActivity() {

    lateinit var warningActivityBinding: WarningActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        warningActivityBinding = WarningActivityBinding.inflate(layoutInflater)
        setContentView(warningActivityBinding.root)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

}