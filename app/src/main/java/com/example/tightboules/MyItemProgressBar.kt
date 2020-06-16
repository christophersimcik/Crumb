package com.example.tightboules

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.main.schedules_item.view.*

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

    private val test = hashMapOf(
        0 to PorterDuff.Mode.DST,
        1 to PorterDuff.Mode.DST_ATOP,
        2 to PorterDuff.Mode.DST_IN,
        3 to PorterDuff.Mode.DST_OUT,
        4 to PorterDuff.Mode.DST_OVER,
        5 to PorterDuff.Mode.SRC,
        6 to PorterDuff.Mode.SRC_ATOP,
        7 to PorterDuff.Mode.SRC_IN,
        8 to PorterDuff.Mode.SRC_OUT,
        9 to PorterDuff.Mode.SRC_OVER,
        10 to PorterDuff.Mode.ADD,
        11 to PorterDuff.Mode.OVERLAY,
        12 to PorterDuff.Mode.SRC,
        13 to PorterDuff.Mode.MULTIPLY,
        14 to PorterDuff.Mode.CLEAR,
        15 to PorterDuff.Mode.DARKEN,
        16 to PorterDuff.Mode.SCREEN,
        17 to PorterDuff.Mode.LIGHTEN,
        18 to PorterDuff.Mode.XOR
    )

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
            drawBorder(canvas)
            drawBall(canvas)
            drawTextBackground(canvas)
            drawMessage(canvas)//JiheeWasHere
    }

    fun update(now: Long, start: Long, end: Long, startAll: Long?, endAll: Int?) {
        updateForegoundPosition(now, start, end)
        this.invalidate()
    }

    private fun drawBorder(canvas: Canvas?) {
        paint.style = Paint.Style.FILL
        paint.color = default
        canvas?.drawPath(outterPath, paint)
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

    fun drawTextBackground(canvas: Canvas?) {
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
        val new = ((oldPos * ((width - offset) - offset)) / (width)) + offset
        return new
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        outterPath = makeOutterPath()
        offset = (height / 2f) - 2f
        position = offset

    }

    fun setMessage(message: String) {
        if (message.equals("Alarm In")) {
            this.message = "Now"
        } else {
            this.message = message
        }
        invalidate()
    }
    private fun updateForegoundPosition(progress: Long, start: Long, end: Long) {
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
    }

   /* private fun updateForegoundPosition(now: Long, start: Long, end: Long) {
        val span: Long
        var pos: Long
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
        this.position = position
    }*/

    private fun drawMessage(canvas: Canvas?) {
        paint.style = Paint.Style.FILL
        paint.textSize = 40f
        paint.textAlign = Paint.Align.CENTER
        paint.setShader(null)
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