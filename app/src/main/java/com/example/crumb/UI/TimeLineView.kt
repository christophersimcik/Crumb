package com.example.crumb.UI

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class TimeLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private lateinit var myPositions: FloatArray
    private lateinit var myColors: IntArray
    private var markerColors = intArrayOf(Color.LTGRAY)
    private var markerPositions = floatArrayOf(0f)
    private var newPath = Path()
    private var offset = 0f
    private val paint = Paint().apply { isDither = true; isAntiAlias = true}

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.drawPath(newPath, paint)
        drawMarkers(canvas)
    }

    private fun drawMarkers(canvas : Canvas?){
        for(i in markerPositions.indices){
            paint.color = markerColors[i]
            val myX = reMap(width.toFloat() * markerPositions[i])
            val myY = height.toFloat()/2f
            val radius = height.toFloat()/2f
            canvas?.drawCircle(myX,myY,radius,paint)
        }
    }

    private fun reMap(oldPos: Float): Float {
        return ((oldPos * ((width - offset) - offset)) / (width)) + offset
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

    private fun createMarkerPositions(positions: FloatArray) : FloatArray{
        val markerPositions = ArrayList<Float>()
        var start = 0f
        for(position in positions){
            markerPositions.add(position)
         //   start = newValue
        }
        return markerPositions.toFloatArray()
    }
}