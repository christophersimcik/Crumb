package com.example.crumb.BroadcastRecievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.crumb.Helpers.AlarmHelper
import com.example.crumb.Dao.IntervalDao
import com.example.crumb.Database.DatabaseScheduler
import com.example.crumb.Models.Interval
import com.example.crumb.ViewModels.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmRebootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val scope = CoroutineScope(Dispatchers.IO)
        val sharedPreferences = context?.getSharedPreferences(SharedViewModel.SHARED_PREFERENCES, 0)
        if (intent?.action.equals(Intent.ACTION_BOOT_COMPLETED) && context != null) {
            System.out.println("MM REBOOTED")
            val database = DatabaseScheduler.getInstance(context)
            val intervalDao = database?.getIntervalDao()
            if (sharedPreferences != null) {
                System.out.println("MM has shared prefs")
                val alarmHelper = AlarmHelper(sharedPreferences)
                scope.launch {
                    val id = sharedPreferences.getLong(AlarmHelper.PARENT_ID, 0L)
                    val listOfSteps = getList(intervalDao, id)
                    System.out.println("MM steps size = " + listOfSteps?.size)
                    val listOfActiveAlarms = checkParentForActiveAlarms(listOfSteps)
                    alarmHelper.resetAlarms(listOfActiveAlarms, context, id)
                    System.out.println("MM in launch")
                }
            }

        }
    }

    private suspend fun getList(intervalDao: IntervalDao?, id: Long?): List<Interval>? {
        return intervalDao?.getAsList(id ?: 0L)
    }

    private fun checkParentForActiveAlarms(list: List<Interval>?): List<Interval> {
        val templist = ArrayList<Interval>()
        if (list != null) {
            for (item in list) {
                if (item.alarm_on && item.alarm_time < System.currentTimeMillis()) templist.add(item)
            }
        }
        return templist.toList()
    }
}