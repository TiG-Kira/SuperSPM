package com.kira.superspm

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import com.kira.superspm.di.appModule
import com.kira.superspm.utils.PluginManager

class SuperSPMApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        startKoin {
            androidContext(this@SuperSPMApp)
            modules(appModule)
        }
        PluginManager.init(this)
    }
}