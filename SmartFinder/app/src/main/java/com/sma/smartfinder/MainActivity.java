package com.sma.smartfinder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.sma.smartfinder.services.ObjectRecoginzerService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

        //Log.i(TAG, "objectFinderService#onHandleIntent()");



                try {
                    Intent requestLocateIntent = new Intent(MainActivity.this, ObjectRecoginzerService.class);

                    String filename = "img_locate.png";
                    FileOutputStream stream = MainActivity.this.openFileOutput(filename, Context.MODE_PRIVATE);

                    Bitmap bmp = null;
                    //String filename = intent.getStringExtra("locate_image");
                    try {
                        InputStream is = getAssets().open("puppy_224.bmp");//this.openFileInput(filename);
                        bmp = BitmapFactory.decodeStream(is);
                        is.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);

                    stream.close();
                    bmp.recycle();

                    requestLocateIntent.putExtra("locate_image", filename);
                    startService(requestLocateIntent);
                    //getActivity().startActivity(new Intent(getContext(), FindObjectActivity.class));
                } catch(IOException e) {
                    Toast.makeText(getApplicationContext(), "There was an error with sending the image to the ObjectFinderService!", Toast.LENGTH_LONG).show();
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
