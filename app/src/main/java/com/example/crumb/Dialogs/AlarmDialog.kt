package com.example.crumb.Dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.crumb.R

import java.lang.ClassCastException
import kotlin.math.min

class AlarmDialog: DialogFragment() {

    private lateinit var listener: DeleteDialogListener
    lateinit var layout: View

    private val alarmName: TextView by lazy { layout.findViewById<TextView>(R.id.alarm_name_text) }
    private val alarmDescription: TextView by lazy { layout.findViewById<TextView>(R.id.alarm_description) }
    private val dismissButton: View by lazy { layout.findViewById<View>(R.id.dismiss_button) }
    private val displayMetrics = DisplayMetrics()
    var name = "No Name"
    var description = "No Description"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val windowManager = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        layout = inflater.inflate(R.layout.alarm_dialog, container, false)
        dismissButton.setOnClickListener {
            listener.onDismiss(requireDialog())
        }
        return layout
    }

    override fun onResume() {
        super.onResume()
        val width = (min(displayMetrics.widthPixels, displayMetrics.heightPixels) * .90).toInt()
        val height = 900
        dialog?.window?.setLayout(width,height)
        layout.invalidate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        alarmName.text = name
        alarmDescription.text = description
        listener.onDialogCreated()
        super.onViewCreated(view, savedInstanceState)
    }

    fun show(manager: FragmentManager, tag: String?, name: String, description: String) {
        if (name == "") {
            this.name = "No Name Provided"
        } else {
            this.name = name
        }
        if (description == "") {
            this.description = " No Description Provided"
        } else {
            this.description = description
        }
        super.show(manager, tag)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = targetFragment as DeleteDialogListener
        } catch (c: ClassCastException) {
            throw ClassCastException(activity.toString() + " have not implemented interface")
        }
    }

    interface DeleteDialogListener {
        fun onDialogCreated()
        fun onDismiss(dialog: Dialog)
        fun onCanceled(dialog: Dialog)
        fun onConfirm(dialog: Dialog, name: String, time: Int)
    }
}
