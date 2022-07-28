package com.lauter.applifecycle

import android.app.Application

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppLifecycleManager.onCreate(this)
    }
}