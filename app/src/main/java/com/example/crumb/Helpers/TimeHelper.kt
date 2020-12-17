package com.example.crumb.Helpers

import android.graphics.Color
import android.util.Log
import com.github.stephenvinouze.materialnumberpickercore.MaterialNumberPicker
import java.text.DecimalFormat

class TimeHelper(private val dayPicker : MaterialNumberPicker, private val hrPicker : MaterialNumberPicker, private val minPicker : MaterialNumberPicker, private val mrdnPicker : MaterialNumberPicker){

    private var days = 0
    private var hours = 0
    private var mins = 0
    private var meridian = 0
    private val amColor = "#e39207"
    private val pmColor = "#1f7de0"

    fun getMinutesFromViews() : Int {
        val daysAsMinutes= convertDaysToMinutes(dayPicker.value)
        val hoursAsMinutes = convertHoursToMinutes(mrdnPicker.value, hrPicker.value)
        val all = daysAsMinutes + hoursAsMinutes + minPicker.value - 1440
        Log.d("TIME_HLPR", "$all")
        return all

    }

    private fun convertHoursToMinutes(meridian : Int, hoursIn : Int) : Int {
        var minutesOut = 0
        when (meridian) {
            0 -> minutesOut = if (hoursIn == 12) 0 else hoursIn * 60
            1 -> minutesOut = if (hoursIn == 12) hoursIn * 60 else (hoursIn + 12) * 60
        }
        return minutesOut
    }

    private fun convertDaysToMinutes(days : Int) : Int {
       return 1440 * days
    }

    fun setValues(timeAsMinutes: Int) {
        getValues(timeAsMinutes)
        dayPicker.value = days
        hrPicker.value = hours
        minPicker.value = mins
        mrdnPicker.value = meridian
        if (meridian == 0) {
            mrdnPicker.separatorColor = Color.parseColor(amColor)
        } else {
            mrdnPicker.separatorColor = Color.parseColor(pmColor)
        }
    }

    private fun getValues(timeAsMinutes: Int) {
        days = (timeAsMinutes / 1440) + 1
        hours = (timeAsMinutes % 1440) / 60
        mins = timeAsMinutes % 1440 % 60
        meridian = 0

        if(hours >= 12) {hours -= 12; meridian = 1}
        if(hours == 0) 12
    }

    fun getHoursAsString() : String{
        return if(hours == 0) "12" else hours.toString()
    }

    fun getMinutesAsString() : String{
        val decimalFormat = DecimalFormat("00")
        return decimalFormat.format(mins)
    }

    fun getMeridanAsString() : String{
        return if(meridian == 0) "AM" else "PM"
    }

}