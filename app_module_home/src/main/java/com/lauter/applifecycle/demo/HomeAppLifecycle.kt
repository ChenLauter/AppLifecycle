package com.lauter.applifecycle.demo

import android.content.Context
import android.util.Log
import com.lauter.applifecycle.AppLifecycle
import com.lauter.applifecycle.AppLifecycleCallback

@AppLifecycle
class HomeAppLifecycle : AppLifecycleCallback {

    override fun getPriority(): Int = 0

    override fun onCreate(context: Context) {
        Log.d("AppLifecycle", "HomeAppLifecycle onCreate")
    }
}