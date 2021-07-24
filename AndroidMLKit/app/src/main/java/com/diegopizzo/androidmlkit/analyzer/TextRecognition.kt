package com.diegopizzo.androidmlkit.analyzer

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.diegopizzo.androidmlkit.analyzer.BaseImageAnalyzer.Source.TEXT_RECOGNITION
import com.diegopizzo.androidmlkit.camera.utils.ImageUtils
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions

class TextRecognition(
    private var listener: AnalyzerListener?,
    private val textValidator: ((textScanned: String) -> Boolean)? = null,
    private val cropPercentageWidth: Int? = null,
    private val cropPercentageHeight: Int? = null
) : BaseImageAnalyzer() {

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees

        val imageToProcess = if (cropPercentageWidth != null && cropPercentageHeight != null) {
            val imageCropped = ImageUtils.cropImage(
                mediaImage,
                rotationDegrees,
                cropPercentageWidth,
                cropPercentageHeight
            )
            InputImage.fromBitmap(imageCropped, 0)
        } else {
            InputImage.fromMediaImage(mediaImage, rotationDegrees)
        }

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(imageToProcess)
            .addOnSuccessListener { visionText ->
                if (textValidator?.invoke(visionText.text) == true) {
                    listener?.onDataScanned(visionText.text, TEXT_RECOGNITION)
                }
            }
            .addOnCompleteListener {
                closeScanning(imageProxy)
            }
            .addOnFailureListener {
                closeScanning(imageProxy)
                listener?.onDataScanningError(it, TEXT_RECOGNITION)
            }
    }
}