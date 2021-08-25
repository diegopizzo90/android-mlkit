package com.diegopizzo.androidmlkit.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.camera.core.CameraSelector
import androidx.camera.core.TorchState
import androidx.camera.view.PreviewView
import androidx.lifecycle.Observer
import com.diegopizzo.androidmlkit.R
import com.diegopizzo.androidmlkit.analyzer.*
import com.diegopizzo.androidmlkit.camera.base.BaseCameraScanningFragment
import com.diegopizzo.androidmlkit.databinding.FragmentCameraScanningBinding
import com.diegopizzo.androidmlkit.view.camera.CodeScannerOverlay
import com.diegopizzo.androidmlkit.view.camera.CustomDataDetectionOverlay
import com.diegopizzo.androidmlkit.view.navigation.ScanningType
import com.diegopizzo.androidmlkit.view.viewmodel.MainViewModel
import com.diegopizzo.androidmlkit.view.viewmodel.MainViewState
import org.koin.android.viewmodel.ext.android.sharedViewModel

class CameraScanningFragment : BaseCameraScanningFragment<FragmentCameraScanningBinding>() {

    private val viewModel: MainViewModel by sharedViewModel()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCameraScanningBinding
        get() = FragmentCameraScanningBinding::inflate

    private var isFlashEnabled: Boolean = false

