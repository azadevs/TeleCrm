package org.telegram.tgcrm.features.check_unread_msgs.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.telegram.tgcrm.features.check_unread_msgs.CheckUnreadMessages

class BootReceiverForCheckMsgs : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            CheckUnreadMessages.firstLaunchForCheckUnreadMsgs(context)
        }
    }
}