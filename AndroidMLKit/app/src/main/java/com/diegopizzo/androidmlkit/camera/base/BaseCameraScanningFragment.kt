package com.diegopizzo.androidmlkit.camera.base

import android.Manifest
import android.content.res.Configuration
import android.graphics.Point
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Size
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.viewbinding.ViewBinding
import com.diegopizzo.androidmlkit.base.FragmentViewBinding
import com.diegopizzo.androidmlkit.camera.utils.ImageUtils
import com.diegopizzo.androidmlkit.util.isTrue
import com.diegopizzo.androidmlkit.view.navigation.ScanningType
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Abstract base Fragment to Scan a barcode/QrCode.
 * Using CameraX https://developer.android.com/training/camerax
 */
abstract class BaseCameraScanningFragment<B : ViewBinding> : FragmentViewBinding<B>() {

    //Provide Camera PreviewView
    protected abstract fun getCameraPreviewView(): PreviewView

    private lateinit var cameraExecutor: ExecutorService
    private var barcodeImageAnalysis: ImageAnalysis? = null
    private var textRecognitionImageAnalysis: ImageAnalysis? = null
    private var faceDetectionImageAnalysis: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    //Provide listener on scanning result
    protected abstract var scanningCameraListener: ScanningCameraListener?

    protected abstract var defaultCameraSelector: Int

    //Provide image analyzer to scan barcode or qrcode
    protected abstract val barcodeAnalyzer: ImageAnalysis.Analyzer?

    //Provide image analyzer to scan a text
    protected abstract val textRecognitionAnalyzer: ImageAnalysis.Analyzer?

    //Provide image analyzer to scan a face
    protected abstract val faceDetectionAnalyzer: ImageAnalysis.Analyzer?

    protected abstract val scanningType: ScanningType

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                scanningCameraListener?.onCameraPermissionDenied()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Request camera permissions
        requestPermission.launch(Manifest.permission.CAMERA)

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun buildCameraPreview(screenAspectRatio: Int, rotation: Int): Preview {
        return when (scanningType) {
            ScanningType.BARCODE, ScanningType.QR_CODE, ScanningType.TEXT_RECOGNITION -> {
                Preview.Builder()
                    .setTargetAspectRatio(screenAspectRatio)
                    .setTargetRotation(rotation)
                    .build().also {
                        it.setSurfaceProvider(getCameraPreviewView().surfaceProvider)
                    }
            }
            else -> {
                Preview.Builder()
                    .setTargetResolution(getSize())
                    .setTargetRotation(rotation)
                    .build().also {
                        it.setSurfaceProvider(getCameraPreviewView().surfaceProvider)
                    }
            }
        }
    }

    private fun setImageAnalysis(screenAspectRatio: Int, rotation: Int) {
        val imageAnalyzerOptions = when (scanningType) {
            ScanningType.BARCODE, ScanningType.QR_CODE, ScanningType.TEXT_RECOGNITION -> {
                ImageAnalysis.Builder()
                    .setTargetAspectRatio(screenAspectRatio)
                    .setTargetRotation(rotation)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            }
            else -> {
                ImageAnalysis.Builder()
                    .setTargetResolution(getSize())
                    .setTargetRotation(rotation)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            }
        }

        barcodeImageAnalysis = imageAnalyzerOptions
            .build().also { imageAnalysis ->
                barcodeAnalyzer?.let { bcAnalyzer ->
                    imageAnalysis.setAnalyzer(cameraExecutor, bcAnalyzer)
                }
            }

        textRecognitionImageAnalysis = imageAnalyzerOptions
            .build().also { imageAnalysis ->
                textRecognitionAnalyzer?.let { txtAnalyzer ->
                    imageAnalysis.setAnalyzer(cameraExecutor, txtAnalyzer)
                }
            }

        faceDetectionImageAnalysis = imageAnalyzerOptions.build().also { imageAnalysis ->
            faceDetectionAnalyzer?.let { faceAnalyzer ->
                imageAnalysis.setAnalyzer(cameraExecutor, faceAnalyzer)
            }
        }
    }

