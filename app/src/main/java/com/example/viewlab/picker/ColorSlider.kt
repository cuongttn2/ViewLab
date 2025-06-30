package com.example.viewlab.picker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.viewlab.R
import kotlin.math.max

abstract class ColorSlider(context: Context, attributeSet: AttributeSet?) :
    View(context, attributeSet) {

    constructor(context: Context) : this(context, null)

    /**
     * Slider bar brush. Initialized with ANTI_ALIAS_FLAG, STROKE and strokeCap depending on lineStrokeCap.
     * */
    protected val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    /**
     * Indicator circle brush. Default fill color is red for easy visibility when not overwritten.
     * */
    protected val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.RED
    }

    /**
     * Width of the drawing space (minus padding) of the View.
     * */
    protected var widthF = 0f

    /**
     * Height of the drawing space (minus padding) of the View.
     * */
    protected var heightF = 0f

    /**
     * Half height slider (convenient for drawing axis and circle radius).
     * */
    protected var heightHalf = 0f

    /**
     * The x coordinate where the slider starts (including padding + circle radius if strokeCap != BUTT).
     * */
    protected var drawingStart = 0f

    /**
     * The y coordinate of the slider axis (horizontal line), centered in the drawing area in height.
     * */
    protected var drawingTop = 0f

    /**
     * ½ độ dày (strokeWidth) của linePaint. Dùng để cộng/trừ khi strokeCap không phải BUTT (đầu tròn/vuông).
     * */
    protected var strokeWidthHalf = 0f

    /**
     * The exact coordinates of the circle's center on the screen.
     * */
    protected var circleX = 0f
    protected var circleY = 0f

    /**
     * Position ratio (0 – 1) to length/width. Used to restore position after screen rotation or resizing.
     * */
    protected var circleXFactor = 0f
    protected var circleYFactor = 0f

    protected var circleColor: Int = Color.TRANSPARENT

    /**
     * Mark the first layout to place the indicator in the default position (end of the bar).
     * */
    protected var isFirstTimeLaying = true

    /**
     * Report that View has just been restored from onRestoreInstanceState → use circleX/Y Factor to recalculate position.
     * */
    protected var isRestoredState = false

    /**
     * Indicates that the slider mode is changing (e.g. from S to V in the color picker) so it should stay in a reasonable position.
     * */
    protected var isSliderChangingState = false

    /**
     * true if the height receives wrap_content, the View will add defaultPaddingVertical to give it a nice space.
     * */
    private var isWrapContent = false

    /**
     * Update linePaint's strokeCap, then requestLayout() (since the round tip changes the drawing width).
     * */
    var lineStrokeCap = Paint.Cap.ROUND
        set(value) {
            field = value
            linePaint.strokeCap = field
            requestLayout()
        }

    /**
     * Change the circle border color and call invalidate() to redraw it.
     * */
    var strokeColor = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Indicator border thickness; change then invalidate().
     * */
    var strokeSize = dp(2)
        set(value) {
            field = value
            invalidate()
        }

    /**
     * 12 dp – extra space on both ends when wrapping_content.
     * */
    private var defaultPaddingVertical = dp(12)

    /**
     * 48 dp – minimum height when wrapping_content.
     * */
    private var wrapContentSize = dp(48).toInt()

    init {
        context.theme.obtainStyledAttributes(attributeSet, R.styleable.MananSlider, 0, 0).apply {
            try {
                lineStrokeCap = Paint.Cap.entries.toTypedArray()[getInt(
                    R.styleable.MananSlider_sliderBarStrokeCap,
                    Paint.Cap.ROUND.ordinal
                )]

                strokeSize = getDimension(R.styleable.MananSlider_sliderStrokeSize, strokeSize)

                strokeColor = getColor(R.styleable.MananSlider_sliderStrokeColor, strokeColor)

            } finally {
                recycle()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measureWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measureHeight = MeasureSpec.getSize(heightMeasureSpec)

        isWrapContent = false

        val finalHeight = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> {
                measureHeight
            }

            MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> {
                isWrapContent = true
                wrapContentSize
            }

            else -> {
                suggestedMinimumHeight
            }
        }

        setMeasuredDimension(
            max(measureWidth, suggestedMinimumWidth),
            max(finalHeight, suggestedMinimumHeight)
        )

    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (changed) {
            calculateBounds(width.toFloat(), height.toFloat())
            initializeSliderPaint()
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

    }

    /**
     * Calculates bounds of drawing elements.
     * @param targetWidth limiting width of drawings, often width of the view is supplied.
     * @param targetHeight limiting height of drawings, often height of the view is supplied.
     */
    protected open fun calculateBounds(targetWidth: Float, targetHeight: Float) {
        val fx = (circleX - drawingStart) / (widthF - drawingStart)
        val fy = (circleY - drawingTop) / (heightF - drawingTop)

        heightF = targetHeight - paddingBottom - paddingTop

        if (isWrapContent) {
            heightF -= (defaultPaddingVertical * 2f)
        }

        heightHalf = heightF * 0.5f
        linePaint.strokeWidth = heightHalf

        widthF = targetWidth - paddingEnd - heightHalf

        drawingStart = heightHalf + paddingStart
        drawingTop = heightHalf + paddingTop

        if (isWrapContent) {
            drawingTop += defaultPaddingVertical
        }

        // If it's first layout pass then set indicator to be at top-end of the color picker amd sliders.
        if (isFirstTimeLaying) {
            isFirstTimeLaying = false
            circleX = widthF
            circleY = drawingTop
        } else if (isRestoredState || isSliderChangingState) {
            // Use the factors that are returned in 'onRestoreInstanceState' to correctly
            // calculate the position of indicators in case of screen rotation.
            circleX = ((widthF - drawingStart) * circleXFactor) + drawingStart
            circleY = ((heightF - drawingTop) * circleYFactor) + drawingTop

            circleXFactor = 0f
            circleYFactor = 0f

            isRestoredState = false
            isSliderChangingState = false
        } else {
            // Calculate position of indicator when a size change happens on view.
            circleX = ((widthF - drawingStart) * fx) + drawingStart
            circleY = ((heightF - drawingTop) * fy) + drawingTop
        }

        strokeWidthHalf = if (linePaint.strokeCap != Paint.Cap.BUTT) {
            // If paint cap is not BUTT then add half the width of stroke at start and end of line.
            linePaint.strokeWidth * 0.5f
        } else {
            0f
        }


    }

    protected open fun initializeSliderPaint() {

    }

    companion object {
        /**
         * The key name stores the X/Y indicator position ratio.
         * */
        private const val CIRCLE_X_KEY = "circleX"
        private const val CIRCLE_Y_KEY = "circleY"
        /**
         * Key contains Parcelable root state of parent View (super.onSaveInstanceState()).
         * */
        private const val STATE_KEY = "p"
    }

}