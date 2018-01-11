package com.sma.smartfinder;

import android.app.Application;

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
}
