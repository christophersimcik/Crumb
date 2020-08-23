package com.example.crumb.BroadcastRecievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.crumb.Helpers.AlarmHelper
import com.example.crumb.Database.DatabaseScheduler
import com.example.crumb.ViewModels.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class AlarmRebootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val scope = CoroutineScope(Dispatchers.IO)
        val sharedPreferences = context?.getSharedPreferences(SharedViewModel.SHARED_PREFERENCES, 0)
        if (intent?.action.equals(Intent.ACTION_BOOT_COMPLETED) && context != null) {
            val database = DatabaseScheduler.getInstance(context)
            val intervalDao = database?.getIntervalDao()
            if (sharedPreferences != null) {
                val alarmHelper = AlarmHelper(sharedPreferences)
            }

        }
    }
}