package com.diegopizzo.androidmlkit.view.navigation

interface IMLKitNavigation {
    fun toCameraScanning(scanningType: ScanningType)
    fun goBack()
}

enum class ScanningType {
    BARCODE, QR_CODE
}