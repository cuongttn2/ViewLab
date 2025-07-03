package com.example.viewlab.picker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.util.AttributeSet
import com.example.viewlab.R
import kotlin.math.floor

class HueSlider(context: Context, attributeSet: AttributeSet?) :
    BaseSlider(context, attributeSet) {

    constructor(context: Context) : this(context, null)

    private val hsvHolder = FloatArray(3)
    private lateinit var gradientShader: LinearGradient

    var hue: Float = 30f
        set(value) {
            field = value
            isSliderChangingState = true
            circleXFactor = (value / 360f)
            calculateBounds(width.toFloat(), height.toFloat())
            invalidate()
        }
        get() =
            hsvHolder[0]

    private var onHueChanged: ((hue: Float, argbColor: Int) -> Unit)? = null
    private var onHueChangedListener: OnHueChangedListener? = null
    private var onHueChangeEnd: ((hue: Float, argbColor: Int) -> Unit)? = null
    private var onHueChangeEndListener: OnHueChangeEndListener? = null

    /**
     * Bitmap that is 360 pixels and each pixels represents a degree in hue.
     */
    private val hueBitmap: Bitmap = BitmapFactory.decodeResource(
        resources,
        R.drawable.full_hue_bitmap,
        BitmapFactory.Options().apply {
            inScaled = false
        })

    override fun initializeSliderPaint() {
//        val colors = intArrayOf(
//            "#FFFF0000".toColorInt(),
//            "#FEFFFF00".toColorInt(),
//            "#FE00FF00".toColorInt(),
//            "#FE00FFFF".toColorInt(),
//            "#FE0000FF".toColorInt(),
//            "#FEFF00FF".toColorInt(),
//            "#FFFF0000".toColorInt(),
//        )
//        val positions = floatArrayOf(0f, 0.167f, 0.333f, 0.5f, 0.667f, 0.833f, 1f)
        val (colors, positions) = generateHueGradient(7)
        gradientShader = LinearGradient(
            drawingStart, drawingTop, widthF, drawingTop, colors, positions,
            Shader.TileMode.CLAMP
        )

        linePaint.shader = gradientShader
    }

    override fun onCirclePositionChanged(circlePositionX: Float, circlePositionY: Float) {
        circleColor = calculateColorAt(circlePositionX)
        callListeners(hsvHolder[0], circleColor)
        invalidate()
    }

    override fun calculateBounds(targetWidth: Float, targetHeight: Float) {
        super.calculateBounds(targetWidth, targetHeight)
        circleColor = calculateColorAt(circleX)
    }

    override fun onDragEnded(lastX: Float, lastY: Float) {
        callOnLastEventListener(hsvHolder[0], circleColor)
    }

    fun setOnHueChangedListener(onHueChangedListener: OnHueChangedListener) {
        this.onHueChangedListener = onHueChangedListener
    }

    fun setOnHueChangedListener(onHueChangedListener: ((hue: Float, argbColor: Int) -> Unit)) {
        onHueChanged = onHueChangedListener
    }

    fun setOnHueChangeEndListener(listener: ((hue: Float, argbColor: Int) -> Unit)) {
        onHueChangeEnd = listener
    }

    fun setOnHueChangeEndListener(listener: OnHueChangeEndListener) {
        onHueChangeEndListener = listener
    }

    private fun callListeners(hue: Float, argbColor: Int) {
        onHueChanged?.invoke(hue, argbColor)
        onHueChangedListener?.onHueChanged(hue, argbColor)
    }

    private fun calculateColorAt(ex: Float): Int {
        // Closer the indicator (handle) to the end of view the higher is hue value.
        hsvHolder[0] = floor(360f * (ex - drawingStart) / (widthF - drawingStart))
        hsvHolder[1] = 1f
        hsvHolder[2] = 1f
        return Color.HSVToColor(hsvHolder)
    }

    private fun callOnLastEventListener(hue: Float, argbColor: Int) {
        onHueChangeEnd?.invoke(hue, argbColor)
        onHueChangeEndListener?.onHueChangeEnd(hue, argbColor)
    }

    interface OnHueChangedListener {
        fun onHueChanged(hue: Float, argbColor: Int)
    }

    interface OnHueChangeEndListener {
        fun onHueChangeEnd(hue: Float, argbColor: Int)
    }

    /**
     * Create color array and position for Hue gradient.
     * @param steps Number of hue samples (≥ 2). 361 → one color per degree (very smooth).
     * @return Pair(colors, positions)
     */
    fun generateHueGradient(steps: Int): Pair<IntArray, FloatArray> {
        require(steps >= 2) { "steps must be at least 2" }

        val colors = IntArray(steps)
        val positions = FloatArray(steps)

        for (i in 0 until steps) {
            val hue = i * 360f / (steps - 1)          // 0 … 360
            val hsv = floatArrayOf(hue, 1f, 1f)       // S=1, V=1
            colors[i] = Color.HSVToColor(hsv)      // ARGB int
            positions[i] = i.toFloat() / (steps - 1)  // 0 … 1
        }
        return colors to positions
    }

    companion object {

    }

}