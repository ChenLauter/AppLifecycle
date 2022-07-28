package com.lauter.applifecycle

import android.content.Context

@AppLifecycle
class MainKotlinAppLifecycle : AppLifecycleCallback {

    override fun onCreate(context: Context) {

    }

    override fun getPriority(): Int = 2
}