package com.diegopizzo.androidmlkit.view.navigation

import androidx.navigation.NavController
import com.diegopizzo.androidmlkit.R

class MLKitNavigation(private val navController: NavController) : IMLKitNavigation {

    override fun toBarcodeScanning() {
        navController.navigate(R.id.action_mainFragment_to_barcodeScanningFragment)
    }

    override fun goBack() {
        navController.popBackStack()
    }
}