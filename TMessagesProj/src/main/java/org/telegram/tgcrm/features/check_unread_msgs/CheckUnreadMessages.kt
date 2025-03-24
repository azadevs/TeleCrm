package org.telegram.tgcrm.features.check_unread_msgs

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig
import org.telegram.tgcrm.TgCrmUtilities
import org.telegram.tgcrm.features.check_unread_msgs.utils.CheckingReceiver
import org.telegram.tgcrm.features.warning.WarningRemote
import org.telegram.tgcrm.model.ActionEntity
import org.telegram.tgcrm.model.UserCredentialsData
import java.util.Calendar

object CheckUnreadMessages {

    const val CHECKER_SPACE_TIME = 1

    private val sharedPreferences =
        ApplicationLoader.applicationContext.getSharedPreferences("db", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()
    @SuppressLint("StaticFieldLeak")
    val firestore = FirebaseFirestore.getInstance()

    fun startChecking(callback: (Boolean) -> Unit) {

        var dialogsDict =
            MessagesController.getInstance(UserConfig.selectedAccount).dialogs_dict

        val calendar = Calendar.getInstance()

        if (TgCrmUtilities.getCachedUserData(UserCredentialsData.USERNAME) == "menejer") {
            for (i in 0 until dialogsDict.size()) {
                val key = dialogsDict.keyAt(i)
                val value = dialogsDict.get(key)

                val difference = (calendar.time.time / 1000) - (value?.last_message_date ?: 0)

                if ((value?.unread_count
                        ?: 0) > 0 && difference > (5 * 60) && value?.last_message_date != 0
                ) {
                    callback(true)
                    break
                }
            }
        } else {
            getActions { actions ->
                for (i in 0 until dialogsDict.size()) {
                    val key = dialogsDict.keyAt(i)
                    val value = dialogsDict.get(key)

                    val difference = (calendar.time.time / 1000) - (value?.last_message_date ?: 0)

                    var owner = ""

                    for (action in actions) {
                        if (action.dialogId == (value?.id ?: 0L)) {
                            owner = action.actionOwner
                        }
                    }

                    if (owner != "" && owner == TgCrmUtilities.getCachedUserData(UserCredentialsData.USERNAME)) {
                        if ((value?.unread_count
                                ?: 0) > 0 && difference > (5 * 60) && value?.last_message_date != 0
                        ) {
                            callback(true)
                            break
                        }
                    }
                }
            }
        }
    }

    fun getActions(callback: (List<ActionEntity>) -> Unit) {
        firestore.collection("actions")
            .addSnapshotListener { queryDocumentSnapshots, e ->
                if (e != null || queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty) {
                    callback(emptyList())
                    return@addSnapshotListener
                }

                val actionMap = mutableMapOf<Long, ActionEntity>()

                for (queryDocumentSnapshot in queryDocumentSnapshots) {
                    val action = queryDocumentSnapshot.toObject(ActionEntity::class.java)
                    val dialogId = action.dialogId

                    if (!actionMap.containsKey(dialogId) || action.actionTime > actionMap[dialogId]!!.actionTime) {
                        actionMap[dialogId] = action
                    }
                }

                callback(actionMap.values.sortedByDescending { it.actionTime })
            }
    }

    fun setWarning() {
        WarningRemote.setWarning(firestore)
    }

    fun setAlarm(context: Context?, timeMills: Long) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, CheckingReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, timeMills.hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeMills,
            pendingIntent
        )
    }

    fun cancelAlarm(context: Context?, timeMills: Long) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, CheckingReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            timeMills.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun saveCache(timeMills: Long) {
        editor.putLong("check_date", timeMills)
        editor.commit()
    }

    fun getCheckDate(): Long {
        return sharedPreferences.getLong("check_date", 0L)
    }

    fun replyChecker(context: Context?) {
        var oldTime = getCheckDate()
        if (oldTime != 0L) {
            cancelAlarm(context, oldTime)
        }
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, CHECKER_SPACE_TIME)
        val futureTimeMillis = calendar.time.time
        setAlarm(context, futureTimeMillis)
        saveCache(futureTimeMillis)
    }

    fun firstLaunchForCheckUnreadMsgs(context: Context?) {
        var oldTime = getCheckDate()
        if (oldTime != 0L) {
            cancelAlarm(context, oldTime)
            setAlarm(context, oldTime)
            saveCache(oldTime)
        } else {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MINUTE, CHECKER_SPACE_TIME)
            val futureTimeMillis = calendar.time.time
            setAlarm(context, futureTimeMillis)
            saveCache(futureTimeMillis)
        }
    }
}

@Keep
class Warning {
    @PropertyName("worker_name")
    var worker: String? = null

    @PropertyName("warning_time")
    var warningTime: Timestamp? = null


    constructor()

    constructor(worker: String?) {
        this.worker = worker
        this.warningTime = Timestamp.now()
    }
}