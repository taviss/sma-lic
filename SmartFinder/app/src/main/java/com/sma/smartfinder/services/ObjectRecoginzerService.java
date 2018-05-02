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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by octavian.salcianu on 3/22/2018.
 */

public class ObjectRecoginzerService  extends IntentService {

    private static final String TAG = ObjectRecoginzerService.class.getSimpleName();

    public ObjectRecoginzerService() {
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
                Future<byte[]> response = HTTPUtility.postImage(camerasAddress + "/recognize", bmp);
                handleResponse(new String(response.get()), filename);
            } else {
                handleResponse(null, null);
            }

        } catch (JSONException|IOException|InterruptedException|ExecutionException e) {
            Log.i(TAG, e.getMessage());
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Could not send request to server!", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void handleResponse(String response, String image) {
        if(response != null) {
            try {
                JSONArray jsonArray = new JSONArray(response);

                if (jsonArray.length() != 0) {
                    //TODO Use Recognition class from sm-core!
                    ArrayList<String> recognitions = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        recognitions.add((String) jsonObject.get("title"));
                    }

                    sendBroadcast(new Intent("com.sma.smartfinder.action.OBJECT_RECOGNIZED").putExtra("image", image).putStringArrayListExtra("recognitions", recognitions));
                    return;
                }
            } catch (JSONException e) {
                Log.i(TAG, e.getMessage());
            }
        }
        sendBroadcast(new Intent("com.sma.smartfinder.action.OBJECT_NOT_RECOGNIZED"));
    }
}
