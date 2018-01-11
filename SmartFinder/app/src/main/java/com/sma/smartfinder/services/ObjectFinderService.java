package com.sma.smartfinder.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.sma.smartfinder.SettingsActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by octavian.salcianu on 1/11/2018.
 */

public class ObjectFinderService extends IntentService {
    private static final String TAG = ObjectFinderService.class.getSimpleName();

    public ObjectFinderService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, "objectFinderService#onHandleIntent()");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String user = preferences.getString("username", "");
        String password = preferences.getString("password", "");
        String camerasAddress = preferences.getString("camera_server_address", "");

        if(camerasAddress.isEmpty()) {
            Log.i(TAG, "No camera address!");
            startActivity(new Intent(this, SettingsActivity.class));
        }

        try {
            URL url = new URL("http://" + camerasAddress);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.connect();

            OutputStream outputStream = connection.getOutputStream();
            Bitmap data = (Bitmap) intent.getParcelableExtra("BitmapImage");

            data.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
            outputStream.close();

            waitForResponse(connection.getInputStream());

        } catch (IOException e) {
            startActivity(new Intent(this, SettingsActivity.class));
            Toast.makeText(getApplicationContext(), "Could not connect to camera server!", Toast.LENGTH_SHORT).show();
        }

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
}
