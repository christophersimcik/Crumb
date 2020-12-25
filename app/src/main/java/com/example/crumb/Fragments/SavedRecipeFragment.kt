package com.example.crumb.Fragments

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.emoji.widget.EmojiEditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.example.crumb.Activities.MainActivity
import com.example.crumb.Adapters.SavedRecipeAdapter
import com.example.crumb.Dialogs.StepDetailDialog
import com.example.crumb.Dialogs.TextDisplayDialog
import com.example.crumb.Dialogs.TextInputDialog
import com.example.crumb.Fragments.FragmentInterfaces.FragmentCallback
import com.example.crumb.Helpers.KeyboardDetectionHelper
import com.example.crumb.Helpers.TimeHelper
import com.example.crumb.Models.Interval
import com.example.crumb.R
import com.example.crumb.UI.CustomLayoutManager
import com.example.crumb.UI.ScrollingCallback
import com.example.crumb.ViewModels.SavedRecipeViewModel
import com.example.crumb.ViewModels.ViewModelFactories.SavedRecipeFactory
import com.github.stephenvinouze.materialnumberpickercore.MaterialNumberPicker
import org.joda.time.DateTime
import java.text.DecimalFormat

class SavedRecipeFragment : Fragment(),
    KeyboardDetectionHelper.KeyBoardObserver,
    TextInputDialog.InputDialogListener,
    TextDisplayDialog.DisplayDialogListener,
    StepDetailDialog.StepDialogListener,
    SavedRecipeAdapter.StepDetailCallback {

    companion object {
        const val TAG = "SAVED_RECIPE_FRAGMENT"
        const val HALF_MINUTE_IN_SECS = 30
    }

    private lateinit var hourSelector: MaterialNumberPicker
    private lateinit var daySelector: MaterialNumberPicker
    private lateinit var minSelector: MaterialNumberPicker
    private lateinit var mrdSelector: MaterialNumberPicker
    private lateinit var editName: EmojiEditText
    private lateinit var nowButton: ImageButton
    private lateinit var meridianText: TextView
    private lateinit var meridianImage: View
    private lateinit var hourText: TextView
    private lateinit var minText: TextView
    private lateinit var myView: View

    private val textDisplayDialog: TextDisplayDialog by lazy { TextDisplayDialog() }
    private val textInputDialog: TextInputDialog by lazy { TextInputDialog() }
    private val stepDetailDialog: StepDetailDialog by lazy { StepDetailDialog() }
    private val keyboardDetection: KeyboardDetectionHelper by lazy {
        KeyboardDetectionHelper(
            requireActivity()
        )
    }

    lateinit var scrollObserver: ScrollingCallback
    private lateinit var timeHelper: TimeHelper
    private var layoutCompleted = false
    private var dataRetrieved = false

    private val imageAM: Drawable? by lazy {
        ContextCompat.getDrawable(requireContext(), R.drawable.am_image)
    }

    private val imagePM: Drawable? by lazy {
        ContextCompat.getDrawable(requireContext(), R.drawable.pm_image)
    }

    val viewModel: SavedRecipeViewModel by lazy {
        ViewModelProviders.of(
            this,
            SavedRecipeFactory(
                requireActivity().application,
                arguments?.getLong("parent_id") ?: 0L
            )
        ).get(SavedRecipeViewModel::class.java)
    }

    private val savedRecipeAdapter: SavedRecipeAdapter by lazy {
        SavedRecipeAdapter(
            requireActivity()
        )
    }

    private val noteObserver: Observer<String> by lazy {
        Observer<String> { note ->
            if (textDisplayDialog.dialog?.isShowing == true) {
                textDisplayDialog.noteDisplay.text = note
            }
        }
    }

    private val startObserver: Observer<Interval> by lazy {
        Observer<Interval> { step: Interval ->
            timeHelper.setValues(step.time)
            setStartTimeViews()
            Log.i(TAG, "interval is ${step.time}")
            dataRetrieved = true

        }
    }

    private val nameObserver: Observer<String> by lazy {
        Observer<String> { name: String ->
            editName.setText(name)
        }
    }

    private val intervalObserver: Observer<List<Interval>> by lazy {
        Observer<List<Interval>> { steps: List<Interval> ->
            savedRecipeAdapter.setData(steps as ArrayList<Interval>)
            savedRecipeAdapter.notifyDataSetChanged()

        }
    }

    private val callback: FragmentCallback by lazy {
        val mainActivity = activity as MainActivity
        mainActivity.sharedViewModel
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
        editName = myView.findViewById<EmojiEditText>(R.id.name_edit_text).apply {
            setOnEditorActionListener(getEditorActionListener())
            addTextChangedListener(getNameTextWatcher())
        }
        hourText = myView.findViewById(R.id.text_hour)
        minText = myView.findViewById(R.id.text_minute)
        meridianText = myView.findViewById(R.id.text_meridian)
        meridianImage = myView.findViewById(R.id.meridian_image)
        daySelector = myView.findViewById(R.id.day_selector)
        daySelector.setOnValueChangedListener { _, _, _ ->
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
        minSelector.setFormatter { df.format(it) }
        mrdSelector = myView.findViewById(R.id.meridian_selector)
        mrdSelector.displayedValues = arrayOf("AM", "PM")
        mrdSelector.setOnValueChangedListener { numberPicker, i, i2 ->
            if (i == 0) {
                mrdSelector.separatorColor = resources.getColor(R.color.pm_color, null)
            } else {
                mrdSelector.separatorColor = resources.getColor(R.color.am_text, null)
            }
            meridianText.text = when (numberPicker.value) {
                0 -> "AM"
                1 -> "PM"
                else -> "n/a"
            }
            meridianImage.background = getMeridianImage(meridianText.text.toString())
            viewModel.update(timeHelper.getMinutesFromViews())
        }
        timeHelper =
            TimeHelper(
                daySelector,
                hourSelector,
                minSelector,
                mrdSelector
            )
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
        callback.fragmentAttached(this)
        keyboardDetection.registerObserver(this)
        textDisplayDialog.setTargetFragment(this, 0)
        textInputDialog.setTargetFragment(this, 0)
        stepDetailDialog.setTargetFragment(this, 0)
        viewModel.getStart()
        return myView
    }

    private fun getMeridianImage(meridian: String): Drawable? {
        return when (meridian) {
            "AM" -> imageAM
            "PM" -> imagePM
            else -> null
        }
    }

    fun showNotes() {
        textDisplayDialog.show(parentFragmentManager, "notes_detail")
        viewModel.getScheduleNotes()
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

    private fun getTimeAsMinutes(): Int {
        val dateTime = DateTime.now()
        return dateTime.minuteOfDay + addBufferMinutes(dateTime)
    }

    private fun addBufferMinutes(dateTime: DateTime): Int{
        return if(dateTime.secondOfMinute > HALF_MINUTE_IN_SECS) 2 else 1
    }

    private fun setStartTimeViews() {
        hourText.text = timeHelper.getHoursAsString()
        hourText.invalidate()
        minText.text = timeHelper.getMinutesAsString()
        minText.invalidate()
        meridianText.text = timeHelper.getMeridanAsString()
        meridianText.invalidate()
        meridianImage.background = getMeridianImage(meridianText.text.toString())
        meridianImage.invalidate()
    }

    private fun getEditorActionListener(): TextView.OnEditorActionListener {
        return TextView.OnEditorActionListener { view, id, event ->
            Log.d(TAG, "registering views and id $id and event = $event")
            if (id == EditorInfo.IME_ACTION_NEXT || event == null) {
                (this@SavedRecipeFragment.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
                    hideSoftInputFromWindow(myView.windowToken, 0)
                    viewModel.updateScheduleName(view.text.toString())
                }
            }
            false
        }
    }

    private fun getNameTextWatcher(): TextWatcher{
        return object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                viewModel.nameField = editable.toString()
            }
        }
    }

}