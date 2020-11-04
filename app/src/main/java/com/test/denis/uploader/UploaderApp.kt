package com.test.denis.uploader

import android.app.Application
import com.test.denis.uploader.di.appComponent
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class UploaderApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            // declare used Android context
            androidContext(this@UploaderApp)
            // use Koin logger
            printLogger()
            // declare modules
            modules(appComponent)
        }
    }
}