package com.example.tightboules

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
import android.widget.TextView
import androidx.emoji.widget.EmojiEditText
import androidx.fragment.app.DialogFragment
import java.lang.ClassCastException

class DeleteDialog(val type : String) : DialogFragment() {

    lateinit var layout: View
    private var position = 0
    lateinit var dismiss: Button
    lateinit var confirm: Button
    lateinit var declare: TextView
    val displayMetrics = DisplayMetrics()
    lateinit var listener: SwipeDeleteDialogListener
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.layout.delete_dialog, null)
        confirm = layout.findViewById<Button>(R.id.confirm_button)
        dismiss = layout.findViewById<Button>(R.id.dismiss_button)
        declare = layout.findViewById<EmojiEditText>(R.id.declare_type_field)
        declare.setText("Delete This " + type + " ?")
        confirm.setOnClickListener {
            listener.swipeConfirm(this.requireDialog(), position)
        }
        dismiss.setOnClickListener {
            listener.swipeDismiss(requireDialog(), position)
        }

        return layout
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        listener.swipeCanceled(requireDialog(), position)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        listener.swipeDialogCreated()
        val windowManager = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = targetFragment as SwipeDeleteDialogListener
        } catch (c: ClassCastException) {
            throw ClassCastException(activity.toString() + " have not implemented interface")
        }
    }

    override fun onResume() {
        super.onResume()
        val width = (Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) * .90).toInt()
        val height = width / 2
        dialog?.window?.setLayout(width,height)
        layout.invalidate()
    }

    fun setPosition(position: Int) {
        this.position = position
    }

    interface SwipeDeleteDialogListener {
        fun swipeDialogCreated()
        fun swipeDismiss(dialog: Dialog, position: Int);
        fun swipeCanceled(dialog: Dialog, position: Int);
        fun swipeConfirm(dialog: Dialog, position: Int);
    }

}






