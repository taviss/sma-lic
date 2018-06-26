package com.sma.smartfinder.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.sma.object.recognizer.api.Recognition;
import com.sma.smartfinder.ObjectFoundActivity;
import com.sma.smartfinder.SettingsActivity;
import com.sma.smartfinder.http.utils.HTTPUtility;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by octavian.salcianu on 1/11/2018.
 *
 * Service for finding an object
 */

public class ObjectFinderService extends IntentService {
    private static final String TAG = ObjectFinderService.class.getSimpleName();

    public ObjectFinderService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, "objectFinderService#onHandleIntent()");

        // Retrieve user info
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

        // Try login
        try {
            Future<Boolean> logged = HTTPUtility.login(camerasAddress + "/login/submit", "userName", user, "userPass", password);
            if(logged.get()) {
                // Send a POST request and expect an image with the object's location
                // The id of the searched for object(image) is passed as an extra
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

    public static final Gson customGson = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class,
            new ByteArrayToBase64TypeAdapter()).create();

    // Using Android's base64 libraries. This can be replaced with any base64 library.
    private static class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
        public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Base64.decode(json.getAsString(), Base64.NO_WRAP);
        }

        public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(Base64.encodeToString(src, Base64.NO_WRAP));
        }
    }

    /**
     * Handles the server response and notifies accordingly
     * @param response
     * @param name
     */
    private void handleResponse(byte[] response, String name) {
        if(new String(response).contains("Object not found!")) {
            sendBroadcast(new Intent("com.sma.smartfinder.action.NO_OBJECT_FOUND"));
        } else {
            try {
                JSONArray jsonArray = new JSONArray(new String(response));

                if (jsonArray.length() != 0) {
                    List<Recognition> recognitionList = customGson.fromJson(new String(response), new TypeToken<Collection<Recognition>>(){}.getType());

                   ArrayList<String> recogs = new ArrayList<>();

                    for(Recognition recognition : recognitionList) {
                        try {
                            BufferedOutputStream bos = new BufferedOutputStream(openFileOutput(recognition.getTitle() + recognition.getId() + ".png", Context.MODE_PRIVATE));
                            bos.write(recognition.getSource());
                            bos.flush();
                            bos.close();
                            recogs.add(recognition.getTitle() + ":" + recognition.getId());
                        } catch (IOException e) {
                            //
                        }
                    }

                    sendBroadcast(new Intent("com.sma.smartfinder.action.OBJECT_FOUND").putStringArrayListExtra("recognitions", recogs));
                    return;
                }
            } catch(JSONException e) {
                // NO-OP;
            }
            sendBroadcast(new Intent("com.sma.smartfinder.action.OBJECT_FOUND").putExtra("image", response).putExtra("name", name));
        }
    }
}
