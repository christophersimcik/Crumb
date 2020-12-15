package com.example.crumb.Dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.emoji.widget.EmojiEditText
import androidx.fragment.app.DialogFragment
import com.example.crumb.R
import java.lang.ClassCastException
const val TAG = "TEXT_INPUT_DIALOG"
class TextInputDialog : DialogFragment() {
    private lateinit var listener: InputDialogListener
    lateinit var layout: View
    lateinit var noteInputField: EmojiEditText
    private val inputMethodManager: InputMethodManager by lazy {
        context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.layout.text_input_dialog_layout, container, false)
        noteInputField = layout.findViewById(R.id.input_text_field)
        val confirmButton = layout.findViewById<Button>(R.id.confirm_button)
        val dismissButton = layout.findViewById<Button>(R.id.dismiss_button)
        confirmButton.setOnClickListener {
            val note = noteInputField.text.toString()
            listener.onConfirm(this.requireDialog(), note)
        }
        dismissButton.setOnClickListener {
            listener.onDismiss(requireDialog())
        }
        return layout
    }

    fun teardown() {
        inputMethodManager.hideSoftInputFromWindow(layout.windowToken, 0)
        noteInputField.setText("")
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        inputMethodManager.hideSoftInputFromWindow(layout.windowToken,0)
        Log.i(TAG, "canceled")
    }

    override fun onResume() {
        super.onResume()
        val displayMetrics = DisplayMetrics()
        val windowManager = targetFragment?.context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = (displayMetrics.widthPixels * 0.85f).toInt()
        val height = (displayMetrics.heightPixels * 0.50f).toInt()
        dialog?.window?.setLayout(width,height)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        listener.onDialogCreated()
        setup()
    }

    fun setup() {
        if (this::noteInputField.isInitialized) {
            noteInputField.requestFocus()
        }
        inputMethodManager.toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = targetFragment as InputDialogListener
        } catch (c: ClassCastException) {
            throw ClassCastException(activity.toString() + " have not implemented interface")
        }
    }

    interface InputDialogListener {
        fun onDialogCreated()
        fun onDismiss(dialog: Dialog)
        fun onCanceled(dialog: Dialog)
        fun onConfirm(dialog: Dialog, note: String)
    }

}