package com.example.tightboules

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.view.*
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageButton
import androidx.emoji.widget.EmojiTextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.callbackFlow
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

class PlayAdapter(val mContext: Context, val viewModel: PlayViewModel) :
    RecyclerView.Adapter<PlayAdapter.ViewHolder>() {

    companion object{
        val MERIDIAN = hashMapOf<Int,String>(
            0 to "am",
            1 to "pm"
        )
    }

    var mData: ArrayList<Interval> = arrayListOf()
    var start = viewModel.start
    var duration = viewModel.duration
    lateinit var stepDetailCallback: PlayAdapter.StepDetailCallback
    lateinit var recyclerView: RecyclerView
    val alarmHelper = AlarmHelper(mContext.getSharedPreferences(SharedViewModel.SHARED_PREFERENCES, 0))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = mData.get(position).name
        holder.makeDuration(mData,position)
        holder.putName(mData.get(position).name)
        if (position == 0) {
            holder.progressBar.setIsFirst(true)
        } else {
            holder.progressBar.setIsFirst(false)
        }
        holder.update(System.currentTimeMillis())
        holder.alarmCheckBox.setChecked(mData.get(position).alarm_on)
        holder.setStartTime(mData.get(position).alarm_time)
        holder.progressBar.setColor(mData.get(position).color)
        holder.step = mData.get(position)
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
        val view = LayoutInflater.from(mContext).inflate(R.layout.play_item, parent, false)
        return ViewHolder(view)
    }

    fun setData(newData: List<Interval>) {
        val diffResult = DiffUtil.calculateDiff(StepDiffUtilCallback(mData, newData));
        mData.clear()
        mData.addAll(newData)
        diffResult.dispatchUpdatesTo(this)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        PlayViewModel.ViewHolderCallback{

        var step : Interval? = null
        var myStart = 0L

        override fun onInitiated(colors: IntArray) {
            notifyDataSetChanged()
        }

        override fun onTimeUpdate(now: Long) {
            update(now)
        }

        val name: EmojiTextView = itemView.findViewById(R.id.play_name_field)
        val startTime : EmojiTextView = itemView.findViewById(R.id.play_start_time_field)
        val noteButton = itemView.findViewById<ImageButton>(R.id.play_notes_button)
        val progressBar: MyItemProgressBar = itemView.findViewById(R.id.my_progerss_bar)
        val alarmCheckBox: CustomCheckBox = itemView.findViewById(R.id.play_checkbox)
        var duration: Long = 0L

        init {
            viewModel.registerTimeObserver(this)
            noteButton.setOnClickListener {
                stepDetailCallback.onStepDialogCalled(mData.get(adapterPosition))
            }
            alarmCheckBox.setOnClickListener {
                val newBool = alarmCheckBox.toggleChecked()
                if (newBool) {
                    setAlarm()
                } else {
                    cancelAlarm()
                }
            }
        }

        fun setStartTime(timeInMillis : Long){
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeInMillis
            var hour = calendar.get(Calendar.HOUR)
            if(hour == 0) hour = 12
            val min = calendar.get(Calendar.MINUTE)
            val meridian = calendar.get(Calendar.AM_PM)
            val timeAsString = hour.toString() + ":" + DecimalFormat("00").format(min)  + " " + MERIDIAN.get(meridian)
            startTime.setText(timeAsString)
        }

        fun cancelAlarm() {
           viewModel.updateInterval(alarmHelper.cancelSpecificAlarm(mData.get(adapterPosition), mContext))
        }

        fun setAlarm() {
            viewModel.updateInterval(alarmHelper.setSpecificAlarm(mData.get(adapterPosition), mContext))
        }

        fun update(now: Long) {
            this.progressBar.update(now, start, myStart, viewModel.start, viewModel.duration)
            if(step != null && step!!.alarm_on) {
                this.progressBar.setMessage(convertMillisToText(myStart - now))
            }else {
                this.progressBar.setMessage("Alarm Off!")
            }
        }

        fun putName(name: String) {
            if (name.equals("")) {
                this.name.setText("No Name")
            } else {
                this.name.setText(name)
            }
        }

        fun makeDuration(list: List<Interval>, position: Int) {
            var dur = 0
            for (index in 0..position) {
                dur += list.get(index).span
            }
            myStart = (start + (dur * 60000))
        }
    }

    fun convertMillisToText(millis: Long): String {
        if (millis > 0) {
            val days = millis / 86400000
            val hours = (millis % 86400000) / 3600000
            val mins = (millis % 86400000 % 3600000) / 60000
            val secs = (millis % 86400000 % 3600000 % 60000) / 1000
            System.out.println("secs = " + secs)
            var dayString = ""
            var hourString = ""
            var minString = ""
            var secString = ""
            if (days > 0L) {
                if (days > 1) {
                    dayString = days.toString() + " Days "
                } else {
                    dayString = days.toString() + " Day "
                }
            }
            if (hours > 0) {
                if (hours > 1) {
                    hourString = hours.toString() + " Hrs "
                } else {
                    hourString = hours.toString() + " Hr "
                }
            }
            if (mins > 0) {
                if (mins > 1) {
                    minString = mins.toString() + " Mins "
                } else {
                    minString = mins.toString() + " Min "
                }
            }
            if (secs > 0) {
                if (secs > 1) {
                    secString = secs.toString() + " Secs "
                } else {
                    secString = secs.toString() + " Sec "
                }
            }
            return "Alarm In " + dayString + hourString + minString + secString
        } else {
            return "Completed!"
        }
    }

    fun registerStepDialogWatcher(stepDetailwatcher: PlayAdapter.StepDetailCallback) {
        stepDetailCallback = stepDetailwatcher
    }

    interface StepDetailCallback {
        fun onStepDialogCalled(step: Interval)
    }

}