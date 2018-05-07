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
            Future<Boolean> logged = HTTPUtility.login(camerasAddress + "/login/submit", "userName", user, "userPass", password);
            if(logged.get()) {
                Future<byte[]> response = HTTPUtility.postImage(camerasAddress + "/locate", String.valueOf(intent.getIntExtra("img_id", 0)), intent.getStringExtra("name"));
                handleResponse(response.get(), intent.getStringExtra("name"));
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

    private void handleResponse(byte[] response, String name) {
        if(new String(response).contains("Object not found!")) {
            sendBroadcast(new Intent("com.sma.smartfinder.action.NO_OBJECT_FOUND"));
        } else {
            sendBroadcast(new Intent("com.sma.smartfinder.action.OBJECT_FOUND").putExtra("image", response).putExtra("name", name));
        }
    }
}
