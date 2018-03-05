package com.sma.smartfinder;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

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



       new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    String user = preferences.getString("username", "");
                    String password = preferences.getString("password", "");
                    String camerasAddress = preferences.getString("camera_server_address", "10.0.2.2:9000/locate");

                    if(camerasAddress.isEmpty()) {
                        //Log.i(TAG, "No camera address!");
                        //startActivity(new Intent(this, SettingsActivity.class));
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "No camera address!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }

                    URL url = new URL("http://" + camerasAddress);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setReadTimeout(10000);
                    connection.setConnectTimeout(15000);
                    connection.setRequestMethod("POST");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    connection.connect();

                    OutputStream outputStream = connection.getOutputStream();
                    Bitmap bmp = null;
                    //String filename = intent.getStringExtra("locate_image");
                    try {
                        InputStream is = getAssets().open("puppy_224.bmp");//this.openFileInput(filename);
                        bmp = BitmapFactory.decodeStream(is);
                        is.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    bmp.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
                    outputStream.close();

                    waitForResponse(connection.getInputStream());

                } catch (IOException e) {
                    //Log.i(TAG, e.getMessage());
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Could not connect to camera server!", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        }).start();


    }

    private void waitForResponse(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream);
        //TODO rework + test with the actual server
        String line;
        while(scanner.hasNext()) {
            line = scanner.nextLine();
            if(line.contains("imageName")) {

                sendBroadcast(new Intent("com.sma.smartfinder.action.OBJECT_FOUND").putExtra("imageName", line));
                return;
            }
        }
        sendBroadcast(new Intent("com.sma.smartfinder.action.NO_OBJECT_FOUND"));
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
