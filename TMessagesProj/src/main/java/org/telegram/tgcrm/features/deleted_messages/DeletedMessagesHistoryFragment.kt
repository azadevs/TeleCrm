package org.telegram.tgcrm.features.deleted_messages

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.telegram.messenger.R
import org.telegram.tgcrm.TgCrmUtilities
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeletedMessagesHistoryFragment : BaseFragment() {
    private var progressDialog: AlertDialog? = null
    private var recyclerView: RecyclerView? = null

    private var deletedMessagesAdapter: DeletedMessagesAdapter? = null

    var deletedMsgs: ArrayList<DeletedMessageByWorker> = ArrayList()

    var tgCrmUtilities: TgCrmUtilities? = null

    override fun createView(context: Context?): View {
        tgCrmUtilities = TgCrmUtilities()
        progressDialog = AlertDialog(getContext(), AlertDialog.ALERT_TYPE_SPINNER)

        actionBar.setBackButtonImage(R.drawable.ic_ab_back)
        actionBar.setAllowOverlayTitle(false)
        actionBar.setActionBarMenuOnItemClick(object : ActionBarMenuOnItemClick() {
            override fun onItemClick(id: Int) {
                finishFragment()
            }
        })

        actionBar.setTitle("O'chirilgan xabarlar")

        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        recyclerView = RecyclerView(context!!)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        deletedMessagesAdapter = DeletedMessagesAdapter(deletedMsgs)
        recyclerView!!.adapter = deletedMessagesAdapter

        getActions()

        linearLayout.addView(recyclerView)

        fragmentView = linearLayout
        return fragmentView
    }

    fun getActions() {
        progressDialog!!.show()
        deletedMsgs.clear()

        tgCrmUtilities!!.getDeletedMessages(true) {
            deletedMsgs.clear()
            progressDialog?.dismiss()
            if (it != null) {
                deletedMsgs.addAll(it.sortedBy { it.deleteTime }.reversed())
            }
            deletedMessagesAdapter?.notifyDataSetChanged()
        }
    }


    override fun onFragmentDestroy() {
        clearViews()
        recyclerView = null
        parentDialog = null
        progressDialog = null
        fragmentView = null
        super.onFragmentDestroy()
    }

    private class DeletedMessagesAdapter(private val items: List<DeletedMessageByWorker>) :
        RecyclerView.Adapter<DeletedMessagesAdapter.DeletedMessagesVH>() {

        inner class DeletedMessagesVH(val deletedMessageItem: DeletedMessageItem):RecyclerView.ViewHolder(deletedMessageItem) {
            fun onBind(deletedMessageByWorker: DeletedMessageByWorker){
                deletedMessageItem.bind(deletedMessageByWorker)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeletedMessagesVH {
            return DeletedMessagesVH(DeletedMessageItem(parent.context))
        }

        override fun onBindViewHolder(holder: DeletedMessagesVH, position: Int) {
            holder.onBind(items[position])
        }

        override fun getItemCount(): Int {
            return items.size
        }

    }
}

class DeletedMessageItem (context: Context) : LinearLayout(context) {

    private val dialogNameTextView: TextView
    private val workerNameTextView: TextView
    private val dialogIdTextView: TextView
    private val messageIdTextView: TextView
    private val typeTextView: TextView
    private val messageTextView: TextView
    private val deletedTimeTextView: TextView

    init {
        orientation = VERTICAL
        setPadding(16, 20, 16, 20)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        dialogNameTextView = TextView(context).apply {
            textSize = 16f
            setTextColor(Theme.getColor(Theme.key_chats_menuItemText))
        }
        workerNameTextView = TextView(context).apply {
            textSize = 16f
            setTextColor(Theme.getColor(Theme.key_chats_menuItemText))
        }
        dialogIdTextView = TextView(context).apply {
            textSize = 16f
            setTextColor(Theme.getColor(Theme.key_chats_menuItemText))
        }
        messageIdTextView = TextView(context).apply {
            textSize = 16f
            setTextColor(Theme.getColor(Theme.key_chats_menuItemText))
        }
        typeTextView = TextView(context).apply {
            textSize = 16f
            setTextColor(Theme.getColor(Theme.key_chats_menuItemText))
        }

        messageTextView = TextView(context).apply {
            textSize = 16f
            setTextColor(Theme.getColor(Theme.key_chats_menuItemText))
        }

        deletedTimeTextView = TextView(context).apply {
            textSize = 12f
            setTextColor(Theme.getColor(Theme.key_chats_menuItemText))
        }

        addView(dialogIdTextView)
        addView(dialogNameTextView)
        addView(workerNameTextView)
        addView(typeTextView)
        addView(messageIdTextView)
        addView(messageTextView)
        addView(deletedTimeTextView)
    }

    fun bind(deletedMessageByWorker: DeletedMessageByWorker){
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val date: Date? = deletedMessageByWorker.deleteTime?.toDate()
        val time = sdf.format(date)

        dialogNameTextView.text = "Chat nomi: " + deletedMessageByWorker.dialogName
        workerNameTextView.text = "Ishchi ismi: " + deletedMessageByWorker.workerName
        dialogIdTextView.text = "dialog idsi: " + deletedMessageByWorker.dialogId.toString()
        messageIdTextView.text = "xabar idsi: " + deletedMessageByWorker.messageId.toString()
        typeTextView.text = "nechi tarafdan o'chirilgani: " + deletedMessageByWorker.type.toString()
        messageTextView.text = "xabar: " + deletedMessageByWorker.messageText
        deletedTimeTextView.text = "o'chirilgan vaqti: " + time
    }
}
