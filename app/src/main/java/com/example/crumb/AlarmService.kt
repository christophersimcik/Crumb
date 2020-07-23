package com.example.crumb

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder

class AlarmService : Service() {
    val mediaPlayer: MediaPlayer by lazy { MediaPlayer.create(this, ringtone) }
    val ringtone: Uri by lazy {
        RingtoneManager.getActualDefaultRingtoneUri(
            this.applicationContext,
            RingtoneManager.TYPE_ALARM
        )
    }

    companion object {
        const val ACTION_STOP_SERVICE = "action_stop_service"
        const val ACTION_STOP_SERVICE_LAUNCH_APP = "action_stop_service_launch_app"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SERVICE -> stopService()
            ACTION_STOP_SERVICE_LAUNCH_APP -> launchApp()
            else -> initService(intent)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun getTitle(bundle: Bundle?): String {
        if (bundle != null && bundle.containsKey(AlarmHelper.NAME)) {
            return bundle.getString(AlarmHelper.NAME)
        }
        return "No Step Name"
    }

    private fun getText(bundle: Bundle?): String {
        if (bundle != null && bundle.containsKey(AlarmHelper.DESCRIPTION)) {
            return bundle.getString(AlarmHelper.DESCRIPTION)
        }
        return "No Step Description"
    }

    private fun initService(intent: Intent?) {
        val bundle = intent?.getBundleExtra(AlarmHelper.DETAILS) ?: Bundle()
        val id = intent?.getLongExtra(AlarmHelper.PARENT_ID, 0) ?: 0L
        val notificationHelper = NotificationHelper(this, id, bundle)
        playAlarm()
        if (isAppInForeground()) {
            startForeground(
                1,
                notificationHelper.getBasicForegroundNotification(getTitle(bundle), getText(bundle))
            )
        } else {
            startForeground(
                1,
                notificationHelper.getBasicBackgroundNotification(getTitle(bundle), getText(bundle))
            )
        }

    }

    private fun stopService() {
        cancelAlarm()
        this.stopSelf()
    }

    private fun launchApp() {
        cancelAlarm()
        val crumbIntent = Intent(this, MainActivity::class.java)
        crumbIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(crumbIntent)
        this.stopSelf()
    }

    private fun playAlarm() {
        mediaPlayer.isLooping = true
        mediaPlayer.start()
    }

    private fun cancelAlarm() {
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processes: List<ActivityManager.RunningAppProcessInfo> =
            activityManager.runningAppProcesses
        if (processes == null) return false
        for (process in processes) {
            if (process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && process.processName.equals(
                    packageName
                )
            ) {
                return true
            }
        }
        return false;
    }
}