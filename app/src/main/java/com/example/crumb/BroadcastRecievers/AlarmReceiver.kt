package com.example.crumb.BroadcastRecievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PowerManager
import com.example.crumb.Helpers.AlarmHelper
import com.example.crumb.Services.AlarmService
import com.example.crumb.Database.DatabaseScheduler
import com.example.crumb.ViewModels.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val TIME_OUT = 3000L

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val powerManager = context?.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock: PowerManager.WakeLock =
            powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "crumb:WakeLock").apply {
                acquire(TIME_OUT)
            }
        val sharedPreferences = context.getSharedPreferences(SharedViewModel.SHARED_PREFERENCES, 0)
        val parentID = intent?.getLongExtra(AlarmHelper.PARENT_ID, 0) ?: 0L
        val myID = intent?.getLongExtra(AlarmHelper.MY_ID, 0L)
        val details = intent?.getBundleExtra(AlarmHelper.DETAILS) ?: Bundle()
        val database = DatabaseScheduler.getInstance(context)
        val intervalDao = database?.getIntervalDao()
        val scope = CoroutineScope(Dispatchers.IO)
        decrementActiveAlarms(sharedPreferences)
        scope.launch {
            intervalDao?.toggleAlarm(myID ?: 0L)
        }
        val alarmService = Intent(context, AlarmService::class.java)
        alarmService.putExtra(AlarmHelper.PARENT_ID, parentID)
        alarmService.putExtra(AlarmHelper.DETAILS, details)
        context.startService(alarmService)
        wakeLock.release()
    }

    private fun decrementActiveAlarms(sharedPreferences: SharedPreferences?) {
        if (sharedPreferences != null && sharedPreferences.contains(AlarmHelper.ACTIVE_ALARMS)) {
            val count = sharedPreferences.getInt(AlarmHelper.ACTIVE_ALARMS, 0) - 1
            sharedPreferences.edit().putInt(AlarmHelper.ACTIVE_ALARMS, count).apply()
        }
    }
}