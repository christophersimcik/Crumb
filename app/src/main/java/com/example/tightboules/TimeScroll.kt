package com.example.tightboules

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.util.AttributeSet
import android.view.*
import java.text.DecimalFormat
import kotlin.text.StringBuilder

class TimeScroll @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var canBuzz = true

    private val DAY = 0
    private val HR = 1
    private val MIN = 2
    private val MRD = 3

    private val HI = 0
    private val MID = 1
    private val LO = 2

    var unlocked = true

    private val textMin = 50f
    private val textMax = 60f

    var touchDown = PointF()

    private val alphaMin = 75
    private val alphaMax = 255
    var midD = 0
    var midH = 0
    var midM = 0
    var midMrd = 0

    var pause = false

    val xS = arrayOf(0f, 0f, 0f, 0f)
    val defaultY = arrayOf(0f, 0f, 0f)

    val day: Day by lazy { Day() }
    val hr: Hr by lazy { Hr() }
    val min: Min by lazy { Min() }
    val mrd: Mrd by lazy { Mrd() }
    val pths: Array<Path> by lazy { createPaths() }
    val sections: Array<TimeFrame> by lazy { arrayOf(day, hr, min, mrd) }
    var active = false

    val bounds = Rect()
    val paint = Paint().apply {
        color = resources.getColor(R.color.default_dark, null);
        style = Paint.Style.FILL;
        textSize = textMax
        isAntiAlias = true
    }

    var spacer = 0f

    lateinit var actionCallback: ActionCallback

    fun getTotal(): Int {

        val hrsVal = midH
        val minsVal = midM
        var hrs = 0
        when (midMrd) {
            0 -> {
                when(hrsVal){
                    12 -> hrs = 0
                    else -> hrs = hrsVal * 60
                }
            }
            1 -> {
                when(hrsVal){
                    12 -> hrs = hrsVal * 60
                    else -> hrs = (hrsVal + 12) * 60
                }
            }
        }
        return (midD * 1440) - 1440 + hrs + minsVal
    }

    fun setDials(times: Array<Int>) {
        sections[0].initValues(times[0])
        sections[1].initValues(times[1])
        sections[2].initValues(times[2])
        sections[3].initValues(times[3])
        midD = times[0]
        midH = times[1]
        midM = times[2]
        midMrd = times[3]
        invalidate()
    }

    private fun deactivate() {
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
                touchDown.x = event.x
                touchDown.y = event.y
                for (section in sections) {
                    section.checkIfActive(touchDown)
                }
            }

            MotionEvent.ACTION_UP -> {
                deactivate()
                settle()
                if (this::actionCallback.isInitialized) {
                    actionCallback.onActionUp()
                }
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                val difference = event.y - touchDown.y
                for (s in sections) {
                    if (s.active) {
                        if (!pause) {
                            if (difference > 0) {
                                s.incr()
                                s.changeValue()
                            }
                            if (difference < 0) {
                                s.decr()
                                s.changeValue()
                            }
                        }
                    }
                }
                touchDown.y = event.y
                if (this::actionCallback.isInitialized) {
                    actionCallback.onMove()
                }
            }
        }
        return true
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        xS[DAY] = (w * .4f) - ((w * .4f) / 2f)
        xS[HR] = (w * .6f) - ((w * .2f) / 2f)
        xS[MIN] = (w * .8f) - ((w * .2f) / 2f)
        xS[MRD] = (w * 1f) - ((w * .2f) / 2f)

        defaultY[0] = 0f
        for (y in 1 until defaultY.size) {
            defaultY[y] = defaultY[y - 1] + h * .50f
        }

        spacer = h / 2f

        for (section in sections) {
            section.initPoints()
        }
        if (this::actionCallback.isInitialized) {
            actionCallback.onLayoutCompleted(this)
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

            val less = if (start - 1 > this.min) {
                start - 1
            } else {
                this.max
            }
            val more = if (start + 1 < this.max) {
                start + 1
            } else {
                this.min
            }
            this.values[0] = more
            this.values[1] = start
            this.values[2] = less
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

            for (i in 0 until points.size) {
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
            for (i in 0 until points.size) {
                points[i].y -= 9.375f
                if (points[i].y == defaultY[MID]) {
                    pause = true
                    Handler().postDelayed(Runnable { pause = false }, 25)
                    if (canBuzz) {
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }
                }
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

        open fun incr() {
            for (i in 0 until points.size) {
                points[i].y += 9.375f
                if (points[i].y == defaultY[MID]) {
                    pause = true
                    Handler().postDelayed(Runnable { pause = false }, 25)
                    if (canBuzz) {
                        if (canBuzz) {
                            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        }
                    }
                }
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
            for (pnt in 0 until points.size) {
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

            for (i in 0 until points.size) {
                when (points[i].y) {
                    lowest -> {
                        points[i].y = defaultY[HI]
                    }
                    highest -> {
                        points[i].y = defaultY[LO]
                    }
                    else -> {
                        points[i].y = defaultY[MID]
                        midD = values[i]
                    }
                }
            }
        }

        override fun incr() {
            for (i in 0 until points.size) {
                points[i].y += 9.375f
                if (points[i].y == defaultY[MID]) {
                    pause = true
                    midD = values[i]
                    Handler().postDelayed(Runnable { pause = false }, 25)
                    if (canBuzz) {
                        if (canBuzz) {
                            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        }
                    }
                }
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

        override fun decr() {
            for (i in 0 until points.size) {
                points[i].y -= 9.375f
                if (points[i].y == defaultY[MID]) {
                    pause = true
                    midD = values[i]
                    Handler().postDelayed(Runnable { pause = false }, 25)
                    if (canBuzz) {
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }
                }
                if (points[i].y < defaultY[HI] - spacer / 2) {
                    values[i] = values[posDecr(i)] - 1
                    updateTxt(i)
                    points[i].y = defaultY[LO] + spacer / 2
                }
            }
            invalidate()
        }

        override fun draw(cnvs: Canvas, paint: Paint) {
            for (pnt in 0 until points.size) {
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

        override fun initValues(start: Int) {
            val mid = start
            val top: Int
            if (mid + 1 < this.max) {
                top = mid + 1
            } else {
                top = this.min
            }
            val bot: Int
            if (mid - 1 > this.min) {
                bot = mid - 1
            } else {
                bot = this.max
            }
            values[0] = top
            values[1] = mid
            values[2] = bot
        }
    }

    inner class Hr : TimeFrame() {
        override val min = 1
        override val max = 12
        override val myWidth = width * .2f
        override val values = arrayOf(max - 1, max, min)

        override fun initPoints() {
            points[0].x = xS[HR]; points[0].y = defaultY[0]
            points[1].x = xS[HR]; points[1].y = defaultY[1]
            points[2].x = xS[HR]; points[2].y = defaultY[2]
        }

        override fun initValues(start: Int) {
            val less: Int
            if (start - 1 > this.min) {
                less = start - 1
            } else {
                less = this.max
            }
            val more: Int
            if (start + 1 < this.max) {
                more = start + 1
            } else {
                more = this.min
            }
            values[0] = more
            values[1] = start
            values[2] = less
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

            for (i in 0 until points.size) {
                when (points[i].y) {
                    lowest -> {
                        points[i].y = defaultY[HI]
                    }
                    highest -> {
                        points[i].y = defaultY[LO]
                    }
                    else -> {
                        points[i].y = defaultY[MID]
                        midH = values[i]
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

        override fun incr() {
            for (i in 0 until points.size) {
                points[i].y += 9.375f
                if (points[i].y == defaultY[MID]) {
                    pause = true
                    midH = values[i]
                    Handler().postDelayed(Runnable { pause = false }, 25)
                    if (canBuzz) {
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }
                }
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


        override fun decr() {
            for (i in 0 until points.size) {
                points[i].y -= 9.375f
                if (points[i].y == defaultY[MID]) {
                    pause = true
                    midH = values[i]
                    Handler().postDelayed(Runnable { pause = false }, 25)
                    if (canBuzz) {
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }
                }
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

    }

    inner class Min() : TimeFrame() {
        override val min = 0
        override val max = 59
        override val myWidth = width * .2f
        override val values = arrayOf(min + 1, min, max)
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
            for (i in 0 until points.size) {
                when (points[i].y) {
                    lowest -> {
                        points[i].y = defaultY[HI]
                    }
                    highest -> {
                        points[i].y = defaultY[LO]
                    }
                    else -> {
                        points[i].y = defaultY[MID]
                        midM = values[i]
                    }
                }
            }
        }

        override fun initValues(start: Int) {
            val less: Int
            if (start - 1 > this.min) {
                less = start - 1
            } else {
                less = this.max
            }
            val more: Int
            if (start + 1 < this.max) {
                more = start + 1
            } else {
                more = this.min
            }
            values[0] = more
            values[1] = start
            values[2] = less
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
            for (pnt in 0 until points.size) {
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

        override fun incr() {
            for (i in 0 until points.size) {
                points[i].y += 9.375f
                if (points[i].y == defaultY[MID]) {
                    pause = true
                    midM = values[i]
                    Handler().postDelayed(Runnable { pause = false }, 25)
                    if (canBuzz) {
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }
                }
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

        override fun decr() {
            for (i in 0 until points.size) {
                points[i].y -= 9.375f
                if (points[i].y == defaultY[MID]) {
                    pause = true
                    midM = values[i]
                    Handler().postDelayed(Runnable { pause = false }, 25)
                    if (canBuzz) {
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }
                }
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

    }

    inner class Mrd() : TimeFrame() {
        override val min = 0
        override val max = 1
        override val myWidth = width * .2f
        override val values = arrayOf(max, min, max)
        val mrds = arrayOf("AM", "PM")

        override fun initValues(start: Int) {
            when (start) {
                0 -> {
                    values[0] = 1
                    values[1] = 0
                    values[2] = 1
                }
                1 -> {
                    values[0] = 0
                    values[1] = 1
                    values[2] = 0
                }
            }
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

            for (i in 0 until points.size) {
                when (points[i].y) {
                    lowest -> {
                        points[i].y = defaultY[HI]
                    }
                    highest -> {
                        points[i].y = defaultY[LO]
                    }
                    else -> {
                        points[i].y = defaultY[MID]
                        midMrd = values[i]
                    }
                }
            }
        }

        override fun draw(cnvs: Canvas, paint: Paint) {
            for (pnt in 0 until points.size) {
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

        override fun incr() {
            for (i in 0 until points.size) {
                points[i].y += 9.375f
                if (points[i].y == defaultY[MID]) {
                    pause = true
                    midMrd = values[i]
                    Handler().postDelayed(Runnable { pause = false }, 25)
                    if (canBuzz) {
                        if (canBuzz) {
                            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        }
                    }
                }
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

        override fun decr() {
            for (i in 0 until points.size) {
                points[i].y -= 9.375f

                if (points[i].y == defaultY[MID]) {
                    pause = true
                    midMrd = values[i]
                    Handler().postDelayed(Runnable { pause = false }, 25)
                    if (canBuzz) {
                        if (canBuzz) {
                            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        }
                    }
                }

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

    fun register(actionCallback: ActionCallback) {
        this.actionCallback = actionCallback
    }

    interface ActionCallback {
        fun onActionUp()
        fun onMove()
        fun onLayoutCompleted(timeScroll: TimeScroll)
    }

    fun incrementUntil(time: Array<Int>) {
        canBuzz = false
        if (time[1] > midH) {
            while (midH != time[1]) {
                sections[1].incr()
            }
        } else {
            while (midH != time[1]) {
                sections[1].decr()
            }
        }
        // minutes
        if (time[2] > midM) {
            while (midM != time[2]) {
                sections[2].incr()
            }
        } else {
            while (midM != time[2]) {
                sections[2].decr()
            }
        }
        while (time[3] != midMrd) {
            sections[3].incr()
        }
        canBuzz = true
    }

}