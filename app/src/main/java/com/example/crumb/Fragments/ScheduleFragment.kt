package com.example.crumb.Fragments

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.crumb.*
import com.example.crumb.Activities.MainActivity
import com.example.crumb.Adapters.ScheduleAdapter
import com.example.crumb.Dialogs.DeleteDialog
import com.example.crumb.Dialogs.RecipeDialog
import com.example.crumb.Fragments.FragmentInterfaces.FragmentCallback
import com.example.crumb.Models.Schedule
import com.example.crumb.UI.CustomLayoutManager
import com.example.crumb.UI.ScrollingCallback
import com.example.crumb.UI.SwipeToDelete
import com.example.crumb.ViewModels.ScheduleViewModel

class ScheduleFragment : Fragment(), DeleteDialog.SwipeDeleteDialogListener,
    RecipeDialog.RecipeCreateDialogListener {

    private val viewModel: ScheduleViewModel by viewModels()
    private val scheduleAdapter: ScheduleAdapter by lazy {
        ScheduleAdapter(
            requireActivity(),
            viewModel,
            requireActivity().supportFragmentManager
        )
    }
    private val callback: FragmentCallback by lazy {
        val mainActivity = activity as MainActivity
        mainActivity.sharedViewModel
    }
    private val navHostFragment: NavHostFragment by lazy {
        requireActivity().supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment
        ) as NavHostFragment
    }
    val deleteDialog: DeleteDialog by lazy {
        DeleteDialog(
            context?.getString(R.string.delete_this_recipe) ?: ""
        )
    }
    private val deletSwipe: SwipeToDelete by lazy {
        object : SwipeToDelete(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteDialog.show(parentFragmentManager, "swipe_delete")
                deleteDialog.setPosition(viewHolder.adapterPosition)
            }
        }
    }

    fun registerScrollObserver(scrollingCallback: ScrollingCallback) {
        scrollObserver = scrollingCallback
    }

    lateinit var scrollObserver: ScrollingCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = layoutInflater.inflate(R.layout.schedules_fragment, container, false)
        val recyclerView = myView.findViewById<RecyclerView>(R.id.schedule_recycler_view)
        val textView = myView.findViewById<TextView>(R.id.no_data_text)
        val itemTouchHelper = ItemTouchHelper(deletSwipe)
        recyclerView.layoutManager = CustomLayoutManager(context)
        recyclerView.adapter = scheduleAdapter
        itemTouchHelper.attachToRecyclerView(recyclerView)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (this@ScheduleFragment::scrollObserver.isInitialized) {
                    when (newState) {
                        RecyclerView.SCROLL_STATE_DRAGGING -> scrollObserver.isScrolling()
                        RecyclerView.SCROLL_STATE_IDLE -> scrollObserver.isNotScrolling()
                    }
                }
            }
        })

        val scheduleObserver = Observer<List<Schedule>> {
            if (it.isNotEmpty()) {
                textView.visibility = View.INVISIBLE
            } else {
                textView.visibility = View.VISIBLE
            }
            scheduleAdapter.setData(it as ArrayList<Schedule>)
            scheduleAdapter.notifyDataSetChanged()
        }

        viewModel.scheduleData?.observe(viewLifecycleOwner, scheduleObserver)
        callback.fragmentAttached(this)
        deleteDialog.setTargetFragment(this, 0)
        return myView
    }

    override fun swipeDialogCreated() {

    }

    override fun swipeDismiss(dialog: Dialog, position: Int) {
        scheduleAdapter.notifyItemChanged(position)
        dialog.dismiss()
    }

    override fun swipeCanceled(dialog: Dialog, position: Int) {
        scheduleAdapter.notifyItemChanged(position)
        dialog.dismiss()
    }

    override fun swipeConfirm(dialog: Dialog, position: Int) {
        viewModel.deleteRecipe(position)
        deleteDialog.dismiss()
    }

    // recipe
    override fun recipeDismiss(dialog: Dialog) {
        dialog.dismiss()
    }

    override fun recipeCanceled(dialog: Dialog) {
        dialog.dismiss()
    }

    override fun recipeConfirm(dialog: Dialog, name: String) {
        viewModel.newSchedule(name, navHostFragment.navController)
        dialog.dismiss()
    }

}