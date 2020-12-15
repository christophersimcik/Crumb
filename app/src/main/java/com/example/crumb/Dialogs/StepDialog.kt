package com.example.crumb.Dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.emoji.widget.EmojiEditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.crumb.Helpers.KeyboardDetectionHelper
import com.example.crumb.Helpers.TimeHelper
import com.example.crumb.R
import com.github.stephenvinouze.materialnumberpickercore.MaterialNumberPicker
import java.text.DecimalFormat

class StepDialog(private val keyboardDetection: KeyboardDetectionHelper) : DialogFragment(),
    KeyboardDetectionHelper.KeyBoardObserver {

    private var myTime = 0
    var initTime = 0
    var isFirst = true

    private var name = ""
    lateinit var layout: View
    private lateinit var dayPicker: MaterialNumberPicker
    private lateinit var hourPicker: MaterialNumberPicker
    private lateinit var minPicker: MaterialNumberPicker
    private lateinit var mrdnPicker: MaterialNumberPicker
    private lateinit var timeHelper: TimeHelper
    lateinit var nameInputField: EmojiEditText
    private lateinit var listener: DeleteDialogListener

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

        layout = inflater.inflate(R.layout.time_select_dialog, container, false)
        val submitButton = layout.findViewById<Button>(R.id.submit_button)
        val dismissButton = layout.findViewById<Button>(R.id.dismiss_button)
        nameInputField = layout.findViewById(R.id.name_input_field)
        dayPicker = layout.findViewById(R.id.day_selector)
        hourPicker = layout.findViewById(R.id.hour_selector)
        minPicker = layout.findViewById(R.id.min_selector)
        val df = DecimalFormat("00")
        minPicker.setFormatter { df.format(it) }
        mrdnPicker = layout.findViewById(R.id.meridian_selector)
        mrdnPicker.displayedValues = arrayOf("AM", "PM")

        mrdnPicker.setOnValueChangedListener { numberPicker, i, i2 ->
            if (i == 0) {
                mrdnPicker.separatorColor = resources.getColor(R.color.pm_color, null)
            } else {
                mrdnPicker.separatorColor = resources.getColor(R.color.am_text, null)
            }
        }

        timeHelper =
            TimeHelper(dayPicker, hourPicker, minPicker, mrdnPicker)
        submitButton.setOnClickListener {
            val name = nameInputField.text.toString()
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
        super.onResume()
        listener.stepDialogCreated()
        val additionalMinute = if (isFirst) 0 else 1
        timeHelper.setValues(initTime + additionalMinute)
        val displayMetrics = DisplayMetrics()
        val windowManager = targetFragment?.context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = (displayMetrics.widthPixels * 0.85f).toInt()
        val height = (displayMetrics.heightPixels * 0.50f).toInt()
        dialog?.window?.setLayout(width,height)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
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
        fun stepDismiss(dialog: Dialog)
        fun stepCanceled(dialog: Dialog)
        fun stepConfirm(dialog: Dialog, name: String, time: Int)
    }

    override fun keyboardDismissed() {
        super.keyboardDismissed()
        if (this::nameInputField.isInitialized) {
            nameInputField.clearFocus()
        }
    }
}