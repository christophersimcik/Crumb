package com.example.tightboules

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.emoji.widget.EmojiEditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

import java.lang.ClassCastException
import java.text.DecimalFormat

class StepDialog(keyboardDetection: KeyboardDetection) : DialogFragment(),
    TimeScroll.ActionCallback, KeyboardDetection.KeyBoardObserver {

    var myTime = 0
    var initTime = 0
    lateinit var layout: View
    lateinit var timeScroll: TimeScroll
    private lateinit var interval: Interval
    lateinit var nameInputField: EmojiEditText
    lateinit var listener: DeleteDialogListener
    private var name = ""
    val keyboardDetection = keyboardDetection

    init {
        keyboardDetection.registerObserver(this)
    }

    fun show(manager: FragmentManager, tag: String?, time: Int) {
        super.show(manager, tag)
        System.out.println("working")


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        layout = inflater.inflate(R.layout.time_select_dialog, null)
        val submitButton = layout.findViewById<Button>(R.id.submit_button)
        val dismissButton = layout.findViewById<Button>(R.id.dismiss_button)
        nameInputField = layout.findViewById<EmojiEditText>(R.id.name_input_field)
        timeScroll = layout.findViewById(R.id.time_selection_bar)
        timeScroll.register(this)
        submitButton.setOnClickListener {
            val name = nameInputField.getText().toString()
            val total = timeScroll.getTotal()
            if (total >= 0) {
                myTime = total
            }
            listener.stepConfirm(this.requireDialog(), name, myTime)
            nameInputField.setText("")
        }
        dismissButton.setOnClickListener {
            listener.stepDismiss(requireDialog())
            nameInputField.setText("")
        }

        return layout
    }

    fun setName(name : String){
        this.name = name
    }

    fun getName() : String {
        return this.name
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        nameInputField.setText("")
    }

    override fun onResume() {
        listener.stepDialogCreated()
        super.onResume()

    }

    fun setTime(time: Int) {
        timeScroll.setDials(getValues(time))
    }

    fun defineInterval(interval: Interval) {
        this.interval = interval
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = targetFragment as DeleteDialogListener
        } catch (c: ClassCastException) {
            throw ClassCastException(activity.toString() + " have not implemented interface")
        }
    }

    override fun onStart() {
        keyboardDetection.getListener()
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        keyboardDetection.removeListener()
    }

    fun getValues(total: Int): Array<Int> {
        val total = total
        val days = (total / 1440) + 1
        var hours = (total % 1440) / 60
        val mins = total % 1440 % 60
        var mrd = 0
        if(hours >= 12){
            mrd = 1
        }
        if (hours > 12) {
            hours = hours - 12
        }
        if (hours == 0) {
            hours = 12
        }
        return arrayOf(days, hours, mins, mrd)
    }

    interface DeleteDialogListener {
        fun stepDialogCreated()
        fun stepDismiss(dialog: Dialog);
        fun stepCanceled(dialog: Dialog);
        fun stepConfirm(dialog: Dialog, name: String, time: Int);
    }

    override fun onActionUp() {
        System.out.println(" im up ")
    }

    override fun onMove() {
        System.out.println("move me")
    }

    override fun onLayoutCompleted(timeScroll: TimeScroll) {
        timeScroll.setDials(getValues(initTime))
    }

    override fun keyboardDismissed() {
        super.keyboardDismissed()
        if (this::nameInputField.isInitialized) {
            nameInputField.clearFocus()
        }
    }

    fun getTime(minutes: Int): String {
        var hours = Math.floor((minutes % 1440) / 60.toDouble()).toInt()
        val mins = Math.floor((minutes % 1440) % 60.toDouble()).toInt()
        if (hours > 12) {
            hours = hours - 12
        }
        if (hours == 0) {
            hours = 12
        }
        return hours.toString() + ":" + DecimalFormat("00").format(mins)
    }


}