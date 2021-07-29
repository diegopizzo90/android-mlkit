package com.diegopizzo.androidmlkit.analyzer

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

abstract class BaseImageAnalyzer : ImageAnalysis.Analyzer {

    fun closeScanning(imageProxy: ImageProxy) {
        imageProxy.close()
    }

    protected abstract var listener: AnalyzerListener?

    fun disposeAnalyzer() {
        listener = null
    }

    interface AnalyzerListener {
        fun onDataScanned(dataScanned: String, source: Source)
        fun onNoDataScanned()
        fun onDataScanningError(e: Exception, source: Source)
    }

    enum class Source {
        BARCODE_ANALYZER, TEXT_RECOGNITION
    }
}