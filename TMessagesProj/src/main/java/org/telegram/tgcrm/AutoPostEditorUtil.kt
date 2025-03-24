package org.telegram.tgcrm

import android.content.Context
import android.graphics.Paint.FontMetricsInt
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.util.Base64
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.InstanceCreator
import com.google.gson.reflect.TypeToken
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.ChatObject
import org.telegram.messenger.DialogObject
import org.telegram.messenger.FileLog
import org.telegram.messenger.MediaDataController
import org.telegram.messenger.MessagesController
import org.telegram.messenger.MessagesStorage
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.RequestDelegate
import org.telegram.tgnet.SerializedData
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC
import org.telegram.ui.Components.AnimatedEmojiSpan
import org.telegram.ui.Components.ChatActivityEnterView
import org.telegram.ui.Components.QuoteSpan
import org.telegram.ui.Components.TextStyleSpan
import org.telegram.ui.Components.TextStyleSpan.TextStyleRun
import org.telegram.ui.Components.URLSpanReplacement
import org.telegram.ui.Components.URLSpanUserMention
import java.lang.reflect.Type
import java.util.concurrent.CountDownLatch

class AutoPostData {
    var active: Boolean? = null
    var dialogId: Long? = null
    var messageOwner: TLRPC.Message? = null

    constructor()

    constructor(active: Boolean?, dialogId: Long?, messageOwner: TLRPC.Message?) {
        this.active = active
        this.dialogId = dialogId
        this.messageOwner = messageOwner
    }
}

object FullChatData {

    var fullChatData: TLRPC.ChatFull? = null

    init {
        fullChatData = null
    }

    fun loadFullChat(dialogId: Long) {
        if (DialogObject.isUserDialog(dialogId)){
            return
        }
        val request: TLObject
        val chat: TLRPC.Chat? =
            MessagesController.getInstance(UserConfig.selectedAccount).getChat(-dialogId)
        if (ChatObject.isChannel(chat)) {
            val req: TLRPC.TL_channels_getFullChannel = TLRPC.TL_channels_getFullChannel()
            req.channel = MessagesController.getInputChannel(chat)
            request = req
        } else {
            val req: TLRPC.TL_messages_getFullChat = TLRPC.TL_messages_getFullChat()
            req.chat_id = -dialogId
            request = req
        }
        ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(
            request,
            RequestDelegate { response: TLObject, error: TLRPC.TL_error? ->
                if (error == null) {
                    val res: TLRPC.TL_messages_chatFull = response as TLRPC.TL_messages_chatFull
                    fullChatData = res.full_chat
                } else {
                    fullChatData = null
                }
            })
    }
}

object AutoPostEditorUtil {

    const val USER_STR = "Qo'shimcha text"

