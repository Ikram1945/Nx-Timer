package com.nx.timer;

import android.app.Application;

public class NxTimerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.install(this);
    }
}