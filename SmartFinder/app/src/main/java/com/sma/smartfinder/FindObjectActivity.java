package com.sma.smartfinder;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import sma.com.smartfinder.R;

/**
 * Activity for finding an object
 */
@Deprecated
public class FindObjectActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_object);

        ((ProgressBar) findViewById(R.id.progressBar2)).setVisibility(View.VISIBLE);
    }
}
