package com.diegopizzo.androidmlkit.view.camera

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.diegopizzo.androidmlkit.view.camera.GraphicOverlay.Graphic
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark
import kotlin.math.max
import kotlin.math.min

class FaceDetectorLandmarkOverlay constructor(overlay: GraphicOverlay, private val face: Face?) :
    Graphic(overlay) {

    private val rectPaint: Paint
    private val labelPaint: Paint
    private val facePositionPaint: Paint

    init {
        rectPaint = Paint()
        rectPaint.color = Color.WHITE
        rectPaint.style = Paint.Style.STROKE
        facePositionPaint = Paint()
        facePositionPaint.color = Color.WHITE
        labelPaint = Paint()
        labelPaint.color = Color.WHITE
        labelPaint.style = Paint.Style.FILL
        // Redraw the overlay, as this graphic has been added.
        postInvalidate()
    }

    override fun draw(canvas: Canvas) {
        drawRect(RectF(face?.boundingBox), canvas)

        drawFaceLandmark(canvas, FaceLandmark.LEFT_EAR)
        drawFaceLandmark(canvas, FaceLandmark.RIGHT_EAR)
        drawFaceLandmark(canvas, FaceLandmark.NOSE_BASE)
        drawFaceLandmark(canvas, FaceLandmark.RIGHT_EYE)
        drawFaceLandmark(canvas, FaceLandmark.LEFT_EYE)
        drawFaceLandmark(canvas, FaceLandmark.RIGHT_CHEEK)
        drawFaceLandmark(canvas, FaceLandmark.LEFT_CHEEK)
        drawFaceLandmark(canvas, FaceLandmark.MOUTH_LEFT)
        drawFaceLandmark(canvas, FaceLandmark.MOUTH_RIGHT)
        drawFaceLandmark(canvas, FaceLandmark.MOUTH_BOTTOM)
    }

    private fun drawFaceLandmark(canvas: Canvas, @FaceLandmark.LandmarkType landmarkType: Int) {
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
        canvas.drawRect(
            rect.left - STROKE_WIDTH,
            rect.top,
            rect.left + STROKE_WIDTH,
            rect.top,
            labelPaint
        )
    }

    companion object {
        private const val FACE_POSITION_RADIUS = 8.0f
        private const val STROKE_WIDTH = 4.0f
    }
}