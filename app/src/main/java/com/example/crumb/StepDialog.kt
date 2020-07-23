package com.example.crumb

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import androidx.emoji.widget.EmojiEditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.github.stephenvinouze.materialnumberpickercore.MaterialNumberPicker

import java.lang.ClassCastException
import java.text.DecimalFormat

class StepDialog(keyboardDetection: KeyboardDetection) : DialogFragment(),
    KeyboardDetection.KeyBoardObserver {

    var myTime = 0
    var initTime = 0
    var isFirst = true

    private var name = ""
    lateinit var layout: View
    lateinit var dayPicker: MaterialNumberPicker
    lateinit var hourPicker: MaterialNumberPicker
    lateinit var minPicker: MaterialNumberPicker
    lateinit var mrdnPicker: MaterialNumberPicker
    lateinit var timeHelper: TimeHelper
    private lateinit var interval: Interval
    lateinit var nameInputField: EmojiEditText
    lateinit var listener: DeleteDialogListener

    val keyboardDetection = keyboardDetection

    init {
        keyboardDetection.registerObserver(this)
    }

    fun show(manager: FragmentManager, tag: String?, time: Int) {
        super.show(manager, tag)
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
        dayPicker = layout.findViewById(R.id.day_selector)
        hourPicker = layout.findViewById(R.id.hour_selector)
        minPicker = layout.findViewById(R.id.min_selector)
        val df = DecimalFormat("00")
        minPicker.setFormatter(NumberPicker.Formatter { df.format(it) })
        mrdnPicker = layout.findViewById(R.id.meridian_selector)
        mrdnPicker.displayedValues = arrayOf("AM", "PM")

        mrdnPicker.setOnValueChangedListener { numberPicker, i, i2 ->
            if (i == 0) {
                mrdnPicker.separatorColor = resources.getColor(R.color.pm_color, null)
            } else {
                mrdnPicker.separatorColor = resources.getColor(R.color.am_text, null)
            }
        }

        timeHelper = TimeHelper(dayPicker, hourPicker, minPicker, mrdnPicker)
        submitButton.setOnClickListener {
            val name = nameInputField.getText().toString()
            val total = timeHelper.getMinutesFromViews()
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

    fun setName(name: String) {
        this.name = name
    }

    fun getName(): String {
        return this.name
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        nameInputField.setText("")
    }

    override fun onResume() {
        listener.stepDialogCreated()
        val additionalMinute = if (isFirst) 0 else 1
        timeHelper.setValues(initTime + additionalMinute)
        super.onResume()

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

    interface DeleteDialogListener {
        fun stepDialogCreated()
        fun stepDismiss(dialog: Dialog);
        fun stepCanceled(dialog: Dialog);
        fun stepConfirm(dialog: Dialog, name: String, time: Int);
    }

    override fun keyboardDismissed() {
        super.keyboardDismissed()
        if (this::nameInputField.isInitialized) {
            nameInputField.clearFocus()
        }
    }
}