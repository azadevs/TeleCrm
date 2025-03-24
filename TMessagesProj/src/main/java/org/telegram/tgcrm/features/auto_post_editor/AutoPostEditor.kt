package org.telegram.tgcrm.features.auto_post_editor

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.util.Base64
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.ColorUtils
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController
import org.telegram.messenger.MediaDataController
import org.telegram.messenger.R
import org.telegram.tgcrm.AutoPostData
import org.telegram.tgcrm.AutoPostEditorUtil
import org.telegram.tgnet.SerializedData
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.ActionBar
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.ChatActivity
import org.telegram.ui.Components.ChatActivityEnterView
import org.telegram.ui.Components.LayoutHelper
import org.telegram.ui.Components.SizeNotifierFrameLayout
import org.telegram.ui.WrappedResourceProvider
import kotlin.math.min

class AutoPostEditor(var dialogId: Long) : BaseFragment() {

    lateinit var doneButton: View
    private var chatActivityEnterView: ChatActivityEnterView? = null
    lateinit var sizeNotifierFrameLayout: SizeNotifierFrameLayout
    var thisData: AutoPostData? = null
    var oldInputStr: CharSequence = ""
    var link = AutoPostEditorUtil.getChatLink(dialogId)
    override fun createView(context: Context?): View {
        thisData = AutoPostEditorUtil.getAutoPostDataByDialogId(dialogId)
        setUpActionBar()
        setUpBaseUi()
        setUpInput()
        oldInputStr = AutoPostEditorUtil.getStrByDialogId(dialogId, chatActivityEnterView as ChatActivityEnterView, true)
        if (oldInputStr == link) oldInputStr = ""
        chatActivityEnterView?.setFieldHint(link)
        chatActivityEnterView?.fieldText = oldInputStr
        chatActivityEnterView?.setSelection(chatActivityEnterView?.fieldText?.length ?: 0)

        return fragmentView
    }

    private fun setUpInput() {
        if (chatActivityEnterView != null) {
            chatActivityEnterView!!.onDestroy()
        }
        val emojiResourceProvider: Theme.ResourcesProvider =
            object : WrappedResourceProvider(resourceProvider) {
                override fun appendColors() {
                    sparseIntArray.put(
                        Theme.key_chat_emojiPanelBackground, ColorUtils.setAlphaComponent(
                            Color.WHITE, 30
                        )
                    )
                }
            }
        chatActivityEnterView = object : ChatActivityEnterView(
            parentActivity, sizeNotifierFrameLayout,
            ChatActivity.instance, false, emojiResourceProvider, true
        ) {

            override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
                if (alpha != 1.0f) {
                    return false
                }
                return super.onInterceptTouchEvent(ev)
            }

            override fun onTouchEvent(event: MotionEvent): Boolean {
                if (alpha != 1.0f) {
                    return false
                }
                return super.onTouchEvent(event)
            }

            override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
                if (alpha != 1.0f) {
                    return false
                }
                return super.dispatchTouchEvent(ev)
            }

            override fun pannelAnimationEnabled(): Boolean {
                return true
            }

            override fun openKeyboard() {
                super.openKeyboard()
            }

            override fun checkAnimation() {

            }