    override var defaultCameraSelector: Int = CameraSelector.LENS_FACING_BACK
    override lateinit var scanningType: ScanningType

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRectangleOverlay()
        setCancelButton()
        arguments?.getString(SCANNER_TYPE_BUNDLE_KEY)?.let {
            scanningType = ScanningType.valueOf(it)
        }
        setUpView()
        viewModel.viewStates().observe(viewLifecycleOwner, viewStateObserver)
    }

    private val viewStateObserver = Observer<MainViewState> { newViewState ->
        newViewState.apply {
            if (isCameraEnabled != null) {
                if (isCameraEnabled) startCamera() else stopCamera()
            }
            binding.apply {
                if (scanningType == ScanningType.FACE_DETECTION) {
                    faceInfoOverlay.setFaceValues(
                        scannedResult,
                        defaultCameraSelector == CameraSelector.LENS_FACING_FRONT
                    )
                }
                graphicOverlay.setImageSourceInfo(
                    imageWidth,
                    imageHeight,
                    defaultCameraSelector == CameraSelector.LENS_FACING_FRONT,
                    isPortraitMode()
                )
                graphicOverlay.clear()
                graphicOverlay.add(
                    CustomDataDetectionOverlay(
                        graphicOverlay,
                        scannedResult,
                        scanningType
                    )
                )
            }
        }
    }

    private fun setUpView() {
        when (scanningType) {
            ScanningType.BARCODE -> {
                setDefaultOverlay()
                binding.cameraController.tvCameraTooltip.text =
                    getString(R.string.point_your_camera_at_a_barcode)
            }
            ScanningType.QR_CODE -> {
                setDefaultOverlay()
                binding.apply {
                    cameraController.tvCameraTooltip.text =
                        getString(R.string.point_your_camera_at_a_qrcode)
                    cameraScannerOverlay.setBoxFormatValue(CodeScannerOverlay.BoxFormat.QRCode)
                }
            }
            ScanningType.TEXT_RECOGNITION -> {
                setDefaultOverlay()
                binding.apply {
                    cameraController.tvCameraTooltip.text =
                        getString(R.string.point_your_camera_at_any_text)
                    cameraScannerOverlay.setBoxFormatValue(CodeScannerOverlay.BoxFormat.TextRecognition)
                }
            }
            ScanningType.FACE_DETECTION -> {
                defaultCameraSelector = CameraSelector.LENS_FACING_FRONT
                setFaceDetectionOverlay()
            }
            ScanningType.OBJECT_DETECTION -> setObjectDetectionOverlay()
        }
    }

    private fun setDefaultOverlay() {
        binding.apply {
            cameraScannerOverlay.visibility = View.VISIBLE
            faceInfoOverlay.visibility = View.GONE
            graphicOverlay.visibility = View.GONE
        }
    }

    private fun setFaceDetectionOverlay() {
        binding.apply {
            cameraScannerOverlay.visibility = View.GONE
            faceInfoOverlay.visibility = View.VISIBLE
            graphicOverlay.visibility = View.VISIBLE
            cameraController.tvCameraTooltip.visibility = View.GONE
        }
    }

    private fun setObjectDetectionOverlay() {
        binding.apply {
            cameraScannerOverlay.visibility = View.GONE
            faceInfoOverlay.visibility = View.GONE
            cameraController.tvCameraTooltip.visibility = View.GONE
            graphicOverlay.visibility = View.VISIBLE
        }
    }

    override fun getCameraPreviewView(): PreviewView {
        return binding.cameraPreview
    }

    override var scanningCameraListener: ScanningCameraListener? = object : ScanningCameraListener {
        override fun onCameraBindingFailed(e: Exception) {
            showToastMessage(R.string.camera_not_available)
        }

        override fun onCameraPermissionDenied() {
            showToastMessage(R.string.camera_permission_denied)
        }

        override fun onCameraInstanceReady() {
            if (isFlashUnitAvailable()) {
                observeFlashlightChanges()
                setFlashClickListener()
            } else {
                binding.cameraController.ivFlashlight.visibility = View.GONE
            }
        }
    }

    private var analyzerListener: BaseImageAnalyzer.AnalyzerListener? =
        object : BaseImageAnalyzer.AnalyzerListener {
            override fun onDataScanned(dataScanned: String) {
                viewModel.onDataScanned(dataScanned)
            }

            override fun onNoDataScanned() {
                binding.apply {
                    if (cameraScannerOverlay.visibility == View.VISIBLE) cameraScannerOverlay.onCodeScanning()
                }
            }

            override fun onDataScanningError(e: Exception) {
                showToastMessage(R.string.error_during_barcode_scanning)
            }

            override fun onCustomDataScanned(scannedResult: List<Any>, width: Int, height: Int) {
                viewModel.onDataScanned(scannedResult, width, height)
            }
        }

    private fun showToastMessage(@StringRes messageRes: Int) {
        Toast.makeText(requireContext(), getString(messageRes), Toast.LENGTH_LONG).show()
    }

    override val barcodeAnalyzer: BaseImageAnalyzer? by lazy {
        if (scanningType != ScanningType.BARCODE && scanningType != ScanningType.QR_CODE) return@lazy null
        BarcodeAnalyzer(
            cropPercentageWidth = binding.cameraScannerOverlay.percentageWidthCropped(),
            cropPercentageHeight = binding.cameraScannerOverlay.percentageHeightCropped(),
            analyzerListener,
            isQrCode = scanningType == ScanningType.QR_CODE
        )
    }

    override val textRecognitionAnalyzer: BaseImageAnalyzer? by lazy {
        if (scanningType != ScanningType.TEXT_RECOGNITION) return@lazy null
        TextRecognition(
            cropPercentageWidth = binding.cameraScannerOverlay.percentageWidthCropped(),
            cropPercentageHeight = binding.cameraScannerOverlay.percentageHeightCropped(),
            listener = analyzerListener
        )
    }

    override val faceDetectionAnalyzer: BaseImageAnalyzer? by lazy {
        if (scanningType != ScanningType.FACE_DETECTION) return@lazy null
        FaceDetection(analyzerListener)
    }

    override val objectDetectionAnalyzer: BaseImageAnalyzer? by lazy {
        if (scanningType != ScanningType.OBJECT_DETECTION) return@lazy null
        ObjectDetection(analyzerListener)
    }

    private fun setRectangleOverlay() {
        binding.cameraScannerOverlay.post {
            binding.cameraScannerOverlay.createView()
        }
    }

    private fun observeFlashlightChanges() {
        flashStateChanges()?.observe(viewLifecycleOwner) {
            it?.let { torchState ->
                when (torchState) {
                    TorchState.ON -> {
                        isFlashEnabled = true
                        binding.cameraController.ivFlashlight.setImageResource(R.drawable.ic_flash_on)
                    }
                    TorchState.OFF -> {
                        isFlashEnabled = false
                        binding.cameraController.ivFlashlight.setImageResource(R.drawable.ic_flash_off)
                    }
                }
            }
        }
    }

    private fun setFlashClickListener() {
        binding.cameraController.ivFlashlight.setOnClickListener {
            controlFlash(!isFlashEnabled)
        }
    }

    private fun setCancelButton() {
        binding.cameraController.ivClose.setOnClickListener {
            viewModel.onCancelButtonClicked()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        barcodeAnalyzer?.disposeAnalyzer()
        textRecognitionAnalyzer?.disposeAnalyzer()
        faceDetectionAnalyzer?.disposeAnalyzer()
    }

    companion object {
        const val SCANNER_TYPE_BUNDLE_KEY = "SCANNER_TYPE_BUNDLE_KEY"
    }
}