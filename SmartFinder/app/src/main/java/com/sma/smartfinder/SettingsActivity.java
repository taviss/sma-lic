package com.sma.smartfinder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null) {
            SettingsFragment settingsFragment = new SettingsFragment();
            getFragmentManager().beginTransaction().add(
                    android.R.id.content,
                    settingsFragment,
                    settingsFragment.getClass().getSimpleName()
            ).commit();
        }
    }

}
