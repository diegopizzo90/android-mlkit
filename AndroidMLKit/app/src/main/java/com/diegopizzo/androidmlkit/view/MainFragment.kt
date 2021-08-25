package com.diegopizzo.androidmlkit.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.diegopizzo.androidmlkit.R
import com.diegopizzo.androidmlkit.base.FragmentViewBinding
import com.diegopizzo.androidmlkit.databinding.FragmentMainBinding
import com.diegopizzo.androidmlkit.view.ItemFeature.ItemFeatureData
import com.diegopizzo.androidmlkit.view.navigation.ScanningType.*
import com.diegopizzo.androidmlkit.view.viewmodel.MainViewModel
import com.diegopizzo.androidmlkit.view.viewmodel.ViewEvent
import org.koin.android.viewmodel.ext.android.sharedViewModel

class MainFragment : FragmentViewBinding<FragmentMainBinding>() {

    private val viewModel: MainViewModel by sharedViewModel()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMainBinding
        get() = FragmentMainBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()
    }

    private fun setRecyclerView() {
        binding.rvMainList.apply {
            adapter = MainAdapter(itemList, object : MainAdapter.OnViewAdapterInteraction {
                override fun onItemClick(item: ItemFeatureData) {
                    when (item.scannerType) {
                        BARCODE -> viewModel.process(ViewEvent.BarcodeScanningButtonClicked)
                        QR_CODE -> viewModel.process(ViewEvent.QrCodeScanningButtonClicked)
                        TEXT_RECOGNITION -> viewModel.process(ViewEvent.TextRecognitionScanningButtonClicked)
                        FACE_DETECTION -> viewModel.process(ViewEvent.FaceDetectionScanningButtonClicked)
                        OBJECT_DETECTION -> viewModel.process(ViewEvent.ObjectDetectionScanningButtonClicked)
                    }
                }
            })
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private val itemList =
        arrayListOf(
            ItemFeatureData(
                R.color.barcode_color,
                R.string.barcode_scan,
                R.drawable.ic_barcode,
                BARCODE
            ),
            ItemFeatureData(
                R.color.qr_code_color,
                R.string.qr_code_scan,
                R.drawable.ic_qr_code,
                QR_CODE
            ),
            ItemFeatureData(
                R.color.text_recognition_color,
                R.string.text_recognition,
                R.drawable.ic_text_recognition,
                TEXT_RECOGNITION
            ),
            ItemFeatureData(
                R.color.face_detection_color,
                R.string.face_detection,
                R.drawable.ic_face_detection,
                FACE_DETECTION
            ),
            ItemFeatureData(
                R.color.object_detection_color,
                R.string.object_detection,
                R.drawable.ic_object_detection,
                OBJECT_DETECTION
            )
        )
}