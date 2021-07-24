package com.diegopizzo.androidmlkit.view.navigation.config

import androidx.navigation.NavController
import com.diegopizzo.androidmlkit.view.navigation.IMLKitNavigation
import com.diegopizzo.androidmlkit.view.navigation.MLKitNavigation
import org.koin.dsl.module

val navigationKoinModule = module {
    factory<IMLKitNavigation> { (navController: NavController) ->
        MLKitNavigation(navController)
    }
}