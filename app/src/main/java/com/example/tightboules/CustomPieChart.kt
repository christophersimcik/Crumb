package com.example.tightboules

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class CustomPieChart @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    lateinit var paths: Array<Path>
    lateinit var percents: Array<Float>
    val test = arrayOf(20f,20f,30f,36f,20f,20f,30f,36f)
    var durations: Array<Float> = test
    var colors = ArrayList<Int>()
    val rect = RectF()
    val paint = Paint().apply { isDither = true; isAntiAlias = true }
    init {
        durations = test
        getPercents(durations)
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        var counter = 0
        System.out.println("* pths = " + paths.size + " clrs + " + colors.size)
        for (path in paths) {
            canvas?.save()
            canvas?.rotate(-90f, rect.centerX(), rect.centerY())
            paint.color = colors[counter]
            System.out.println("* pths = " + paths.size + " clrs + " + colors[counter])
            canvas?.drawPath(path, paint)
            canvas?.restore()
            if (counter < colors.size - 1) {
                counter++
            } else {
                counter = 0
            }
        }

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        rect.left = 0f
        rect.right = w.toFloat()
        rect.top = 0f
        rect.bottom = height.toFloat()
        paths = createPaths()
        super.onSizeChanged(w, h, oldw, oldh)
    }

    fun getPercents(durations: Array<Float>): Array<Float> {
        var total = 0f
        percents = Array(durations.size) { i -> 0f }
        for (duration in durations) {
            total += duration
        }
        for (i in 0..durations.size - 1) {
            percents[i] = 360 * durations[i] / total
        }
        makeColors(percents.size)
        return percents
    }

    fun createPaths(): Array<Path> {
        var prev = 0f
        var next : Float
        val paths = Array(percents.size) { i -> Path() }
        for (i in 0..percents.size - 1) {
            next = percents[i]
            paths[i].arcTo(rect, prev, next)
            paths[i].lineTo(rect.centerX(),rect.centerY())
            prev += next
        }
        return paths
    }

    fun makeColors(size : Int){
        var cols : IntArray
        for(i in 0..size){
            cols = randomColor()
            if (i > 0) {
                cols = compareValues(colors[i], cols[0])
            }
            for( col in cols){
                colors.add(col)
            }
        }

    }

    fun randomColor() : IntArray {
        val colorArray = IntArray(2)
        val alpha = 255
        val red = Random.nextInt(0, 255)
        val green = Random.nextInt(0, 255)
        val blue = Random.nextInt(0, 255)
        colorArray[0] = Color.argb(alpha, red, green, blue)
        colorArray[1] = getCompliment(colorArray[0])
        return colorArray
    }

    fun getCompliment(col: Int): Int {
        val alpha = 255
        val red = 255 - Color.red(col)
        val green = 255 - Color.green(col)
        val blue = 255 - Color.blue(col)
        return Color.argb(alpha, red, green, blue)
    }

    fun compareValues(oldCol: Int, newCol: Int): IntArray {
        val oldHSV = FloatArray(3)
        val newHSV = FloatArray(3)
        val minDiff = Math.min(oldHSV[2], newHSV[2])
        val maxDiff = height - Math.max(oldHSV[2], newHSV[2])
        val minAmt = Math.min(minDiff, maxDiff)
        Color.colorToHSV(oldCol, oldHSV)
        Color.colorToHSV(newCol, newHSV)
        if (Math.abs(newHSV[2] - oldHSV[2]) < 100) {
            newHSV[2] = newHSV[2] - (minAmt / 2)
            oldHSV[2] = oldHSV[2] + (minAmt / 2)
        }
        return IntArray(2){Color.HSVToColor(oldHSV); Color.HSVToColor(newHSV)}
    }

}