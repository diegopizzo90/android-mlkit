package com.diegopizzo.androidmlkit.analyzer

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.diegopizzo.androidmlkit.analyzer.BaseImageAnalyzer.Source.BARCODE_ANALYZER
import com.diegopizzo.androidmlkit.camera.utils.ImageUtils
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer(
    private val cropPercentageWidth: Int? = null,
    private val cropPercentageHeight: Int? = null
) : BaseImageAnalyzer() {

    var listener: AnalyzerListener? = null

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
            InputImage.fromBitmap(imageCropped, rotationDegrees)
        } else {
            InputImage.fromMediaImage(mediaImage, rotationDegrees)
        }

        val scanner = BarcodeScanning.getClient()

        scanner.process(imageToProcess)
            .addOnSuccessListener {
                it.firstOrNull().let { barcode ->
                    val rawValue = barcode?.rawValue
                    if (rawValue != null) {
                        listener?.onDataScanned(rawValue, BARCODE_ANALYZER)
                    } else {
                        listener?.onNoDataScanned()
                    }
                }
            }
            .addOnCompleteListener {
                closeScanning(imageProxy)
            }
            .addOnFailureListener {
                closeScanning(imageProxy)
                listener?.onDataScanningError(it, BARCODE_ANALYZER)
            }

    }
}