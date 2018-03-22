package com.sma.smartfinder.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.sma.smartfinder.SettingsActivity;
import com.sma.smartfinder.http.utils.HTTPUtility;

import org.json.JSONException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "No camera address!", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        try {
            Bitmap bmp = null;
            String filename = intent.getStringExtra("locate_image");
            try {
                FileInputStream is = this.openFileInput(filename);
                bmp = BitmapFactory.decodeStream(is);
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Future<Boolean> logged = HTTPUtility.login(camerasAddress + "/login/submit", "userName", user, "userPass", password);
            if(logged.get()) {
                Future<String> response = HTTPUtility.postImage(camerasAddress + "/locate", bmp);
                handleResponse(response.get());
            } else {
                throw new IllegalStateException("Cannot log in!");
            }

        } catch (IllegalStateException|IOException|JSONException|InterruptedException|ExecutionException e) {
            Log.i(TAG, e.getMessage());
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

    private void handleResponse(String response) {
        Scanner scanner = new Scanner(response);
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
