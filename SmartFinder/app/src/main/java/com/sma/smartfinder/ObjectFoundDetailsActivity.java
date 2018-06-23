package com.sma.smartfinder;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import sma.com.smartfinder.R;

/**
 * Activity for displaying a found object
 */
public class ObjectFoundDetailsActivity extends BaseActivity {
    /**
     * The name of the found object
     */
    private TextView textName;

    /**
     * The image of where it was found
     */
    private ImageView objectView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_found);

        textName = (TextView) findViewById(R.id.list_item_object_name);
        objectView = (ImageView) findViewById(R.id.list_item_object_view);

        // Display the image using data passed to this activity
        byte[] blob = getIntent().getByteArrayExtra("image");
        String name = getIntent().getStringExtra("name");
        objectView.setImageBitmap(BitmapFactory.decodeByteArray(blob, 0, blob.length));
        textName.setText(name);
    }
}
