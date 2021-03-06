package com.example.crumb.Adapters

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.emoji.widget.EmojiEditText
import androidx.emoji.widget.EmojiTextView
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.crumb.Helpers.KeyboardDetectionHelper
import com.example.crumb.Models.Schedule
import com.example.crumb.R
import com.example.crumb.UI.TimeLineView
import com.example.crumb.ViewModels.ScheduleViewModel

class ScheduleAdapter(
    private val mContext: Context,
    val viewModel: ScheduleViewModel,
    fragmentManager: FragmentManager
) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>(),
    KeyboardDetectionHelper.KeyBoardObserver {
    private var activeTitle: EmojiEditText? = null
    private var mData: ArrayList<Schedule> = arrayListOf()
    private val keyboardDetection = KeyboardDetectionHelper(mContext as Activity)
    val navHostFragment =
        fragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    private lateinit var recyclerView: RecyclerView

    init {
        keyboardDetection.registerObserver(this)
    }

    override fun keyboardDismissed() {
        activeTitle?.clearFocus()
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = mData[position].name
        holder.description.text = mData[position].description
        holder.steps.text = getSteps(mData[position].steps)
        holder.date.text = mData[position].date
        holder.parentID = mData[position].id
        viewModel.getTotals(mData[position].id, holder)
    }


    private fun getSteps(steps: Int): SpannableStringBuilder {
        val stringBuilder = SpannableStringBuilder()
        stringBuilder.append("In ", AbsoluteSizeSpan(35), Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        stringBuilder.append(
            steps.toString(),
            AbsoluteSizeSpan(40),
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        stringBuilder.append(
            " Step" + checkPlurality(steps.toDouble()),
            AbsoluteSizeSpan(35),
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        return stringBuilder
    }

    private fun checkPlurality(number: Double): String {
        return if (number == 1.0) {
            ""
        } else {
            "s"
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.schedules_item, parent, false)
        return ViewHolder(view)
    }

    fun setData(data: ArrayList<Schedule>) {
        mData = data
    }


    override fun getItemCount(): Int {
        return mData.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ScheduleViewModel.TotalsCallback {


        override fun onTotalsAvailable(percentages: List<Float>?, colors: List<Int>?) {
            timeLine.update(percentages, colors)
        }

        val name: EmojiTextView = itemView.findViewById(R.id.name_text)
        val description: EmojiTextView = itemView.findViewById(R.id.description_text)
        val steps: TextView = itemView.findViewById(R.id.steps_text)
        val date: TextView = itemView.findViewById(R.id.date_text)
        private val timeLine: TimeLineView = itemView.findViewById(R.id.timeline_view)
        var parentID: Long = 0L

        init {
            viewModel.register(this)
            itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putLong("parent_id", parentID)
                navHostFragment.navController.navigate(
                    R.id.action_scheduleFragment_to_savedRecipeFragment,
                    bundle
                )
            }
        }
    }


}