            override fun onLineCountChanged(oldLineCount: Int, newLineCount: Int) {

            }

            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }

        chatActivityEnterView?.setDelegate(object :
            ChatActivityEnterView.ChatActivityEnterViewDelegate {
            override fun onMessageSend(message: CharSequence, notify: Boolean, scheduleDate: Int) {}

            override fun needSendTyping() {}

            override fun onTextChanged(
                text: CharSequence,
                bigChange: Boolean,
                fromDraft: Boolean
            ) {
                updateDoneButton()
            }

            override fun onTextSelectionChanged(start: Int, end: Int) {}

            override fun onTextSpansChanged(text: CharSequence) {}

            override fun onAttachButtonHidden() {}

            override fun onAttachButtonShow() {}

            override fun onWindowSizeChanged(size: Int) {}

            override fun onStickersTab(opened: Boolean) {}

            override fun onMessageEditEnd(loading: Boolean) {}

            override fun didPressAttachButton() {}

            override fun needStartRecordVideo(
                state: Int,
                notify: Boolean,
                scheduleDate: Int,
                ttl: Int,
                effectId: Long
            ) {
            }

            override fun toggleVideoRecordingPause() {}

            override fun needChangeVideoPreviewState(state: Int, seekProgress: Float) {}

            override fun onSwitchRecordMode(video: Boolean) {}

            override fun onPreAudioVideoRecord() {}

            override fun needStartRecordAudio(state: Int) {}

            override fun needShowMediaBanHint() {}

            override fun onStickersExpandedChange() {}

            override fun onUpdateSlowModeButton(button: View, show: Boolean, time: CharSequence) {}

            override fun onSendLongClick() {}

            override fun onAudioVideoInterfaceUpdated() {}
        })
        chatActivityEnterView?.setAllowStickersAndGifs(true, true, true)

        sizeNotifierFrameLayout.addView(
            chatActivityEnterView,
            LayoutHelper.createFrame(
                LayoutHelper.MATCH_PARENT,
                LayoutHelper.WRAP_CONTENT.toFloat(),
                Gravity.LEFT or Gravity.BOTTOM,
                0f,
                0f,
                0f,
                0f
            )
        )
    }

    private fun setUpBaseUi() {
        sizeNotifierFrameLayout = object : SizeNotifierFrameLayout(context) {
            private var ignoreLayout = false

            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                val widthSize = MeasureSpec.getSize(widthMeasureSpec)
                var heightSize = MeasureSpec.getSize(heightMeasureSpec)

                setMeasuredDimension(widthSize, heightSize)
                heightSize -= paddingTop

                measureChildWithMargins(actionBar, widthMeasureSpec, 0, heightMeasureSpec, 0)

                val keyboardSize = measureKeyboardHeight()
                if (keyboardSize > AndroidUtilities.dp(20f)) {
                    ignoreLayout = true
                    chatActivityEnterView!!.hideEmojiView()
                    ignoreLayout = false
                }

                val childCount = childCount
                for (i in 0 until childCount) {
                    val child = getChildAt(i)
                    if (child == null || child.visibility == GONE || child === actionBar) {
                        continue
                    }
                    if (chatActivityEnterView != null && chatActivityEnterView!!.isPopupView(child)) {
                        if (AndroidUtilities.isInMultiwindow || AndroidUtilities.isTablet()) {
                            if (AndroidUtilities.isTablet()) {
                                child.measure(
                                    MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY),
                                    MeasureSpec.makeMeasureSpec(
                                        min(
                                            AndroidUtilities.dp((if (AndroidUtilities.isTablet()) 200 else 320).toFloat())
                                                .toDouble(),
                                            (heightSize - AndroidUtilities.statusBarHeight + paddingTop).toDouble()
                                        )
                                            .toInt(), MeasureSpec.EXACTLY
                                    )
                                )
                            } else {
                                child.measure(
                                    MeasureSpec.makeMeasureSpec(
                                        widthSize,
                                        MeasureSpec.EXACTLY
                                    ),
                                    MeasureSpec.makeMeasureSpec(
                                        heightSize - AndroidUtilities.statusBarHeight + paddingTop,
                                        MeasureSpec.EXACTLY
                                    )
                                )
                            }
                        } else {
                            child.measure(
                                MeasureSpec.makeMeasureSpec(
                                    widthSize,
                                    MeasureSpec.EXACTLY
                                ),
                                MeasureSpec.makeMeasureSpec(
                                    child.layoutParams.height,
                                    MeasureSpec.EXACTLY
                                )
                            )
                        }
                    } else {
                        measureChildWithMargins(
                            child,
                            widthMeasureSpec,
                            0,
                            heightMeasureSpec,
                            0
                        )
                    }
                }
            }

            override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
                val count = childCount

                val keyboardSize = measureKeyboardHeight()
                val paddingBottom =
                    if (keyboardSize <= AndroidUtilities.dp(20f) && !AndroidUtilities.isInMultiwindow && !AndroidUtilities.isTablet()) chatActivityEnterView!!.emojiPadding else 0
                setBottomClip(paddingBottom)

                for (i in 0 until count) {
                    val child = getChildAt(i)
                    if (child.visibility == GONE) {
                        continue
                    }
                    val lp = child.layoutParams as LayoutParams

                    val width = child.measuredWidth
                    val height = child.measuredHeight

                    var childLeft: Int
                    var childTop: Int

                    var gravity = lp.gravity
                    if (gravity == -1) {
                        gravity = Gravity.TOP or Gravity.LEFT
                    }

                    val absoluteGravity = gravity and Gravity.HORIZONTAL_GRAVITY_MASK
                    val verticalGravity = gravity and Gravity.VERTICAL_GRAVITY_MASK

                    childLeft =
                        when (absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                            Gravity.CENTER_HORIZONTAL -> (r - l - width) / 2 + lp.leftMargin - lp.rightMargin
                            Gravity.RIGHT -> r - width - lp.rightMargin
                            Gravity.LEFT -> lp.leftMargin
                            else -> lp.leftMargin
                        }
                    childTop = when (verticalGravity) {
                        Gravity.TOP -> lp.topMargin + paddingTop
                        Gravity.CENTER_VERTICAL -> ((b - paddingBottom) - t - height) / 2 + lp.topMargin - lp.bottomMargin
                        Gravity.BOTTOM -> (b - paddingBottom) - t - height - lp.bottomMargin
                        else -> lp.topMargin
                    }
                    if (chatActivityEnterView != null && chatActivityEnterView!!.isPopupView(child)) {
                        childTop = if (AndroidUtilities.isTablet()) {
                            measuredHeight - child.measuredHeight
                        } else {
                            measuredHeight + keyboardSize - child.measuredHeight
                        }
                    }
                    child.layout(childLeft, childTop, childLeft + width, childTop + height)
                }

                notifyHeightChanged()
            }

            override fun requestLayout() {
                if (ignoreLayout) {
                    return
                }
                super.requestLayout()
            }
        }
        sizeNotifierFrameLayout.setOnTouchListener { v: View?, event: MotionEvent? -> true }
        fragmentView = sizeNotifierFrameLayout
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray))
    }

    private fun setUpActionBar() {
        actionBar.setTitle("Chat settings")
        actionBar.setBackButtonImage(R.drawable.ic_ab_back)
        actionBar.setAllowOverlayTitle(false)
        actionBar.setActionBarMenuOnItemClick(object : ActionBar.ActionBarMenuOnItemClick() {
            override fun onItemClick(id: Int) {
                if (id != -1) {
                    showDialogApply()
                } else {
                    if (checkEditData()) {
                        finishFragment()
                    } else {
                        discardData()
                    }
                }
            }
        })

        val menu = actionBar.createMenu()
        doneButton = menu.addItemWithWidth(
            1,
            R.drawable.ic_ab_done,
            AndroidUtilities.dp(56f),
            LocaleController.getString("Done", R.string.Done)
        )
        val hasChanges = false
        doneButton.setAlpha(if (hasChanges) 1.0f else 0.0f)
        doneButton.setScaleX(if (hasChanges) 1.0f else 0.0f)
        doneButton.setScaleY(if (hasChanges) 1.0f else 0.0f)
        doneButton.setEnabled(hasChanges)
    }

    private fun checkEditData(): Boolean {
        return oldInputStr.toString() == (if (chatActivityEnterView?.editField == null) "" else chatActivityEnterView?.editField?.getTextToUse()).toString()
    }

    private fun updateDoneButton() {
        var enabled = false
        if (!checkEditData()) {
            enabled = true
        }
        doneButton.isEnabled = enabled
        doneButton.animate().alpha(if (enabled) 1.0f else 0.0f)
            .scaleX(if (enabled) 1.0f else 0.0f).scaleY(if (enabled) 1.0f else 0.0f)
            .setDuration(180).start()
    }

    override fun onBackPressed(): Boolean {
        if (chatActivityEnterView != null && chatActivityEnterView!!.isPopupShowing) {
            chatActivityEnterView!!.hidePopup(true)
            return false
        } else if (checkEditData()) {
            return true
        } else {
            discardData()
            return false
        }
    }

    override fun onPause() {
        super.onPause()
        if (chatActivityEnterView != null) {
            chatActivityEnterView?.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (chatActivityEnterView != null) {
            chatActivityEnterView!!.onResume()
            chatActivityEnterView!!.editField?.requestFocus()
        }
    }

    override fun onFragmentDestroy() {
        super.onFragmentDestroy()
        if (chatActivityEnterView != null) {
            chatActivityEnterView!!.onDestroy()
        }
    }

    private fun discardData() {
        val builder = AlertDialog.Builder(
            parentActivity
        )
        builder.setTitle(LocaleController.getString(R.string.AppName))
        builder.setMessage("LanguageCode.getMyTitles(39)")
        builder.setPositiveButton(
            LocaleController.getString("ApplyTheme", R.string.ApplyTheme)
        ) { dialogInterface: DialogInterface?, i: Int ->
            save()
            finishFragment()
        }
        builder.setNegativeButton(
            LocaleController.getString("PassportDiscard", R.string.PassportDiscard)
        ) { dialog: DialogInterface?, which: Int -> finishFragment() }
        showDialog(builder.create())
    }

    private fun showDialogApply() {
        val builder = AlertDialog.Builder(
            parentActivity
        )
        builder.setMessage("LanguageCode.getMyTitles(40)")
        builder.setTitle(LocaleController.getString(R.string.AppName))
        builder.setPositiveButton(
            LocaleController.getString("OK", R.string.OK)
        ) { dialogInterface: DialogInterface?, i: Int ->
            save()
            finishFragment()
        }
        builder.setNegativeButton(
            LocaleController.getString("Cancel", R.string.Cancel),
            null
        )
        showDialog(builder.create())
    }

    private fun save(){
        val active = thisData?.active ?: false
        var message = (if (chatActivityEnterView?.editField == null) "" else chatActivityEnterView?.editField?.getTextToUse())?.trim()
        var messageOwner = TLRPC.Message()
        val messages = arrayOf(message)
        messageOwner.message = message.toString()
        messageOwner.entities = MediaDataController.getInstance(currentAccount).getEntities(messages, true)

        val serializedDataCalc = SerializedData(true)
        writePreviousMessageData(messageOwner, serializedDataCalc)
        val prevMessageData = SerializedData(serializedDataCalc.length())
        writePreviousMessageData(messageOwner, prevMessageData)
        val params = HashMap<String, String>()
        params["prevMedia"] = Base64.encodeToString(prevMessageData.toByteArray(), Base64.DEFAULT)
        prevMessageData.cleanup()

        messageOwner.params = params
        AutoPostEditorUtil.saveAutoEditData(
            AutoPostData(
                active,
                dialogId,
                messageOwner
            )
        )
    }

//    if (chatActivityEnterView.length() == 0)
//    {
//        val v = parentActivity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//        v?.vibrate(200)
//        AndroidUtilities.shakeView(chatActivityEnterView)
//        return
//    }

    private fun writePreviousMessageData(message: TLRPC.Message, data: SerializedData) {
        if (message.media == null) {
            val media: TLRPC.TL_messageMediaEmpty = TLRPC.TL_messageMediaEmpty()
            media.serializeToStream(data)
        } else {
            message.media.serializeToStream(data)
        }
        data.writeString(if (message.message != null) message.message else "")
        data.writeString(if (message.attachPath != null) message.attachPath else "")
        var count = message?.entities?.size?:0
        data.writeInt32(count)
        for (a in 0 until count) {
            message.entities.get(a).serializeToStream(data)
        }
    }

}