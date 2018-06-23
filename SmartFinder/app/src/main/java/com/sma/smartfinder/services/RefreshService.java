package com.sma.smartfinder.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sma.smartfinder.SmartFinderApplication;
import com.sma.smartfinder.SmartFinderApplicationHolder;
import com.sma.smartfinder.db.ObjectContract;
import com.sma.smartfinder.http.utils.HTTPUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by Tavi on 6/23/2018.
 */

public class RefreshService extends IntentService {
    private static final String TAG = RefreshService.class.getName();

    public RefreshService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try {
            String address = SmartFinderApplicationHolder.getApplication().getCameraAddress();
            String user = SmartFinderApplicationHolder.getApplication().getUser();
            Future<byte[]> response = HTTPUtility.get(address + "/users/" + user + "/images");
            retrieveImages(new String(response.get()));
        } catch(IOException|InterruptedException|ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the response from the server and converts JSON recognitions to an ArrayList
     * @param response
     */
    private void retrieveImages(String response) {
        if(response != null) {
            try {
                JSONArray jsonArray = new JSONArray(response);

                if (jsonArray.length() != 0) {
                    ContentValues values = new ContentValues();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        values.clear();
                        values.put(ObjectContract.Column.ID, jsonObject.getString("id"));
                        values.put(ObjectContract.Column.OBJECT_NAME, jsonObject.getString("id"));


                        getContentResolver().insert(ObjectContract.CONTENT_URI, values);
                    }
                }
            } catch (JSONException e) {
                Log.i(TAG, e.getMessage());
            }
        }
    }
}