    private fun getAspectRatio(): Int {
        val metrics = DisplayMetrics().also {
            getCameraPreviewView().display?.getRealMetrics(it)
        }
        return ImageUtils.calculateAspectRatio(metrics.widthPixels, metrics.heightPixels)
    }

    private fun getSize(): Size {
        val point = Point()
        getCameraPreviewView().display?.getRealSize(point)
        return Size(point.x, point.y)
    }

    private fun getDisplayRotation(): Int {
        return getCameraPreviewView().display?.rotation ?: 0
    }

    protected fun startCamera() {
        // Create an instance of the ProcessCameraProvider,
        // which will be used to bind the use cases to a lifecycle owner.
        val cameraProviderFuture = context?.let { ProcessCameraProvider.getInstance(it) }

        // Add a listener to the cameraProviderFuture.
        // The first argument is a Runnable, which will be where the magic actually happens.
        // The second argument (way down below) is an Executor that runs on the main thread.
        cameraProviderFuture?.addListener({
            // Add a ProcessCameraProvider, which binds the lifecycle of your camera to
            // the LifecycleOwner within the application's life.
            cameraProvider = cameraProviderFuture.get()

            // Select back camera as a default
            val cameraSelector =
                CameraSelector.Builder().requireLensFacing(defaultCameraSelector).build()

            val screenAspectRatio = getAspectRatio()
            val rotation = getDisplayRotation()

            // Initialize the Preview object, get a surface provider from your PreviewView,
            // and set it on the preview instance.
            val preview = buildCameraPreview(screenAspectRatio, rotation)

            // Setup the ImageAnalyzer for the ImageAnalysis use case
            setImageAnalysis(screenAspectRatio, rotation)

            try {
                when (scanningType) {
                    ScanningType.BARCODE, ScanningType.QR_CODE -> bindCamera(
                        cameraSelector,
                        preview,
                        barcodeImageAnalysis
                    )
                    ScanningType.TEXT_RECOGNITION -> bindCamera(
                        cameraSelector,
                        preview,
                        textRecognitionImageAnalysis
                    )
                    ScanningType.MULTIPLE -> bindCamera(
                        cameraSelector,
                        preview,
                        barcodeImageAnalysis,
                        textRecognitionImageAnalysis
                    )
                    ScanningType.FACE_DETECTION -> bindCamera(
                        cameraSelector,
                        preview,
                        faceDetectionImageAnalysis
                    )
                }
                scanningCameraListener?.onCameraInstanceReady()
            } catch (e: Exception) {
                //Some devices doesn't support more than one use cases (barcode analyzer and text recognition)
                if (e is IllegalArgumentException) {
                    bindCamera(cameraSelector, preview, textRecognitionImageAnalysis)
                } else {
                    scanningCameraListener?.onCameraBindingFailed(e)
                }
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCamera(
        cameraSelector: CameraSelector, preview: Preview,
        vararg imageAnalysis: ImageAnalysis?
    ) {
        // Unbind use cases before rebinding
        cameraProvider?.unbindAll()
        // Bind use cases to camera
        camera = cameraProvider?.bindToLifecycle(
            this,
            cameraSelector,
            preview,
            *imageAnalysis
        )
    }

    protected fun isFlashUnitAvailable(): Boolean {
        return camera?.cameraInfo?.hasFlashUnit().isTrue()
    }

    protected fun flashStateChanges(): LiveData<Int>? {
        return camera?.cameraInfo?.torchState
    }

    protected fun controlFlash(isFlashEnabled: Boolean) {
        camera?.cameraControl?.enableTorch(isFlashEnabled)
    }

    protected fun stopCamera() {
        cameraProvider?.unbindAll()
    }

    protected fun isPortraitMode(): Boolean {
        return context?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    override fun onDestroyView() {
        super.onDestroyView()
        camera = null
        cameraProvider = null
        cameraExecutor.shutdown()
    }

    override fun onDestroy() {
        super.onDestroy()
        barcodeImageAnalysis = null
        textRecognitionImageAnalysis = null
        faceDetectionImageAnalysis = null
        scanningCameraListener = null
    }

    interface ScanningCameraListener {
        fun onCameraBindingFailed(e: Exception)
        fun onCameraPermissionDenied()
        fun onCameraInstanceReady()
    }
}