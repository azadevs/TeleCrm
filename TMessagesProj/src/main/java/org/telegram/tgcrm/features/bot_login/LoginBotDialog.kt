package org.telegram.tgcrm.features.bot_login

import android.content.Context
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import org.telegram.ui.ActionBar.AlertDialog

object LoginBotDialog {

    fun botTokenDialog(context: Context, callback: (String) -> Unit) {

        fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val botTokenEt = EditText(context).apply {
            setHint("token")
            textSize = 18f
            maxLines = 5
            setTextColor(resources.getColor(android.R.color.holo_blue_light))
            setHintTextColor(resources.getColor(android.R.color.holo_blue_light))
            setPadding(20, 30, 20, 30)
        }

        layout.addView(botTokenEt)

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Bot Token")
        builder.setMessage("Bot Token kiriting")
        builder.setView(layout)
        builder.setPositiveButton("ok") { dialog, which ->
        }
        builder.setNegativeButton("cancel") { dialog, which ->
        }

        val dialog = builder.create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val token = botTokenEt.text.toString()
                if (token != "") {
                    callback(token)
                    dialog.dismiss()
                }
            }
        }

        builder.show()
    }
}