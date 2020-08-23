package com.example.crumb.Dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import androidx.emoji.widget.EmojiTextView
import androidx.fragment.app.DialogFragment
import com.example.crumb.R
import java.lang.ClassCastException

class TextDisplayDialog : DialogFragment() {

    private lateinit var listener: DisplayDialogListener
    private lateinit var editButton : ImageButton
    lateinit var noteDisplay : EmojiTextView
    lateinit var layout: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.layout.text_display_dialog_layout, container, false)
        noteDisplay = layout.findViewById(R.id.input_text_field)
        editButton = layout.findViewById(R.id.edit_text_button)
        editButton.setOnClickListener{
            listener.onDisplayConfirm(requireDialog(),noteDisplay.text.toString())
        }
        val dismissButton = layout.findViewById<Button>(R.id.dismiss_button)
        dismissButton.setOnClickListener {
            listener.onDisplayDismiss(requireDialog())
        }
        return layout
    }

    fun disableEditOption(){
        editButton.visibility = View.INVISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        listener.onDisplayDialogCreated()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = targetFragment as DisplayDialogListener
        } catch (c: ClassCastException) {
            throw ClassCastException(activity.toString() + " have not implemented interface")
        }
    }

    override fun onResume() {
        super.onResume()
        val displayMetrics = DisplayMetrics()
        val windowManager = targetFragment?.context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = (displayMetrics.widthPixels.coerceAtMost(displayMetrics.heightPixels) * .90).toInt()
        val height = (displayMetrics.widthPixels.coerceAtLeast(displayMetrics.heightPixels) * .70).toInt()
        dialog?.window?.setLayout(width,height)
    }

    interface DisplayDialogListener {
        fun onDisplayDialogCreated()
        fun onDisplayDismiss(dialog: Dialog)
        fun onDisplayCanceled(dialog: Dialog)
        fun onDisplayConfirm(dialog: Dialog, note: String)
    }

}