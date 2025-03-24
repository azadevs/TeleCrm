package org.telegram.tgcrm.features.deleted_messages

import android.content.Context
import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.MessageObject


object DeletedMsg {
    private const val TAG = "DeletedMsg"

    private val sharedPreferences =
        ApplicationLoader.applicationContext.getSharedPreferences("db", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()
    private var gson = Gson()
    var myDelete = false

    fun whoDelete(dialogId: Long, msgId: Int): String {
        val whoDeletedMsgs = getAllIds()
        var resultStr = ""
        for (i in 0 until whoDeletedMsgs.size) {
            val whoDeletedMsg = whoDeletedMsgs[i]
            if (whoDeletedMsg.dialogId == dialogId && whoDeletedMsg.id == msgId) {
                resultStr =
                    when (whoDeletedMsg.who) {
                        By.Me -> {
                            "Men tarafdan o'chirildi" + if (whoDeletedMsg.deleteForAll != false) " 2" else " 1"
                        }

                        By.You -> {
                            "Mijoz tarafdan o'chirildi" + if (whoDeletedMsg.deleteForAll != false) " 2" else " 1"
                        }

                        By.Channel -> {
                            "Kanal tarafdan o'chirildi"
                        }

                        null -> ""
                    }
                break
            }
        }

        return resultStr
    }

    fun whoDeleteStr(by: By?): String {
        return when (by) {
            By.Me -> {
                "Men tarafdan o'chirildi"
            }

            By.You -> {
                "Klient tarafdan o'chirildi"
            }

            By.Channel -> {
                "Mijoz tarafdan o'chirildi"
            }

            null -> ""
        }
    }

    fun getAllIds(): ArrayList<WhoDeletedMsg> {
        val markMessages = ArrayList<WhoDeletedMsg>()

        val gson = Gson()
        val str = sharedPreferences.getString("mark_delete", "")
        if (str !== "") {
            val type: TypeToken<*> = object : TypeToken<List<WhoDeletedMsg?>?>() {
            }
            val fromJson = gson.fromJson<java.util.ArrayList<WhoDeletedMsg>>(str, type.type)
            for (markId in fromJson) {
                markMessages.add(markId)
            }
        }

        return markMessages
    }

    fun saveDeletedMessagesId(messageIds: ArrayList<WhoDeletedMsg>) {
        val str = gson.toJson(messageIds)
        editor.putString("mark_delete", str)
        editor.commit()
    }

    fun notify(
        ids: ArrayList<Int>,
        messages: ArrayList<MessageObject>,
        dialogId: Long?
    ): ArrayList<MessageObject> {
        val notifyMessages = ArrayList<MessageObject>()
        for (i in 0 until messages.size) {
            for (j in 0 until ids.size) {
                if (messages[i].messageOwner.id == ids[j] && messages[i].messageOwner.dialog_id == dialogId) {
                    notifyMessages.add(messages[i])
                }
            }
        }
        return notifyMessages
    }
}

data class WhoDeletedMsg(
    val dialogId: Long?,
    val id: Int?,
    val who: By?,
    val deleteForAll: Boolean?
)

enum class By {
    Me,
    You,
    Channel
}

@Keep
class DeletedMessageByWorker {
    @PropertyName("dialog_name")
    var dialogName: String? = null

    @PropertyName("worker_name")
    var workerName: String? = null

    @PropertyName("dialog_id")
    var dialogId: Long? = null

    @PropertyName("message_id")
    var messageId: Int? = null

    @PropertyName("type")
    var type: Int? = null

    @PropertyName("message_text")
    var messageText: String? = null

    @PropertyName("delete_time")
    var deleteTime: Timestamp? = null

    constructor()
    constructor(
        dialogName: String?,
        workerName: String?,
        dialogId: Long?,
        messageId: Int?,
        type: Int?,
        messageText: String?,
        deleteTime: Timestamp?
    ) {
        this.dialogName = dialogName
        this.workerName = workerName
        this.dialogId = dialogId
        this.messageId = messageId
        this.type = type
        this.messageText = messageText
        this.deleteTime = deleteTime
    }


}