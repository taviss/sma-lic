package com.sma.smartfinder;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by octavian.salcianu on 1/11/2018.
 */

public class SmartFinderApplication extends Application {
    private boolean isInForeground = true;

    public boolean isInForeground() {
        return this.isInForeground;
    }

    public void setToBackground() {
        this.isInForeground = false;
    }

    public void setToForeground() {
        this.isInForeground = true;
    }

    private static final String TAG = SmartFinderApplication.class.getName();

    //private static final String DEFAULT_ADDRESS = "108.61.188.44";
    private static final String DEFAULT_ADDRESS = "10.0.2.2:9000";

    private String user;
    private String pass;
    private String cameraAddress;
    private boolean tryLogin = true;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "SMApp#onCreate()");
        SmartFinderApplicationHolder.setApplication(this);
        updateLoginDetails();
    }

    public String getUser() {
        return this.user;
    }

    public String getPass() {
        return this.pass;
    }

    public String getCameraAddress() {
        return this.cameraAddress;
    }

    public void disableLogin() {
        this.tryLogin = false;
    }

    public boolean tryLogin() {
        boolean ret = tryLogin;
        if(!ret)
            tryLogin = true;

        return ret;
    }

    public void updateLoginDetails() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String username = preferences.getString("username", null);
        String password = preferences.getString("password", null);
        String cameraAddress = preferences.getString("camera_server_address", DEFAULT_ADDRESS);

        Log.i(TAG, "Retrieved username as " + username);
        Log.i(TAG, "Retrieved camera_server_address as " + cameraAddress);
        //Log.d(TAG, "Retrieved password as " + password);

        this.cameraAddress = cameraAddress;
        this.user = username;
        this.pass = password;
        this.tryLogin = true;
    }
}
