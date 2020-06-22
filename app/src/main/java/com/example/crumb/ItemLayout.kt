package com.example.crumb

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.LinearLayout

class ItemLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0 ) : LinearLayout(context, attrs, defStyleAttr) {

    val rect = RectF()
    val padding = 0f
    val corners = 25f
    val border = 5f
    val tail = 25f
    val paint = Paint().apply {color = Color.RED; style = Paint.Style.STROKE; strokeWidth = border}

    init{
        this.setWillNotDraw(false)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rect.top = 0f + padding
        rect.bottom = (h - tail) - padding
        rect.left = padding
        rect.right = w - padding
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width =  MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width,height + tail.toInt())
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

}