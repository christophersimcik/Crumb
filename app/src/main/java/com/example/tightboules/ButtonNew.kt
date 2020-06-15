package com.example.tightboules

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.AttributeSet
import android.util.SizeF
import android.view.View
import androidx.core.animation.addListener
import android.graphics.drawable.GradientDrawable
import android.view.MotionEvent

class ButtonNew @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), ScrollingCallback {

    companion object {
        val RECIPES = 0
        val STEPS = 1
        val DETAIL = 2
        val PLAY = 3
    }

    var radius = 0f
    var playing = false

    var mySize = SizeF(
        context.resources.getDimension(R.dimen.my_width),
        context.resources.getDimension(R.dimen.my_height)
    )
    var isDetail: Boolean = false
    var imageRotation = 0f
    private var buttonPressed = false
    private var isRotating = false
    var mode = 0
    val strokeSize = 10

    val circleSz = SizeF(
        context.resources.getDimension(R.dimen.circle_width),
        context.resources.getDimension(R.dimen.circle_height)
    )
    val squareSz = SizeF(
        context.resources.getDimension(R.dimen.square_width),
        context.resources.getDimension(R.dimen.square_height)
    )
    val rectSz = SizeF(
        context.resources.getDimension(R.dimen.rect_width),
        context.resources.getDimension(R.dimen.rect_height)
    )

    private val colorGreen = context.resources.getColor(R.color.green, null)
    private val colorLtGray = context.resources.getColor(R.color.inactive_light, null)
    private val colorActive = context.resources.getColor(R.color.red_accent, null)
    private val colorDefault = context.resources.getColor(R.color.default_dark, null)

    fun toCircle() {
        if (!playing) {
            val animatorSet = AnimatorSet()
            animatorSet.duration = 250
            animatorSet.addListener(onEnd = {
                playing = false; //rectify()
            })
            animatorSet.playTogether(
                toCircleWidth(),
                toCircleHeight(),
                cornerRadiiToCircle()
            )
            animatorSet.start()
            playing = true
            isDetail = false
        }
    }

    fun toSquare() {
        if (!playing) {
            val animatorSet = AnimatorSet()
            animatorSet.duration = 250
            animatorSet.addListener(onEnd = {
                playing = false; //rectify()
            })
            animatorSet.playTogether(
                toSquareWidth(),
                toSquareHeight(),
                cornerRadiiToSquare()
            )
            animatorSet.start()
            playing = true
            isDetail = false
        }
    }

    fun toRect() {
        if (!playing) {
            val animatorSet = AnimatorSet()
            animatorSet.duration = 250
            animatorSet.addListener(onEnd = {
                playing = false; //rectify()
            })
            animatorSet.playTogether(
                toRectWidth(),
                toRectHeight(),
                cornerRadiiToRect()
            )
            animatorSet.start()
            playing = true
            isDetail = true
        }
    }

    private fun toCircleHeight(): ValueAnimator {
        val animator = ValueAnimator.ofFloat(height.toFloat(), circleSz.height)
        animator.addUpdateListener { valueAnimator ->
            mySize = SizeF(mySize.width, valueAnimator.animatedValue as Float)
            val layoutParams = this.layoutParams
            layoutParams.width = mySize.width.toInt()
            layoutParams.height = mySize.height.toInt()
            this.layoutParams = layoutParams
            invalidate()
        }
        return animator
    }

    private fun toCircleWidth(): ValueAnimator {
        val animator = ValueAnimator.ofFloat(width.toFloat(), circleSz.width)
        animator.addUpdateListener { valueAnimator ->
            mySize = SizeF(valueAnimator.animatedValue as Float, mySize.height)
            val layoutParams = this.layoutParams
            layoutParams.width = mySize.width.toInt()
            layoutParams.height = mySize.height.toInt()
            this.layoutParams = layoutParams
            invalidate()
        }
        return animator
    }

    private fun cornerRadiiToCircle(): ValueAnimator {
        val animator = ValueAnimator.ofFloat(radius, circleSz.width / 2f)
        animator.addUpdateListener { valueAnimator ->
            radius = valueAnimator.animatedValue as Float
            background = createDrawable().mutate()
            invalidate()
        }
        return animator
    }

    private fun toSquareHeight(): ValueAnimator {
        val animator = ValueAnimator.ofFloat(height.toFloat(), squareSz.height)
        animator.addUpdateListener { valueAnimator ->
            mySize = SizeF(mySize.width, valueAnimator.animatedValue as Float)
            val layoutParams = this.layoutParams
            layoutParams.width = mySize.width.toInt()
            layoutParams.height = mySize.height.toInt()
            this.layoutParams = layoutParams
            invalidate()
        }
        return animator
    }

