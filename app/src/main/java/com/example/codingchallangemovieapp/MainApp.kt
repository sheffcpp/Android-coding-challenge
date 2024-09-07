package com.example.codingchallangemovieapp

import android.app.Application
import com.example.codingchallangemovieapp.di.mainModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin


class MainApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApp)
            modules(mainModule)
        }
    }
}