package com.example.crumb.Dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.emoji.widget.EmojiEditText
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import com.example.crumb.Fragments.ScheduleFragment
import com.example.crumb.R
import java.lang.ClassCastException

class RecipeDialog : DialogFragment() {

    lateinit var layout: View
    private lateinit var dismiss: Button
    private lateinit var confirm: Button
    lateinit var name: TextView
    lateinit var fragment : ScheduleFragment
    private lateinit var navController: NavController
    private lateinit var listener: RecipeCreateDialogListener
    private val displayMetrics = DisplayMetrics()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.layout.recipe_name_dialog, container, false)
        confirm = layout.findViewById(R.id.confirm_button)
        dismiss = layout.findViewById(R.id.dismiss_button)
        name = layout.findViewById<EmojiEditText>(R.id.recipe_name_field)
        confirm.setOnClickListener {
            listener.recipeConfirm(this.requireDialog(),name.text.toString())
            name.text = ""
        }
        dismiss.setOnClickListener {
            name.text = ""
            listener.recipeDismiss(requireDialog())
        }

        return layout
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        listener.recipeCanceled(requireDialog())
    }

    fun setFragAndNav(fragment: ScheduleFragment, navController: NavController) {
        this.fragment = fragment
        this.navController = navController
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val windowManager = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = targetFragment as RecipeCreateDialogListener
        } catch (c: ClassCastException) {
            throw ClassCastException(activity.toString() + " have not implemented interface")
        }
    }

    override fun onResume() {
        super.onResume()
        val width = (displayMetrics.heightPixels.coerceAtMost(displayMetrics.widthPixels) * .90).toInt()
        val height = (width * .50).toInt()
        dialog?.window?.setLayout(width,height)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        layout.invalidate()
    }

    interface RecipeCreateDialogListener {
        fun recipeDismiss(dialog: Dialog)
        fun recipeCanceled(dialog: Dialog)
        fun recipeConfirm(dialog: Dialog, name:String)
    }

}






