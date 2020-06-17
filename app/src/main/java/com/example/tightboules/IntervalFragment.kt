package com.example.tightboules

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.time_select_dialog.*

class IntervalFragment :

    Fragment(),
    IntervalAdapter.GetDialog,
    StepDialog.DeleteDialogListener,
    IntervalViewModel.CreationListener,
    TextInputDialog.InputDialogListener,
    IntervalAdapter.GetInputDialog,
    DeleteDialog.SwipeDeleteDialogListener{

    val textInputDialog : TextInputDialog by lazy{TextInputDialog()}
    val recyclerView: RecyclerView by lazy { myView.findViewById<RecyclerView>(R.id.intervals_recycler_view) }

    lateinit var myView: View
    lateinit var dialog: StepDialog
    lateinit var keyboardDetection : KeyboardDetection
    lateinit var scrollObserver : ScrollingCallback

    val deleteDialog: DeleteDialog by lazy { DeleteDialog("Step") }

    val deletSwipe: SwipeToDelete by lazy {
        object : SwipeToDelete(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteDialog.show(parentFragmentManager, "swipe_delete")
                deleteDialog.setPosition(viewHolder.adapterPosition)
            }
        }
    }

    val viewModel: IntervalViewModel by lazy {
        ViewModelProviders.of(
            this,
            IntervalFactory(
                requireActivity().application,
                requireNotNull(arguments?.getLong("parent_id"))
            )
        ).get(IntervalViewModel::class.java)
    }

    val intervalAdapter: IntervalAdapter by lazy {
        IntervalAdapter(
            requireActivity(),
            viewModel,
            this
        )
    }

    val intervalObserver: Observer<List<Interval>> by lazy {
        Observer<List<Interval>> { steps: List<Interval> ->
            if(steps.isEmpty()){
                viewModel.create()
                viewModel.lastTime = 0
                viewModel.lastPositon = 0
            }else {
                intervalAdapter.setData(steps as ArrayList<Interval>)
                viewModel.lastTime = steps.last().time
                viewModel.lastPositon = steps.lastIndex
            }
            intervalAdapter.notifyDataSetChanged()
        }
    }

    val callback: FragmentCallback by lazy {
        val mainActivity = activity as MainActivity
        mainActivity.sharedViewModel
    }

    fun registerScrollObserver(scrollingCallback: ScrollingCallback){
        scrollObserver = scrollingCallback
    }


    override fun onAttach(context: Context) {
        keyboardDetection = KeyboardDetection(requireActivity())
        dialog = StepDialog(keyboardDetection)
        dialog.isCancelable = false
        super.onAttach(context)
    }

    override fun onStart() {
        super.onStart()
        keyboardDetection.getListener()
        intervalAdapter
        intervalObserver
    }

    override fun onStop() {
        super.onStop()
        keyboardDetection.removeListener()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        myView = layoutInflater.inflate(R.layout.intervals_fragment, container, false)
        recyclerView.layoutManager = CustomLayoutManager(context)
        recyclerView.adapter = intervalAdapter
        recyclerView.addOnScrollListener(object:RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(this@IntervalFragment::scrollObserver.isInitialized) {
                    when (newState) {
                        RecyclerView.SCROLL_STATE_DRAGGING -> scrollObserver.isScrolling()
                        RecyclerView.SCROLL_STATE_IDLE -> scrollObserver.isNotScrolling()
                    }
                }
            }
        })
        val itemTouchHelper = ItemTouchHelper(deletSwipe)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        viewModel.registerListener(this)
        viewModel.intervalData?.observe(viewLifecycleOwner, intervalObserver)
        callback.fragmentAttached(this)
        dialog.setTargetFragment(this, 0)
        textInputDialog.setTargetFragment(this,0)
        deleteDialog.setTargetFragment(this, 0)
        intervalAdapter.registerGetInputDialog(this)
        return myView
    }

    override fun onPause() {
        super.onPause()
    }

    override fun showInputDialog() {
        textInputDialog.show(parentFragmentManager, "input_dialog")
    }


    override fun showDialog(time : Int, name : String) {
        dialog.initTime = time
        dialog.setName(name)
        dialog.show(parentFragmentManager, "dialog", time)
    }

    override fun onDialogCreated() {
        textInputDialog.noteInputField.setText(viewModel.selected.notes)
    }

    override fun onCreated(time : Int) {
        recyclerView.smoothScrollToPosition(recyclerView.bottom)
        dialog.initTime = viewModel.lastTime
        dialog.show(parentFragmentManager, "dialog", time)
    }

    override fun onDismiss(dialog: Dialog) {
        textInputDialog.teardown()
        textInputDialog.dismiss()
    }

    override fun onCanceled(dialog: Dialog) {
        textInputDialog.teardown()
        textInputDialog.dismiss()
    }

    override fun onConfirm(dialog: Dialog, note: String) {
        viewModel.selected.notes = note
        viewModel.updateName(viewModel.selected)
        this.dialog.setName("")
        textInputDialog.teardown()
        dialog.dismiss()
    }

    override fun stepDialogCreated() {
        dialog.nameInputField.setText(dialog.getName())
    }

    override fun stepDismiss(dialog: Dialog) {
        recyclerView.smoothScrollToPosition(viewModel.selectedPosition)
        if(viewModel.newlyCreated){
            viewModel.deleteNew()
        }
        viewModel.newlyCreated = false
        this.dialog.setName("")
        dialog.dismiss()
    }

    override fun stepCanceled(dialog: Dialog) {
        viewModel.newlyCreated = false
        this.dialog.setName("")
        dialog.dismiss()
    }

    override fun stepConfirm(dialog: Dialog, name: String, time: Int) {
        viewModel.updateSelected(name, time, intervalAdapter)
        recyclerView.smoothScrollToPosition(viewModel.selectedPosition)
        viewModel.newlyCreated = false
        this.dialog.setName("")
        dialog.dismiss()}

    override fun swipeDialogCreated() {}

    override fun swipeDismiss(dialog: Dialog, position: Int) {
        intervalAdapter.notifyItemChanged(position);
        dialog.dismiss()
    }

    override fun swipeCanceled(dialog: Dialog, position: Int) {
        intervalAdapter.notifyItemChanged(position);
        dialog.dismiss()}

    override fun swipeConfirm(dialog: Dialog, position: Int) {
        viewModel.delete(position)
        dialog.dismiss()
    }


}