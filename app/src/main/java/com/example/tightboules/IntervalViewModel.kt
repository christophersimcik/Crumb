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
    var newlyCreated = false
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
            update()
            updateSchedule()
        }
    }

    suspend fun updateSingle(interval: Interval) {
            intervalDao?.update(interval)
    }

    fun updateName(interval : Interval){
        viewModelScope.launch {
            intervalDao?.update(interval)
        }
    }

    suspend fun update() {
            val list = intervalDao?.getAsList(parentID)
            if (!list.isNullOrEmpty()) {
                val start = (list.first().time)
                val end = (list.last().time)
                var duration = end - start
                if (duration == 0) { duration = 1 }
                var counter = 0
                var previousTime = (list.first().time)
                    for (item in list) {
                        item.span = item.time - previousTime
                        item.sequence = counter + 1
                        item.percentage = (item.time.toFloat() - start) / duration.toFloat()
                        previousTime = item.time
                        counter++
                    }
                    intervalDao?.updateAll(list)
        }
    }

    suspend fun updateSchedule() {
        val list = intervalDao?.getAsList(parentID)
        val schedule = scheduleDao?.getSelected(parentID)
        val steps = list?.size ?: 0
        val start = list?.first()?.time ?: 0
        val end = list?.last()?.time ?: 0
        val duration = end - start

        schedule?.steps = steps
        schedule?.start = start
        schedule?.end = end
        schedule?.duration = duration

        if (schedule != null) {
            scheduleDao?.updateSingle(schedule)
        }
    }

    fun deleteNew(){
        viewModelScope.launch{
            intervalDao?.delete(selected)
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
                updateSchedule()
            }
        }
    }

    fun create() {
        viewModelScope.launch {
            val interval = createInterval()
            intervalDao?.insert(interval)
            selected = interval
            selectedPosition = lastPositon
            update()
            updateSchedule()
            val endTime = intervalData?.value?.last()
            listener.onCreated(endTime?.time ?: 0)
            newlyCreated = true
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