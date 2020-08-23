package com.example.crumb.Helpers

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.core.app.NotificationCompat
import com.example.crumb.R
import com.example.crumb.Services.AlarmService

class NotificationHelper(
    val context: Context
) {

    private val channelID : String = context.resources.getString(R.string.channel_present_and_soung_id)
    private val channelName : String = context.resources.getString(R.string.channel_present_and_sound)
    private val channelDescr : String = context.resources.getString(R.string.channel_present_and_sound_description)
    init{
        createNotificationChannel()
    }

    @TargetApi(26)
    private fun createNotificationChannel(){
            val id = channelID
            val name = channelName
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(id,name,importance)
            mChannel.description = channelDescr
            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
    }

    private fun makeBasicBackgroundNotification(title : String, text : String) : Notification {
        val dismissText = SpannableString("Dismiss")
        dismissText.setSpan(ForegroundColorSpan(context.resources.getColor(R.color.red_accent,null)),0, dismissText.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        val launchText = SpannableString("Launch")
        launchText.setSpan(ForegroundColorSpan(context.resources.getColor(R.color.pm_color,null)),0, launchText.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        val builder = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.crumb_notification_icon)
            .setContentTitle(title)
            .setContentText(text)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setStyle(NotificationCompat.BigTextStyle().bigText(""))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(R.drawable.cancel_alarm_icon, dismissText, createServiceCancelingPendingIntent())
            .addAction(R.drawable.launch_icon, launchText, createActivityLaunchingPendingIntent())
        return builder.build()
    }

    private fun makeBasicForegroundNotification(title : String, text : String) : Notification {
        val dismissText = SpannableString("Dismiss")
        dismissText.setSpan(ForegroundColorSpan(context.resources.getColor(R.color.red_accent,null)),0, dismissText.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        val builder = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.crumb_notification_icon)
            .setContentTitle(title)
            .setContentText(text)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setStyle(NotificationCompat.BigTextStyle().bigText(""))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(R.drawable.cancel_alarm_icon, dismissText, createServiceCancelingPendingIntent())
        return builder.build()
    }

    fun getBasicBackgroundNotification(title : String, text : String) : Notification {
        return makeBasicBackgroundNotification(title, text)
    }

    fun getBasicForegroundNotification(title : String, text : String) : Notification {
        return makeBasicForegroundNotification(title, text)
    }

    private fun createServiceCancelingPendingIntent() : PendingIntent{
        val serviceCancelingIntent = Intent(context, AlarmService::class.java)
        serviceCancelingIntent.action = AlarmService.ACTION_STOP_SERVICE
        return PendingIntent.getService(context, 0, serviceCancelingIntent,0)
    }

    private fun createActivityLaunchingPendingIntent() : PendingIntent{
        val serviceCancelingIntent = Intent(context, AlarmService::class.java)
        serviceCancelingIntent.action = AlarmService.ACTION_STOP_SERVICE_LAUNCH_APP
        return PendingIntent.getService(context, 0, serviceCancelingIntent,0)
    }
}