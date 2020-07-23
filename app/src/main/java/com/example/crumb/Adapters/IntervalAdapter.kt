package com.example.crumb.Adapters

import android.app.Activity
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
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.emoji.widget.EmojiEditText
import androidx.emoji.widget.EmojiTextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.crumb.*
import com.example.crumb.Adapters.DiffUtilCallbacks.StepDiffUtilCallback
import com.example.crumb.Helpers.KeyboardDetectionHelper
import com.example.crumb.Models.Interval
import com.example.crumb.UI.CustomLayoutManager
import com.example.crumb.ViewModels.IntervalViewModel
import java.text.DecimalFormat
import kotlin.collections.ArrayList

class IntervalAdapter(mContext: Context, viewModel: IntervalViewModel, getDialog: GetDialog) :
    RecyclerView.Adapter<IntervalAdapter.ViewHolder>(),
    KeyboardDetectionHelper.KeyBoardObserver {

    val inputMethodManager =
        mContext.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    var activeViewHolder: ViewHolder? = null
    val mContext = mContext
    val viewModel = viewModel
    var activeTitle: EmojiEditText? = null
    var steps: ArrayList<Interval> = arrayListOf()
    val dialogRelay = getDialog
    lateinit var recyclerView: RecyclerView
    lateinit var getInputDialog: GetInputDialog


    fun setData(newList: ArrayList<Interval>) {
        val sizeChanged: Boolean
        if (newList.size != steps.size) {
            sizeChanged = true
        } else {
            sizeChanged = false
        }
        val diffResult = DiffUtil.calculateDiff(
            StepDiffUtilCallback(
                steps,
                newList
            )
        );
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
        holder.span.setText(getSpan(step.span).append(" from last"))
        if(step.percentage > 0f){
            holder.percentage.text = DecimalFormat("#.#").format(step.percentage * 100) + "%"
        }else{
            holder.percentage.text = "Start"
        }
        val seqTmp = "Step " + step.sequence.toString()
        val seqTxt = SpannableString(seqTmp)
        seqTxt.setSpan(AbsoluteSizeSpan(25),0,4,Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        holder.sequence.setText(seqTxt)
        if (position == steps.size - 1) {
            holder.setDrawables(true, step.color)
        } else {
            holder.setDrawables(false, step.color)
        }
        holder.step = steps.get(position)
        holder.myPosition = position
    }

    fun getName(name: String): String {
        if (name.equals("")) {
            return "No Title"
        } else {
            return name
        }
    }

    fun getTime(minutes: Int): String {
        var hours = Math.floor((minutes % 1440) / 60.toDouble()).toInt()
        val mins = Math.floor((minutes % 1440) % 60.toDouble()).toInt()
        if (hours > 12) {
            hours = hours - 12
        }
        if (hours == 0) {
            hours = 12
        }
        return hours.toString() + ":" + DecimalFormat("00").format(mins)
    }

    fun getMeridian(minutes: Int, viewHolder: ViewHolder): String {
        val hours = Math.floor((minutes % 1440) / 60.toDouble()).toInt()
        if (hours >= 12) {
            viewHolder.setMeridianImage(false)
            return "PM"
        } else {
            viewHolder.setMeridianImage(true)
            return "AM"
        }
    }

    fun getDay(minutes: Int): String {
        return "Day " + (Math.floor((minutes / 1440).toDouble()).toInt() + 1).toString()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.step_item, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layoutManager: CustomLayoutManager =
            recyclerView.layoutManager as CustomLayoutManager
        val name: EmojiTextView = itemView.findViewById(R.id.step_name_text)
        val noteButton: ImageView = itemView.findViewById<ImageView>(R.id.description_add_button)
        val day: TextView = itemView.findViewById(R.id.step_day_text)
        val time: TextView = itemView.findViewById(R.id.step_time_text)
        val merdian: TextView = itemView.findViewById(R.id.step_meridian_text)
        val span: TextView = itemView.findViewById(R.id.step_span_text)
        val percentage: TextView = itemView.findViewById(R.id.step_percentage_text)
        val sequence: TextView = itemView.findViewById(R.id.step_sequence_text)
        val terminus = mContext.resources.getDrawable(R.drawable.item_container_inactive, null)
        val node = mContext.resources.getDrawable(R.drawable.item_container_tail_inactive, null)
        val imageAMPM: View = itemView.findViewById(R.id.meridian_image)
        val imageAM = mContext.resources.getDrawable(R.drawable.am_image, null)
        val imagePM = mContext.resources.getDrawable(R.drawable.pm_image, null)
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
                tail.mutate().colorFilter = PorterDuffColorFilter(mContext.resources.getColor(R.color.hilight,null), PorterDuff.Mode.MULTIPLY)
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

    fun getSpan(minutes: Int): SpannableStringBuilder {
        val decimalFormat = DecimalFormat("#")
        val stringBuilder = SpannableStringBuilder()
        val days = Math.floor((minutes / 1440).toDouble())
        var hours = Math.floor((minutes % 1440) / 60.toDouble())
        val mins = Math.floor((minutes % 1440) % 60.toDouble())
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

    fun checkPlurality(number: Double): String {
        if (number == 1.0) {
            return ""
        } else {
            return "s"
        }
    }

}

