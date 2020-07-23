package com.example.crumb.UI

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.widget.ImageButton
import android.widget.Toast
import com.example.crumb.R

class CustomCheckBox @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageButton(context, attrs, defStyleAttr) {
    private var checked = false
    val activeColor = context.resources.getColor(R.color.red_accent, null)
    val inactiveColor = context.resources.getColor(R.color.default_dark, null)
    val activePath = Path()
    val inactivePath = Path()
    val paint = Paint().apply{isDither = true; isAntiAlias = true; strokeWidth = 5f}

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        activePath.reset()
        inactivePath.reset()
        activePath.addCircle(w/2f,h/2f,(w * .70f)/2, Path.Direction.CW)
        inactivePath.addCircle(w/2f,h/2f,(w * .75f)/2, Path.Direction.CW)
        invalidate()
    }

    fun drawActive(canvas : Canvas?){
        paint.style = Paint.Style.FILL
        paint.color = activeColor
        canvas?.drawPath(activePath, paint)
    }

    fun drawInactive(canvas : Canvas?){
        paint.style = Paint.Style.STROKE
        paint.color = inactiveColor
        canvas?.drawPath(inactivePath, paint)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if(checked){
            drawActive(canvas)
        }else{
            drawInactive(canvas)
        }
    }


    fun getChecked(): Boolean {
        return checked
    }
    fun setChecked(bool : Boolean) {
        checked = bool
        invalidate()
    }

    fun toggleChecked() : Boolean {
        if(checked){
            Toast.makeText(context,"Alarm Off",Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(context,"Alarm On",Toast.LENGTH_SHORT).show()
        }
        checked = !checked
        invalidate()
        return checked
    }

}