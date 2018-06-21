package com.sma.smartfinder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sma.smartfinder.db.CameraContract;
import com.sma.smartfinder.db.CameraDbHelper;
import com.sma.smartfinder.http.utils.HTTPUtility;

import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import sma.com.smartfinder.R;

/**
 * Activity for listing all the cameras for the logged in user
 */
public class CamerasActivity extends BaseActivity {
    /**
     * Button for adding a new camera
     */
    private Button addCamera;

    /**
     * The list of cameras
     */
    private ListView camerasList;

    /**
     * Current tapped camera
     */
    private String currentSelection;

    /**
     * An adapter to display camera addresses as a list
     */
    private CameraAdapter adapter;

    /**
     * Current tapped camera ID
     */
    private int currentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cameras);

        camerasList = findViewById(R.id.cameras_list);
        addCamera = findViewById(R.id.add_new_camera);

        addCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CamerasActivity.this, AddCameraActivity.class));
                finish();
            }
        });

        // Dialog for deleting a camera
        final AlertDialog.Builder builder = new AlertDialog.Builder(CamerasActivity.this);


        final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE: {
                        final SmartFinderApplication smartFinderApplication = SmartFinderApplicationHolder.getApplication();
                        try {
                            // Send DELETE HTTP and delete from local database if successful
                            Future<Boolean> delete = HTTPUtility.deleteCamera(smartFinderApplication.getCameraAddress() + "/cameras", String.valueOf(currentId));
                            if(delete.get()) {
                                CameraDbHelper dbHelper = new CameraDbHelper(CamerasActivity.this);
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                db.delete(CameraContract.Column.TABLE_NAME, CameraContract.Column._ID + "=" + currentId, null);

                                // Update the cursor so changes are reflected in the list
                                Cursor cameraCursor = db.rawQuery("SELECT * FROM camera WHERE owner='" + SmartFinderApplicationHolder.getApplication().getUser() + "'" , null);
                                adapter.swapCursor(cameraCursor);
                                adapter.notifyDataSetChanged();
                            }
                        } catch(JSONException|IOException|InterruptedException|ExecutionException e) {
                            Log.e("DEBUG", e.getMessage());
                        }
                        break;
                    }
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        camerasList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor cursor = (Cursor) adapter.getItem(i);
                cursor.moveToPosition(i);

                // Get the current selection
                currentSelection = (String) cursor.getString(cursor.getColumnIndexOrThrow(CameraContract.Column.COLUMN_NAME_ADDRESS));
                currentId = (int) cursor.getInt(cursor.getColumnIndexOrThrow(CameraContract.Column._ID));

                builder.setMessage("Delete camera?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
            }
        });

        // Create a cursor for all the cameras of this current user
        CameraDbHelper handler = new CameraDbHelper(this);
        SQLiteDatabase db = handler.getWritableDatabase();
        Cursor cameraCursor = db.rawQuery("SELECT * FROM camera WHERE owner='" + SmartFinderApplicationHolder.getApplication().getUser() + "'", null);
        //cameraCursor.moveToFirst();
        //String address = cameraCursor.getString(cameraCursor.getColumnIndexOrThrow(CameraContract.Column.COLUMN_NAME_ADDRESS));

        adapter = new CameraAdapter(this, cameraCursor);
        camerasList.setAdapter(adapter);
    }

    class CameraAdapter extends CursorAdapter {
        public CameraAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }

        // The newView method is used to inflate a new view and return it,
        // you don't bind any data to the view at this point.
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.item_camera, parent, false);
        }

        // The bindView method is used to bind all data to a given view
        // such as setting the text on a TextView.
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView cameraId = (TextView) view.findViewById(R.id.camera_address_item);
            String address = cursor.getString(cursor.getColumnIndexOrThrow(CameraContract.Column.COLUMN_NAME_ADDRESS));
            cameraId.setText(String.valueOf(address));
        }
    }
}
