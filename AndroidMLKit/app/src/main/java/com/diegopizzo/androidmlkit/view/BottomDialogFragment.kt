package com.diegopizzo.androidmlkit.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.diegopizzo.androidmlkit.databinding.FragmentBottomSheetBinding
import com.diegopizzo.androidmlkit.view.viewmodel.MainViewModel
import com.diegopizzo.androidmlkit.view.viewmodel.ViewEvent
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.viewmodel.ext.android.sharedViewModel

class BottomDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBottomSheetBinding

    private val viewModel: MainViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        binding.tvDataScanned.text = arguments?.getString(DATA_SCANNED_KEY) ?: ""
        setCancelButton()
    }

    private fun setCancelButton() {
        binding.ivCancel.setOnClickListener {
            dismiss()
            viewModel.process(ViewEvent.DialogCancelButtonClicked)
        }
    }

    companion object {
        const val DATA_SCANNED_KEY = "DATA_SCANNED_KEY"

        @JvmStatic
        fun newInstance(bundle: Bundle): BottomDialogFragment {
            val fragment = BottomDialogFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}