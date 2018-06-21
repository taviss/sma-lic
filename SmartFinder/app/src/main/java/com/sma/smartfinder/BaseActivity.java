package com.sma.smartfinder;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by octavian.salcianu on 1/11/2018.
 *
 * A base activity for shared logic
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
