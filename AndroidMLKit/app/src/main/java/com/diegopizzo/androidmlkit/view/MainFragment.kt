package com.diegopizzo.androidmlkit.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.diegopizzo.androidmlkit.base.FragmentViewBinding
import com.diegopizzo.androidmlkit.databinding.FragmentMainBinding
import com.diegopizzo.androidmlkit.view.viewmodel.MainViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.core.parameter.parametersOf

class MainFragment : FragmentViewBinding<FragmentMainBinding>() {

    private val viewModel: MainViewModel by sharedViewModel()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMainBinding
        get() = FragmentMainBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        barcodeScanClickListener()
    }

    private fun barcodeScanClickListener() {
        binding.itemBarcode.setClickListener {
            viewModel.onBarcodeScannerClicked()
        }
    }
}