package com.diegopizzo.androidmlkit.view.camera

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.diegopizzo.androidmlkit.view.camera.GraphicOverlay.Graphic
import com.diegopizzo.androidmlkit.view.navigation.ScanningType
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark
import com.google.mlkit.vision.objects.DetectedObject
import kotlin.math.max
import kotlin.math.min

class CustomDataDetectionOverlay constructor(
    overlay: GraphicOverlay,
    private val scannedResult: List<Any?>,
    private val scanningType: ScanningType
) :
    Graphic(overlay) {

    private val rectPaint: Paint
    private val labelPaint: Paint
    private val facePositionPaint: Paint

    init {
        rectPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
        }
        facePositionPaint = Paint().apply {
            color = Color.WHITE
        }
        labelPaint = Paint().apply {
            color = Color.WHITE
            textSize = 50.0F
            style = Paint.Style.FILL
        }
        // Redraw the overlay, as this graphic has been added.
        postInvalidate()
    }

    override fun draw(canvas: Canvas) {
        try {
            when (scanningType) {
                ScanningType.FACE_DETECTION -> {
                    val faces = scannedResult as List<Face?>
                    faces.map {
                        drawRect(RectF(it?.boundingBox), canvas)

                        drawFaceLandmark(it, canvas, FaceLandmark.LEFT_EAR)
                        drawFaceLandmark(it, canvas, FaceLandmark.RIGHT_EAR)
                        drawFaceLandmark(it, canvas, FaceLandmark.NOSE_BASE)
                        drawFaceLandmark(it, canvas, FaceLandmark.RIGHT_EYE)
                        drawFaceLandmark(it, canvas, FaceLandmark.LEFT_EYE)
                        drawFaceLandmark(it, canvas, FaceLandmark.RIGHT_CHEEK)
                        drawFaceLandmark(it, canvas, FaceLandmark.LEFT_CHEEK)
                        drawFaceLandmark(it, canvas, FaceLandmark.MOUTH_LEFT)
                        drawFaceLandmark(it, canvas, FaceLandmark.MOUTH_RIGHT)
                        drawFaceLandmark(it, canvas, FaceLandmark.MOUTH_BOTTOM)
                    }
                }
                ScanningType.OBJECT_DETECTION -> {
                    val objects = scannedResult as List<DetectedObject?>
                    objects.map {
                        val box = it?.boundingBox
                        drawRect(RectF(box), canvas)
                        it?.labels?.map { label ->
                            drawText(label?.text ?: "", RectF(box), canvas)
                        }
                    }
                }
            }
        } catch (e: ClassCastException) {
            return
        }
    }

    private fun drawFaceLandmark(
        face: Face?,
        canvas: Canvas,
        @FaceLandmark.LandmarkType landmarkType: Int
    ) {
        val faceLandmark = face?.getLandmark(landmarkType)
        if (faceLandmark != null) {
            canvas.drawCircle(
                translateX(faceLandmark.position.x),
                translateY(faceLandmark.position.y),
                FACE_POSITION_RADIUS,
                facePositionPaint
            )
        }
    }

    private fun drawRect(rect: RectF, canvas: Canvas) {
        // If the image is flipped, the left will be translated to right, and the right to left.
        val x0 = translateX(rect.left)
        val x1 = translateX(rect.right)
        rect.left = min(x0, x1)
        rect.right = max(x0, x1)
        rect.top = translateY(rect.top)
        rect.bottom = translateY(rect.bottom)
        canvas.drawRect(rect, rectPaint)
    }

    private fun drawText(text: String, rect: RectF, canvas: Canvas) {
        val x0 = translateX(rect.left)
        val x1 = translateX(rect.right)
        canvas.drawText(
            text,
            x0,
            x1,
            labelPaint
        )
    }

    companion object {
        private const val FACE_POSITION_RADIUS = 8.0f
        private const val STROKE_WIDTH = 4.0f
    }
}