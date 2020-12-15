package com.example.crumb.ViewModels

import android.app.Application
import androidx.lifecycle.*
import com.example.crumb.Database.DatabaseScheduler
import com.example.crumb.Helpers.AlarmHelper
import com.example.crumb.Helpers.ColorHelper
import com.example.crumb.Models.Interval
import kotlinx.coroutines.launch

class IntervalViewModel(application: Application, id: Long) : AndroidViewModel(application) {


    private val database = DatabaseScheduler.getInstance(application)
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

    fun updateSelected(name: String, time: Int) {
        viewModelScope.launch {
            selected.name = name
            selected.time = time
            intervalDao?.update(selected)
            update()
        }
    }

    private suspend fun updateAllStepsInRecipe(list: List<Interval>) {
        intervalDao?.updateAll(list)
    }


    fun update() {
        viewModelScope.launch {
            val list = intervalDao?.getAsList(parentID)
            list?.let { recipe ->
                sortRecipe(recipe)
                updateEachStepInRecipe(recipe)
            }
            updateSchedule()
        }
    }

    private suspend fun updateEachStepInRecipe(list: List<Interval>) {
        val duration = getDuration(list)
        list.forEachIndexed { index, interval ->
            interval.span = if (index > 0) interval.time - list[index - 1].time else 0
            interval.sequence = index + 1
            interval.percentage = (interval.time - list.first().time) / duration.toFloat()
        }
        updateAllStepsInRecipe(list)
    }

    private fun getDuration(list: List<Interval>): Int {
        val difference = list.last().time - list.first().time
        return if (difference > 0) difference else 1
    }

    private fun sortRecipe(list: List<Interval>) {
        list.sortedBy { it.time }
    }

    private suspend fun updateSchedule() {
        val list = intervalDao?.getAsList(parentID) ?: emptyList()
        val schedule = scheduleDao?.getSelected(parentID)
        if (list.isNotEmpty()) {
            schedule?.steps = list.size
            schedule?.start = list.first().time
            schedule?.end = list.last().time
            schedule?.duration = list.last().time - list.first().time
        }
        if (schedule != null) scheduleDao?.updateSingle(schedule)
    }

    fun deleteNew() {
        viewModelScope.launch {
            intervalDao?.delete(selected)
        }
    }

    fun delete(position: Int) {
        intervalData?.value?.get(position)?.let { interval ->
            viewModelScope.launch {
                if (interval.alarm_on) {
                    alarmHelper.cancelAnAlarm(interval, getApplication())
                }
                intervalDao?.delete(interval)
                update()
            }
        }
    }

    fun create() {
        viewModelScope.launch {
            val newInterval = createInterval()
            intervalDao?.insert(newInterval)
            selected = newInterval
            selectedPosition = lastPositon
            update()
            val end = validateEndTime()
            listener.onCreated(end)
            newlyCreated = true
        }
    }

    private fun validateEndTime(): Int {
        return intervalData?.let { data ->
            data.value?.let { list ->
                if (list.isNotEmpty()) list.last().time else 0
            }
        }
            ?: 0
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