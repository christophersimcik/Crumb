package com.example.crumb.UI

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.crumb.R

class CustomChainView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        val SINGLE = 0
        val START = 1
        val MIDDLE = 2
        val END = 3
    }

    var radius = 0f
    var offset = 10f
    var centerWidth = 0f
    var centerHeight = 0f
    var tailWidth = 7f
    var tailColor = context.resources.getColor(R.color.hilight, null)
    val activeColor = context.resources.getColor(R.color.red_accent, null)
    private var color = Color.LTGRAY
    var buttonPressed = false
    private var mode = SINGLE
    private val paint = Paint().apply { strokeWidth = tailWidth }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerWidth = w.toFloat() / 2f
        centerHeight = h.toFloat() / 2f
        radius = Math.min(w, h) / 4f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawLine(canvas)
        drawCircle(canvas)
    }

    fun drawLine(canvas: Canvas?) {
        paint.style = Paint.Style.STROKE
        paint.color = tailColor
        when (mode) {
            START -> canvas?.drawLine(
                centerWidth,
                centerHeight + (radius + offset),
                centerWidth,
                height.toFloat(),
                paint
            )
            MIDDLE -> {
                canvas?.drawLine(
                    centerWidth,
                    0f,
                    centerWidth,
                    centerHeight - (radius + offset),
                    paint
                )
                canvas?.drawLine(
                    centerWidth,
                    centerHeight + (radius + offset),
                    centerWidth,
                    height.toFloat(),
                    paint
                )
            }
            END -> canvas?.drawLine(
                centerWidth,
                0f,
                centerWidth,
                centerHeight - (radius + offset),
                paint
            )
        }
    }

    fun setMode(mode: Int) {
        this.mode = mode
        invalidate()
    }

    fun setColor(color: Int) {
        this.color = color
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                buttonPressed = true; invalidate()
            }
            MotionEvent.ACTION_UP -> {
                buttonPressed = false; invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.rawX < left || event.rawX > right || event.rawY < top || event.rawY > bottom) {
                    buttonPressed = false; invalidate()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    fun drawCircle(canvas: Canvas?) {
        paint.style = Paint.Style.FILL
        if (buttonPressed) {
            paint.color = activeColor
        } else {
            paint.color = color
        }
        canvas?.drawCircle(centerWidth, centerHeight, radius, paint)
    }


}