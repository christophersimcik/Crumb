package com.example.crumb

import kotlin.math.abs

fun Int.addMinute() : Int {return this + 1 }

fun String.getDuration(end: String): String{
    return abs(end.toLong() - this.toLong()).toString()
}

fun String.subtractString(string: String): Long{
    return (this.toLong() - string.toLong())
}