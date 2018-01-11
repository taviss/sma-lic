package com.sma.smartfinder;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sma.smartfinder.db.ObjectContract;

import java.util.Date;

import sma.com.smartfinder.R;

public class ObjectFoundActivity extends BaseActivity {
    private static final String TAG = ObjectFoundActivity.class.getSimpleName();

    private TextView textName, textCreatedAt;
    private ImageView objectView;
    private Button btnAccept;

    private byte[] image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_found);

        textName = (TextView) findViewById(R.id.list_item_object_name);
        textCreatedAt = (TextView) findViewById(R.id.list_item_created_at);
        objectView = (ImageView) findViewById(R.id.list_item_object_view);
        btnAccept = (Button) findViewById(R.id.btnAcceptObject);

        textName.setText(getIntent().getStringExtra("imageName"));
        textCreatedAt.setText(DateUtils.getRelativeTimeSpanString(System.currentTimeMillis()/1000));

        //TODO
        objectView.setImageBitmap(null);

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                values.put(ObjectContract.Column.OBJECT_NAME, textName.getText().toString());
                values.put(ObjectContract.Column.IMG, image);
                values.put(ObjectContract.Column.CREATED_AT, new Date().getTime());
                Uri uri = getContentResolver().insert(ObjectContract.CONTENT_URI, values);

                if(uri != null) {
                    Log.i(TAG, String.format("Inserted: %s", textName.getText()));
                }
            }
        });
    }
}