    private fun toSquareWidth(): ValueAnimator {
        val animator = ValueAnimator.ofFloat(width.toFloat(), squareSz.width)
        animator.addUpdateListener { valueAnimator ->
            mySize = SizeF(valueAnimator.animatedValue as Float, mySize.height)
            val layoutParams = this.layoutParams
            layoutParams.width = mySize.width.toInt()
            layoutParams.height = mySize.height.toInt()
            this.layoutParams = layoutParams
            invalidate()
        }
        return animator
    }

    private fun cornerRadiiToSquare(): ValueAnimator {
        val animator = ValueAnimator.ofFloat(radius, 25f)
        animator.addUpdateListener { valueAnimator ->
            radius = valueAnimator.animatedValue as Float
            background = createDrawable().mutate()
            invalidate()
        }
        return animator
    }

    private fun toRectHeight(): ValueAnimator {
        val animator = ValueAnimator.ofFloat(height.toFloat(), rectSz.height)
        animator.addUpdateListener { valueAnimator ->
            mySize = SizeF(mySize.width, valueAnimator.animatedValue as Float)
            val layoutParams = this.layoutParams
            layoutParams.width = mySize.width.toInt()
            layoutParams.height = mySize.height.toInt()
            this.layoutParams = layoutParams
            invalidate()
        }
        return animator
    }

    private fun toRectWidth(): ValueAnimator {
        val animator = ValueAnimator.ofFloat(width.toFloat(), rectSz.width)
        animator.addUpdateListener { valueAnimator ->
            mySize = SizeF(valueAnimator.animatedValue as Float, mySize.height)
            val layoutParams = this.layoutParams
            layoutParams.width = mySize.width.toInt()
            layoutParams.height = mySize.height.toInt()
            this.layoutParams = layoutParams
            invalidate()
        }
        return animator
    }

    private fun cornerRadiiToRect(): ValueAnimator {
        val animator = ValueAnimator.ofFloat(radius, 15f)
        animator.addUpdateListener { valueAnimator ->
            radius = valueAnimator.animatedValue as Float
            background = createDrawable().mutate()
        }
        return animator
    }

    fun animatePlusSymbol() {
        System.out.println("image rotation = " + imageRotation)
        val animator = ValueAnimator.ofFloat(0f, 90f)
        animator.addListener(onEnd = {
            isRotating = false; imageRotation = 0f
        })
        animator.addUpdateListener { valueAnimator ->
            imageRotation = valueAnimator.animatedValue as Float
            System.out.println("image rotation = " + imageRotation)
            invalidate()
        }
        animator.start()
        isRotating = true
    }

    private fun createDrawable(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(resources.getColor(R.color.default_light, null))
            if (buttonPressed) {
                scaleX = .9f
                scaleY = .9f
                setStroke(strokeSize, colorActive)
            } else {
                scaleX = 1f
                scaleY = 1f
                setStroke(strokeSize, colorDefault)
            }
            cornerRadii =
                floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(mySize.width.toInt(), mySize.height.toInt())
    }

    override fun isScrolling() {
        this.visibility = INVISIBLE
    }

    override fun isNotScrolling() {
        this.visibility = VISIBLE
    }

    override fun onDraw(canvas: Canvas?) {
        val paint = Paint().apply {

            when (mode) {
                RECIPES -> {
                    if (isRotating) {
                        color = colorActive
                    } else {
                        color = colorDefault
                    }
                }
                STEPS -> {
                    if (isRotating) {
                        color = colorActive
                    } else {
                        color = colorDefault
                    }
                }
            }
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 10.0f
        }
        if (isDetail) {
            makeText(paint, canvas)
        } else {
            canvas?.rotate(imageRotation, mySize.width.toFloat() / 2f, mySize.width.toFloat() / 2f)
            canvas?.drawPath(createImage(), paint)
        }

        super.onDraw(canvas)
    }

    fun makeText(paint: Paint, canvas: Canvas?) {
        paint.style = Paint.Style.FILL
        val color = paint.color
        if (buttonPressed) {
            paint.color = colorLtGray
        } else {
            paint.color = colorGreen
        }
        val rect = Rect()
        val text = "START"
        paint.getTextBounds(text, 0, text.length, rect)
        paint.textSize = 50f
        paint.textAlign = Paint.Align.CENTER
        canvas?.drawText(text, this.width / 2f, height / 2f + rect.height() * 2, paint)
        paint.style = Paint.Style.STROKE
        paint.color = color
    }

    private fun createImage(): Path {
        val plusSign = Path()
        val mid = this.width.toFloat() / 2f
        plusSign.moveTo(mid, mid - mid * .50f)
        plusSign.lineTo(mid, mid + mid * .50f)
        plusSign.moveTo(mid - mid * .50f, mid)
        plusSign.lineTo(mid + mid * .50f, mid)
        return plusSign
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                buttonPressed = true
                background = createDrawable()
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                buttonPressed = false
                background = createDrawable()
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.rawX < left || event.x > right || event.y < top || event.y > bottom) {
                    buttonPressed = false
                    background = createDrawable()
                    invalidate()
                }
            }
        }
        return super.onTouchEvent(event)
    }


}