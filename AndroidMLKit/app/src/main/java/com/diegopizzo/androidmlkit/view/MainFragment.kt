package com.diegopizzo.androidmlkit.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.diegopizzo.androidmlkit.base.FragmentViewBinding
import com.diegopizzo.androidmlkit.databinding.FragmentMainBinding
import com.diegopizzo.androidmlkit.view.viewmodel.MainViewModel
import com.diegopizzo.androidmlkit.view.viewmodel.ViewEvent
import org.koin.android.viewmodel.ext.android.sharedViewModel

class MainFragment : FragmentViewBinding<FragmentMainBinding>() {

    private val viewModel: MainViewModel by sharedViewModel()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMainBinding
        get() = FragmentMainBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListeners()
    }

    private fun setClickListeners() {
        binding.apply {
            itemBarcode.setClickListener {
                viewModel.process(ViewEvent.BarcodeScanningButtonClicked)
            }
            itemQrCode.setClickListener {
                viewModel.process(ViewEvent.QrCodeScanningButtonClicked)
            }
            itemTextRecognition.setClickListener {
                viewModel.process(ViewEvent.TextRecognitionScanningButtonClicked)
            }
            itemFaceDetection.setClickListener {
                viewModel.process(ViewEvent.FaceDetectionScanningButtonClicked)
            }
        }
    }
}