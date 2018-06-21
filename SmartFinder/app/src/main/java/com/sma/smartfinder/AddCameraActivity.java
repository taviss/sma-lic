package com.sma.smartfinder;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sma.smartfinder.db.CameraContract;
import com.sma.smartfinder.db.CameraDbHelper;
import com.sma.smartfinder.http.utils.HTTPUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import sma.com.smartfinder.R;

/**
 * Activity for adding a camera
 */
public class AddCameraActivity extends BaseActivity{
    private final String TAG = AddCameraActivity.class.getName();

    /**
     * Button for adding a new camera
     */
    private Button addCameraButton;

    /**
     * EditText for address
     */
    private EditText cameraAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_camera);

        addCameraButton = findViewById(R.id.add_new_camera_but);
        cameraAddress = findViewById(R.id.new_camera_address);

        addCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cameraAddress.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Address may not be empty!", Toast.LENGTH_LONG).show();
                } else {
                    try {
                        // Try login and try adding the camera
                        final SmartFinderApplication smartFinderApplication = SmartFinderApplicationHolder.getApplication();
                        Future<Boolean> loggedIn = HTTPUtility.login(smartFinderApplication.getCameraAddress() + "/login/submit", "userName", smartFinderApplication.getUser(), "userPass", smartFinderApplication.getPass());
                        if (loggedIn.get()) {
                            Future<byte[]> addCamera = HTTPUtility.addCamera(smartFinderApplication.getCameraAddress() + "/cameras", cameraAddress.getText().toString());
                            handleResponse(new String(addCamera.get()));
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Login failed!", Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException |JSONException |InterruptedException|ExecutionException e) {
                        Toast.makeText(getApplicationContext(), "Operation failed!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    /**
     * Handles the response from the server and updates the local database accordingly
     * @param response
     */
    public void handleResponse(String response) {
        if(response != null) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                int id = (int)jsonObject.get("id");

                // Insert the new camera, using the ID provided by the server
                ContentValues values = new ContentValues();
                values.put(CameraContract.Column._ID, id);
                values.put(CameraContract.Column.COLUMN_NAME_OWNER, SmartFinderApplicationHolder.getApplication().getUser());
                values.put(CameraContract.Column.COLUMN_NAME_ADDRESS, cameraAddress.getText().toString());

                CameraDbHelper dbHelper = new CameraDbHelper(this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                db.insert(CameraContract.Column.TABLE_NAME, null, values);

                startActivity(new Intent(AddCameraActivity.this, CamerasActivity.class));
            } catch (JSONException e) {
                Log.i(TAG, e.getMessage());
            }
        }
    }
}
