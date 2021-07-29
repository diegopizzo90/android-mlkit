package com.diegopizzo.androidmlkit.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.camera.core.TorchState
import androidx.camera.view.PreviewView
import androidx.lifecycle.Observer
import com.diegopizzo.androidmlkit.R
import com.diegopizzo.androidmlkit.analyzer.BarcodeAnalyzer
import com.diegopizzo.androidmlkit.analyzer.BaseImageAnalyzer
import com.diegopizzo.androidmlkit.camera.base.BaseCameraScanningFragment
import com.diegopizzo.androidmlkit.databinding.FragmentCameraScanningBinding
import com.diegopizzo.androidmlkit.view.camera.CodeScannerOverlay
import com.diegopizzo.androidmlkit.view.navigation.ScanningType
import com.diegopizzo.androidmlkit.view.viewmodel.MainViewModel
import com.diegopizzo.androidmlkit.view.viewmodel.MainViewState
import org.koin.android.viewmodel.ext.android.sharedViewModel

class CameraScanningFragment : BaseCameraScanningFragment<FragmentCameraScanningBinding>() {

    private val viewModel: MainViewModel by sharedViewModel()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCameraScanningBinding
        get() = FragmentCameraScanningBinding::inflate

    private var isFlashEnabled: Boolean = false
    private lateinit var scanningType: ScanningType

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRectangleOverlay()
        setCancelButton()
        arguments?.getString(SCANNER_TYPE_BUNDLE_KEY)?.let {
            scanningType = ScanningType.valueOf(it)
            setUpView()
        }
        viewModel.viewStates().observe(viewLifecycleOwner, viewStateObserver)
    }

    private val viewStateObserver = Observer<MainViewState> {
        if (it?.isCameraEnabled != null) {
            if (it.isCameraEnabled) startCamera() else stopCamera()
        }
    }

    private fun setUpView() {
        when (scanningType) {
            ScanningType.BARCODE -> {
                binding.cameraController.tvCameraTooltip.text =
                    getString(R.string.point_your_camera_at_a_barcode)
            }
            ScanningType.QR_CODE -> {
                binding.apply {
                    cameraController.tvCameraTooltip.text =
                        getString(R.string.point_your_camera_at_a_qrcode)
                    cameraScannerOverlay.setBoxFormatValue(CodeScannerOverlay.BoxFormat.QRCode)
                }
            }
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
            override fun onDataScanned(dataScanned: String, source: BaseImageAnalyzer.Source) {
                viewModel.onDataScanned(dataScanned)
            }

            override fun onNoDataScanned() {
                binding.cameraScannerOverlay.onCodeScanning()
            }

            override fun onDataScanningError(e: Exception, source: BaseImageAnalyzer.Source) {
                showToastMessage(R.string.error_during_barcode_scanning)
            }
        }

    private fun showToastMessage(@StringRes messageRes: Int) {
        Toast.makeText(requireContext(), getString(messageRes), Toast.LENGTH_LONG).show()
    }

    override val barcodeAnalyzer: BaseImageAnalyzer by lazy {
        BarcodeAnalyzer(
            cropPercentageWidth = binding.cameraScannerOverlay.percentageWidthCropped(),
            cropPercentageHeight = binding.cameraScannerOverlay.percentageHeightCropped(),
            analyzerListener,
            isQrCode = scanningType == ScanningType.QR_CODE
        )
    }

    override val textRecognitionAnalyzer: BaseImageAnalyzer? = null

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
        barcodeAnalyzer.disposeAnalyzer()
        textRecognitionAnalyzer?.disposeAnalyzer()
    }

    companion object {
        const val SCANNER_TYPE_BUNDLE_KEY = "SCANNER_TYPE_BUNDLE_KEY"
    }
}