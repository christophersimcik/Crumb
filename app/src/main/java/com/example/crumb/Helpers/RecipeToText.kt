package com.example.crumb.Helpers

import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import com.example.crumb.Models.Interval
import java.text.DecimalFormat
import kotlin.math.floor

class RecipeToText(val list : List<Interval>, private val colors : IntArray) {

    private val stringBuilder = SpannableStringBuilder()
    private var counter = 1
    private var cursor = 0

    fun getText() : String{
        stringBuilder.clear()
        counter = 1
        for(l in list){
            stringBuilder.append(counter.toString()+". " + getName(l) + "\n")
            stringBuilder.append("Start on " + getDay(l) + " at " + getTime(l) + "\n")
            stringBuilder.append("\n")
            cursor = stringBuilder.length-1
            counter++
        }
        stringBuilder.append("\n")
        return stringBuilder.toString()
    }

    private fun getName(interval : Interval) : String{
        return if(interval.name == "") "No Name" else interval.name
    }

    private fun getDay(interval : Interval): String {
        val minutes = interval.time
        return "Day " + (floor((minutes / 1440).toDouble()).toInt() + 1).toString()
    }

    private fun getTime(interval : Interval): SpannableString {
        val minutes = interval.time
        val decimalFormat = DecimalFormat("00")
        var hours = floor((minutes % 1440.0) / 60.0).toInt()
        var meridian = "AM"
        val mins = floor((minutes % 1440) % 60.0).toInt()
        if (hours >= 12) {
            meridian = "PM"
            hours -= 12
        }
        if (hours == 0) {
            hours = 12
        }

        val color: Int

        color = when (meridian) {
            "AM" -> colors[0]
            "PM" -> colors[1]
            else -> colors[2]
        }

        val spannableString =
            SpannableString(hours.toString() + ":" + decimalFormat.format(mins) + " " + meridian)
        spannableString.setSpan(
            RelativeSizeSpan(.80f),
            spannableString.length - 2,
            spannableString.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(color),
            spannableString.length - 2,
            spannableString.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        return spannableString
    }


}