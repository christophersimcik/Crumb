package com.example.crumb

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
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayFragment :
    Fragment(),
    AlarmDialog.DeleteDialogListener,
    TextDisplayDialog.DisplayDialogListener,
    PlayAdapter.StepDetailCallback,
    PlayViewModel.AlarmCancelObserver{

    private lateinit var myView: View

    val callback: FragmentCallback by lazy {
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
    private val playAdapter: PlayAdapter by lazy { PlayAdapter(requireContext(), viewModel) }
    private val name_field: EmojiTextView by lazy { myView.findViewById<EmojiTextView>(R.id.play_name) }
    private val progressTotal: MyProgressBar by lazy { myView.findViewById<MyProgressBar>(R.id.total_progrss_bar) }
    private val durationText: TextView by lazy { myView.findViewById<TextView>(R.id.text_duration) }
    private val endMessageText: TextView by lazy { myView.findViewById<TextView>(R.id.text_end_message) }

    val noteObserver: Observer<String> by lazy {
        Observer<String> { note ->
            when (note) {
                "" -> {
                    if (textDisplayDialog.dialog?.isShowing ?: false) {
                        textDisplayDialog.noteDisplay.setText("No Notes")
                    }
                }
                else -> {
                    if (textDisplayDialog.dialog?.isShowing ?: false) {
                        textDisplayDialog.noteDisplay.setText(note)
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
        val steps = schedule?.steps
        name_field.setText(schedule?.name)
        viewModel.duration = schedule?.duration
        System.out.println("** schedule = " + schedule?.id)
        if (steps == 1) {
            durationText.text =
                viewModel.convertMinutesToText(schedule.duration) + "In " + schedule.steps.toString() + " Step"
        } else {
            durationText.text =
                viewModel.convertMinutesToText(schedule!!.duration) + "In " + schedule?.steps.toString() + " Steps"
        }
    }

    private val endObserver = Observer<String> { msg: String ->
        endMessageText.text = msg

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        return myView
    }

    fun updateTotal(view: MyProgressBar) {
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
}
