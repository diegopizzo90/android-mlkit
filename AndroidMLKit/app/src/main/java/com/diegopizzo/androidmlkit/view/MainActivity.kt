package com.diegopizzo.androidmlkit.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.diegopizzo.androidmlkit.R
import com.diegopizzo.androidmlkit.view.viewmodel.MainViewModel
import com.diegopizzo.androidmlkit.view.viewmodel.ViewEffect
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModel {
        parametersOf(
            (supportFragmentManager.findFragmentById(R.id.container_view) as NavHostFragment).navController
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel.viewEffects().observe(this, viewEffectObserver)
    }

    private val viewEffectObserver = Observer<ViewEffect> {
        when (it) {
            is ViewEffect.ShowBottomSheetFragment -> {
                BottomDialogFragment.newInstance(
                    bundleOf(Pair(BottomDialogFragment.DATA_SCANNED_KEY, it.dataScanned))
                ).apply {
                    show(supportFragmentManager, null)
                }
            }
        }
    }
}