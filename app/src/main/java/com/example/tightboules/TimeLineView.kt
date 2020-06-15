package com.example.tightboules

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class TimeLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    lateinit var myPositions: FloatArray
    lateinit var myColors: IntArray
    var markerColors = intArrayOf(Color.LTGRAY)
    var markerPositions = floatArrayOf(0f)
    var newPath = Path()
    var offset = 0f
    val paint = Paint().apply { isDither = true; isAntiAlias = true}

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.drawPath(newPath, paint)
        drawMarkers(canvas)
    }

    fun drawMarkers(canvas : Canvas?){
        for(i in 0.. markerPositions.size - 1){
            paint.color = markerColors.get(i)
            val myX = reMap(width.toFloat() * markerPositions.get(i))
            val myY = height.toFloat()/2f
            val radius = height.toFloat()/2f
            canvas?.drawCircle(myX,myY,radius,paint)
        }
    }

    private fun reMap(oldPos: Float): Float {
        val new = ((oldPos * ((width - offset) - offset)) / (width)) + offset
        return new
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        offset = height.toFloat()/2f
        super.onSizeChanged(w, h, oldw, oldh)
    }

    fun update(percentages: List<Float>?, colors: List<Int>?) {
        if (percentages != null && colors != null) {
            myPositions = percentages.toFloatArray()
            myColors = colors.toIntArray()
            markerPositions = createMarkerPositions(myPositions)
            markerColors = myColors
            invalidate()
        }
    }

    fun createMarkerPositions(positions: FloatArray) : FloatArray{
        val markerPositions = ArrayList<Float>()
        var start = 0f
        for(position in positions){
            markerPositions.add(position)
         //   start = newValue
        }
        return markerPositions.toFloatArray()
    }
}