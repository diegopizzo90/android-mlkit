package com.diegopizzo.androidmlkit.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.TorchState
import androidx.camera.view.PreviewView
import androidx.lifecycle.Observer
import com.diegopizzo.androidmlkit.R
import com.diegopizzo.androidmlkit.analyzer.BarcodeAnalyzer
import com.diegopizzo.androidmlkit.analyzer.BaseImageAnalyzer
import com.diegopizzo.androidmlkit.camera.base.BaseCameraScanningFragment
import com.diegopizzo.androidmlkit.databinding.FragmentBarcodeScanningBinding
import com.diegopizzo.androidmlkit.view.viewmodel.MainViewModel
import com.diegopizzo.androidmlkit.view.viewmodel.MainViewState
import org.koin.android.viewmodel.ext.android.sharedViewModel

class BarcodeScanningFragment : BaseCameraScanningFragment<FragmentBarcodeScanningBinding>() {

    private val viewModel: MainViewModel by sharedViewModel()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentBarcodeScanningBinding
        get() = FragmentBarcodeScanningBinding::inflate

    private var isFlashEnabled: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRectangleOverlay()
        setCancelButton()
        viewModel.viewStates().observe(viewLifecycleOwner, viewStateObserver)
    }

    private val viewStateObserver = Observer<MainViewState> {
        if (it?.isCameraEnabled != null) {
            if (it.isCameraEnabled) startCamera() else stopCamera()
        }
    }

    override fun getCameraPreviewView(): PreviewView {
        return binding.barcodeCameraPreview
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
                binding.barcodeScannerOverlay.onCodeScanning()
            }

            override fun onDataScanningError(e: Exception, source: BaseImageAnalyzer.Source) {
                showToastMessage(R.string.error_during_barcode_scanning)
            }
        }

    private fun showToastMessage(@StringRes messageRes: Int) {
        Toast.makeText(requireContext(), getString(messageRes), Toast.LENGTH_LONG).show()
    }

    override val barcodeAnalyzer: ImageAnalysis.Analyzer by lazy {
        BarcodeAnalyzer(
            cropPercentageWidth = binding.barcodeScannerOverlay.percentageWidthCropped(),
            cropPercentageHeight = binding.barcodeScannerOverlay.percentageHeightCropped()
        ).apply {
            listener = analyzerListener
        }
    }

    override val textRecognitionAnalyzer: ImageAnalysis.Analyzer? = null

    private fun setRectangleOverlay() {
        binding.barcodeScannerOverlay.post {
            binding.barcodeScannerOverlay.createView()
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
        (barcodeAnalyzer as BarcodeAnalyzer).listener = null
    }
}