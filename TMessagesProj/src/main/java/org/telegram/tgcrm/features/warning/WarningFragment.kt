package org.telegram.tgcrm.features.warning

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import org.telegram.messenger.R
import org.telegram.tgcrm.TgCrmUtilities
import org.telegram.tgcrm.features.check_unread_msgs.Warning
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WarningFragment : BaseFragment() {

    private var progressDialog: AlertDialog? = null
    private var recyclerView: RecyclerView? = null
    private var warningAdapter: WarningAdapter? = null
    var warnings: ArrayList<Warning> = ArrayList()

    override fun createView(context: Context?): View? {

        progressDialog = AlertDialog(getContext(), AlertDialog.ALERT_TYPE_SPINNER)

        actionBar.setBackButtonImage(R.drawable.ic_ab_back)
        actionBar.setAllowOverlayTitle(false)
        actionBar.setActionBarMenuOnItemClick(object : ActionBarMenuOnItemClick() {
            override fun onItemClick(id: Int) {
                finishFragment()
            }
        })

        actionBar.setTitle("Jarimalarim")

        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        recyclerView = RecyclerView(context!!)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        warningAdapter = WarningAdapter(warnings)
        recyclerView!!.adapter = warningAdapter

        getWarnings()

        linearLayout.addView(recyclerView)

        fragmentView = linearLayout
        return fragmentView

    }

    fun getWarnings() {
        progressDialog!!.show()
        warnings.clear()

        WarningRemote.getWarning(FirebaseFirestore.getInstance()) {
            warnings.clear()
            progressDialog?.dismiss()
            if (it != null) {
                warnings.addAll(it.sortedBy { it.warningTime }.reversed())
            }
            warningAdapter?.notifyDataSetChanged()
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

    private class WarningAdapter(private val items: List<Warning>) :
        RecyclerView.Adapter<WarningAdapter.WarningVH>() {

        inner class WarningVH(val warningItem: WarningItem) : RecyclerView.ViewHolder(warningItem) {
            fun onBind(warning: Warning) {
                warningItem.bind(warning)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WarningVH {
            return WarningVH(WarningItem(parent.context))
        }

        override fun onBindViewHolder(holder: WarningVH, position: Int) {
            holder.onBind(items[position])
        }

        override fun getItemCount(): Int {
            return items.size
        }

    }

}

class WarningItem(context: Context) : LinearLayout(context) {

    private val workerNameTextView: TextView
    private val warningTimeTextView: TextView

    init {
        orientation = VERTICAL
        setPadding(16, 20, 16, 20)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        workerNameTextView = TextView(context).apply {
            textSize = 16f
            setTextColor(Theme.getColor(Theme.key_chats_menuItemText))
        }

        warningTimeTextView = TextView(context).apply {
            textSize = 12f
            setTextColor(Theme.getColor(Theme.key_chats_menuItemText))
        }

        addView(workerNameTextView)
        addView(warningTimeTextView)
    }

    fun bind(warning: Warning) {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val date: Date? = warning.warningTime?.toDate()
        val time = sdf.format(date)

        workerNameTextView.text = "Ishchi ismi: " + warning.worker
        warningTimeTextView.text = "jarima vaqti: " + time
    }
}