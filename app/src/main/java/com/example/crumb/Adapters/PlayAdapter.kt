package com.example.crumb.Adapters

import android.content.Context
import android.view.*
import android.widget.ImageButton
import androidx.emoji.widget.EmojiTextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.crumb.*
import com.example.crumb.Adapters.DiffUtilCallbacks.StepDiffUtilCallback
import com.example.crumb.Helpers.AlarmHelper
import com.example.crumb.Models.Interval
import com.example.crumb.UI.CustomCheckBox
import com.example.crumb.UI.MyItemProgressBar
import com.example.crumb.ViewModels.PlayViewModel
import com.example.crumb.ViewModels.SharedViewModel
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

class PlayAdapter(val context: Context, val viewModel: PlayViewModel) :
    RecyclerView.Adapter<PlayAdapter.ViewHolder>() {

    companion object {

        const val MILLIS_IN_SECOND = 1000
        const val MILLIS_IN_MINUTE = 60000
        const val MILLIS_IN_HOUR = 3600000
        const val MILLIS_IN_DAY = 86400000

        val MERIDIAN = hashMapOf(
            0 to "am",
            1 to "pm"
        )
    }

    var mData: ArrayList<Interval> = arrayListOf()
    var start = viewModel.start
    lateinit var stepDetailCallback: StepDetailCallback
    private lateinit var recyclerView: RecyclerView
    val alarmHelper = AlarmHelper(
        context.getSharedPreferences(
            SharedViewModel.SHARED_PREFERENCES,
            0
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = mData[position].name
        holder.makeDuration(mData, position)
        holder.myStart = mData[position].alarm_time
        holder.putName(mData[position].name)
        if (position == 0) {
            holder.progressBar.setIsFirst(true)
        } else {
            holder.progressBar.setIsFirst(false)
        }
        holder.update(System.currentTimeMillis())
        holder.alarmCheckBox.setChecked(mData[position].alarm_on)
        holder.setStartTime(mData[position].alarm_time)
        holder.progressBar.setColor(mData[position].color)
        holder.step = mData[position]
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.play_item, parent, false)
        return ViewHolder(view)
    }

    fun setData(newData: List<Interval>) {
        val diffResult = DiffUtil.calculateDiff(
            StepDiffUtilCallback(
                mData,
                newData
            )
        )
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
        PlayViewModel.ViewHolderCallback {

        var step: Interval? = null
        var myStart = 0L

        override fun onInitiated(colors: IntArray) {
            notifyDataSetChanged()
        }

        override fun onTimeUpdate(now: Long) {
            update(now)
        }

        val name: EmojiTextView = itemView.findViewById(R.id.play_name_field)
        private val startTime: EmojiTextView = itemView.findViewById(R.id.play_start_time_field)
        private val noteButton = itemView.findViewById<ImageButton>(R.id.play_notes_button)
        val progressBar: MyItemProgressBar = itemView.findViewById(R.id.my_progerss_bar)
        val alarmCheckBox: CustomCheckBox = itemView.findViewById(R.id.play_checkbox)

        init {
            viewModel.registerTimeObserver(this)
            noteButton.setOnClickListener {
                stepDetailCallback.onStepDialogCalled(mData[adapterPosition])
            }
            alarmCheckBox.setOnClickListener {
                when (alarmCheckBox.toggleChecked()) {
                    true -> setAlarm()
                    false -> cancelAlarm()
                }
            }
        }

        fun setStartTime(timeInMillis: Long) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeInMillis
            var hour = calendar.get(Calendar.HOUR)
            if (hour == 0) hour = 12
            val min = calendar.get(Calendar.MINUTE)
            val meridian = calendar.get(Calendar.AM_PM)
            val timeAsString =
                hour.toString() + ":" + DecimalFormat("00").format(min) + " " + MERIDIAN[meridian]
            startTime.text = timeAsString
        }

        private fun cancelAlarm() {
            step?.let {
                viewModel.updateInterval(alarmHelper.cancelAnAlarm(it, context))
            }
        }

        private fun setAlarm() {
            step?.let {
                viewModel.updateInterval(
                    alarmHelper.setSpecificAlarm(
                        it.alarm_time,
                        it,
                        context
                    )
                )
            }
        }

        fun update(now: Long) {
            this.progressBar.update(now, start, myStart)
            step?.let { interval ->
                val text = convertMillisToText(myStart - now)
                if (interval.alarm_on) {
                    this.progressBar.setMessage(text)
                } else {
                    if (text == context.getString(R.string.completed)) {
                        this.progressBar.setMessage(text)
                    } else {
                        this.progressBar.setMessage("Alarm Off!")
                    }
                }
            }
        }

        fun putName(name: String) {
            if (name == "") {
                this.name.text = context.getText(R.string.no_name_text)
            } else {
                this.name.text = name
            }
        }

        fun makeDuration(list: List<Interval>, position: Int): Long {
            var dur = 0L
            for (index in 0..position) {
                dur += list[index].span.toLong()
            }
            myStart = (start + (dur * 60000))
            return myStart
        }
    }

    fun convertMillisToText(millis: Long): String {
        if (millis > 0) {
            val days = millis / MILLIS_IN_DAY
            val hours = (millis % MILLIS_IN_DAY) / MILLIS_IN_HOUR
            val mins = (millis % MILLIS_IN_DAY % MILLIS_IN_HOUR) / MILLIS_IN_MINUTE
            val secs =
                (millis % MILLIS_IN_DAY % MILLIS_IN_HOUR % MILLIS_IN_MINUTE) / MILLIS_IN_SECOND
            var dayString = ""
            var hourString = ""
            var minString = ""
            var secString = ""
            if (days > 0L) {
                dayString = if (days > 1) {
                    "$days Days "
                } else {
                    "$days Day "
                }
            }
            if (hours > 0) {
                hourString = if (hours > 1) {
                    "$hours Hrs "
                } else {
                    "$hours Hr "
                }
            }
            if (mins > 0) {
                minString = if (mins > 1) {
                    "$mins Mins "
                } else {
                    "$mins Min "
                }
            }
            if (secs > 0) {
                secString = if (secs > 1) {
                    "$secs Secs "
                } else {
                    "$secs Sec "
                }
            }
            return context.getString(R.string.alarm_in, "$dayString$hourString$minString$secString")
        } else {
            return context.getString(R.string.completed)
        }
    }

    fun registerStepDialogWatcher(stepDetailwatcher: StepDetailCallback) {
        stepDetailCallback = stepDetailwatcher
    }

    interface StepDetailCallback {
        fun onStepDialogCalled(step: Interval)
    }

}