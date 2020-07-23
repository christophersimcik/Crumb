package com.example.crumb.Dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.util.DisplayMetrics
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.emoji.widget.EmojiEditText
import androidx.fragment.app.DialogFragment
import com.example.crumb.Models.Interval
import com.example.crumb.R
import java.lang.ClassCastException
import java.text.DecimalFormat

class StepDetailDialog : DialogFragment() {

    lateinit var listener: StepDialogListener
    lateinit var layout: View
    lateinit var stepName: EmojiEditText
    lateinit var stepDescription: EmojiEditText
    lateinit var stepTimeFromStart: TextView
    lateinit var stepTimeFromLast: TextView
    lateinit var percentageOfTotal: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.layout.interval_data_dialog, null)
        stepName = layout.findViewById(R.id.step_name_field) as EmojiEditText
        stepDescription = layout.findViewById(R.id.step_notes_field) as EmojiEditText
        stepTimeFromStart = layout.findViewById(R.id.time_from_start)
        stepTimeFromLast = layout.findViewById(R.id.time_from_last)
        percentageOfTotal = layout.findViewById(R.id.percentage_of_total)
        val submitButton = layout.findViewById<Button>(R.id.confirm_button)
        submitButton.setOnClickListener {
            listener.onStepConfirm(
                requireDialog(),
                stepName.text.toString(),
                stepDescription.text.toString()
            )
        }
        val dismissButton = layout.findViewById<Button>(R.id.dismiss_button)
        dismissButton.setOnClickListener {
            listener.onStepDismiss(requireDialog())
        }
        return layout
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = targetFragment as StepDialogListener
        } catch (c: ClassCastException) {
            throw ClassCastException(activity.toString() + " have not implemented interface")
        }
    }

    override fun onResume() {
        super.onResume()
        val displayMetrics = DisplayMetrics()
        val windowManager =
            targetFragment?.context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width =
            (Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) * .90).toInt()
        val height =
            (Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels) * .70).toInt()
        dialog?.window?.setLayout(width, height)
        listener.onStepDialogCreated()
    }

    fun setupFields(step: Interval) {
        stepName.editableText.clear()
        stepName.editableText.append(step.name)
        stepDescription.editableText.clear()
        stepDescription.editableText.append(step.notes)
        if (step.sequence == 1) {
            stepTimeFromLast.setText(" First Step")
        } else {
            stepTimeFromLast.setText(textFromLast(step.span))
        }
        stepTimeFromStart.setText(getAlarmTime(step.time))
        percentageOfTotal.setText(textPercentage(step.percentage))
    }

    private fun textFromLast(time: Int): String {
        return getTime(time).toString() + " From Last Step"
    }

    private fun getAlarmTime(time: Int): String {
        return " Alarm At " + getAsTime(time) + " " + getMeridian(time)
    }

    private fun textFromStart(time: Int): String {
        return getTime(time).toString() + " From Recipe Start"
    }

    private fun textPercentage(time: Float): String {
        if (time == 0.0f) {
            return ""
        }
        return " " + DecimalFormat("#").format(time * 100) + "%" + " Of Total"
    }

    interface StepDialogListener {
        fun onStepDialogCreated()
        fun onStepDismiss(dialog: Dialog);
        fun onStepCanceled(dialog: Dialog);
        fun onStepConfirm(dialog: Dialog, name: String, notes: String);
    }

    private fun getTime(minutes: Int): SpannableStringBuilder {
        val decimalFormat = DecimalFormat("#")
        val stringBuilder = SpannableStringBuilder()
        val days = Math.floor((minutes / 1440).toDouble())
        var hours = Math.floor((minutes % 1440) / 60.toDouble())
        val mins = Math.floor((minutes % 1440) % 60.toDouble())
        if (days >= 1.0) {
            stringBuilder.append(
                decimalFormat.format(days),
                AbsoluteSizeSpan(40),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            stringBuilder.append(
                " Day" + checkPlurality(days),
                AbsoluteSizeSpan(35),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
        if (hours >= 1.0) {
            stringBuilder.append(
                " " + decimalFormat.format(hours),
                AbsoluteSizeSpan(40),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            stringBuilder.append(
                " Hr" + checkPlurality(hours),
                AbsoluteSizeSpan(35),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )

        }
        if (mins >= 0.0) {
            stringBuilder.append(
                " " + decimalFormat.format(mins),
                AbsoluteSizeSpan(40),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            stringBuilder.append(
                " Min" + checkPlurality(mins),
                AbsoluteSizeSpan(35),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
        return stringBuilder
    }


    private fun checkPlurality(number: Double): String {
        if (number == 1.0) {
            return ""
        } else {
            return "s"
        }
    }

    fun getAsTime(minutes: Int): String {
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

    fun getMeridian(minutes: Int): String {
        val hours = Math.floor((minutes % 1440) / 60.toDouble()).toInt()
        if (hours > 12) {
            return "PM"
        } else {
            return "AM"
        }
    }


}