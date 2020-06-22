package com.example.crumb

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class TimeLineCustom @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    var newColors: IntArray = IntArray(2) { i -> 0 }
    var newPositions: FloatArray = FloatArray(2) { i -> 0f }
    var newPath = Path()
    val paint = Paint().apply { isDither = true; isAntiAlias = true }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        if(newColors.size == newPositions.size) {
            paint.shader = LinearGradient(
                0f,
                height / 2f,
                width.toFloat(),
                height / 2f,
                newColors,
                newPositions,
                Shader.TileMode.CLAMP
            )
        }
        canvas?.drawPath(newPath, paint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        newPath = createNewPath()
        super.onSizeChanged(w, h, oldw, oldh)
    }

    fun update(list: List<Float>?) {
        if (list != null) {
            newPositions = positionsAsFloatArray(getPositions(list))
            newColors = colorsAsIntArray(getColors(getNumberOfColors(list.size)))
        }
        invalidate()
    }

    fun createNewPath(): Path {
        val path = Path()
        val left = 0f
        val right = width.toFloat()
        val top = 0f
        val bottom = height.toFloat()
        val radii = floatArrayOf(
            height.toFloat(),
            height.toFloat(),
            height.toFloat(),
            height.toFloat(),
            height.toFloat(),
            height.toFloat(),
            height.toFloat(),
            height.toFloat()
        )

        path.addRoundRect(left, top, right, bottom, radii, Path.Direction.CW)
        return path
    }

    fun getCompliment(col: Int): Int {
        val alpha = 255
        val red = 255 - Color.red(col)
        val green = 255 - Color.green(col)
        val blue = 255 - Color.blue(col)
        return Color.argb(alpha, red, green, blue)
    }

    fun getPositions(floats : List<Float>) : ArrayList<Pair<Float,Float>>{
        val list = ArrayList<Pair<Float,Float>>()
        var start = 0f
        for(f in floats){
            list.add(Pair(start,start+f))
            start = start + f
        }
        return list
    }

    fun getNumberOfColors(size : Int) : IntArray{
        val list = ArrayList<Int>()
        var sequencer = 0
        for(i in 1..size){
            when(sequencer){
                0 -> list.add(getNewColor())
                1 -> list.add(getCompliment(list.last()))
                2 -> list.add(getDifferentColor(list.last(),getNewColor()))
            }
            cycleSequencer(sequencer)
        }

        return list.toIntArray()
    }

    fun cycleSequencer(i : Int) : Int{
        var next = 0
        if(i > 2){
            next = 0
        }else{
            next += 1
        }
        return next
    }

    fun getColors(colors : IntArray) : ArrayList<Pair<Int,Int>>{
        val list = ArrayList<Pair<Int,Int>>()
        for(c in colors){
          list.add(Pair(c,c))
        }
        return list
    }

    fun getNewColor(): Int {
        val alpha = 255
        val red = Random.nextInt(25, 255)
        val green = Random.nextInt(25, 255)
        val blue = Random.nextInt(25, 255)
        return Color.argb(alpha, red, green, blue)
    }

    fun getDifferentColor(oldRGB: Int, newRGB: Int): Int {
        val oldHSV = FloatArray(3)
        val newHSV = FloatArray(3)
        Color.colorToHSV(oldRGB, oldHSV)
        Color.colorToHSV(newRGB, newHSV)
        val minDiff = (newHSV[1] * 10).toInt()
        val maxDiff = (1f - newHSV[1] * 10).toInt()
        val minAmt = Math.min(minDiff, maxDiff)
        if (Math.abs(newHSV[1] - oldHSV[1]) < .35f)
            when (minAmt) {
                minDiff -> newHSV[1] = Random.nextInt(minDiff + 3,10)*.10f
                maxDiff -> newHSV[1] = Random.nextInt(maxDiff - 3, 0)*.10f
            }
        return Color.HSVToColor(newHSV)
    }

    fun colorsAsIntArray(list :ArrayList<Pair<Int,Int>>) : IntArray{
        val ints = ArrayList<Int>()
        for(pair in list){
            ints.add(pair.first)
            ints.add(pair.second)
        }
        return ints.toIntArray()
    }

    fun positionsAsFloatArray(list :ArrayList<Pair<Float,Float>>) : FloatArray{
       val floats = ArrayList<Float>()
        for(pair in list){
            floats.add(pair.first)
            floats.add(pair.second)
        }
        return floats.toFloatArray()
    }

}