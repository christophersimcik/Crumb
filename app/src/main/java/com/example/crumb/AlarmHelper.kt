package com.example.crumb

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.util.*
import kotlin.collections.ArrayList

class AlarmHelper(sharedPreferences: SharedPreferences) {

    companion object {
        val NAME = "name"
        val MY_ID = "my_id"
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
        val intervalDao = DatabaseScheduler.getInstance(context)?.getIntervalDao() as IntervalDao
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch { intervalDao.toggleAlarm(step.id) }
        val intent = Intent(context, AlarmReceiver::class.java)
        val alarmIntent = PendingIntent.getBroadcast(
            context,
            step.pending_intent_id,
            intent,0
        )
        if (alarmManager != null) {
            alarmManager!!.cancel(alarmIntent)
        }
        step.alarm_on = false
        val count = sharedPreferences.getInt(ACTIVE_ALARMS, 0)
        if (count > 1) {
            sharedPreferences.edit().putInt(ACTIVE_ALARMS, count - 1).commit()
        } else {
            sharedPreferences.edit().putInt(ACTIVE_ALARMS, 0).commit()
        }
        return step
    }

    fun setSpecificAlarm(interval: Interval, context: Context): Interval {
        if(!interval.alarm_on) {
            alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val startTime = DateTime.now().withTimeAtStartOfDay().plusMinutes(interval.time)
            val calendar = Calendar.getInstance().apply { timeInMillis = startTime.millis }
            val pendingIntentID = (System.currentTimeMillis() + 1).toInt()
            val intent = Intent(context, AlarmReceiver::class.java)
            val myParentId: Long = interval.parentId
            intent.putExtra(MY_ID, interval.id)
            intent.putExtra(PARENT_ID, myParentId)
            intent.putExtra(DETAILS, createBundle(interval))
            val alarmIntent = PendingIntent.getBroadcast(context, pendingIntentID, intent, 0)
            alarmManager?.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmIntent)
            interval.alarm_on = true
            interval.pending_intent_id = pendingIntentID
            interval.alarm_time = calendar.timeInMillis
            sharedPreferences.edit().putLong(PARENT_ID, interval.parentId).commit()
            val count = sharedPreferences.getInt(ACTIVE_ALARMS, 0)
            sharedPreferences.edit().putInt(ACTIVE_ALARMS, count + 1).commit()
        }
        return interval
    }

    fun setAlarms(list: List<Interval>, context: Context, parentID: Long): List<Interval> {
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendars = setCalendars(list)
        for (i in 0 until calendars.size) {
            if(!list.get(i).alarm_on) {
                val counter = System.currentTimeMillis().toInt() + 1
                val intent = Intent(context, AlarmReceiver::class.java)
                intent.putExtra(PARENT_ID, parentID)
                intent.putExtra(MY_ID, list.get(i).id)
                intent.putExtra(DETAILS, bundles[i])
                list.get(i).alarm_on = true
                list.get(i).intent_id = ""
                list.get(i).alarm_time = calendars[i].timeInMillis
                list.get(i).pending_intent_id = counter
                val alarmIntent = PendingIntent.getBroadcast(context, counter, intent, 0)
                val build = android.os.Build.VERSION.SDK_INT
                System.out.println("buildtype = " + build)
                if(build >= 23){
                    alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, list.get(i).alarm_time, alarmIntent)
                    System.out.println("buildtype >= 23 " + build)
                }else{
                    alarmManager?.setExact(AlarmManager.RTC_WAKEUP, list.get(i).alarm_time, alarmIntent)
                    System.out.println("buildtype < 23 " + build)
                }
                sharedPreferences.edit().putLong(PARENT_ID, parentID).commit()
                val count = sharedPreferences.getInt(ACTIVE_ALARMS, 0)
                sharedPreferences.edit().putInt(ACTIVE_ALARMS, count + 1).commit()
            }
        }
        return list
    }

    fun resetAlarms(list: List<Interval>, context: Context, parentID: Long): List<Interval> {
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendars = setCalendars(list)
        for (i in 0 until calendars.size) {
            val counter = System.currentTimeMillis().toInt() + 1
            val intent = Intent(context, AlarmReceiver::class.java)
            intent.putExtra(PARENT_ID, parentID)
            intent.putExtra(MY_ID, list.get(i).id)
            intent.putExtra(DETAILS, bundles[i])
            list.get(i).alarm_on = true
            list.get(i).intent_id = ""
            list.get(i).alarm_time = calendars[i].timeInMillis
            list.get(i).pending_intent_id = counter
            val alarmIntent = PendingIntent.getBroadcast(context, counter, intent, 0)
            val build = android.os.Build.VERSION.SDK_INT
            if(build >= 23){
                alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, list.get(i).alarm_time, alarmIntent)
            }else{
                alarmManager?.setExact(AlarmManager.RTC_WAKEUP, list.get(i).alarm_time, alarmIntent)
            }
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
            sharedPreferences.edit().putLong(START_TIME, calendar.timeInMillis).commit()
            for (item in 1 until list.size) {
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
            }
        }
        return calendars
    }

    fun createBundle(interval: Interval): Bundle {
        val bundle = Bundle()
        System.out.println("name = " + interval.name)
        System.out.println("notes = " + interval.notes)
        val name = if(interval.name.equals("")) "No Name" else interval.name
        val note = if(interval.notes.equals("")) "No Notes" else interval.notes
        bundle.putString(NAME, name)
        bundle.putString(DESCRIPTION, note)
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