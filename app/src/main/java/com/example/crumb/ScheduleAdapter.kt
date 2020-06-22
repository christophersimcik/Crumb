package com.example.crumb

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.view.*
import android.widget.TextView
import androidx.emoji.widget.EmojiEditText
import androidx.emoji.widget.EmojiTextView
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat
import kotlin.collections.ArrayList

class ScheduleAdapter(
    val mContext: Context,
    val viewModel: ScheduleViewModel,
    fragmentManager: FragmentManager
) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>(), KeyboardDetection.KeyBoardObserver {
    var activeTitle: EmojiEditText? = null
    var mData: ArrayList<Schedule> = arrayListOf()
    val keyboardDetection = KeyboardDetection(mContext as Activity)
    val navHostFragment =
        fragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    lateinit var recyclerView: RecyclerView

    init {
        keyboardDetection.registerObserver(this)
    }

    override fun keyboardDismissed() {
        activeTitle?.clearFocus()
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.setText(mData.get(position).name)
        holder.description.setText(mData.get(position).description)
        holder.duration.setText(getTime(mData.get(position).duration))
        holder.steps.setText(getSteps(mData.get(position).steps))
        holder.date.setText(mData.get(position).date)
        holder.parentID = mData.get(position).id
        viewModel.getTotals(mData.get(position).id, holder)
    }


    fun getSteps(steps: Int): SpannableStringBuilder {
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

    fun getTime(minutes: Int): SpannableStringBuilder {
        val decimalFormat = DecimalFormat("#")
        val stringBuilder = SpannableStringBuilder()
        val days = Math.floor((minutes / 1440).toDouble())
        var hours = Math.floor((minutes % 1440) / 60.toDouble())
        val mins = Math.floor((minutes % 1440) % 60.toDouble())
        if (days >= 1.0) {
            stringBuilder.append(
                decimalFormat.format(days),
                AbsoluteSizeSpan(40),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            stringBuilder.append(
                " Day" + checkPlurality(days),
                AbsoluteSizeSpan(35),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
        if (hours >= 1.0) {
            stringBuilder.append(
                " " + decimalFormat.format(hours),
                AbsoluteSizeSpan(40),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            stringBuilder.append(
                " Hr" + checkPlurality(hours),
                AbsoluteSizeSpan(35),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )

        }
        if (mins >= 0.0) {
            stringBuilder.append(
                " " + decimalFormat.format(mins),
                AbsoluteSizeSpan(40),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            stringBuilder.append(
                " Min" + checkPlurality(mins),
                AbsoluteSizeSpan(35),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
        return stringBuilder
    }

    fun checkPlurality(number: Double): String {
        if (number == 1.0) {
            return ""
        } else {
            return "s"
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

        private val CustomLayoutManager = recyclerView.layoutManager as CustomLayoutManager
        val name: EmojiTextView = itemView.findViewById(R.id.name_text)
        val description: EmojiTextView = itemView.findViewById(R.id.description_text)
        val duration: TextView = itemView.findViewById(R.id.duration_text)
        val steps: TextView = itemView.findViewById(R.id.steps_text)
        val date: TextView = itemView.findViewById(R.id.date_text)
        val timeLine: TimeLineView = itemView.findViewById(R.id.timeline_view)
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