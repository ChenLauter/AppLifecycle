package com.lauter.applifecycle

import android.content.Context
import android.util.Log

@AppLifecycle
class MainAppLifecycle : AppLifecycleCallback {

    override fun onCreate(context: Context) {
        Log.d("AppLifecycle", "MainAppLifecycle onCreate")
    }

    override fun getPriority(): Int = 2
}