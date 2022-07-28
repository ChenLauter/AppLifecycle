package com.lauter.applifecycle;

import android.content.Context;

import androidx.annotation.NonNull;

@AppLifecycle
public class MainJavaAppLifecycle implements AppLifecycleCallback{

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void onCreate(@NonNull Context context) {

    }
}
