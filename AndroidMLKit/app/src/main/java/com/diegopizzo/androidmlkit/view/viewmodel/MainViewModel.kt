package com.diegopizzo.androidmlkit.view.viewmodel

import android.util.Patterns
import android.webkit.URLUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diegopizzo.androidmlkit.util.SingleLiveEvent
import com.diegopizzo.androidmlkit.view.navigation.IMLKitNavigation
import com.diegopizzo.androidmlkit.view.navigation.ScanningType
import com.diegopizzo.androidmlkit.view.viewmodel.ViewEffect.ShowBottomSheetFragment
import com.diegopizzo.androidmlkit.view.viewmodel.ViewEvent.*

class MainViewModel(private val navigation: IMLKitNavigation) : ViewModel() {

    private val _viewEffects: SingleLiveEvent<ViewEffect> = SingleLiveEvent()
    fun viewEffects(): SingleLiveEvent<ViewEffect> = _viewEffects

    private val _viewStates: MutableLiveData<MainViewState> = MutableLiveData()
    fun viewStates(): LiveData<MainViewState> = _viewStates

    private var _viewState: MainViewState? = null
    var viewState: MainViewState
        get() = _viewState
            ?: throw UninitializedPropertyAccessException("\"viewState\" was queried before being initialized")
        set(value) {
            _viewState = value
            _viewStates.value = value
        }

    init {
        viewState = MainViewState()
    }

    private fun isUrlValid(url: String): Boolean {
        return Patterns.WEB_URL.matcher(url).matches() && URLUtil.isValidUrl(url)
    }

    fun onDataScanned(dataScanned: String) {
        viewState = viewState.copy(
            isCameraEnabled = false,
            isOpenLinkButtonVisible = isUrlValid(dataScanned)
        )
        _viewEffects.value = ShowBottomSheetFragment(dataScanned)
    }

    fun onCancelButtonClicked() {
        navigation.goBack()
    }

    fun process(viewEvent: ViewEvent) {
        when (viewEvent) {
            BottomDialogCancelButtonClicked -> {
                viewState = viewState.copy(isCameraEnabled = true)
            }
            BarcodeScanningButtonClicked -> navigation.toCameraScanning(ScanningType.BARCODE)
            QrCodeScanningButtonClicked -> navigation.toCameraScanning(ScanningType.QR_CODE)
        }
    }
}

sealed class ViewEffect {
    class ShowBottomSheetFragment(val dataScanned: String) : ViewEffect()
}

sealed class ViewEvent {
    object BarcodeScanningButtonClicked : ViewEvent()
    object QrCodeScanningButtonClicked : ViewEvent()
    object BottomDialogCancelButtonClicked : ViewEvent()
}

data class MainViewState(
    val isCameraEnabled: Boolean? = null,
    val isOpenLinkButtonVisible: Boolean = false
)