package com.example.crumb.Helpers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import com.example.crumb.BroadcastRecievers.AlarmReceiver
import com.example.crumb.Dao.IntervalDao
import com.example.crumb.Database.DatabaseScheduler
import com.example.crumb.Models.Interval
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.util.*
import kotlin.collections.ArrayList

// shared pref requires context ?! possible memory leak !!!!
class AlarmHelper(private val sharedPreferences: SharedPreferences) {

    companion object {
        const val NAME = "name"
        const val MY_ID = "my_id"
        const val DETAILS = "details"
        const val PENDING_INTENT_ID = "pending_intent_id"
        const val PARENT_ID = "parent_id"
        const val START_TIME = "start_time"
        const val DESCRIPTION = "description"
        const val ACTIVE_ALARMS = "active_alarms"
        const val ALARM_IS_ACTIVE = "alarm_is_active"
        const val TAG = "ALARM_HELPER"
    }

    private var alarmManager: AlarmManager? = null
    private val bundles = ArrayList<Bundle>()

    fun cancelAnAlarm(interval: Interval, context: Context): Interval {
        val intervalDao = DatabaseScheduler.getInstance(context)?.getIntervalDao() as IntervalDao
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        //turn off alarm
        CoroutineScope(Dispatchers.IO).launch { intervalDao.toggleAlarm(interval.id) }
        val intent = Intent(context, AlarmReceiver::class.java)
        val alarmIntent = PendingIntent.getBroadcast(
            context,
            interval.pending_intent_id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager?.cancel(alarmIntent)
        decrementActiveAlarms()
        return interval.also { it.alarm_on = false }
    }


    private fun decrementActiveAlarms() {
        val numberOfActiveAlarms = (sharedPreferences.getInt(ACTIVE_ALARMS, 0) - 1).coerceAtLeast(0)
        sharedPreferences.edit().putInt(ACTIVE_ALARMS, numberOfActiveAlarms).apply()
    }

    private fun incrementActiveAlarms() {
        val numberOfActiveAlarms = sharedPreferences.getInt(ACTIVE_ALARMS, 0) + 1
        sharedPreferences.edit().putInt(ACTIVE_ALARMS, numberOfActiveAlarms).apply()
    }


    fun setSpecificAlarm(start: Long, interval: Interval, context: Context): Interval {
        if (!interval.alarm_on) {
            alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val calendar = Calendar.getInstance().apply { timeInMillis = start }
            val id = getNewPendingIntentId()
            val intent = Intent(context, AlarmReceiver::class.java)
            val myParentId: Long = interval.parentId
            intent.putExtra(MY_ID, interval.id)
            intent.putExtra(PARENT_ID, myParentId)
            intent.putExtra(DETAILS, createBundle(interval))
            val alarmIntent = PendingIntent.getBroadcast(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager?.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmIntent)
            interval.alarm_on = true
            interval.pending_intent_id = id
            interval.alarm_time = calendar.timeInMillis
            sharedPreferences.edit().putLong(PARENT_ID, interval.parentId).apply()
            incrementActiveAlarms()
        }
        return interval
    }

    fun setAlarms(
        time: Long,
        list: List<Interval>,
        context: Context,
        parentID: Long
    ): List<Interval> {
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val scheduleDao = DatabaseScheduler.getInstance(context)?.getScheduleDao()
        val calendars = setCalendars(time, list)
        calendars.forEach {
            Log.d(TAG, "cal ${it.time}")
        }
        CoroutineScope(Dispatchers.IO).launch {
            scheduleDao?.updateStartTime(parentID, calendars.first().timeInMillis)
        }
        list.forEachIndexed { index, interval ->
            // if (interval.alarm_on) {
            val counter = System.currentTimeMillis().toInt() + 1
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.putExtra(PARENT_ID, parentID)
            intent.putExtra(MY_ID, interval.id)
            intent.putExtra(DETAILS, bundles[index])
            interval.alarm_on = true
            interval.intent_id = ""
            interval.alarm_time = calendars[index].timeInMillis
            interval.pending_intent_id = counter
            val alarmIntent = PendingIntent.getBroadcast(context, counter, intent, 0)
            val build = android.os.Build.VERSION.SDK_INT
            if (build >= 23) {
                alarmManager?.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    interval.alarm_time,
                    alarmIntent
                )
            } else {
                alarmManager?.setExact(
                    AlarmManager.RTC_WAKEUP,
                    interval.alarm_time,
                    alarmIntent
                )
            }
            sharedPreferences.edit().putLong(PARENT_ID, parentID).apply()
            val count = sharedPreferences.getInt(ACTIVE_ALARMS, 0)
            sharedPreferences.edit().putInt(ACTIVE_ALARMS, count + 1).apply()
        }

        return list
    }

    private fun setCalendars(time: Long, list: List<Interval>): ArrayList<Calendar> {
        val calendars = ArrayList<Calendar>()
        if (list.isNotEmpty()) {
            val currentMinute = DateTime.now().minuteOfDay
            val isPast = checkIfPassed(currentMinute, DateTime(time).minuteOfDay)
            val startTime = if (isPast) {
                DateTime.now().withTimeAtStartOfDay().plusDays(1)
            } else {
                DateTime.now().withTimeAtStartOfDay()
            }
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, startTime.year)
                set(Calendar.DATE, startTime.dayOfMonth)
                set(Calendar.HOUR_OF_DAY, startTime.hourOfDay)
                set(Calendar.MINUTE, startTime.minuteOfHour)
                set(Calendar.SECOND, 0)
            }
            sharedPreferences.edit().putLong(START_TIME, calendar.timeInMillis).apply()
            list.forEach {
                val alarm = startTime.plusMinutes(it.time)
                Log.d(TAG, "alarm mills = ${alarm.millis}")
                calendars.add(Calendar.getInstance().also { calendar ->
                    calendar.timeInMillis = alarm.millis
                })
                bundles.add(createBundle(it))
            }
        }
        return calendars
    }

    private fun createBundle(interval: Interval): Bundle {
        val bundle = Bundle()
        val name = if (interval.name == "") "No Name" else interval.name
        val note = if (interval.notes == "") "No Notes" else interval.notes
        bundle.putString(NAME, name)
        bundle.putString(DESCRIPTION, note)
        return bundle
    }

    private fun checkIfPassed(now: Int, target: Int): Boolean {
        Log.d(TAG, "now = $now and target = $target")
        return now > target
    }


    private fun getNewPendingIntentId(): Int {
        val id = sharedPreferences.getInt(PENDING_INTENT_ID, 1)
        val nextID = if (id + 1 < Int.MAX_VALUE) id + 1 else 1
        incrementPendingIntentId(nextID)
        return nextID
    }

    private fun incrementPendingIntentId(id: Int) {
        sharedPreferences.edit().putInt(PENDING_INTENT_ID, id).apply()
    }

}