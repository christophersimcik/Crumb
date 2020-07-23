package com.example.crumb

import android.app.Dialog
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.view.View.OnScrollChangeListener
import android.widget.*
import androidx.emoji.widget.EmojiEditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.github.stephenvinouze.materialnumberpickercore.MaterialNumberPicker
import kotlinx.android.synthetic.main.saved_recipe_fragment.*
import org.joda.time.DateTime
import java.lang.reflect.Field
import java.text.DecimalFormat
import kotlin.collections.ArrayList

class SavedRecipeFragment : Fragment(), TimeScroll.ActionCallback,
    KeyboardDetection.KeyBoardObserver, TextInputDialog.InputDialogListener,
    TextDisplayDialog.DisplayDialogListener, StepDetailDialog.StepDialogListener,
    SavedRecipeAdapter.StepDetailCallback {

    lateinit var hourText: TextView
    lateinit var minText: TextView
    lateinit var meridianText: TextView
    lateinit var meridianImage: View
    lateinit var daySelector : MaterialNumberPicker
    lateinit var hourSelector : MaterialNumberPicker
    lateinit var minSelector : MaterialNumberPicker
    lateinit var mrdSelector : MaterialNumberPicker
    lateinit var myView: View
    lateinit var editName: EmojiEditText
    lateinit var nowButton: ImageButton


    private lateinit var timeHelper :TimeHelper

    val textDisplayDialog: TextDisplayDialog by lazy { TextDisplayDialog() }
    val textInputDialog: TextInputDialog by lazy { TextInputDialog() }
    val stepDetailDialog: StepDetailDialog by lazy { StepDetailDialog() }
    val keyboardDetection: KeyboardDetection by lazy { KeyboardDetection(requireActivity()) }
    var layoutCompleted = false
    var dataRetrieved = false
    lateinit var scrollObserver: ScrollingCallback
    val imageAM: Drawable by lazy {
        requireActivity().resources.getDrawable(
            R.drawable.am_image,
            null
        )
    }

    val imagePM: Drawable by lazy {
        requireActivity().resources.getDrawable(
            R.drawable.pm_image,
            null
        )
    }

    val viewModel: SavedRecipeViewModel by lazy {
        ViewModelProviders.of(
            this,
            SavedRecipeFactory(
                requireActivity().application,
                requireNotNull(arguments?.getLong("parent_id"))
            )
        ).get(SavedRecipeViewModel::class.java)
    }

    val savedRecipeAdapter: SavedRecipeAdapter by lazy { SavedRecipeAdapter(requireActivity()) }

    val noteObserver: Observer<String> by lazy {
        Observer<String> { note ->
            if (textDisplayDialog.dialog?.isShowing ?: false) {
                textDisplayDialog.noteDisplay.setText(note)
            }
        }
    }

    val startObserver: Observer<Interval> by lazy {
        Observer<Interval> { step: Interval ->
            timeHelper.setValues(step.time)
            setStartTimeViews()
            dataRetrieved = true

        }
    }

    val nameObserver: Observer<String> by lazy {
        Observer<String> { name: String ->
            editName.setText(name)
        }
    }

    val intervalObserver: Observer<List<Interval>> by lazy {
        Observer<List<Interval>> { steps: List<Interval> ->
            savedRecipeAdapter.setData(steps as ArrayList<Interval>)
            savedRecipeAdapter.notifyDataSetChanged()

        }
    }

    val callback: FragmentCallback by lazy {
        val mainActivity = activity as MainActivity
        mainActivity.sharedViewModel
    }

    val navHostFragment: NavHostFragment by lazy {
        requireActivity().supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment
        ) as NavHostFragment
    }

    override fun onStart() {
        super.onStart()
        keyboardDetection.getListener()
        savedRecipeAdapter
        intervalObserver
        startObserver
    }

    override fun onStop() {
        super.onStop()
        keyboardDetection.removeListener()

    }

    fun registerScrollObserver(scrollingCallback: ScrollingCallback) {
        scrollObserver = scrollingCallback
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layoutCompleted = false
        dataRetrieved = false
        myView = layoutInflater.inflate(R.layout.saved_recipe_fragment, container, false)
        editName = myView.findViewById<EmojiEditText>(R.id.name_edit_text)
        hourText = myView.findViewById(R.id.text_hour)
        minText = myView.findViewById(R.id.text_minute)
        meridianText = myView.findViewById(R.id.text_meridian)
        meridianImage = myView.findViewById(R.id.meridian_image)
        daySelector = myView.findViewById(R.id.day_selector)
        daySelector.setOnValueChangedListener { numberPicker, i, i2 ->
            viewModel.update(timeHelper.getMinutesFromViews())
        }
        hourSelector = myView.findViewById(R.id.hour_selector)
        hourSelector.setOnValueChangedListener { numberPicker, i, i2 ->
            hourText.text = numberPicker.value.toString()
            viewModel.update(timeHelper.getMinutesFromViews())
        }
        minSelector = myView.findViewById(R.id.min_selector)
        val df = DecimalFormat("00")
        minSelector.setOnValueChangedListener { numberPicker, i, i2 ->
            minText.text = df.format(numberPicker.value)
            viewModel.update(timeHelper.getMinutesFromViews())
        }
        minSelector.setFormatter(NumberPicker.Formatter { df.format(it)})
        mrdSelector = myView.findViewById(R.id.meridian_selector)
        mrdSelector.displayedValues = arrayOf("AM","PM")
        mrdSelector.setOnValueChangedListener { numberPicker, i, i2 ->
            if(i == 0){
                mrdSelector.separatorColor = resources.getColor(R.color.pm_color,null)
            }else{
                mrdSelector.separatorColor = resources.getColor(R.color.am_text,null)
            }
            meridianText.text = when(numberPicker.value){
                0 -> "AM";
                1 -> "PM";
                else -> "n/a"
            }
            meridianImage.background = getMeridianImage(meridianText.text.toString())
            viewModel.update(timeHelper.getMinutesFromViews())
        }
        timeHelper = TimeHelper(daySelector,hourSelector,minSelector,mrdSelector)
        nowButton = myView.findViewById(R.id.now_button)
        nowButton.setOnClickListener {
            val now = getTimeAsMinutes()
            timeHelper.setValues(now)
            setStartTimeViews()
            viewModel.update(now)
            Toast.makeText(requireContext(), "Start Time Is Now", Toast.LENGTH_SHORT).show()
        }
        val recyclerView = myView.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = CustomLayoutManager(context)
        recyclerView.adapter = savedRecipeAdapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (this@SavedRecipeFragment::scrollObserver.isInitialized) {
                    when (newState) {
                        RecyclerView.SCROLL_STATE_DRAGGING -> scrollObserver.isScrolling()
                        RecyclerView.SCROLL_STATE_IDLE -> scrollObserver.isNotScrolling()
                    }
                }
            }
        })
        savedRecipeAdapter.registerStepDialogWatcher(this)
        viewModel.intervalData?.observe(viewLifecycleOwner, intervalObserver)
        viewModel.start.observe(viewLifecycleOwner, startObserver)
        viewModel.notes.observe(viewLifecycleOwner, noteObserver)
        viewModel.recipeName?.observe(viewLifecycleOwner, nameObserver)
        viewModel.getStart()
        callback.fragmentAttached(this)
        keyboardDetection.registerObserver(this)
        textDisplayDialog.setTargetFragment(this, 0)
        textInputDialog.setTargetFragment(this, 0)
        stepDetailDialog.setTargetFragment(this, 0)
        return myView
    }

    fun getMeridianImage(meridian: String): Drawable? {
        when (meridian) {
            "AM" -> return imageAM
            "PM" -> return imagePM
            else -> return null
        }
    }

    fun showNotes() {
        textDisplayDialog.show(parentFragmentManager, "notes_detail")
        viewModel.getScheduleNotes()
    }

    override fun onMove() {

    }

    override fun onActionUp() {

    }

    override fun onLayoutCompleted(timeScroll: TimeScroll) {
        System.out.println("layout completed")
        layoutCompleted = true
        if (dataRetrieved) {
            val time = viewModel.start.value?.time ?: 0
        }
    }

    override fun keyboardDismissed() {
        viewModel.updateScheduleName(editName.text.toString())
        editName.clearFocus()
    }

    //input
    override fun onDialogCreated() {
        textInputDialog.setup()

    }

    //input
    override fun onDismiss(dialog: Dialog) {
        textInputDialog.teardown()
        dialog.dismiss()
    }

    //input
    override fun onCanceled(dialog: Dialog) {
        textInputDialog.teardown()
        dialog.dismiss()
    }

    //input
    override fun onConfirm(dialog: Dialog, note: String) {
        viewModel.updateScheduleNotes(note)
        textInputDialog.teardown()
        dialog.dismiss()
    }

    //display
    override fun onDisplayDialogCreated() {
    }

    //display
    override fun onDisplayDismiss(dialog: Dialog) {
        dialog.dismiss()
    }

    //display
    override fun onDisplayCanceled(dialog: Dialog) {
        dialog.dismiss()
    }

    //display
    override fun onDisplayConfirm(dialog: Dialog, note: String) {
        dialog.dismiss()
        textInputDialog.show(parentFragmentManager, "input_dialog")
        viewModel.getScheduleNotes()
    }

    override fun onStepDialogCalled(step: Interval) {
        viewModel.selectedInterval = step
        stepDetailDialog.show(parentFragmentManager, "detail_dialog")

    }

    override fun onStepDialogCreated() {
        stepDetailDialog.setupFields(viewModel.selectedInterval)
    }

    override fun onStepDismiss(dialog: Dialog) {
        stepDetailDialog.stepName.editableText.clear()
        stepDetailDialog.dismiss()
    }

    override fun onStepCanceled(dialog: Dialog) {
        stepDetailDialog.stepName.editableText.clear()
        stepDetailDialog.dismiss()
    }

    override fun onStepConfirm(dialog: Dialog, name: String, notes: String) {
        viewModel.selectedInterval.name = name
        viewModel.selectedInterval.notes = notes
        viewModel.updateStep(viewModel.selectedInterval)
        stepDetailDialog.dismiss()
    }

    fun getTimeAsMinutes(): Int {
        val dateTime = DateTime.now()
        val millisOfDay = dateTime.millisOfDay
        return millisOfDay / 60000
    }

    private fun setStartTimeViews(){
        hourText.text = timeHelper.getHoursAsString()
        hourText.invalidate()
        minText.text = timeHelper.getMinutesAsString()
        minText.invalidate()
        meridianText.text = timeHelper.getMeridanAsString()
        meridianText.invalidate()
        meridianImage.background = getMeridianImage(meridianText.text.toString())
        meridianImage.invalidate()
    }


}