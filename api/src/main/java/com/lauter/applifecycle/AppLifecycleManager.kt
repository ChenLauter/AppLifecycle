package com.lauter.applifecycle

import android.content.Context
import android.util.Log

object AppLifecycleManager {

    private var callbacks: MutableList<AppLifecycleCallback>? = null

    fun onCreate(context: Context) {
        Log.d("AppLifecycleManager","$callbacks")
        callbacks?.run {
            sortBy { it.getPriority() }
            forEach { it.onCreate(context) }
        }
    }

    private fun registerAppLifecycleCallback(name: String) {
        try {
            if (callbacks == null) {
                callbacks = mutableListOf()
            }
            val instance = Class.forName(name).getConstructor().newInstance()
            if (instance is AppLifecycleCallback && !callbacks!!.contains(instance)) {
                callbacks!!.add(instance)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}