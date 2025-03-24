package org.telegram.tgcrm.features.chat_settings

import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.telegram.tgcrm.features.auto_post_editor.AutoPostEditor
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.tgcrm.AutoPostData
import org.telegram.tgcrm.AutoPostEditorUtil
import org.telegram.ui.ActionBar.ActionBar
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Cells.NotificationsCheckCell
import org.telegram.ui.Cells.TextCheckCell
import org.telegram.ui.Cells.TextInfoPrivacyCell
import org.telegram.ui.Cells.TextSettingsCell
import org.telegram.ui.Components.LayoutHelper
import org.telegram.ui.Components.RecyclerListView
import org.telegram.ui.Components.SizeNotifierFrameLayout

class ChatSettings(val dialogId: Long) : BaseFragment() {

    private var listView: RecyclerListView? = null
    private var listAdapter: MyListAdapter? = null
    private var autoPostEditor = -1
    private var rowCount = 0
    var thisAutoPostData: AutoPostData? = null

    override fun onResume() {
        thisAutoPostData = AutoPostEditorUtil.getAutoPostDataByDialogId(dialogId)
        super.onResume()
    }

    override fun createView(context: Context?): View {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back)
        actionBar.setAllowOverlayTitle(false)
        actionBar.setActionBarMenuOnItemClick(object : ActionBar.ActionBarMenuOnItemClick() {
            override fun onItemClick(id: Int) {
                if (id == -1) {
                    finishFragment()
                }
            }
        })

        var fragmentContentView: View
        val frameLayout = FrameLayout(context!!)
        fragmentContentView = frameLayout

        val contentView: SizeNotifierFrameLayout = object : SizeNotifierFrameLayout(context) {
            override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
                fragmentContentView.layout(0, 0, measuredWidth, measuredHeight.also {})

                notifyHeightChanged()
            }

            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                val width = MeasureSpec.getSize(widthMeasureSpec)
                val height = MeasureSpec.getSize(heightMeasureSpec)
                setMeasuredDimension(width, height)

                var frameHeight = height
                fragmentContentView.measure(
                    MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(frameHeight, MeasureSpec.EXACTLY)
                )
            }
        }

        contentView.setDelegate { _: Int, _: Boolean -> }

        fragmentView = contentView
        contentView.addView(
            fragmentContentView,
            LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1f)
        )

        actionBar.setTitle("Chat sozlamalari")
        frameLayout.tag = Theme.key_windowBackgroundGray
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray))
        listView = RecyclerListView(context)
        listView?.setLayoutManager(object : LinearLayoutManager(context, VERTICAL, false) {
            override fun supportsPredictiveItemAnimations(): Boolean {
                return false
            }
        })
        listView?.setVerticalScrollBarEnabled(false)
        listView?.setItemAnimator(null)
        listView?.setLayoutAnimation(null)
        frameLayout.addView(
            listView,
            LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT.toFloat())
        )
        listView?.setAdapter(MyListAdapter(context).also {
            listAdapter = it
        })
        listView?.setOnItemClickListener { view, position, x, y ->
            if (!view.isEnabled) {
                return@setOnItemClickListener
            }
            if (position == autoPostEditor) {
                if (LocaleController.isRTL && x <= AndroidUtilities.dp(76f) || !LocaleController.isRTL && x >= view.measuredWidth - AndroidUtilities.dp(
                        76f
                    )
                ) {
                    val checkCell = view as NotificationsCheckCell
                    checkCell.isChecked = !checkCell.isChecked
                    thisAutoPostData =
                        AutoPostData(checkCell.isChecked, dialogId, thisAutoPostData?.messageOwner)
                    AutoPostEditorUtil.saveAutoEditData(thisAutoPostData!!)

                } else {
                    presentFragment(AutoPostEditor(dialogId))
                }
            }
        }

        updateRows()
        if (listAdapter != null) {
            listAdapter?.notifyDataSetChanged()
        }

        return fragmentView
    }

    override fun hasForceLightStatusBar(): Boolean {
        return false
    }

    private fun updateRows() {
        rowCount = 0
        if (AutoPostEditorUtil.checkChannel(dialogId)) {
            autoPostEditor = rowCount++
        }
    }

    private inner class MyListAdapter(private val mContext: Context) :
        RecyclerListView.SelectionAdapter() {

        private val VIEW_TYPE_CHECK = 0
        private val VIEW_TYPE_SETTING = 1
        private val TYPE_NIGHT_THEME = 2


        override fun isEnabled(holder: RecyclerView.ViewHolder): Boolean {
            val position = holder.adapterPosition
            return position == autoPostEditor
        }

        override fun getItemCount(): Int {
            return rowCount
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view: View
            when (viewType) {
                TYPE_NIGHT_THEME -> {
                    view = NotificationsCheckCell(mContext, 21, 60, true)
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
                }

                VIEW_TYPE_CHECK -> {
                    view = TextCheckCell(mContext)
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
                }

                VIEW_TYPE_SETTING -> {
                    view = TextSettingsCell(mContext)
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
                }

                else -> view = TextInfoPrivacyCell(mContext)
            }
            return RecyclerListView.Holder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder.itemViewType) {
                TYPE_NIGHT_THEME -> {
                    var checkCell: NotificationsCheckCell? =
                        holder.itemView as NotificationsCheckCell
                    if (position == autoPostEditor) {
                        checkCell?.setTextAndValueAndIconAndCheck(
                            "Avtomatik text qo'shuvchi",
                            "Bu xsusiyat yoqilganda oldindan kiritilgan text kanalda chiqariladigan har bir post oxiridan avtomatik qo'shiladi. Agar text oldindan qo'shilmasa lekin bu xsusiyat faollashtirilsa post oxirida kanal manzili qo'shiladi.",
                            R.drawable.msg_photo_text2,
                            thisAutoPostData?.active ?: false,
                            0,
                            true,
                            true
                        )
                    }
                }

                VIEW_TYPE_CHECK -> {

                }

                VIEW_TYPE_SETTING -> {

                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            if (position == autoPostEditor) {
                return TYPE_NIGHT_THEME
            }
            return VIEW_TYPE_CHECK
        }
    }

    private fun showDialogApply(message: String, callback: (Boolean) -> Unit) {
        val builder = AlertDialog.Builder(
            parentActivity
        )
        builder.setMessage(message)
        builder.setTitle("Pro Messenger")
        builder.setPositiveButton(
            LocaleController.getString("OK", R.string.OK)
        ) { dialogInterface: DialogInterface?, i: Int ->
            callback(true)
            finishFragment()
        }
        builder.setNegativeButton(
            LocaleController.getString("Cancel", R.string.Cancel),
            null
        ).setOnCancelListener {
            callback(false)
        }
        showDialog(builder.create())
    }

}