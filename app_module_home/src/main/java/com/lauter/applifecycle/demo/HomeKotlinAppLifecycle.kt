package com.lauter.applifecycle.demo

import android.content.Context
import com.lauter.applifecycle.AppLifecycle
import com.lauter.applifecycle.AppLifecycleCallback

@AppLifecycle
class HomeKotlinAppLifecycle : AppLifecycleCallback {

    override fun getPriority(): Int = 0

    override fun onCreate(context: Context) {

    }
}