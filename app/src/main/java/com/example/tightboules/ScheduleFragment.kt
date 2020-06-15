package com.example.tightboules

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AbsListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.joda.time.DateTime

class ScheduleFragment : Fragment(), DeleteDialog.SwipeDeleteDialogListener,
    RecipeDialog.RecipeCreateDialogListener {

    val viewModel: ScheduleViewModel by viewModels()
    val scheduleAdapter: ScheduleAdapter by lazy {
        ScheduleAdapter(
            requireActivity(),
            viewModel,
            requireActivity().supportFragmentManager
        )
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
    val deleteDialog: DeleteDialog by lazy { DeleteDialog("Recipe") }
    val deletSwipe: SwipeToDelete by lazy {
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
            if (it.size > 0) {
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
        scheduleAdapter.notifyItemChanged(position);
        dialog.dismiss()
    }

    override fun swipeCanceled(dialog: Dialog, position: Int) {
        scheduleAdapter.notifyItemChanged(position);
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