package com.example.crumb.ViewModels

import android.app.Application
import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.crumb.Adapters.PlayAdapter
import com.example.crumb.Dao.IntervalDao
import com.example.crumb.Database.DatabaseScheduler
import com.example.crumb.Dialogs.AlarmDialog
import com.example.crumb.Fragments.PlayFragment
import com.example.crumb.Helpers.AlarmHelper
import com.example.crumb.Models.Interval
import com.example.crumb.UI.MyProgressBar
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class PlayViewModel(application: Application, val id: Long) : AndroidViewModel(application) {

    companion object {
        const val SECOND_AS_MILLIS = 60000L
        const val MINUTES_IN_DAY = 1440
        const val MINUTES_IN_HOUR = 60
    }

    private val database = DatabaseScheduler.getInstance(application)
    private val scheduleDao = database?.getScheduleDao()
    private val intervalDao: IntervalDao? = database?.getIntervalDao()
    val recipeData = scheduleDao?.getSelectedAsLiveData(id)
    val stepData = intervalDao?.getAllIntervalsInSchedule(id)

    private val alarmHelper = AlarmHelper(
        application.getSharedPreferences(
            SharedViewModel.SHARED_PREFERENCES,
            0
        )
    )
    private val observers = ArrayList<ViewHolderCallback>()
    private var dataRetrieved: Boolean = false
    private val alarmDialog = AlarmDialog()
    private lateinit var alarmCancelObserver: AlarmCancelObserver
    private lateinit var activeAlarmsWatcher: ActiveAlarms

    val relativeEndTime = MutableLiveData<String>()
    val notes = MutableLiveData<String>()
    var duration: Int = 0
    var start = 0L

    fun updateTotal(list: List<Interval>, view: MyProgressBar) {
        viewModelScope.launch {
            duration = scheduleDao?.getDuration(id) ?: 0
            view.initialize(list)
            dataRetrieved = true
        }
    }

    fun getIntervalNotes(step: Interval) {
        viewModelScope.launch {
            notes.postValue(intervalDao?.getNotes(step.id))
        }
    }

    fun hasActiveAlarms() {
        if (this::activeAlarmsWatcher.isInitialized) {
            viewModelScope.launch {
                val countOfAlarms = intervalDao?.getAlarmCount(id) ?: 0
                if (countOfAlarms > 0) {
                    activeAlarmsWatcher.hasActiveAlarms()
                } else {
                    activeAlarmsWatcher.noActiveAlarms()
                }
            }
        }
    }

    fun updateInterval(interval: Interval) {
        viewModelScope.launch {
            intervalDao?.update(interval)
        }
    }

    fun getScheduleNotes() {
        viewModelScope.launch {
            notes.postValue(scheduleDao?.getNotes(id) ?: "No Notes")
        }
    }

    fun computeCurrentPositon(view: MyProgressBar) {
        val now = Calendar.getInstance().timeInMillis
        val end = (start + (duration * SECOND_AS_MILLIS))
        val relativeStartTime = start - now//* SECOND_AS_MILLIS
        if (relativeStartTime > 0) {
            view.active = false
            relativeEndTime.postValue("* Recipe Is Not In Progress Yet!")
            val message = convertMillisToText(relativeStartTime)
            view.setMessage(message)
        } else {
            view.active = true
            view.setMessage("")
            if (end - now > 0) {
                relativeEndTime.postValue(convertMillisToText(end - now))
            } else {
                relativeEndTime.postValue("Completed!")
                view.active = false
            }
        }
        view.update(now, start, end)
        for (observer in observers) {
            observer.onTimeUpdate(now)
        }
    }

    interface ViewHolderCallback {
        fun onTimeUpdate(now: Long)
        fun onInitiated(colors: IntArray)
    }

    fun registerTimeObserver(viewHolderCallback: ViewHolderCallback) {
        observers.add(viewHolderCallback)
    }

    fun initializeAlarmDialog(fragment: PlayFragment) {
        alarmDialog.setTargetFragment(fragment, 0)
        alarmDialog.isCancelable = false
    }

    fun showAlarmDialog(fragmentManager: FragmentManager, name: String, description: String) {
        alarmDialog.show(fragmentManager, "alarm", name, description)
    }

    fun dismissDialog() {
        alarmDialog.dismiss()
    }

    fun cancel() {
        viewModelScope.launch {
            val list: List<Interval>? = if (stepData?.value == null) {
                intervalDao?.getAsList(id)
            } else {
                stepData.value
            }
            if (list != null) {
                cancelAllAlarms(list)
                intervalDao?.updateAll(list)
            }
        }
        alarmCancelObserver.onAlarmCancelled()
    }

    private fun cancelAllAlarms(list: List<Interval>) {
        for (step in list) {
            if (step.alarm_on) {
                alarmHelper.cancelAnAlarm(step, getApplication())
                step.alarm_on = false
                step.alarm_time = 0L
            }
        }
    }

    private fun convertMillisToText(millis: Long): String {
        Log.d("PLY ADAPTER", "millis = $millis")
        val days = millis / PlayAdapter.MILLIS_IN_DAY
        val hours = (millis % PlayAdapter.MILLIS_IN_DAY) / PlayAdapter.MILLIS_IN_HOUR
        val mins = (millis % PlayAdapter.MILLIS_IN_DAY % PlayAdapter.MILLIS_IN_HOUR) / PlayAdapter.MILLIS_IN_MINUTE
        val secs = (millis % PlayAdapter.MILLIS_IN_DAY % PlayAdapter.MILLIS_IN_HOUR % PlayAdapter.MILLIS_IN_MINUTE) / PlayAdapter.MILLIS_IN_SECOND
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
        return dayString + hourString + minString + secString
    }

    fun convertMinutesToText(minutes: Int): String {
        var dayString = ""
        var hourString = ""
        var minString = ""

        val days: Int = minutes / MINUTES_IN_DAY
        val hours: Int = minutes % MINUTES_IN_DAY / MINUTES_IN_HOUR
        val mins: Int = minutes % MINUTES_IN_DAY % MINUTES_IN_HOUR

        if (days > 0) {
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
        return dayString + hourString + minString
    }

    interface AlarmCancelObserver {
        fun onAlarmCancelled()
    }

    fun registerActiveAlarmsWatcher(activeAlarmsWatcher: ActiveAlarms) {
        this.activeAlarmsWatcher = activeAlarmsWatcher
    }

    fun registerAlarmCancelObserver(alarmCancelObserver: AlarmCancelObserver) {
        this.alarmCancelObserver = alarmCancelObserver
    }

    interface ActiveAlarms {
        fun hasActiveAlarms()
        fun noActiveAlarms()
    }
}
