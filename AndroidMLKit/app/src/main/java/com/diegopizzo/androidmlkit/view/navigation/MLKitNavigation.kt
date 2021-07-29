package com.diegopizzo.androidmlkit.view.navigation

import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.diegopizzo.androidmlkit.R
import com.diegopizzo.androidmlkit.view.CameraScanningFragment.Companion.SCANNER_TYPE_BUNDLE_KEY

class MLKitNavigation(private val navController: NavController) : IMLKitNavigation {

    override fun toCameraScanning(scanningType: ScanningType) {
        navController.navigate(
            R.id.action_mainFragment_to_cameraScanningFragment,
            bundleOf(SCANNER_TYPE_BUNDLE_KEY to scanningType.name)
        )
    }

    override fun goBack() {
        navController.popBackStack()
    }
}