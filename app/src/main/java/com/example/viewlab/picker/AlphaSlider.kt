package com.example.viewlab.picker

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet

class AlphaSlider(context: Context, attributeSet: AttributeSet?) :
    BaseSlider(context, attributeSet) {

    constructor(context: Context) : this(context, null)

    private lateinit var alphaLinearGradient: LinearGradient

    var selectedColor = Color.RED
        set(value) {
            if (field != value) {
                field = value
                initializeSliderPaint()
                invalidate()
            }
        }

    private var _alphaValue = 1f

    var alphaValue: Float = 1f
        set(value) {
            field = value
            isSliderChangingState = true
            circleXFactor = value
            calculateBounds(width.toFloat(), height.toFloat())
            invalidate()
        }
        get() = _alphaValue


    private var onAlphaChanged: ((alpha: Float) -> Unit)? = null
    private var onAlphaChangedListener: OnAlphaChangedListener? = null

    private var onAlphaChangeEnd: ((alpha: Float) -> Unit)? = null
    private var onAlphaChangeEndListener: OnAlphaChangeEndListener? = null

    override fun initializeSliderPaint() {
        alphaLinearGradient = LinearGradient(
            drawingStart,
            0f,
            widthF,
            0f,
            Color.argb(
                0,
                Color.red(selectedColor),
                Color.green(selectedColor),
                Color.blue(selectedColor)
            ),
            selectedColor,
            Shader.TileMode.MIRROR
        )

        circleColor =
            Color.argb(
                (255 * _alphaValue).toInt(),
                Color.red(selectedColor),
                Color.green(selectedColor),
                Color.blue(selectedColor)
            )


        linePaint.shader = alphaLinearGradient

    }

    override fun onSaveInstanceState(): Parcelable {
        return (super.onSaveInstanceState() as Bundle).apply {
            putInt(SELECTED_COLOR_KEY, selectedColor)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        selectedColor = (state as Bundle).getInt(SELECTED_COLOR_KEY)
        super.onRestoreInstanceState(state)
    }

    override fun onCirclePositionChanged(circlePositionX: Float, circlePositionY: Float) {
        _alphaValue = calculateAlphaAt(circlePositionX).coerceIn(0f, 1f)
        circleColor = calculateCircleColor()
        callListeners(_alphaValue)

        invalidate()
    }

    private fun callListeners(alpha: Float) {
        onAlphaChanged?.invoke(alpha)
        onAlphaChangedListener?.onAlphaChanged(alpha)
    }

    private fun calculateCircleColor(): Int {
        return Color.argb(
            (255 * _alphaValue).toInt(),
            Color.red(selectedColor),
            Color.green(selectedColor),
            Color.blue(selectedColor)
        )
    }

    private fun calculateAlphaAt(ex: Float): Float {
        return (ex - drawingStart) / (widthF - drawingStart)
    }

    override fun onDragEnded(lastX: Float, lastY: Float) {
        callEndListeners(_alphaValue)
    }

    override fun calculateBounds(targetWidth: Float, targetHeight: Float) {
        super.calculateBounds(targetWidth, targetHeight)

        _alphaValue = calculateAlphaAt(circleX).coerceIn(0f, 1f)

        circleColor = calculateCircleColor()

    }


    private fun callEndListeners(alpha: Float) {
        onAlphaChangeEndListener?.onAlphaChangeEnd(alpha)
        onAlphaChangeEnd?.invoke(alpha)
    }

    fun setOnAlphaChangedListener(onAlphaChangedListener: OnAlphaChangedListener) {
        this.onAlphaChangedListener = onAlphaChangedListener
    }

    fun setOnAlphaChangedListener(onAlphaChangedListener: ((alpha: Float) -> Unit)) {
        onAlphaChanged = onAlphaChangedListener
    }

    fun setOnAlphaChangeEndListener(listener: ((alpha: Float) -> Unit)) {
        onAlphaChangeEnd = listener
    }

    fun setOnAlphaChangeEndListener(listener: OnAlphaChangeEndListener) {
        onAlphaChangeEndListener = listener
    }


    interface OnAlphaChangedListener {
        fun onAlphaChanged(alpha: Float)
    }

    interface OnAlphaChangeEndListener {
        fun onAlphaChangeEnd(alpha: Float)
    }

    companion object {
        private const val SELECTED_COLOR_KEY = "sel"
    }

}