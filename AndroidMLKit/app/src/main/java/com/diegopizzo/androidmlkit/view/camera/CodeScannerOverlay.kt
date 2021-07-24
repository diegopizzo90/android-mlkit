package com.diegopizzo.androidmlkit.view.camera

import android.content.Context
import android.graphics.*
import android.graphics.Paint.Style
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.diegopizzo.androidmlkit.R

class CodeScannerOverlay(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val boxPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.reticle_stroke_gray)
        style = Style.STROKE
        strokeWidth =
            context.resources.getDimensionPixelOffset(R.dimen.frame_stroke_width).toFloat()
    }

    private val scrimPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.reticle_background)
    }

    private val eraserPaint: Paint = Paint().apply {
        strokeWidth = boxPaint.strokeWidth
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val boxCornerRadius: Float =
        context.resources.getDimensionPixelOffset(R.dimen.frame_corner_radius).toFloat()

    private var boxRect: RectF? = null

    private val ripplePaint: Paint = Paint().apply {
        style = Style.STROKE
        color = ContextCompat.getColor(context, R.color.ripple)
    }

    private val rippleSizeOffset: Int =
        resources.getDimensionPixelOffset(R.dimen.frame_ripple_size_offset)
    private val rippleStrokeWidth: Int =
        resources.getDimensionPixelOffset(R.dimen.frame_ripple_stroke_width)
    private val rippleAlpha: Int = ripplePaint.alpha
    private val cameraAnimator = CameraAnimator(this)
    private val boxFormat: BoxFormat
    private val boxWidthPercentage: Int
    private val boxHeightPercentage: Int

    init {
        val styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.CodeScannerOverlay)
        boxFormat = fromValueToEnum(
            styledAttributes.getInt(
                R.styleable.CodeScannerOverlay_box_format,
                BoxFormat.Barcode.value
            )
        )
        boxWidthPercentage = styledAttributes.getInt(
            R.styleable.CodeScannerOverlay_box_width_percentage,
            getDefaultBoxDimension(boxFormat).first
        )
        boxHeightPercentage = styledAttributes.getInt(
            R.styleable.CodeScannerOverlay_box_height_percentage,
            getDefaultBoxDimension(boxFormat).second
        )
        styledAttributes.recycle()
    }

    private fun getDefaultBoxDimension(boxFormat: BoxFormat): Pair<Int, Int> {
        return when (boxFormat) {
            BoxFormat.Barcode -> Pair(BARCODE_WIDTH_DEFAULT, BARCODE_HEIGHT_DEFAULT)
            BoxFormat.QRCode -> Pair(QR_CODE_WIDTH_DEFAULT, QR_CODE_HEIGHT_DEFAULT)
        }
    }

    /**
     * Create box overlay
     */
    fun createView() {
        val overlayWidth = width.toFloat()
        val overlayHeight = height.toFloat()

        val (boxWidth, boxHeight) = Pair(
            overlayWidth * boxWidthPercentage / 100,
            overlayHeight * boxHeightPercentage / 100
        )

        val cx = overlayWidth / 2
        val cy = overlayHeight / 2
        boxRect =
            RectF(cx - boxWidth / 2, cy - boxHeight / 2, cx + boxWidth / 2, cy + boxHeight / 2)

        invalidate()
    }

    /**
     * Used for sensor animation
     * https://github.com/googlesamples/mlkit/blob/master/android/material-showcase/screenshots/live_barcode.gif
     */
    fun onCodeScanning() {
        boxPaint.color = ContextCompat.getColor(context, R.color.reticle_stroke_gray)
        startSensorAnimation()
    }

    /**
     * Stop sensor animation and highlight the box borders
     */
    fun onCodeScanned() {
        stopSensorAnimation()
        boxPaint.color = ContextCompat.getColor(context, R.color.reticle_stroke_light)
        invalidate()
    }

    /**
     * Calculate and return the percentage that needs to be cropped
     */
    fun percentageWidthCropped(): Int {
        return 100 - boxWidthPercentage
    }

    fun percentageHeightCropped(): Int {
        return 100 - boxHeightPercentage
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        boxRect?.let {
            // Draws the dark background scrim and leaves the box area clear.
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), scrimPaint)
            // As the stroke is always centered, so erase twice with FILL and STROKE respectively to clear
            // all area that the box rect would occupy.
            eraserPaint.style = Style.FILL
            canvas.drawRoundRect(it, boxCornerRadius, boxCornerRadius, eraserPaint)
            eraserPaint.style = Style.STROKE
            canvas.drawRoundRect(it, boxCornerRadius, boxCornerRadius, eraserPaint)
            // Draws the box.
            canvas.drawRoundRect(it, boxCornerRadius, boxCornerRadius, boxPaint)
            // Draws the ripple to simulate the breathing animation effect.
            ripplePaint.alpha = (rippleAlpha * cameraAnimator.rippleAlphaScale).toInt()
            ripplePaint.strokeWidth = rippleStrokeWidth * cameraAnimator.rippleStrokeWidthScale
            val offset = rippleSizeOffset * cameraAnimator.rippleSizeScale
            val rippleRect = RectF(
                it.left - offset,
                it.top - offset,
                it.right + offset,
                it.bottom + offset
            )
            canvas.drawRoundRect(rippleRect, boxCornerRadius, boxCornerRadius, ripplePaint)
        }
    }

    private fun startSensorAnimation() {
        cameraAnimator.start()
    }

    private fun stopSensorAnimation() {
        cameraAnimator.cancel()
    }


    enum class BoxFormat(val value: Int) {
        Barcode(0), QRCode(1)
    }

    companion object {
        private fun fromValueToEnum(value: Int): BoxFormat {
            BoxFormat.values().map { if (it.value == value) return it }
            throw IllegalArgumentException("no enum constant")
        }

        private const val BARCODE_WIDTH_DEFAULT = 85
        private const val BARCODE_HEIGHT_DEFAULT = 20

        private const val QR_CODE_WIDTH_DEFAULT = 80
        private const val QR_CODE_HEIGHT_DEFAULT = 36
    }
}
