package com.sma.smartfinder;

import android.app.Fragment;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sma.smartfinder.db.ObjectContract;
import com.sma.smartfinder.services.ObjectFinderService;

import sma.com.smartfinder.R;

public class DetailsFragment extends Fragment {
    private TextView textName, textCreatedAt;
    private ImageView objectView;
    private Button btnLocate;

    private Bitmap currentImage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, null, false);

        textName = (TextView) view.findViewById(R.id.list_item_object_name);
        textCreatedAt = (TextView) view.findViewById(R.id.list_item_created_at);
        objectView = (ImageView) view.findViewById(R.id.list_item_object_view);
        btnLocate = (Button) view.findViewById(R.id.btnFindObject);

        btnLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent requestLocateIntent = new Intent(getActivity(), ObjectFinderService.class);
                requestLocateIntent.putExtra("BitmapImage", currentImage);
                getActivity().startService(requestLocateIntent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        long id = getActivity().getIntent().getLongExtra(ObjectContract.Column.ID, -1);
        updateView(id);
    }

    public void updateView(long id) {
        if(id == -1) {
            textName.setText("");
            objectView.setVisibility(View.INVISIBLE);
            textCreatedAt.setText("");
            return;
        }

        Uri uri = ContentUris.withAppendedId(ObjectContract.CONTENT_URI, id);

        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);

        if(!cursor.moveToFirst())
            return;

        String name = cursor.getString(cursor.getColumnIndex(ObjectContract.Column.OBJECT_NAME));
        byte[] blob = cursor.getBlob(cursor.getColumnIndex(ObjectContract.Column.IMG));
        long createdAt = cursor.getLong(cursor.getColumnIndex(ObjectContract.Column.CREATED_AT));

        textName.setText(name);
        textCreatedAt.setText(DateUtils.getRelativeTimeSpanString(createdAt));

        currentImage = BitmapFactory.decodeByteArray(blob, 0, blob.length);
        objectView.setImageBitmap(currentImage);
    }
}
