package com.sma.smartfinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import sma.com.smartfinder.R;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null) {
            ObjectsFragment objectsFragment = new ObjectsFragment();
            getFragmentManager().beginTransaction().add(
                    android.R.id.content,
                    objectsFragment,
                    objectsFragment.getClass().getSimpleName()
            ).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.action_detect:
                startActivity(new Intent(this, DetectActivity.class));
                return true;

            case R.id.action_remove:
                /*
                int rows = getContentResolver().delete(StatusContract.CONTENT_URI, null, null);
                Toast.makeText(this, "Deleted " + rows + " rows",
                        Toast.LENGTH_LONG).show();
                        */
                return true;

            default:
                return false;
        }
    }
}
