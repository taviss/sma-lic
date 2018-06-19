package com.sma.smartfinder;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sma.smartfinder.db.CameraContract;
import com.sma.smartfinder.db.CameraDbHelper;

import sma.com.smartfinder.R;

public class CamerasActivity extends BaseActivity {
    private Button addCamera;
    private  ListView camerasList;

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
            }
        });

        CameraDbHelper handler = new CameraDbHelper(this);
        SQLiteDatabase db = handler.getWritableDatabase();
        Cursor cameraCursor = db.rawQuery("SELECT * FROM camera", null);
        //cameraCursor.moveToFirst();
        //String address = cameraCursor.getString(cameraCursor.getColumnIndexOrThrow(CameraContract.CameraEntry.COLUMN_NAME_ADDRESS));

        CameraAdapter cameraAdapter = new CameraAdapter(this, cameraCursor);
        camerasList.setAdapter(cameraAdapter);
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
            String address = cursor.getString(cursor.getColumnIndexOrThrow(CameraContract.CameraEntry.COLUMN_NAME_ADDRESS));
            cameraId.setText(String.valueOf(address));
        }
    }
}
