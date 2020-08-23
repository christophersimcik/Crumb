package com.example.crumb.Adapters

import android.app.Activity
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.*
import android.widget.TextView
import androidx.emoji.widget.EmojiEditText
import androidx.emoji.widget.EmojiTextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.crumb.*
import com.example.crumb.Adapters.DiffUtilCallbacks.StepDiffUtilCallback
import com.example.crumb.Helpers.KeyboardDetectionHelper
import com.example.crumb.Models.Interval
import com.example.crumb.UI.CustomChainView
import java.text.DecimalFormat
import kotlin.collections.ArrayList
import kotlin.math.floor

class SavedRecipeAdapter(val context: Context) : RecyclerView.Adapter<SavedRecipeAdapter.ViewHolder>(),
    KeyboardDetectionHelper.KeyBoardObserver {
    private val amColor = context.resources.getColor(R.color.am_text, null)
    private val pmColor = context.resources.getColor(R.color.pm_color, null)
    private val neutralColor = context.resources.getColor(R.color.inactive_light, null)
    private var activeTitle: EmojiEditText? = null
    var steps: ArrayList<Interval> = arrayListOf()
    private val keyboardDetection = KeyboardDetectionHelper(context as Activity)

    lateinit var stepDetailCallback: StepDetailCallback
    private lateinit var recyclerView: RecyclerView

    init {
        keyboardDetection.registerObserver(this)
    }

    override fun keyboardDismissed() {
        activeTitle?.clearFocus()
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val interval = steps[position]
        holder.name.text = steps[position].name
        holder.startTimeText.text = getTime(steps[position].time)
        holder.startDayText.text = getDay(steps[position].time)
        holder.chooseBackground(itemCount, position, steps[position].color)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.saved_recipe_item, parent, false)
        return ViewHolder(view)
    }

    private fun getTime(minutes: Int): SpannableString {
        val decimalFormat = DecimalFormat("00")
        var hours = floor((minutes % 1440.0) / 60.0).toInt()
        var meridian = "AM"
        val mins = floor((minutes % 1440) % 60.0).toInt()
        if (hours >= 12) {
            meridian = "PM"
            hours -= 12
        }
        if (hours == 0) {
            hours = 12
        }


        val color: Int

        color = when (meridian) {
            "AM" -> amColor
            "PM" -> pmColor
            else -> neutralColor
        }

        val spannableString =
            SpannableString(hours.toString() + ":" + decimalFormat.format(mins) + " " + meridian)
        spannableString.setSpan(
            RelativeSizeSpan(.80f),
            spannableString.length - 2,
            spannableString.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(color),
            spannableString.length - 2,
            spannableString.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        return spannableString
    }

    private fun getDay(minutes: Int): String {
        return "Day " + (floor((minutes / 1440).toDouble()).toInt() + 1).toString()
    }

    fun setData(newData: ArrayList<Interval>) {
        val diffResult = DiffUtil.calculateDiff(
            StepDiffUtilCallback(
                steps,
                newData
            )
        )
        steps.clear()
        steps.addAll(newData)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int {
        return steps.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val name: EmojiTextView = itemView.findViewById(R.id.name_text)
        val startTimeText: TextView = itemView.findViewById(R.id.start_time_text)
        val startDayText: TextView = itemView.findViewById(R.id.start_day_text)
        private val node: CustomChainView = itemView.findViewById(R.id.node_image)

        init {
            node.setOnClickListener {
                node.invalidate()
                stepDetailCallback.onStepDialogCalled(steps[adapterPosition])
            }
        }

        fun chooseBackground(size: Int, position: Int, color : Int) {
            node.setColor(color)
            if (size == 1) {
                node.setMode(CustomChainView.SINGLE)
                return
            }
            when (position) {
                0 -> {
                    node.setMode(CustomChainView.START)
                    return
                }
                size - 1 -> {
                    node.setMode(CustomChainView.END)
                    return
                }
                else -> {
                    node.setMode(CustomChainView.MIDDLE)
                    return
                }
            }
        }
    }

    fun registerStepDialogWatcher(stepDetailwatcher: StepDetailCallback) {
        stepDetailCallback = stepDetailwatcher
    }

    interface StepDetailCallback {
        fun onStepDialogCalled(step: Interval)
    }
}