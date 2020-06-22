package com.example.crumb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.media.RingtoneManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver() : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val sharedPreferences = context?.getSharedPreferences(SharedViewModel.SHARED_PREFERENCES, 0)
        val ringtone = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
        val newIntent = Intent(context, MainActivity::class.java);
        val parentID = intent?.getLongExtra(AlarmHelper.PARENT_ID, 0L)
        val myID = intent?.getLongExtra(AlarmHelper.MY_ID, 0L)
        val details = intent?.getBundleExtra(AlarmHelper.DETAILS)
        val database = DatabaseScheduler.getInstance(context!!)
        val intervalDao = database?.getIntervalDao()
        val scope = CoroutineScope(Dispatchers.IO)
        val mediaPlayer = MediaPlayer.create(context, ringtone)
        decrementActiveAlarms(sharedPreferences)
        scope.launch {
            intervalDao?.toggleAlarm(myID ?: 0L)
        }
        newIntent.putExtra(AlarmHelper.PARENT_ID, parentID)
        System.out.println("p id = " + parentID)
        newIntent.putExtra(AlarmHelper.DETAILS, details)
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(newIntent)
        mediaPlayer.start()
        mediaPlayer.release()
    }

    fun decrementActiveAlarms(sharedPreferences: SharedPreferences?) {
        if (sharedPreferences != null && sharedPreferences.contains(AlarmHelper.ACTIVE_ALARMS)) {
            val count = sharedPreferences.getInt(AlarmHelper.ACTIVE_ALARMS, 0) - 1
            sharedPreferences.edit().putInt(AlarmHelper.ACTIVE_ALARMS, count).commit()
        }
    }
}