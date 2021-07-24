package com.diegopizzo.androidmlkit.view.viewmodel.config

import androidx.navigation.NavController
import com.diegopizzo.androidmlkit.view.viewmodel.MainViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val mainViewModelModule = module {
    viewModel { (navController: NavController) ->
        MainViewModel(get { parametersOf(navController) })
    }
}