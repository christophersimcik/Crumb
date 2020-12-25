package com.example.crumb.ViewModels

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.crumb.*
import com.example.crumb.Adapters.ScheduleAdapter
import com.example.crumb.Dao.IntervalDao
import com.example.crumb.Database.DatabaseScheduler
import com.example.crumb.Helpers.AlarmHelper
import com.example.crumb.Models.Interval
import com.example.crumb.Models.Schedule
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        val monthOfYearMap: Map<Int, String> = hashMapOf(
            1 to "Jan",
            2 to "Feb",
            3 to "Mar",
            4 to "Apr",
            5 to "May",
            6 to "Jun",
            7 to "Jul",
            8 to "Aug",
            9 to "Sep",
            10 to "Oct",
            11 to "Nov",
            12 to "Dec"
        )
    }

    private val database = DatabaseScheduler.getInstance(application)
    private val scheduleDao = database?.getScheduleDao()
    private val intervalDao: IntervalDao? = database?.getIntervalDao()
    val scheduleData = scheduleDao?.getAll()
    private val sharedPreferences = application.getSharedPreferences(SharedViewModel.SHARED_PREFERENCES,0)
    private val alarmHelper = AlarmHelper(sharedPreferences)
    val bundle = Bundle()
    private val totalsAvailable = ArrayList<TotalsCallback>()

    fun getTotals(id: Long, viewHolder : ScheduleAdapter.ViewHolder){
        viewModelScope.launch {
            val allPercentages = intervalDao?.getAllPercentages(id)
            val allcolors = intervalDao?.getAllColors(id)
            for(totals in totalsAvailable) {
                if(totals == viewHolder){
                    totals.onTotalsAvailable(allPercentages, allcolors)
                }
            }
        }
    }

    fun deleteRecipe(position : Int) {
        val schedule = scheduleData?.value?.get(position)
        if (schedule != null) {
            viewModelScope.launch {
                val list = intervalDao?.getAsList(schedule.id)
                if(list != null){
                    cancelAllAlarms(list)
                }
                scheduleDao?.delete(schedule)
            }
        }
    }

    private fun cancelAllAlarms(list : List<Interval>){
        for(step in list){
            if(step.alarm_on){
                alarmHelper.cancelAnAlarm(step,getApplication())
            }
        }
    }

    fun newSchedule(name: String, navController: NavController) {
        viewModelScope.launch {
            val count = scheduleDao?.getCount("No Name") ?: 0
            val schedule = createSchedule(count)
            val bundle = Bundle()
            schedule.name = name
            bundle.putLong("parent_id", schedule.id)
            scheduleDao?.insert(schedule)
            navController.navigate(
                R.id.action_scheduleFragment_to_intervalFragment,
                bundle
            )
        }
    }

    private fun createSchedule(count : Int): Schedule {
            val dtf = DateTimeFormat.forPattern(" dd, YYYY")
            val dateTime = DateTime.now()
            val id = System.currentTimeMillis()
            bundle.putLong("parent_id", id)
            val name = if(count > 0){"No Name" + " (# " + (count + 1) + ")"}else{"No Name"}
            return Schedule(
                id,
                name,
                monthOfYearMap[dateTime.monthOfYear] + dateTime.toString(
                    dtf
                ),
                "",
                0,
                0,
                0,
                0
            )
    }

    fun register(totalsAvailable: TotalsCallback) {
        this.totalsAvailable.add(totalsAvailable)
    }

    interface TotalsCallback {
        fun onTotalsAvailable(floats : List<Float>?, colors : List<Int>?)
    }

}