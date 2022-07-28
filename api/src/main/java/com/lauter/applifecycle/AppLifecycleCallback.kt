package com.lauter.applifecycle

import android.content.Context

interface AppLifecycleCallback {

    fun getPriority(): Int = 0
    fun onCreate(context: Context)
}