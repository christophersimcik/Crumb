package com.example.tightboules

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.TextView
import androidx.fragment.app.DialogFragment

import java.lang.ClassCastException

class AlarmDialog() : DialogFragment(), TimeScroll.ActionCallback {

    lateinit var listener: DeleteDialogListener
    lateinit var layout: View
    val alarmName: TextView by lazy { layout.findViewById<TextView>(R.id.alarm_name_text) }
    val alarmDescription: TextView by lazy { layout.findViewById<TextView>(R.id.alarm_description) }
    val myFragment: PlayFragment by lazy { getTargetFragment() as PlayFragment }
    val dismissButton: View by lazy { layout.findViewById<View>(R.id.dismiss_button) }
    var initTime = 0
    var isPlaying = false
    val displayMetrics = DisplayMetrics()
    lateinit var mediaPlayer: MediaPlayer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val windowManager = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        layout = inflater.inflate(R.layout.alarm_dialog, null)
        dismissButton.setOnClickListener {
            listener.onDismiss(requireDialog())
            cancelAlarm()
        }
        return layout
    }

    override fun onResume() {
        super.onResume()
        val width = (Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) * .90).toInt()
        val height = 900
        dialog?.window?.setLayout(width,height)
        layout.invalidate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        listener.onDialogCreated()
        alarmName.text = myFragment.name
        alarmDescription.text = myFragment.description
        playAlarm()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = targetFragment as DeleteDialogListener
        } catch (c: ClassCastException) {
            throw ClassCastException(activity.toString() + " have not implemented interface")
        }
    }

    fun getValues(total: Int): Array<Int> {
        val total = total
        val days = (total / 1440) + 1
        var hours = (total % 1440) / 60
        val mins = total % 1440 % 60
        var mrd = 0
        if (hours > 12) {
            hours = hours - 12
            mrd = 1
        }
        if (hours == 0) {
            hours = 12
        }
        return arrayOf(days, hours, mins, mrd)
    }

    interface DeleteDialogListener {
        fun onDialogCreated()
        fun onDismiss(dialog: Dialog);
        fun onCanceled(dialog: Dialog);
        fun onConfirm(dialog: Dialog, name: String, time: Int);
    }

    override fun onActionUp() {
        System.out.println(" im up ")
    }

    override fun onMove() {
        System.out.println("move me")
    }

    override fun onLayoutCompleted(timeScroll: TimeScroll) {
        timeScroll.setDials(getValues(initTime))
    }

    private fun playAlarm() {
        val ringtone =
            RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
        mediaPlayer = MediaPlayer.create(targetFragment?.context, ringtone)
        mediaPlayer.isLooping = true
        mediaPlayer.start()
        isPlaying = true
    }

    private fun cancelAlarm() {
        mediaPlayer.stop()
        mediaPlayer.release()
        isPlaying = false
    }

    fun animate(){

        dismissButton.scaleX
        dismissButton.scaleY
    }
}
