package com.example.tightboules

import android.app.AlarmManager
import android.app.Application
import android.widget.Toast
import androidx.lifecycle.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeParser
import java.util.*

class IntervalViewModel(application: Application, id: Long) : AndroidViewModel(application) {

    val database = DatabaseScheduler.getInstance(application)
    val intervalDao = database?.getIntervalDao()
    val scheduleDao = database?.getScheduleDao()
    val parentID = id
    var lastTime = 0
    var selectedPosition = 0
    var lastPositon = 0
    val intervalData = intervalDao?.getAllIntervalsInSchedule(parentID)
    val alarmHelper = AlarmHelper(application.getSharedPreferences(SharedViewModel.SHARED_PREFERENCES, 0))
    lateinit var listener: CreationListener
    lateinit var selected: Interval
    val colorHelper = ColorHelper()
    var time: Int = 0

    fun updateSelected(name: String, time: Int, adapter: IntervalAdapter) {
        viewModelScope.launch {
            selected.name = name
            selected.time = time
            updateSingle(selected)
            updateSchedule()
            update()
            adapter.notifyDataSetChanged()
        }
    }

    fun updateSingle(interval: Interval) {
        viewModelScope.launch {
            intervalDao?.update(interval)
        }
    }

    fun update() {
        viewModelScope.launch {
            val list = intervalDao?.getAsList(parentID)
            if (!list.isNullOrEmpty()) {
                val start = (list?.first()?.time) ?: 0
                val end = (list?.last()?.time) ?: 0
                var duration = end - start
                if (duration == 0) {
                    duration = 1
                }
                var counter = 0
                var previousTime = (list?.first()?.time) ?: 0
                if (list != null) {
                    for (item in list) {
                        item.span = item.time - previousTime
                        item.sequence = counter + 1
                        item.percentage = (item.time.toFloat() - start) / duration.toFloat()
                        counter++
                    }
                    intervalDao?.updateAll(list)
                }
            }
        }
    }

    suspend fun updateSchedule() {
        val list = intervalDao?.getAsList(parentID)
        val schedule = scheduleDao?.getSelected(parentID)
        val steps = list?.size ?: 0
        val start = list?.get(0)?.time ?: 0
        val end = list?.get(steps - 1)?.time ?: 0
        val duration = end - start

        schedule?.steps = steps
        schedule?.start = start
        schedule?.end = end
        schedule?.duration = duration

        if (schedule != null) {
            scheduleDao?.updateSingle(schedule)
        }
    }

    fun delete(position: Int) {
        val interval = intervalData?.value?.get(position)
        if (interval != null) {
            viewModelScope.launch {
                if (interval.alarm_on) {
                    alarmHelper.cancelSpecificAlarm(interval, getApplication())
                }
                intervalDao?.delete(interval)
                update()
            }
        }
    }

    fun create() {
        viewModelScope.launch {
            val interval = createInterval()
            intervalDao?.insert(interval)
            selected = interval
            selectedPosition = lastPositon
            updateSchedule()
            update()
            val endTime = intervalData?.value?.last()
            listener.onCreated(endTime?.time ?: 0)
        }
    }

    private fun createInterval(): Interval {
        val id = System.currentTimeMillis()
        return Interval(
            id,
            parentID,
            0,
            "",
            "",
            0,
            0,
            0f,
            false,
            "",
            0,
            0L,
             colorHelper.aRandomColor()
        )
    }

    fun registerListener(listener: CreationListener) {
        this.listener = listener
    }

    interface CreationListener {
        fun onCreated(time: Int)
    }


}