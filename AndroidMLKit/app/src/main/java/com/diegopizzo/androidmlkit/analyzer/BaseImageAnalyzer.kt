package com.diegopizzo.androidmlkit.analyzer

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.face.Face

abstract class BaseImageAnalyzer : ImageAnalysis.Analyzer {

    fun closeScanning(imageProxy: ImageProxy) {
        imageProxy.close()
    }

    protected abstract var listener: AnalyzerListener?

    fun disposeAnalyzer() {
        listener = null
    }

    interface AnalyzerListener {
        fun onDataScanned(dataScanned: String)
        fun onNoDataScanned()
        fun onDataScanningError(e: Exception)
        fun onFaceDataScanned(faceData: Face, width: Int, height: Int)
    }
}