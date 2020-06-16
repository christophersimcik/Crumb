package com.example.tightboules

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import org.joda.time.DateTime
import java.util.*
import kotlin.collections.ArrayList

class AlarmHelper(sharedPreferences: SharedPreferences) {

    companion object {
        val MY_ID = "my_id"
        val NAME = "name"
        val DETAILS = "details"
        val PARENT_ID = "parent_id"
        val START_TIME = "start_time"
        val DESCRIPTION = "description"
        val ACTIVE_ALARMS = "active_alarms"
        val ALARM_IS_ACTIVE = "alarm_is_active"
    }

    private var alarmManager: AlarmManager? = null
    private val sharedPreferences = sharedPreferences
    private val bundles = ArrayList<Bundle>()

    fun cancelSpecificAlarm(step: Interval, context: Context): Interval {
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val alarmIntent = PendingIntent.getBroadcast(
            context,
            step.pending_intent_id,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        )
        if (alarmManager != null) {
            alarmManager!!.cancel(alarmIntent)
        }
        step.alarm_on = false
        val count = sharedPreferences.getInt(ACTIVE_ALARMS,0)
        if(count > 1) {
            sharedPreferences.edit().putInt(ACTIVE_ALARMS, count - 1).apply()
        }else{
            sharedPreferences.edit().putInt(ACTIVE_ALARMS, 0).apply()
            sharedPreferences.edit().putLong(PARENT_ID,0L).apply()
        }
        return step
    }

    fun setSpecificAlarm(interval : Interval, context: Context): Interval {
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val alarmIntent = PendingIntent.getBroadcast(context, interval.pending_intent_id, intent, PendingIntent.FLAG_ONE_SHOT)
        alarmManager?.set(
            AlarmManager.RTC_WAKEUP,
            interval.alarm_time,
            alarmIntent
        )
        interval.alarm_on = true
        sharedPreferences.edit().putLong(PARENT_ID,interval.parentId).apply()
        val count = sharedPreferences.getInt(ACTIVE_ALARMS,0)
        sharedPreferences.edit().putInt(ACTIVE_ALARMS, count +1).apply()
        return interval
    }

    fun setAlarms(list: List<Interval>, context: Context, parentID: Long): List<Interval> {
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendars = setCalendars(list)
        for (i in 0..calendars.size - 1) {
            val counter = System.currentTimeMillis().toInt() + 1
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.putExtra(PARENT_ID, parentID)
            intent.putExtra(MY_ID, list.get(i).id)
            intent.putExtra(DETAILS, bundles[i])
            list.get(i).alarm_on = true
            list.get(i).intent_id = ""
            list.get(i).alarm_time = calendars[i].timeInMillis
            list.get(i).pending_intent_id = counter
            val alarmIntent = PendingIntent.getBroadcast(context, counter, intent, PendingIntent.FLAG_ONE_SHOT)
            alarmManager?.setWindow(AlarmManager.RTC_WAKEUP, list.get(i).alarm_time,1000, alarmIntent)
            sharedPreferences.edit().putLong(PARENT_ID,parentID).apply()
            val count = sharedPreferences.getInt(ACTIVE_ALARMS,0)
            sharedPreferences.edit().putInt(ACTIVE_ALARMS, count +1).apply()
        }
        return list
    }


    private fun setCalendars(list: List<Interval>): ArrayList<Calendar> {
        val calendars = ArrayList<Calendar>()
        if (!list.isEmpty()) {
            val myTime = DateTime.now()
            val now = myTime.minuteOfDay
            val passed = checkIfPassed(now, list[0].time)
            var startTime: DateTime
            var targetTime: DateTime
            if (passed) {
                startTime =
                    DateTime.now().withTimeAtStartOfDay().plusMinutes(list.get(0).time).plusDays(1)
            } else {
                startTime = DateTime.now().withTimeAtStartOfDay().plusMinutes(list.get(0).time)
            }
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.DATE, startTime.dayOfMonth)
                set(Calendar.HOUR_OF_DAY, startTime.hourOfDay)
                set(Calendar.MINUTE, startTime.minuteOfHour)
                set(Calendar.SECOND, 0)
            }
            calendars.add(calendar)
            bundles.add(createBundle(list[0]))
            //nklh f
            sharedPreferences.edit().putLong(START_TIME, calendar.timeInMillis).commit()
            for (item in 1..list.size - 1) {
                targetTime = startTime.plusMinutes(list[item].span)
                val myCalendar = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                    set(Calendar.DATE, targetTime.dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, targetTime.hourOfDay)
                    set(Calendar.MINUTE, targetTime.minuteOfHour)
                    set(Calendar.SECOND, 0)
                }
                calendars.add(myCalendar)
                bundles.add(createBundle(list[item]))
                startTime = targetTime
                System.out.println("MILLIS" + targetTime.toDateTime().millis)
            }
        }
        return calendars
    }

    fun createBundle(interval: Interval): Bundle {
        val bundle = Bundle()
        bundle.putString(NAME, interval.name)
        bundle.putString(DESCRIPTION, interval.notes)
        return bundle
    }

    fun checkIfPassed(now: Int, target: Int): Boolean {
        if (now > target) {
            return true
        } else {
            return false
        }
    }
}