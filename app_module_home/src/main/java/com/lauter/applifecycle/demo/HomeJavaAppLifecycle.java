package com.lauter.applifecycle.demo;

import android.content.Context;

import androidx.annotation.NonNull;

import com.lauter.applifecycle.AppLifecycle;
import com.lauter.applifecycle.AppLifecycleCallback;

@AppLifecycle
public class HomeJavaAppLifecycle implements AppLifecycleCallback {

    @Override
    public void onCreate(@NonNull Context context) {

    }

    @Override
    public int getPriority() {
        return 0;
    }
}
