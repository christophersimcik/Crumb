package com.example.crumb

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import java.text.DecimalFormat
import kotlin.text.StringBuilder

class CustomTimeScrollSAVE @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val DAY = 0
    val HR = 1
    val MIN = 2
    val MRD = 3

    var minimum = 0

    val HI = 0
    val MID = 1
    val LO = 2

    var unlocked = true

    val textMin = 50f
    val textMax = 60f

    var touchDown = PointF()

    val alphaMin = 75
    val alphaMax = 255

    var myDay = 1
    var myHr = 1
    var myMin = 0
    var myMrd = 0

    val xS = arrayOf(0f, 0f, 0f, 0f)
    val defaultY = arrayOf(0f, 0f, 0f)

    val day: Day by lazy { Day() }
    val hr: Hr by lazy { Hr() }
    val min: Min by lazy { Min() }
    val mrd: Mrd by lazy { Mrd() }
    val pths: Array<Path> by lazy { createPaths() }
    val sections: Array<TimeFrame> by lazy { arrayOf(day, hr, min, mrd) }
    var active = false

    lateinit var actionUpCallback: ActionUpCallback

    val bounds = Rect()
    val paint = Paint().apply {
        color = resources.getColor(R.color.default_dark, null);
        style = Paint.Style.FILL;
        textSize = textMax
        isAntiAlias = true
    }
    var spacer = 0f
    lateinit var longClickObserver: LongClickObserver

    var gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                longClickObserver.onLongClicked(e.rawY)
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                playSoundEffect(SoundEffectConstants.CLICK)
                super.onLongPress(e)
            }

        })

    fun getTotal(): Int {
        var days = (myDay - 1) * 1440
        var hrs = 0
        var mins = myMin
        when (myMrd) {
            0 -> {
                if (myHr == 12) {
                    hrs = 0
                } else {
                    hrs = myHr * 60
                }
            }
            1 -> {
                hrs = (myHr + 12) * 60
            }
        }
        return days + hrs + mins
    }

    fun getValues(total: Int): Array<Int> {
        val total = total + 1
        val days = (total / 1440) + 1
        var hours = (total % 1440) / 60
        val mins = total % 1440 % 60
        var mrd = 0
        if (hours > 12) {
            hours = hours - 12
            mrd = 1
        }
        return arrayOf(days, hours, mins, mrd)
    }

    fun resetDials(total: Int) {
        val values = getValues(total)
        setDials(values[0], values[1], values[2], values[3])
        actionUpCallback.onActionUp()
    }


    fun setDials(day: Int, hr: Int, min: Int, mrd: Int) {
        sections[0].initValues(day)
        myDay = day
        sections[1].initValues(hr)
        myHr = hr
        sections[2].initValues(min)
        myMin = min
        sections[3].initValues(mrd)
        myMrd = mrd
        invalidate()
    }

    fun deactivate() {
        for (section in sections) {
            section.active = false
        }
    }

    fun settle() {
        for (section in sections) {
            section.settle()
        }
    }

    fun createPaths(): Array<Path> {
        val array = arrayOf(Path(), Path(), Path())
        array[0].moveTo(width * .4f, 0f)
        array[0].lineTo(width * .4f, height.toFloat())
        array[1].moveTo(width * .6f, 0f)
        array[1].lineTo(width * .6f, height.toFloat())
        array[2].moveTo(width * .8f, 0f)
        array[2].lineTo(width * .8f, height.toFloat())
        return array
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                System.out.println("action down")
                touchDown.x = event.x
                touchDown.y = event.y
                if (active) {
                    for (section in sections) {
                        section.checkIfActive(touchDown)
                    }
                }
                true
            }

            MotionEvent.ACTION_UP -> {
                System.out.println("action up")
                actionUpCallback.onActionUp()
                deactivate()
                settle()
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                System.out.println("action move")
                val difference = event.y - touchDown.y
                for (s in sections) {
                    if (s.active) {
                        if (difference > 0) {
                            s.incr()
                            s.changeValue()
                        }
                        if (getTotal() - 1 > minimum) {
                            if (difference < 0) {
                                s.decr()
                                s.changeValue()
                            }
                        } else {
                            // do something to indicate infringement
                        }
                        actionUpCallback.onMove()
                    }
                }
                touchDown.y = event.y
            }
        }
        gestureDetector.onTouchEvent(event)
        return true
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        xS[DAY] = (w * .4f) - ((w * .4f) / 2)
        xS[HR] = (w * .6f) - ((w * .2f) / 2)
        xS[MIN] = (w * .8f) - ((w * .2f) / 2)
        xS[MRD] = (w * 1f) - ((w * .2f) / 2)

        defaultY[0] = 0f
        for (y in 1..defaultY.size - 1) {
            defaultY[y] = defaultY[y - 1] + h * .50f
        }

        spacer = h / 2f

        for (section in sections) {
            section.initPoints()
        }
    }

    override fun draw(canvas: Canvas) {
        if (active) {
            paint.color = resources.getColor(R.color.red_accent, null)
        } else {
            paint.color = resources.getColor(R.color.default_dark, null)
        }
        super.draw(canvas)
        for (section in sections) {
            section.draw(canvas, paint)
        }
        for (pth in pths) {
            paint.style = Paint.Style.STROKE
            paint.alpha = alphaMax
            canvas.drawPath(pth, paint)
            paint.style = Paint.Style.FILL
        }
    }

    interface LongClickObserver {
        fun onLongClicked(y: Float)
    }

    fun registerObserver(longClickObserver: LongClickObserver, actionUpCallback: ActionUpCallback) {
        this.longClickObserver = longClickObserver
        this.actionUpCallback = actionUpCallback
    }

    open inner class TimeFrame() {
        open val min = 0
        open val max = Int.MAX_VALUE
        val points = arrayOf(PointF(), PointF(), PointF())
        open val values = arrayOf(min + 1, min, max)
        val txt = StringBuilder()
        var centerValue = min
        var offsetX = 0f
        var offsetY = 0f
        var active = false
        var scale = 0f
        var alpha = 255
        open val myWidth = width * .4f

        open fun checkIfActive(point: PointF) {
            val left = xS[0] - myWidth / 2
            val right = xS[0] + myWidth / 2
            if (point.x > left && point.x < right) {
                active = true
            } else {
                active = false
            }
        }

        open fun changeValue() {}

        open fun initValues(start: Int) {
            val less = if (start - 1 > min) {
                start - 1
            } else {
                max
            }
            val more = if (start + 1 < max) {
                start + 1
            } else {
                min
            }
            values[0] = more
            values[1] = start
            values[2] = less
            invalidate()
        }

        open fun settle() {
            var lowest = Float.MAX_VALUE
            var highest = Float.MIN_VALUE
            for (point in points) {
                if (point.y < lowest) {
                    lowest = point.y
                }
                if (point.y > highest) {
                    highest = point.y
                }
            }

            for (i in 0..points.size - 1) {
                when (points[i].y) {
                    lowest -> {
                        points[i].y = defaultY[HI]
                    }
                    highest -> {
                        points[i].y = defaultY[LO]
                    }
                    else -> {
                        points[i].y = defaultY[MID]
                    }
                }
            }
        }

        fun mapDistanceToScale(yPos: Float) {
            val distance = Math.abs(yPos - (height / 2))
            scale = (((distance) * (textMin - textMax)) / (height / 2)) + textMax
        }

        fun mapDistanceToAlpha(yPos: Float) {
            val distance = Math.abs(yPos - (height / 2))
            alpha = Math.round((((distance) * (alphaMin - alphaMax)) / (height / 2)) + alphaMax)
        }

        open fun initPoints() {
            points[0].x = xS[DAY]; points[0].y = defaultY[0]
            points[1].x = xS[DAY]; points[1].y = defaultY[1]
            points[2].x = xS[DAY]; points[2].y = defaultY[2]
        }


        open fun decr() {
            for (i in 0..points.size - 1) {
                points[i].y -= 9.375f
                performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                if (points[i].y < defaultY[HI] - spacer / 2) {
                    values[i] = values[posDecr(i)] - 1
                    updateTxt(i)
                    points[i].y = defaultY[LO] + spacer / 2
                }
                if (Math.abs(points[i].y - defaultY[MID]) < 18.75f) {
                    centerValue = values[i]
                }
            }
            invalidate()
        }

        fun incr() {
            for (i in 0..points.size - 1) {
                points[i].y += 9.375f
                performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                if (points[i].y > height + spacer / 2) {
                    values[i] = values[posIncr(i)] + 1
                    updateTxt(i)
                    points[i].y = defaultY[HI] - spacer / 2
                }
                if (Math.abs(points[i].y - defaultY[MID]) < 18.75f) {
                    centerValue = values[i]
                }
            }
            invalidate()
        }

        fun posIncr(i: Int): Int {
            when (i) {
                LO -> return HI
                MID -> return LO
                HI -> return MID
            }
            return 0
        }

        fun posDecr(i: Int): Int {
            when (i) {
                LO -> return MID
                MID -> return HI
                HI -> return LO
            }
            return 0
        }

        open fun draw(cnvs: Canvas, paint: Paint) {
            for (pnt in 0..points.size - 1) {
                mapDistanceToScale(points[pnt].y)
                mapDistanceToAlpha(points[pnt].y)
                paint.textSize = scale
                paint.alpha = alpha
                txt.clear()
                txt.insert(0, values[pnt])
                paint.getTextBounds(txt.toString(), 0, txt.toString().length, bounds)
                offsetX = bounds.width() / 2f
                offsetY = bounds.height() / 2f
                cnvs.drawText(
                    txt.toString(),
                    points[pnt].x - offsetX,
                    points[pnt].y + offsetY,
                    paint
                )
            }
        }

        fun updateTxt(position: Int) {
            if (values[position] > max) {
                values[position] = min
            }
            if (values[position] < min) {
                values[position] = max
            }
        }
    }

    inner class Day : TimeFrame() {
        override val min = 1
        override val max = 365
        override val myWidth = width * .4f
        override val values = arrayOf(min + 1, min, max)
        override fun initPoints() {
            points[0].x = xS[DAY]; points[0].y = defaultY[0]
            points[1].x = xS[DAY]; points[1].y = defaultY[1]
            points[2].x = xS[DAY]; points[2].y = defaultY[2]
        }

        override fun checkIfActive(point: PointF) {
            if (unlocked) {
                super.checkIfActive(point)
            }
        }

        override fun changeValue() {
            myDay = centerValue
        }

        override fun settle() {
            var lowest = Float.MAX_VALUE
            var highest = Float.MIN_VALUE
            for (point in points) {
                if (point.y < lowest) {
                    lowest = point.y
                }
                if (point.y > highest) {
                    highest = point.y
                }
            }

            for (i in 0..points.size - 1) {
                when (points[i].y) {
                    lowest -> {
                        points[i].y = defaultY[HI]
                    }
                    highest -> {
                        points[i].y = defaultY[LO]
                    }
                    else -> {
                        points[i].y = defaultY[MID]; myDay = values[i]
                    }
                }
            }
        }


        override fun decr() {
            for (i in 0..points.size - 1) {
                points[i].y -= 9.375f
                performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                if (points[i].y < defaultY[HI] - spacer / 2) {
                    values[i] = values[posDecr(i)] - 1
                    updateTxt(i)
                    points[i].y = defaultY[LO] + spacer / 2
                }
            }
            invalidate()
        }

        override fun draw(cnvs: Canvas, paint: Paint) {
            for (pnt in 0..points.size - 1) {
                mapDistanceToScale(points[pnt].y)
                mapDistanceToAlpha(points[pnt].y)
                paint.textSize = scale
                if (unlocked) {
                    paint.alpha = alpha
                } else {
                    paint.alpha = 100
                }
                txt.clear()
                txt.insert(0, "Day " + values[pnt])
                paint.getTextBounds(txt.toString(), 0, txt.toString().length, bounds)
                offsetX = bounds.width() / 2f
                offsetY = bounds.height() / 2f
                cnvs.drawText(
                    txt.toString(),
                    points[pnt].x - offsetX,
                    points[pnt].y + offsetY,
                    paint
                )
            }
        }
    }

    inner class Hr : TimeFrame() {
        override val min = 1
        override val max = 12
        override val myWidth = width * .2f
        override fun initPoints() {
            points[0].x = xS[HR]; points[0].y = defaultY[0]
            points[1].x = xS[HR]; points[1].y = defaultY[1]
            points[2].x = xS[HR]; points[2].y = defaultY[2]
        }

        override fun changeValue() {
            myHr = centerValue
        }

        override fun settle() {
            var lowest = Float.MAX_VALUE
            var highest = Float.MIN_VALUE
            for (point in points) {
                if (point.y < lowest) {
                    lowest = point.y
                }
                if (point.y > highest) {
                    highest = point.y
                }
            }

            for (i in 0..points.size - 1) {
                when (points[i].y) {
                    lowest -> {
                        points[i].y = defaultY[HI]
                    }
                    highest -> {
                        points[i].y = defaultY[LO]
                    }
                    else -> {
                        points[i].y = defaultY[MID]; myHr = values[i]
                    }
                }
            }
        }

        override fun checkIfActive(point: PointF) {
            val left = xS[HR] - myWidth / 2
            val right = xS[HR] + myWidth / 2
            if (point.x > left && point.x < right) {
                active = true
            } else {
                active = false
            }
        }
    }

    inner class Min() : TimeFrame() {
        override val min = 0
        override val max = 59
        override val myWidth = width * .2f

        override fun settle() {
            var lowest = Float.MAX_VALUE
            var highest = Float.MIN_VALUE
            for (point in points) {
                if (point.y < lowest) {
                    lowest = point.y
                }
                if (point.y > highest) {
                    highest = point.y
                }
            }
            for (i in 0..points.size - 1) {
                when (points[i].y) {
                    lowest -> {
                        points[i].y = defaultY[HI]
                    }
                    highest -> {
                        points[i].y = defaultY[LO]
                    }
                    else -> {
                        points[i].y = defaultY[MID]; myMin = values[i]
                    }
                }
            }
        }

        override fun changeValue() {
            myMin = centerValue
        }


        override fun initPoints() {
            points[0].x = xS[MIN]; points[0].y = defaultY[0]
            points[1].x = xS[MIN]; points[1].y = defaultY[1]
            points[2].x = xS[MIN]; points[2].y = defaultY[2]
        }

        override fun checkIfActive(point: PointF) {
            val left = xS[MIN] - myWidth / 2
            val right = xS[MIN] + myWidth / 2
            if (point.x > left && point.x < right) {
                active = true
            } else {
                active = false
            }
        }

        override fun draw(cnvs: Canvas, paint: Paint) {
            val df = DecimalFormat("00")
            for (pnt in 0..points.size - 1) {
                mapDistanceToScale(points[pnt].y)
                mapDistanceToAlpha(points[pnt].y)
                paint.textSize = scale
                paint.alpha = alpha
                txt.clear()
                txt.insert(0, df.format(values[pnt]))
                paint.getTextBounds(txt.toString(), 0, txt.toString().length, bounds)
                offsetX = bounds.width() / 2f
                offsetY = bounds.height() / 2f
                cnvs.drawText(
                    txt.toString(),
                    points[pnt].x - offsetX,
                    points[pnt].y + offsetY,
                    paint
                )
            }
        }

    }

    inner class Mrd() : TimeFrame() {
        override val min = 0
        override val max = 1
        override val myWidth = width * .2f
        override val values = arrayOf(max, min, max)
        val mrds = arrayOf("AM", "PM")

        override fun settle() {
            var lowest = Float.MAX_VALUE
            var highest = Float.MIN_VALUE
            for (point in points) {
                if (point.y < lowest) {
                    lowest = point.y
                }
                if (point.y > highest) {
                    highest = point.y
                }
            }

            for (i in 0..points.size - 1) {
                when (points[i].y) {
                    lowest -> {
                        points[i].y = defaultY[HI]
                    }
                    highest -> {
                        points[i].y = defaultY[LO]
                    }
                    else -> {
                        points[i].y = defaultY[MID]; myMrd = values[i]
                    }
                }
            }
        }

        override fun changeValue() {
            myMrd = centerValue
        }

        override fun draw(cnvs: Canvas, paint: Paint) {
            for (pnt in 0..points.size - 1) {
                txt.clear()
                txt.insert(0, mrds[values[pnt]])
                mapDistanceToScale(points[pnt].y)
                mapDistanceToAlpha(points[pnt].y)
                paint.textSize = scale
                paint.alpha = alpha
                paint.getTextBounds(txt.toString(), 0, txt.toString().length, bounds)
                offsetX = bounds.width() / 2f
                offsetY = bounds.height() / 2f
                cnvs.drawText(
                    txt.toString(),
                    points[pnt].x - offsetX,
                    points[pnt].y + offsetY,
                    paint
                )
            }
        }

        override fun initPoints() {
            points[0].x = xS[MRD]; points[0].y = defaultY[0]
            points[1].x = xS[MRD]; points[1].y = defaultY[1]
            points[2].x = xS[MRD]; points[2].y = defaultY[2]
        }

        override fun checkIfActive(point: PointF) {
            val r = Rect()
            getGlobalVisibleRect(r)
            val left = xS[MRD] - myWidth / 2
            val right = xS[MRD] + myWidth / 2
            if (point.x > left && point.x < right) {
                active = true
            } else {
                active = false
            }
        }
    }

    interface ActionUpCallback {
        fun onActionUp()
        fun onMove()
    }
}