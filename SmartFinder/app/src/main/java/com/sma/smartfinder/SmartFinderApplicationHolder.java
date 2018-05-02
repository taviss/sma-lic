package com.sma.smartfinder;

import android.app.Application;

/**
 * Created by octavian.salcianu on 3/22/2018.
 */

public class SmartFinderApplicationHolder {
    private static SmartFinderApplication application;

    public static void setApplication(Application application) {
        SmartFinderApplicationHolder.application = (SmartFinderApplication)application;
    }

    public static SmartFinderApplication getApplication() {
        return SmartFinderApplicationHolder.application;
    }
}
