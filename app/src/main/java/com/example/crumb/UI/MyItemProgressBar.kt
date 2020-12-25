package com.example.crumb.UI

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.crumb.R

class MyItemProgressBar @JvmOverloads constructor(

    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val default = context.resources.getColor(R.color.default_light_darker, null)
    private val defaultDark = context.resources.getColor(R.color.default_dark, null)
    private val redAccent = context.resources.getColor(R.color.red_accent, null)
    private var color = Color.LTGRAY
    private val paint = Paint().apply { isAntiAlias = true; strokeWidth = 5f }
    private var outterPath: Path? = null
    private var position = 0f
    private var message = ""
    private var offset = 0f
    private var isFirst = false

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
            drawBorder(canvas)
            drawBall(canvas)
            drawTextBackground(canvas)
            drawMessage(canvas)//JiheeWasHere
    }

    fun update(now: Long, start: Long, end: Long) {
        updateForegoundPosition(now, start, end)
        this.invalidate()
    }

    private fun drawBorder(canvas: Canvas?) {
        paint.style = Paint.Style.FILL
        paint.color = default
        canvas?.drawPath(outterPath ?: Path(), paint)
    }

    fun setColor(color : Int){
        this.color = color
    }

    private fun makeOutterPath(): Path {
        val path = Path()
        val left = 2f
        val right = width.toFloat() - 2f
        val top = 2f
        val bottom = height.toFloat() - 2f
        val radius = height.toFloat() - 2f
        val radii = floatArrayOf(
            radius,
            radius,
            radius,
            radius,
            radius,
            radius,
            radius,
            radius
        )
        path.addRoundRect(left, top, right, bottom, radii, Path.Direction.CW)
        return path
    }

    private fun drawBall(canvas: Canvas?) {
            if (position < 1) {
                paint.color = color
            } else {
                paint.color = redAccent
            }
        canvas?.drawCircle(
            reMap(width * position),
            height.toFloat() / 2f,
            (height.toFloat() / 2f) - 2,
            paint
        )
    }

    private fun drawTextBackground(canvas: Canvas?) {
        paint.color = default
        paint.alpha = 200
        val rect = Rect()
        paint.getTextBounds(message, 0, message.length, rect)
        canvas?.drawRect(
            width.toFloat() / 2f - rect.width() / 2f - 20,
            height.toFloat() / 2f - rect.height() / 2f - 10,
            width.toFloat() / 2f + rect.width() / 2f + 20,
            height.toFloat() / 2f + rect.height() / 2f + 10,
            paint
        )

        paint.alpha = 255
    }

    private fun reMap(oldPos: Float): Float {
        return ((oldPos * ((width - offset) - offset)) / (width)) + offset
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        outterPath = makeOutterPath()
        offset = (height / 2f) - 2f
        position = offset

    }

    fun setMessage(message: String) {
        if (message == "Alarm In") {
            this.message = "Now"
        } else {
            this.message = message
        }
        invalidate()
    }
   /* private fun updateForegoundPosition(progress: Long, start: Long, end: Long) {
        var position = 0f
        val span = end - start
        val pos = progress - start
        if (span != 0L) {
            position = (pos.toFloat() / span.toFloat())
            if (position < 0f) {
                position = 0f
            }
            if (position > 1f) {
                position = 1f
            }
        }
        this.position = position
    }*/

    private fun updateForegoundPosition(now: Long, start: Long, end: Long) {
     //   Log.d("PROG", "now = $now and start = $start and end =$end")
        val span: Long
        val pos: Long
        if (isFirst) {
            span = end - (start - 86400000L)
            pos = now - (start - 86400000L)
        } else {
            span = end - start
            pos = now - start
        }

        if (span != 0L) {
            position = (pos.toFloat() / span.toFloat())
            if (position < 0f) {
                position = 0f
            }
            if (position > 1f) {
                position = 1f
            }
        } else {
            position = 1f
        }
    }

    private fun drawMessage(canvas: Canvas?) {
        paint.style = Paint.Style.FILL
        paint.textSize = 40f
        paint.textAlign = Paint.Align.CENTER
        paint.shader = null
        paint.color = defaultDark
        val rect = Rect()
        paint.getTextBounds(message, 0, message.length, rect)
        canvas?.drawText(
            message,
            width / 2f,
            height / 2f + rect.height() / 2f,
            paint
        )
    }

    fun setIsFirst(bool : Boolean){
        isFirst = bool
        invalidate()
    }

}