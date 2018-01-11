package com.sma.smartfinder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class DetailsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null) {
            DetailsFragment detailsFragment = new DetailsFragment();
            getFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, detailsFragment, detailsFragment.getClass().getSimpleName()).commit();
        }
    }

}
