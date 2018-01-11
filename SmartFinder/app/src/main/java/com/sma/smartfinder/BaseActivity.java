package com.sma.smartfinder;

import android.app.Activity;

/**
 * Created by octavian.salcianu on 1/11/2018.
 */

public class BaseActivity extends Activity {
    @Override
    protected void onResume() {
        super.onResume();
        ((SmartFinderApplication) getApplication()).setToForeground();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((SmartFinderApplication) getApplication()).setToForeground();
    }
}
