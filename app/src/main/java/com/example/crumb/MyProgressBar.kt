package com.example.crumb

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class MyProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var outterPath: Path? = null
    private val defaultLight = context.resources.getColor(R.color.default_light, null)
    private val defaultDark = context.resources.getColor(R.color.default_dark, null)
    private val transparent = Color.TRANSPARENT
    private val semiTransparent = context.resources.getColor(R.color.semi_transparent, null)
    private val paint = Paint().apply { isAntiAlias = true; strokeWidth = 5f; isDither = true }
    private var backGroundColors: IntArray? = null
    private var backGroundPositions: FloatArray? = null
    private var foreGroundColors = intArrayOf(transparent, transparent, semiTransparent)
    private var foreGroundPosition = 0f
    private var message = ""
    private var offset = 0f
    var active = false

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

    private var initialized: Boolean = false
    var myWidth = 0f
    var myHeight = 0f

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (initialized) {
            if (active) {
                drawBackground(canvas)
                drawCircle(canvas)
                drawForeGround(canvas)

            } else {
                drawCircle(canvas)
                canvas?.drawColor(semiTransparent)
            }
            drawMessage(canvas)
        }
    }

    fun drawCircle(canvas: Canvas?) {
        val max = ((backGroundPositions?.size) ?: 1) - 1
        for (i in 0..max) {
            paint.color = backGroundColors?.get(i) ?: Color.LTGRAY
            paint.alpha = 200
            val position = backGroundPositions?.get(i) ?: 0f
            val x = reMap((width).toFloat() * position)
            canvas?.drawCircle(x,height.toFloat()/2f, offset, paint)
        }
    }



    private fun reMap(oldPos: Float): Float {
        return ((oldPos * ((width - offset - 2) - offset)) / (width)) + offset
    }

    fun update(progress: Long, start: Long, end: Long) {
        updateForegoundPosition(progress, start, end)
        this.invalidate()
    }

    fun initialize(list: List<Interval>) {
        backGroundColors = makeBackgroundColors(list)
        backGroundPositions = getBackgroundPositions(list)
        initialized = true
        this.invalidate()
    }

    private fun drawForeGround(canvas: Canvas?) {
        paint.style = Paint.Style.FILL
        val x = reMap((width).toFloat() * foreGroundPosition)
        paint.color = defaultLight
        val rect = RectF()
        rect.set(x-5f,0f,x+5f,height.toFloat())
        canvas?.drawRect(rect,paint)
    }

    private fun drawBackground(canvas: Canvas?){
        paint.color = context.resources.getColor(R.color.default_light_darker,null)
        canvas?.drawPath(outterPath ?: Path(), paint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        myWidth = w.toFloat()
        myHeight = h.toFloat()
        outterPath = makeOutterPath()
        offset = (myHeight / 2f) - 2
    }

    private fun makeOutterPath(): Path {
        val path = Path()
        val left = 0f
        val right = width.toFloat() - 0f
        val top = 0f
        val bottom = height.toFloat() - 0f
        val radius = height.toFloat() - 0f
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
        foreGroundPosition = position
    }

    private fun getBackgroundPositions(list: List<Interval>): FloatArray {
        val positions = ArrayList<Float>()
        for (item in list) {
            positions.add(item.percentage)
        }
        return positions.toFloatArray()
    }

    private fun makeBackgroundColors(list: List<Interval>): IntArray {
        val colors = ArrayList<Int>()
        for (item in list) {
            colors.add(item.color)
        }
        return colors.toIntArray()
    }


    fun setMessage(message: String) {
        if (message.equals("")) {
            this.message = message
        } else {
            this.message = "Starts in " + message
        }
        invalidate()
    }

    fun drawMessage(canvas: Canvas?) {
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

}