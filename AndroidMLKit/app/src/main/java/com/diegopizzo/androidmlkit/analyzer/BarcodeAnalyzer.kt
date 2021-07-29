package com.diegopizzo.androidmlkit.analyzer

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.diegopizzo.androidmlkit.analyzer.BaseImageAnalyzer.Source.BARCODE_ANALYZER
import com.diegopizzo.androidmlkit.camera.utils.ImageUtils
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer(
    private val cropPercentageWidth: Int? = null,
    private val cropPercentageHeight: Int? = null,
    override var listener: AnalyzerListener?,
    private val isQrCode: Boolean = false
) : BaseImageAnalyzer() {

    private val barcodeOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_CODE_93,
            Barcode.FORMAT_CODABAR,
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_ITF,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_PDF417,
            Barcode.FORMAT_AZTEC
        ).build()

    private val qrCodeOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()

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

        val scanner = BarcodeScanning.getClient(if (isQrCode) qrCodeOptions else barcodeOptions)

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