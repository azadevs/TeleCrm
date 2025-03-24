package org.telegram.tgcrm.features.alarm_screen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.messenger.databinding.AlarmScreenBinding
import org.telegram.tgcrm.TgCrmUtilities
import org.telegram.tgcrm.model.UserCredentialsData
import org.telegram.ui.ActionBar.ActionBar
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.SizeNotifierFrameLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AlarmFragment(var dialogId: Long) : BaseFragment() {

    lateinit var doneButton: View
    lateinit var sizeNotifierFrameLayout: SizeNotifierFrameLayout

    var message: String = ""
    var time: Long = 0L
    var workerId = TgCrmUtilities.getCachedUserData(UserCredentialsData.USERNAME)
    var oldAlarm = MyAlarmManager.getAlarmByDialogId(dialogId)

    var sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
    lateinit var alarmScreenBinding: AlarmScreenBinding

    override fun createView(context: Context?): View? {
        alarmScreenBinding = AlarmScreenBinding.inflate(LayoutInflater.from(context))
        message = oldAlarm?.message ?: ""
        time = oldAlarm?.time ?: 0L
        workerId =
            oldAlarm?.workerId ?: TgCrmUtilities.getCachedUserData(UserCredentialsData.USERNAME)

        setUpActionBar()
        alarmScreenBinding.timeTv.text = sdf.format(Date(time))
        updateMessage()

        alarmScreenBinding.firstMessage.setTextColor(Theme.getColor(Theme.key_chats_menuItemText))
        alarmScreenBinding.secondMessage.setTextColor(Theme.getColor(Theme.key_chats_menuItemText))
        alarmScreenBinding.timeTv.setTextColor(Theme.getColor(Theme.key_chats_menuItemText))

        alarmScreenBinding.firstMessage.setOnClickListener {
            message = "Aloqaga chiqish"
            updateMessage()
            updateDoneButton()
        }

        alarmScreenBinding.secondMessage.setOnClickListener {
            message = "Tabrik yo'llash"
            updateMessage()
            updateDoneButton()
        }

        alarmScreenBinding.removeAlarm.setOnClickListener {
            if (oldAlarm == null){
                Toast.makeText(context, "Budulnik qoyilmagan", Toast.LENGTH_SHORT).show()
            }else{
                showDialogRemove()
            }
        }

        val calendar = Calendar.getInstance()


        val datePicker = DatePickerDialog(
            context!!,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val timePicker = TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)

                        val selectedTimeInMillis = calendar.timeInMillis
                        time = calendar.timeInMillis

                        alarmScreenBinding.timeTv.text = sdf.format(Date(selectedTimeInMillis))

                        updateDoneButton()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                )
                timePicker.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        alarmScreenBinding.timeTv.setOnClickListener {
            datePicker.show()
        }

        fragmentView = alarmScreenBinding.root

        return fragmentView
    }

    private fun setUpActionBar() {
        actionBar.setTitle("Eslatma")
        actionBar.setBackButtonImage(R.drawable.ic_ab_back)
        actionBar.setAllowOverlayTitle(false)
        actionBar.setActionBarMenuOnItemClick(object : ActionBar.ActionBarMenuOnItemClick() {
            override fun onItemClick(id: Int) {
                if (id != -1) {
                    showDialogApply()
                } else {
                    if (!checkEditData()) {
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

    fun updateMessage() {
        if (message.toLowerCase().contains(("Aloqaga chiqish").toLowerCase())) {
            alarmScreenBinding.firstMessage.setBackgroundResource(R.drawable.btn_bg)
            alarmScreenBinding.secondMessage.setBackgroundResource(R.drawable.btn_bg_unsellected)
        } else if (message.toLowerCase().contains(("Tabrik yo'llash").toLowerCase())) {
            alarmScreenBinding.firstMessage.setBackgroundResource(R.drawable.btn_bg_unsellected)
            alarmScreenBinding.secondMessage.setBackgroundResource(R.drawable.btn_bg)
        }
    }

    private fun checkEditData(): Boolean {
        var status = false
        if ((oldAlarm?.message != message || oldAlarm?.time != time || oldAlarm?.workerId != workerId) && workerId != "" && time != 0L && message != "") {
            status = true
        }

        return status
    }

    private fun updateDoneButton() {
        var enabled = checkEditData()
        doneButton.isEnabled = enabled
        doneButton.animate().alpha(if (enabled) 1.0f else 0.0f)
            .scaleX(if (enabled) 1.0f else 0.0f).scaleY(if (enabled) 1.0f else 0.0f)
            .setDuration(180).start()
    }

    override fun onBackPressed(): Boolean {
        if (!checkEditData()) {
            return true
        } else {
            discardData()
            return false
        }
    }

    override fun onFragmentDestroy() {
        super.onFragmentDestroy()
        fragmentView = null
    }

    private fun discardData() {
        val builder = AlertDialog.Builder(
            parentActivity
        )
        builder.setTitle(LocaleController.getString(R.string.AppName))
        builder.setMessage("Saqlaysizmi ?")
        builder.setPositiveButton(
            "Saqlash"
        ) { dialogInterface: DialogInterface?, i: Int ->
            MyAlarmManager.createAlarm(context, AlarmEntity(workerId, dialogId, messagesController.getFullName(dialogId), message, time))
            finishFragment()
        }
        builder.setNegativeButton(
            "Bekor qilish"
        ) { dialog: DialogInterface?, which: Int -> finishFragment() }
        showDialog(builder.create())
    }

    private fun showDialogApply() {
        val builder = AlertDialog.Builder(
            parentActivity
        )
        builder.setMessage("Saqlaysizmi ?")
        builder.setTitle(LocaleController.getString(R.string.AppName))
        builder.setPositiveButton(
            "Saqlash"
        ) { dialogInterface: DialogInterface?, i: Int ->
            MyAlarmManager.createAlarm(context, AlarmEntity(workerId, dialogId, messagesController.getFullName(dialogId), message, time))
            finishFragment()
        }
        builder.setNegativeButton(
            "Bekor qilish",
            null
        )
        showDialog(builder.create())
    }

    private fun showDialogRemove() {
        val builder = AlertDialog.Builder(
            parentActivity
        )
        builder.setMessage("O'chirmoqchimisiz ?")
        builder.setTitle(LocaleController.getString(R.string.AppName))
        builder.setPositiveButton(
            "O'chirish"
        ) { dialogInterface: DialogInterface?, i: Int ->
            MyAlarmManager.removeAlarmFromCache(context, oldAlarm!!)
            finishFragment()
        }
        builder.setNegativeButton(
            "Bekor qilish",
            null
        )
        showDialog(builder.create())
    }

}