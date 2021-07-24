package com.diegopizzo.androidmlkit.app

import android.app.Application
import com.diegopizzo.androidmlkit.view.viewmodel.config.mainViewModelModule
import com.diegopizzo.androidmlkit.view.navigation.config.navigationKoinModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MLKitApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MLKitApplication)
            modules(
                listOf(
                    navigationKoinModule,
                    mainViewModelModule
                )
            )
        }
    }
}