    private val sharedPreferences =
        ApplicationLoader.applicationContext.getSharedPreferences("db", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()
    val gson = GsonBuilder()
        .registerTypeAdapter(TLRPC.Message::class.java, MessageInstanceCreator())
        .registerTypeAdapter(TLRPC.MessageEntity::class.java, MessageEntityInstanceCreator())
        .create()
    var messagesController = MessagesController.getInstance(UserConfig.selectedAccount)

    fun getChatLink(dialogId: Long): String {
        if (DialogObject.isUserDialog(dialogId)){
            return USER_STR
        }
        val chat: TLRPC.Chat? =
            MessagesController.getInstance(UserConfig.selectedAccount).getChat(-dialogId)
        if (chat != null) {
            if (chat?.username ?: "" != "") {
                return "https://t.me/" + chat?.username
            } else {
                var info: TLRPC.ChatFull? = FullChatData.fullChatData
                if (info == null) {
                    info = MessagesStorage.getInstance(UserConfig.selectedAccount).loadChatInfo(
                        -dialogId,
                        ChatObject.isChannel(chat),
                        CountDownLatch(1),
                        false,
                        false
                    )
                }
                return info?.exported_invite?.link ?: ""
            }
        } else {
            return ""
        }
    }

    private fun getAutoEditData(): ArrayList<AutoPostData> {

        val list = ArrayList<AutoPostData>()

        try {
            val str = sharedPreferences.getString("auto_post_editor", "")
            if (str != "") {
                val type: TypeToken<*> = object : TypeToken<List<AutoPostData?>?>() {
                }
                val fromJson = gson.fromJson<ArrayList<AutoPostData>>(str, type.type)
                for (data in fromJson) {
                    list.add(data)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return list
    }

    fun saveAutoEditData(autoPostData: AutoPostData) {
        val list = getAutoEditData().toMutableList()
        val autoPostDataByDialogId = list.find { it.dialogId == autoPostData.dialogId }

        if (autoPostDataByDialogId != null) {
            val index = list.indexOf(autoPostDataByDialogId)
            if (index != -1) {
                list[index] = autoPostData
            }
        } else {
            list.add(autoPostData)
        }


        val str = gson.toJson(list)
        editor.putString("auto_post_editor", str)
        editor.commit()
    }

    fun getAutoPostDataByDialogId(dialogId: Long?): AutoPostData? {
        return getAutoEditData().find { it.dialogId == dialogId }
    }

    fun getStrByDialogId(dialogId: Long, chatActivityEnterView: ChatActivityEnterView, forceToAction: Boolean = false): CharSequence {
        var message: CharSequence = ""
        val autoPostData = getAutoPostDataByDialogId(dialogId)
        message = if (autoPostData?.active == true || forceToAction) {
            if ((autoPostData?.messageOwner?.message ?: "") == "") {
                getChatLink(dialogId)
            } else {
                splitEntities(autoPostData?.messageOwner, chatActivityEnterView)
//                "\n${autoPostData.messageOwner?.message?:""}"
            }
        } else {
            ""
        }

        return message
    }

    fun checkChannel(dialogId: Long): Boolean {
        var isChannel: Boolean
        if (DialogObject.isChatDialog(dialogId)) {
            val chat: TLRPC.Chat =
                MessagesController.getInstance(UserConfig.selectedAccount).getChat(-dialogId)
            isChannel = ChatObject.isChannel(chat) && !chat.megagroup
            var isGroup = !isChannel
            if (isChannel && (chat.creator || chat.admin_rights != null && chat.admin_rights.post_messages) && !chat.megagroup) {
                return true
            } else if (isGroup) {
                return true
            } else {
                return false
            }
        } else if (DialogObject.isUserDialog(dialogId)){
            return true
        }else{
            return false
        }
    }

    private fun splitEntities(draftMessage:TLRPC.Message?, chatActivityEnterView: ChatActivityEnterView):CharSequence{
        val param: String = draftMessage?.params?.get("prevMedia")?:""
        var previousMessageEntities = ArrayList<TLRPC.MessageEntity?>()
        if (param != "") {
            val serializedData = SerializedData(Base64.decode(param, Base64.DEFAULT))
            var constructor = serializedData.readInt32(false)
            var previousMessage = serializedData.readString(false)
            var previousAttachPath = serializedData.readString(false)
            val count = serializedData.readInt32(false)
            for (a in 0 until count) {
                constructor = serializedData.readInt32(false)
                val entity: TLRPC.MessageEntity =
                    TLRPC.MessageEntity.TLdeserialize(serializedData, constructor, false)
                previousMessageEntities.add(entity)
            }
            serializedData.cleanup()
        }
        val message: CharSequence
        if (!previousMessageEntities.isEmpty()) {
            val stringBuilder = SpannableStringBuilder.valueOf(draftMessage?.message?:"")
            MediaDataController.sortEntities(previousMessageEntities)
            for (a in 0 until (previousMessageEntities.size?:0)) {
                var entity: TLRPC.MessageEntity? = previousMessageEntities.get(a)
                if (entity is TLRPC.TL_inputMessageEntityMentionName || entity is TLRPC.TL_messageEntityMentionName) {
                    var user_id: Long = if (entity is TLRPC.TL_inputMessageEntityMentionName) {
                        (entity as TLRPC.TL_inputMessageEntityMentionName).user_id.user_id
                    } else {
                        (entity as TLRPC.TL_messageEntityMentionName).user_id
                    }
                    if (entity.offset + entity.length < stringBuilder.length && stringBuilder[entity.offset + entity.length] == ' ') {
                        entity.length++
                    }
                    stringBuilder.setSpan(
                        URLSpanUserMention("" + user_id, 3),
                        entity.offset,
                        entity.offset + entity.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                } else if (entity is TLRPC.TL_messageEntityCode || entity is TLRPC.TL_messageEntityPre) {
                    val run = TextStyleRun()
                    run.flags = run.flags or TextStyleSpan.FLAG_STYLE_MONO
                    MediaDataController.addStyleToText(
                        TextStyleSpan(run),
                        entity.offset,
                        entity.offset + entity.length,
                        stringBuilder,
                        true
                    )
                } else if (entity is TLRPC.TL_messageEntityBold) {
                    val run = TextStyleRun()
                    run.flags = run.flags or TextStyleSpan.FLAG_STYLE_BOLD
                    MediaDataController.addStyleToText(
                        TextStyleSpan(run),
                        entity.offset,
                        entity.offset + entity.length,
                        stringBuilder,
                        true
                    )
                } else if (entity is TLRPC.TL_messageEntityItalic) {
                    val run = TextStyleRun()
                    run.flags = run.flags or TextStyleSpan.FLAG_STYLE_ITALIC
                    MediaDataController.addStyleToText(
                        TextStyleSpan(run),
                        entity.offset,
                        entity.offset + entity.length,
                        stringBuilder,
                        true
                    )
                } else if (entity is TLRPC.TL_messageEntityStrike) {
                    val run = TextStyleRun()
                    run.flags = run.flags or TextStyleSpan.FLAG_STYLE_STRIKE
                    MediaDataController.addStyleToText(
                        TextStyleSpan(run),
                        entity.offset,
                        entity.offset + entity.length,
                        stringBuilder,
                        true
                    )
                } else if (entity is TLRPC.TL_messageEntityUnderline) {
                    val run = TextStyleRun()
                    run.flags = run.flags or TextStyleSpan.FLAG_STYLE_UNDERLINE
                    MediaDataController.addStyleToText(
                        TextStyleSpan(run),
                        entity.offset,
                        entity.offset + entity.length,
                        stringBuilder,
                        true
                    )
                } else if (entity is TLRPC.TL_messageEntityTextUrl) {
                    stringBuilder.setSpan(
                        URLSpanReplacement(entity.url),
                        entity.offset,
                        entity.offset + entity.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                } else if (entity is TLRPC.TL_messageEntitySpoiler) {
                    val run = TextStyleRun()
                    run.flags = run.flags or TextStyleSpan.FLAG_STYLE_SPOILER
                    MediaDataController.addStyleToText(
                        TextStyleSpan(run),
                        entity.offset,
                        entity.offset + entity.length,
                        stringBuilder,
                        true
                    )
                } else if (entity is TLRPC.TL_messageEntityBlockquote) {
                    QuoteSpan.putQuoteToEditable(
                        stringBuilder,
                        entity.offset,
                        entity.offset + entity.length,
                        entity.collapsed
                    )
                } else if (entity is TLRPC.TL_messageEntityCustomEmoji) {
                    var fontMetrics: FontMetricsInt? = null
                    try {
                        fontMetrics =
                            chatActivityEnterView.getEditField()?.getPaint()?.getFontMetricsInt()
                    } catch (e: java.lang.Exception) {
                        FileLog.e(e, false)
                    }
                    val e: TLRPC.TL_messageEntityCustomEmoji =
                        entity as TLRPC.TL_messageEntityCustomEmoji
                    var span: AnimatedEmojiSpan?
                    if (e.document != null) {
                        span = AnimatedEmojiSpan(e.document, fontMetrics)
                    } else {
                        span = AnimatedEmojiSpan(e.document_id, fontMetrics)
                    }
                    stringBuilder.setSpan(
                        span,
                        entity.offset,
                        entity.offset + entity.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }else if (entity is TLRPC.MessageEntity){
                    Log.d("AED", "splitEntities: ")
                }
            }
            message = stringBuilder

        }else{
            message = draftMessage?.message?:""
        }
        return message
    }
}

class MessageInstanceCreator : InstanceCreator<TLRPC.Message> {
    override fun createInstance(type: Type): TLRPC.Message {
        return TLRPC.Message()
    }
}

class MessageEntityInstanceCreator : InstanceCreator<TLRPC.MessageEntity> {
    override fun createInstance(type: Type): TLRPC.MessageEntity {
        return object : TLRPC.MessageEntity() {}
    }
}