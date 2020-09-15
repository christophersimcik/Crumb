package com.example.crumb.ViewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.crumb.Adapters.IntervalAdapter
import com.example.crumb.Database.DatabaseScheduler
import com.example.crumb.Helpers.AlarmHelper
import com.example.crumb.Helpers.ColorHelper
import com.example.crumb.Models.Interval
import kotlinx.coroutines.launch

class IntervalViewModel(application: Application, id: Long) : AndroidViewModel(application) {
    companion object{
        val TAG = "INTERVAL_VIEW_MODEL"
    }
    val database = DatabaseScheduler.getInstance(application)
    private val intervalDao = database?.getIntervalDao()
    private val scheduleDao = database?.getScheduleDao()
    private val parentID = id
    var lastTime = 0
    var selectedPosition = 0
    var lastPositon = 0
    var newlyCreated = false
    val intervalData = intervalDao?.getAllIntervalsInSchedule(parentID)
    private val alarmHelper = AlarmHelper(
        application.getSharedPreferences(
            SharedViewModel.SHARED_PREFERENCES,
            0
        )
    )
    private lateinit var listener: CreationListener
    lateinit var selected: Interval
    private val colorHelper = ColorHelper()
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

    fun updateAll(){
        viewModelScope.launch {
            updateSingle(selected)
            update()
            updateSchedule()
        }
    }

    private suspend fun updateSingle(interval: Interval) {
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
                var previousTime = (list.first().time)
                    for ((counter, item) in list.withIndex()) {
                        item.span = item.time - previousTime
                        item.sequence = counter + 1
                        item.percentage = (item.time.toFloat() - start) / duration.toFloat()
                        previousTime = item.time
                        Log.i(TAG,"sequence is ${item.sequence}")
                    }
                    intervalDao?.updateAll(list)
        }
    }

    private suspend fun updateSchedule() {
        val list = intervalDao?.getAsList(parentID) ?: emptyList()
        val schedule = scheduleDao?.getSelected(parentID)
        if(list.isNotEmpty()) {
            val steps = list.size
            val start = list.first().time
            val end = list.last().time
            val duration = end - start

            schedule?.steps = steps
            schedule?.start = start
            schedule?.end = end
            schedule?.duration = duration
        }

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
        if (intervalData?.value?.isNullOrEmpty() != false) {
            return
        } else {
            val interval = intervalData.value?.get(position)
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