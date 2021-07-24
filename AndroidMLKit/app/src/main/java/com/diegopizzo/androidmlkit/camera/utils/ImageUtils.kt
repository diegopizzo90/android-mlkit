package com.diegopizzo.androidmlkit.camera.utils

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.media.Image
import androidx.annotation.ColorInt
import androidx.camera.core.AspectRatio
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

/**
 * Utility class for manipulating images.
 */
object ImageUtils {
    private val CHANNEL_RANGE = 0 until (1 shl 18)

    private fun convertYuv420888ImageToBitmap(image: Image): Bitmap {
        require(image.format == ImageFormat.YUV_420_888) {
            "Unsupported image format $(image.format)"
        }

        val planes = image.planes

        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        val yuvBytes = planes.map { plane ->
            val buffer = plane.buffer
            val yuvBytes = ByteArray(buffer.capacity())
            buffer[yuvBytes]
            buffer.rewind()  // Be kind…
            yuvBytes
        }

        val yRowStride = planes[0].rowStride
        val uvRowStride = planes[1].rowStride
        val uvPixelStride = planes[1].pixelStride
        val width = image.width
        val height = image.height
        @ColorInt val argb8888 = IntArray(width * height)
        var i = 0
        for (y in 0 until height) {
            val pY = yRowStride * y
            val uvRowStart = uvRowStride * (y shr 1)
            for (x in 0 until width) {
                val uvOffset = (x shr 1) * uvPixelStride
                argb8888[i++] =
                    yuvToRgb(
                        yuvBytes[0][pY + x].toIntUnsigned(),
                        yuvBytes[1][uvRowStart + uvOffset].toIntUnsigned(),
                        yuvBytes[2][uvRowStart + uvOffset].toIntUnsigned()
                    )
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(argb8888, 0, width, 0, 0, width, height)
        return bitmap
    }

    private fun rotateAndCrop(
        bitmap: Bitmap,
        imageRotationDegrees: Int,
        cropRect: Rect
    ): Bitmap {
        val matrix = Matrix()
        matrix.preRotate(imageRotationDegrees.toFloat())
        return Bitmap.createBitmap(
            bitmap,
            cropRect.left,
            cropRect.top,
            cropRect.width(),
            cropRect.height(),
            matrix,
            true
        )
    }

    @ColorInt
    private fun yuvToRgb(nY: Int, nU: Int, nV: Int): Int {
        var nY = nY
        var nU = nU
        var nV = nV
        nY -= 16
        nU -= 128
        nV -= 128
        nY = nY.coerceAtLeast(0)

        var nR = 1192 * nY + 1634 * nV
        var nG = 1192 * nY - 833 * nV - 400 * nU
        var nB = 1192 * nY + 2066 * nU

        // Clamp the values before normalizing them to 8 bits.
        nR = nR.coerceIn(CHANNEL_RANGE) shr 10 and 0xff
        nG = nG.coerceIn(CHANNEL_RANGE) shr 10 and 0xff
        nB = nB.coerceIn(CHANNEL_RANGE) shr 10 and 0xff
        return -0x1000000 or (nR shl 16) or (nG shl 8) or nB
    }

    fun calculateAspectRatio(width: Int, height: Int): Int {
        val previewRatio = ln(max(width, height).toDouble() / min(width, height))
        if (abs(previewRatio - ln(RATIO_4_3_VALUE))
            <= abs(previewRatio - ln(RATIO_16_9_VALUE))
        ) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    fun cropImage(imageToAnalyze: Image, rotationDegrees: Int, desiredWidth: Int, desiredHeight: Int): Bitmap {
        // We requested a setTargetAspectRatio, but it's not guaranteed that's what the camera
        // stack is able to support, so we calculate the actual ratio from the first frame to
        // know how to appropriately crop the image we want to analyze.
        val imageWidth = imageToAnalyze.width
        val imageHeight = imageToAnalyze.height
        val actualAspectRatio = imageWidth / imageHeight
        val imageToAnalyzeBitmap = convertYuv420888ImageToBitmap(imageToAnalyze)

        val cropRect = Rect(0, 0, imageWidth, imageHeight)

        // If the image has a way wider aspect ratio than expected, crop less of the height so we
        // don't end up cropping too much of the image. If the image has a way taller aspect ratio
        // than expected, we don't have to make any changes to our cropping so we don't handle it
        // here.
        var desiredHeightConverted = desiredHeight
        if (actualAspectRatio > 3) {
            desiredHeightConverted = desiredHeight / 2
        }

        // If the image is rotated by 90 (or 270) degrees, swap height and width when calculating
        // the crop.
        val (widthCrop, heightCrop) = when (rotationDegrees) {
            90, 270 -> Pair(desiredHeightConverted / 100f, desiredWidth / 100f)
            else -> Pair(desiredWidth / 100f, desiredHeightConverted / 100f)
        }

        cropRect.inset((imageWidth * widthCrop / 2).toInt(), (imageHeight * heightCrop / 2).toInt())

        return rotateAndCrop(imageToAnalyzeBitmap, rotationDegrees, cropRect)
    }

    private const val RATIO_4_3_VALUE = 4.0 / 3.0
    private const val RATIO_16_9_VALUE = 16.0 / 9.0
}

private fun Byte.toIntUnsigned(): Int {
    return toInt() and 0xFF
}