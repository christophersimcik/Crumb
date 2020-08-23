package com.example.crumb.Fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.emoji.widget.EmojiTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.crumb.*
import com.example.crumb.Activities.MainActivity
import com.example.crumb.Adapters.PlayAdapter
import com.example.crumb.Dialogs.AlarmDialog
import com.example.crumb.Dialogs.DeleteDialog
import com.example.crumb.Dialogs.TextDisplayDialog
import com.example.crumb.Fragments.FragmentInterfaces.FragmentCallback
import com.example.crumb.Helpers.AlarmHelper
import com.example.crumb.Models.Interval
import com.example.crumb.Models.Schedule
import com.example.crumb.UI.CustomLayoutManager
import com.example.crumb.UI.MyProgressBar
import com.example.crumb.ViewModels.PlayViewModel
import com.example.crumb.ViewModels.ViewModelFactories.PlayFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayFragment : Fragment(),
    DeleteDialog.SwipeDeleteDialogListener,
    AlarmDialog.DeleteDialogListener,
    TextDisplayDialog.DisplayDialogListener,
    PlayAdapter.StepDetailCallback,
    PlayViewModel.AlarmCancelObserver {

    private lateinit var myView: View

    private val callback: FragmentCallback by lazy {
        val mainActivity = activity as MainActivity
        mainActivity.sharedViewModel
    }

    val viewModel: PlayViewModel by lazy {
        ViewModelProviders.of(
            this, PlayFactory(
                requireActivity().application,
                requireNotNull(arguments?.getLong(AlarmHelper.PARENT_ID))
            )
        ).get(PlayViewModel::class.java)
    }

    private val textDisplayDialog: TextDisplayDialog by lazy { TextDisplayDialog() }
    private val recyclerView: RecyclerView by lazy { myView.findViewById<RecyclerView>(R.id.recycler_view_progress) }
    private val myLayoutManager = CustomLayoutManager(context)
    private val playAdapter: PlayAdapter by lazy {
        PlayAdapter(
            requireContext(),
            viewModel
        )
    }
    private val nameField: EmojiTextView by lazy { myView.findViewById<EmojiTextView>(R.id.play_name) }
    private val progressTotal: MyProgressBar by lazy { myView.findViewById<MyProgressBar>(
        R.id.total_progrss_bar
    ) }
    private val durationText: TextView by lazy { myView.findViewById<TextView>(R.id.text_duration) }
    private val endMessageText: TextView by lazy { myView.findViewById<TextView>(R.id.text_end_message) }
    private val deleteDialog: DeleteDialog by lazy {
        DeleteDialog(
            "Cancel All Alarms?"
        )
    }

    private val noteObserver: Observer<String> by lazy {
        Observer<String> { note ->
            when (note) {
                "" -> {
                    if (textDisplayDialog.dialog?.isShowing == true) {
                        textDisplayDialog.noteDisplay.text = context?.getText(R.string.no_name_text)
                    }
                }
                else -> {
                    if (textDisplayDialog.dialog?.isShowing == true) {
                        textDisplayDialog.noteDisplay.text = note
                    }
                }
            }
        }
    }

    private val stepObserver = Observer<List<Interval>> { list: List<Interval> ->
        viewModel.updateTotal(list, progressTotal)
        viewModel.hasActiveAlarms()
        playAdapter.setData(list)
    }

    private val recipeObserver = Observer { schedule: Schedule? ->
        val steps = schedule?.steps ?: 0
        val duration = schedule?.duration ?: 0
        nameField.text = schedule?.name
        viewModel.duration = duration
        if (steps == 1 ) {
            val minutesAsText = viewModel.convertMinutesToText(duration) + "In " + steps.toString() + " Step"
            durationText.text = minutesAsText
        } else {
            val minutesAsText = viewModel.convertMinutesToText(duration) + "In " + steps.toString() + " Steps"
            durationText.text = minutesAsText }
    }

    private val endObserver = Observer<String> { msg: String ->
        endMessageText.text = msg

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        myView = inflater.inflate(R.layout.play_fragment, container, false)
        viewModel.stepData?.observe(this.viewLifecycleOwner, stepObserver)
        viewModel.recipeData?.observe(this.viewLifecycleOwner, recipeObserver)
        viewModel.relativeEndTime.observe(this.viewLifecycleOwner, endObserver)
        recyclerView.layoutManager = myLayoutManager
        recyclerView.adapter = playAdapter
        playAdapter.registerStepDialogWatcher(this)
        viewModel.initializeAlarmDialog(this)
        val bundle = arguments
        if (arguments?.getBoolean(AlarmHelper.ALARM_IS_ACTIVE,false) == true) {
            viewModel.showAlarmDialog(
                this.parentFragmentManager,
                bundle?.getString(AlarmHelper.NAME,"")?: "",
                bundle?.getString(AlarmHelper.DESCRIPTION, "")?: "")
        }
        viewModel.registerAlarmCancelObserver(this)
        updateTotal(progressTotal)
        callback.fragmentAttached(this)
        viewModel.notes.observe(viewLifecycleOwner, noteObserver)
        textDisplayDialog.setTargetFragment(this, 0)
        deleteDialog.setTargetFragment(this,0)
        return myView
    }

    private fun updateTotal(view: MyProgressBar) {
        lifecycleScope.launch {
            while (isActive) {
                viewModel.computeCurrentPositon(view)
                delay(1000)
            }
        }
    }

    fun showNotes() {
        textDisplayDialog.show(parentFragmentManager, "notes_detail")
        viewModel.getScheduleNotes()
    }

    // refresh itemview after alarms cancelled
    override fun onAlarmCancelled() {
        playAdapter.notifyDataSetChanged()
    }

    override fun onDialogCreated() {
    }

    override fun onDismiss(dialog: Dialog) {
        viewModel.dismissDialog()
    }

    override fun onCanceled(dialog: Dialog) {
    }

    override fun onConfirm(dialog: Dialog, name: String, time: Int) {
    }

    override fun onDisplayDialogCreated() {
        textDisplayDialog.disableEditOption()
    }

    override fun onDisplayDismiss(dialog: Dialog) {
        dialog.dismiss()
    }

    override fun onDisplayCanceled(dialog: Dialog) {
        dialog.dismiss()
    }

    override fun onDisplayConfirm(dialog: Dialog, note: String) {
        dialog.dismiss()
    }

    override fun onStepDialogCalled(step: Interval) {
        textDisplayDialog.show(parentFragmentManager, "notes_detail")
        viewModel.getIntervalNotes(step)
    }

    fun showDeleteDialog(){
        deleteDialog.show(parentFragmentManager, "swipe_delete")
    }

    override fun swipeDialogCreated() {

    }

    override fun swipeDismiss(dialog: Dialog, position: Int) {
        deleteDialog.dismiss()
    }

    override fun swipeCanceled(dialog: Dialog, position: Int) {
        deleteDialog.dismiss()
    }

    override fun swipeConfirm(dialog: Dialog, position: Int) {
        viewModel.cancel()
        deleteDialog.dismiss()
        findNavController().navigate(R.id.action_playFragment_to_scheduleFragment)
    }
}
