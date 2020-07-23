package com.example.crumb

import android.graphics.Color
import android.widget.NumberPicker
import com.github.stephenvinouze.materialnumberpickercore.MaterialNumberPicker
import java.text.DecimalFormat

class TimeHelper(val dayPicker : NumberPicker, val hrPicker : NumberPicker, val minPicker : NumberPicker, val mrdnPicker : NumberPicker){

    private var days = 0
    private var hours = 0
    private var mins = 0
    private var meridian = 0
    private val meridianPicker = mrdnPicker as MaterialNumberPicker
    private val amColor = "#e39207"
    private val pmColor = "#1f7de0"

    fun getMinutesFromViews() : Int {
        val daysAsMinutes= convertDaysToMinutes(dayPicker.value)
        val hoursAsMinutes = convertHoursToMinutes(mrdnPicker.value, hrPicker.value)

        return daysAsMinutes + hoursAsMinutes + minPicker.value - 1440

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
            System.out.println("mrd = " + meridian)
            meridianPicker.separatorColor = Color.parseColor(amColor)
        } else {
            System.out.println("mrd = " + meridian)
            meridianPicker.separatorColor = Color.parseColor(pmColor)
        }
    }

    private fun getValues(timeAsMinutes: Int) {
        days = (timeAsMinutes / 1440) + 1
        hours = (timeAsMinutes % 1440) / 60
        mins = timeAsMinutes % 1440 % 60
        meridian = 0

        if(hours >= 12) {hours = hours - 12; meridian = 1}
        if(hours == 0) 12
    }

    fun getDaysAsString() : String{
        return days.toString()
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