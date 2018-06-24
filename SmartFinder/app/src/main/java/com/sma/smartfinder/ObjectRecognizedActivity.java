package com.sma.smartfinder;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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
import com.sma.smartfinder.db.ObjectContract;
import com.sma.smartfinder.http.utils.HTTPUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import sma.com.smartfinder.R;

/**
 * Activity for displaying a recognized object
 */
public class ObjectRecognizedActivity extends BaseActivity {
    private static final String TAG = ObjectRecognizedActivity.class.getSimpleName();

    /**
     * Display the image
     */
    private ImageView objectView;

    /**
     * A list view of recognitions
     */
    private ListView listView;

    /**
     * Currently selected recognition
     */
    private String currentSelectionName = null;

    /**
     * The image as bitmap
     */
    private Bitmap image;



    private TextView info = null;

    private static final String NONE_OF_THE_ABOVE = "None of the above";

    /**
     * A list of recognitions populated by the server
     */
    private List<Recognition> recognitions;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_recognized);

        listView = (ListView) findViewById(R.id.list_view_recognitions);
        objectView = (ImageView) findViewById(R.id.list_item_object_view);

        String json = getIntent().getStringExtra("recognitions");
        recognitions = customGson.fromJson(json, new TypeToken<Collection<Recognition>>(){}.getType());

        //System.out.println(list.get(0).getTitle());

        List<String> recognitionsNames = new ArrayList();
        if(recognitions != null && recognitions.size() > 0) {
            // If there are recognitions passed to this activity, add the NONE OF THE ABOVE option

            for (Recognition recognition : recognitions) {
                recognitionsNames.add(recognition.getTitle());
            }

            recognitionsNames.add(NONE_OF_THE_ABOVE);
        }

        info = (TextView)findViewById(R.id.object_recognized_info);

        // Load the image
        Bitmap bmp = null;
        String filename = getIntent().getStringExtra("image");
        try {
            FileInputStream is = this.openFileInput(filename);
            bmp = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Display the image
        objectView.setImageBitmap(bmp);
        image = bmp;

        // If there are no recognitions, prompt the user to enter a label
        if(recognitions == null || recognitions.size() == 0) {
            startActivity(new Intent(ObjectRecognizedActivity.this, UserRecognitionActivity.class).putExtra("image", image));
        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, recognitionsNames);

            listView.setAdapter(adapter);

            // Upon clicking a recognition, prompt the user to accept or not
            final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            // If the user accepts the recognition
                            if (image != null && currentSelectionName != null) {
                                HashMap<String, String> extras = new HashMap<>();
                                extras.put("imageClass", currentSelectionName);

                                String user = SmartFinderApplicationHolder.getApplication().getUser();
                                String password = SmartFinderApplicationHolder.getApplication().getPass();
                                String camerasAddress = SmartFinderApplicationHolder.getApplication().getCameraAddress();

                                if (camerasAddress.isEmpty()) {
                                    Log.i(TAG, "No camera address!");
                                    startActivity(new Intent(ObjectRecognizedActivity.this, SettingsActivity.class));
                                    Handler handler = new Handler(Looper.getMainLooper());
                                    handler.post(new Runnable() {

                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "No camera address!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    return;
                                }

                                // Try uploading the image
                                try {
                                    Future<Boolean> logged = HTTPUtility.login(camerasAddress + "/login/submit", "userName", user, "userPass", password);
                                    if (logged.get()) {
                                        // Get the bounded image only
                                        byte[] boundedImage = null;
                                        for(Recognition recognition : recognitions) {
                                            if(recognition.getTitle().equalsIgnoreCase(currentSelectionName)) {
                                                boundedImage = recognition.getBoundedObject();
                                                break;
                                            }
                                        }
                                        Future<byte[]> response = HTTPUtility.postImage(camerasAddress + "/images", boundedImage , extras);
                                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                        image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                        byte[] byteImage = stream.toByteArray();
                                        handleResponse(new String(response.get()) ,byteImage, boundedImage);
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    }
                                } catch (IOException | JSONException | InterruptedException | ExecutionException e) {
                                    Log.i(TAG, "Exception!");
                                    startActivity(new Intent(ObjectRecognizedActivity.this, SettingsActivity.class));
                                    Handler handler = new Handler(Looper.getMainLooper());
                                    handler.post(new Runnable() {

                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "Something went wrong! Check credentials!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    return;
                                }
                            }
                            objectView.setImageBitmap(null);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            };

            final AlertDialog.Builder builder = new AlertDialog.Builder(ObjectRecognizedActivity.this);

            // ListView Item Click Listener
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    currentSelectionName = (String) listView.getItemAtPosition(position);

                    if (currentSelectionName.equals(NONE_OF_THE_ABOVE)) {
                        startActivity(new Intent(ObjectRecognizedActivity.this, UserRecognitionActivity.class).putExtra("image", image));
                    } else {
                        builder.setMessage("Accept this recognition?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();
                    }

                }

            });

        }
    }



    /**
     * Handles the response from the server and inserts the newly recognized object into the local database if successful
     * @param response
     */
    public void handleResponse(String response, byte[] image, byte[] boundedObject) {
        if(response != null) {
            try {
                // Get the inserted id
                JSONObject jsonObject = new JSONObject(response);
                int id = (int)jsonObject.get("id");

                // Save the PNG image as byte array(BLOB)
                //ByteArrayOutputStream stream = new ByteArrayOutputStream();
                //image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                //byte[] byteArray = stream.toByteArray();

                ContentValues values = new ContentValues();
                values.put(ObjectContract.Column.ID, id);
                values.put(ObjectContract.Column.OWNER, SmartFinderApplicationHolder.getApplication().getUser());
                values.put(ObjectContract.Column.OBJECT_NAME, currentSelectionName);
                values.put(ObjectContract.Column.IMG, image);
                values.put(ObjectContract.Column.CROPPED_IMG, boundedObject);
                values.put(ObjectContract.Column.CREATED_AT, new Date().getTime());
                Uri uri = getContentResolver().insert(ObjectContract.CONTENT_URI, values);

                if(uri != null) {
                    Log.i(TAG, String.format("Inserted: %s", currentSelectionName));
                }

                return;
            } catch (JSONException e) {
                Log.i(TAG, e.getMessage());
            }
        }
    }
}
