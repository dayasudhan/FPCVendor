package com.kuruvatech.fpcvendor;

import android.app.Application;
import android.content.Intent;

/**
 * Created by Gagan on 8/16/2016.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        startService(new Intent(this, MainActivity.class));
    }
}
