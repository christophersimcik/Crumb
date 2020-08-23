package com.example.crumb.Adapters

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.LayerDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.RelativeSizeSpan
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.emoji.widget.EmojiTextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.crumb.*
import com.example.crumb.Adapters.DiffUtilCallbacks.StepDiffUtilCallback
import com.example.crumb.Helpers.KeyboardDetectionHelper
import com.example.crumb.Models.Interval
import com.example.crumb.ViewModels.IntervalViewModel
import java.text.DecimalFormat
import kotlin.collections.ArrayList
import kotlin.math.floor

class IntervalAdapter(val context : Context, val viewModel: IntervalViewModel, getDialog: GetDialog) :
    RecyclerView.Adapter<IntervalAdapter.ViewHolder>(),
    KeyboardDetectionHelper.KeyBoardObserver {

    var steps: ArrayList<Interval> = arrayListOf()
    val dialogRelay = getDialog
    private lateinit var recyclerView: RecyclerView
    lateinit var getInputDialog: GetInputDialog


    fun setData(newList: ArrayList<Interval>) {
        val sizeChanged = newList.size != steps.size
        val diffResult = DiffUtil.calculateDiff(
            StepDiffUtilCallback(
                steps,
                newList
            )
        )
        steps.clear()
        steps.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
        if (sizeChanged) {
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return steps.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val step = steps[position]
        holder.name.text = getName(step.name)
        holder.day.text = getDay(step.time)
        holder.time.text = getTime(step.time)
        holder.merdian.text = getMeridian(step.time, holder)
        holder.span.text = getSpan(step.span).append(" from last")
        if(step.percentage > 0f){
            val formattedTxt =  DecimalFormat("#.#").format(step.percentage * 100) + "%"
            holder.percentage.text = formattedTxt
        }else{
            holder.percentage.text = context.getString(R.string.start_text)
        }
        val seqTmp = "Step " + step.sequence.toString()
        val seqTxt = SpannableString(seqTmp)
        seqTxt.setSpan(AbsoluteSizeSpan(25),0,4,Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        holder.sequence.text = seqTxt
        if (position == steps.size - 1) {
            holder.setDrawables(true, step.color)
        } else {
            holder.setDrawables(false, step.color)
        }
        holder.step = steps[position]
        holder.myPosition = position
    }

    private fun getName(name: String): String {
        return if (name == "") {
            "No Title"
        } else {
            name
        }
    }

    private fun getTime(minutes: Int): String {
        var hours = floor((minutes % 1440) / 60.toDouble()).toInt()
        val mins = floor((minutes % 1440) % 60.toDouble()).toInt()
        if (hours > 12) {
            hours -= 12
        }
        if (hours == 0) {
            hours = 12
        }
        return hours.toString() + ":" + DecimalFormat("00").format(mins)
    }

    private fun getMeridian(minutes: Int, viewHolder: ViewHolder): String {
        val hours = floor((minutes % 1440) / 60.toDouble()).toInt()
        return if (hours >= 12) {
            viewHolder.setMeridianImage(false)
            "PM"
        } else {
            viewHolder.setMeridianImage(true)
            "AM"
        }
    }

    private fun getDay(minutes: Int): String {
        return "Day " + (floor((minutes / 1440).toDouble()).toInt() + 1).toString()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.step_item, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: EmojiTextView = itemView.findViewById(R.id.step_name_text)
        private val noteButton: ImageView = itemView.findViewById(R.id.description_add_button)
        val day: TextView = itemView.findViewById(R.id.step_day_text)
        val time: TextView = itemView.findViewById(R.id.step_time_text)
        val merdian: TextView = itemView.findViewById(R.id.step_meridian_text)
        val span: TextView = itemView.findViewById(R.id.step_span_text)
        val percentage: TextView = itemView.findViewById(R.id.step_percentage_text)
        val sequence: TextView = itemView.findViewById(R.id.step_sequence_text)
        private val imageAMPM: View = itemView.findViewById(R.id.meridian_image)
        private val terminus = ContextCompat.getDrawable(context, R.drawable.item_container_inactive)
        private val node = ContextCompat.getDrawable(context, R.drawable.item_container_tail_inactive)
        private val imageAM = ContextCompat.getDrawable(context, R.drawable.am_image)
        private val imagePM = ContextCompat.getDrawable(context, R.drawable.pm_image)
        lateinit var step : Interval
        var myPosition = 0

        init {
            itemView.setOnLongClickListener { view: View ->
                dialogRelay.showDialog(step.time, step.name)
                viewModel.selected = step
                viewModel.selectedPosition = myPosition
                true
            }
            noteButton.setOnClickListener {
                if(this@IntervalAdapter::getInputDialog.isInitialized){
                    getInputDialog.showInputDialog()
                }
                viewModel.selected = step            }
        }



        fun setDrawables(end: Boolean, color : Int) {
            if (end) {
                itemView.background = terminus
                val drawable = itemView.background as LayerDrawable
                val cBlock = drawable.findDrawableByLayerId(R.id.color_block)
                cBlock.setTint(color)
            } else {
                itemView.background = node
                val drawable = itemView.background as LayerDrawable
                val tail = drawable.findDrawableByLayerId(R.id.tail)
                tail.mutate().colorFilter = PorterDuffColorFilter(context.resources.getColor(R.color.hilight,null), PorterDuff.Mode.MULTIPLY)
                val cBlock = drawable.findDrawableByLayerId(R.id.color_block)
                cBlock.setTint(color)
            }

            itemView.invalidate()
        }

        fun setMeridianImage(am: Boolean) {
            if (am) {
                imageAMPM.background = imageAM
            } else {
                imageAMPM.background = imagePM
            }
            itemView.invalidate()
        }
    }

    interface GetDialog {
        fun showDialog(time: Int, name : String)
    }

    fun registerGetInputDialog(getInputDialog: GetInputDialog){
        this.getInputDialog = getInputDialog
    }

    interface GetInputDialog {
        fun showInputDialog()
    }

    private fun getSpan(minutes: Int): SpannableStringBuilder {
        val decimalFormat = DecimalFormat("#")
        val stringBuilder = SpannableStringBuilder()
        val days = floor((minutes / 1440).toDouble())
        val hours = floor((minutes % 1440) / 60.toDouble())
        val mins = floor((minutes % 1440) % 60.toDouble())
        if (days >= 1.0) {
            stringBuilder.append(
                decimalFormat.format(days),
                AbsoluteSizeSpan(50),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            stringBuilder.append(
                " Day" + checkPlurality(days),
                RelativeSizeSpan(.5f),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
        if (hours >= 1.0) {
            stringBuilder.append(
                " " + decimalFormat.format(hours),
                AbsoluteSizeSpan(50),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            stringBuilder.append(
                " Hr" + checkPlurality(hours),
                RelativeSizeSpan(.5f),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )

        }
        if (mins >= 0.0) {
            stringBuilder.append(
                " " + decimalFormat.format(mins),
                AbsoluteSizeSpan(50),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            stringBuilder.append(
                " Min" + checkPlurality(mins),
                RelativeSizeSpan(.5f),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
        return stringBuilder
    }

    private fun checkPlurality(number: Double): String {
        return if (number == 1.0) {
            ""
        } else {
            "s"
        }
    }

}

