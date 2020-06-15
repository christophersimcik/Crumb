package com.example.tightboules

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.RelativeLayout

class MainRelativeLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    var lineWeight = 5.0f
    var offset = 10.0f
    val paint = Paint().apply { isAntiAlias = true; strokeWidth = lineWeight }
    var colorStroke = resources.getColor(R.color.default_dark, null)
    var rounding = 50.0f
    val buttonSize = resources.getDimension(R.dimen.create_button_size)
    init {
        this.setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        drawBackground(canvas)
        super.onDraw(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun drawBackground(canvas: Canvas) {
        paint.style = Paint.Style.STROKE
        paint.strokeMiter = 10f
        paint.color = colorStroke
        paint.isAntiAlias = true
        canvas.drawPath(makeShape(), paint)

    }

    private fun makeShape(): Path {
        val path = Path()
        val startF = offset
        val widthF = width - offset
        val heightF = height - offset
        val button = buttonSize

        path.moveTo(widthF - (rounding + offset), heightF)
        path.lineTo(rounding + offset, heightF)
        path.quadTo(startF, heightF, startF, heightF - rounding)
        path.lineTo(startF, rounding + offset)
        path.quadTo(startF, startF, rounding + offset, startF)
        path.lineTo(widthF - rounding, startF)
        path.quadTo(widthF, startF, widthF, rounding + offset)
        path.lineTo(widthF, heightF - (rounding + offset))
        path.quadTo(widthF,heightF,widthF - (rounding + offset),heightF)
        return path
    }

    fun play(bool: Boolean) {
        val animator: ValueAnimator
        if (bool) {
            animator = ValueAnimator.ofFloat(rounding, rounding / 10.0f)
        } else {
            animator = ValueAnimator.ofFloat(rounding, rounding * 10.0f)
        }
        animator.duration = 350
        animator.addUpdateListener { valueAnimator ->
            rounding = valueAnimator.animatedValue as Float
            invalidate()

        }
        animator.start()
    }

}