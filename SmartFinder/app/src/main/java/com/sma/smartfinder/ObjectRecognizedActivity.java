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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sma.smartfinder.db.ObjectContract;
import com.sma.smartfinder.http.utils.HTTPUtility;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import sma.com.smartfinder.R;

public class ObjectRecognizedActivity extends BaseActivity {
    private static final String TAG = ObjectRecognizedActivity.class.getSimpleName();

    private ImageView objectView;
    private ListView listView;

    private List<String> recognitions;

    private String currentSelectionName = null;

    private Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_recognized);

        listView = (ListView) findViewById(R.id.list_view_recognitions);
        objectView = (ImageView) findViewById(R.id.list_item_object_view);

        recognitions = getIntent().getStringArrayListExtra("recognitions");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, recognitions);

        listView.setAdapter(adapter);

        final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        if(image != null && currentSelectionName != null) {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] byteArray = stream.toByteArray();

                            ContentValues values = new ContentValues();
                            values.put(ObjectContract.Column.OBJECT_NAME, currentSelectionName);
                            values.put(ObjectContract.Column.IMG, byteArray);
                            values.put(ObjectContract.Column.CREATED_AT, new Date().getTime());
                            Uri uri = getContentResolver().insert(ObjectContract.CONTENT_URI, values);

                            if(uri != null) {
                                Log.i(TAG, String.format("Inserted: %s", currentSelectionName));
                            }

                            HashMap<String, String> extras = new HashMap<>();
                            extras.put("imageClass", currentSelectionName);

                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                            String user = preferences.getString("username", "");
                            String password = preferences.getString("password", "");
                            String camerasAddress = preferences.getString("camera_server_address", "");

                            if(camerasAddress.isEmpty()) {
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

                            try {
                                Future<Boolean> logged = HTTPUtility.login(camerasAddress + "/login/submit", "userName", user, "userPass", password);
                                if(logged.get()) {
                                    HTTPUtility.postImage(camerasAddress + "/images", image, extras);
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                }
                            } catch(IOException|JSONException|InterruptedException|ExecutionException e) {
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

                builder.setMessage("Accept this recognition?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

            }

        });

        Bitmap bmp = null;
        String filename = getIntent().getStringExtra("image");
        try {
            FileInputStream is = this.openFileInput(filename);
            bmp = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //TODO
        objectView.setImageBitmap(bmp);
        image = bmp;

        /*
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bmp != null) {
                    ContentValues values = new ContentValues();
                    values.put(ObjectContract.Column.OBJECT_NAME, currentImage.first);
                    values.put(ObjectContract.Column.IMG, currentImage.second);
                    values.put(ObjectContract.Column.CREATED_AT, new Date().getTime());
                    Uri uri = getContentResolver().insert(ObjectContract.CONTENT_URI, values);

                    if(uri != null) {
                        Log.i(TAG, String.format("Inserted: %s", currentImage.first));
                    }
                }
                imageViewResult.setImageBitmap(null);
                textViewResult.setText("");
                btnAcceptObject.setVisibility(View.INVISIBLE);
            }
        });*/
    }
}
