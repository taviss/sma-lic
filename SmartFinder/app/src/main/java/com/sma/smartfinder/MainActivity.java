package com.sma.smartfinder;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sma.smartfinder.db.ObjectContract;
import com.sma.smartfinder.http.utils.HTTPUtility;
import com.sma.smartfinder.services.ObjectRecoginzerService;

import org.json.JSONException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import sma.com.smartfinder.R;

/**
 * Main activity - holds all the recognized objects
 */
public class MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    /**
     * Convert from
     */
    private static final String[] FROM = {
            ObjectContract.Column.OBJECT_NAME,
            ObjectContract.Column.CREATED_AT,
            ObjectContract.Column.IMG
    };

    /**
     * Convert to
     */
    private static final int[] TO = {
            R.id.list_item_object_name,
            R.id.list_item_created_at,
            R.id.list_item_background
    };

    private static final int LOADER_ID = 42;

    private SimpleCursorAdapter mAdapter;

    private BottomNavigationView bottomNavigationView;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id != LOADER_ID) {
            return null;
        }

        Log.d(TAG, "onCreateLoader");
        // Load current user's objects
        String selection = ObjectContract.Column.OWNER + "=?";
        String[] selectionArgs = { SmartFinderApplicationHolder.getApplication().getUser() };
        return new CursorLoader(this, ObjectContract.CONTENT_URI, null, selection, selectionArgs, ObjectContract.DEFAULT_SORT);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Create fragments for loaded data for details display
        DetailsFragment detailsFragment = (DetailsFragment) getFragmentManager().findFragmentById(R.id.fragment_details);

        if(detailsFragment != null && detailsFragment.isVisible() && data.getCount() == 0) {
            detailsFragment.updateView(-1);
            Toast.makeText(this, "No data", Toast.LENGTH_LONG).show();
        }

        Log.d(TAG, "onLoadFinished");
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    class ObjectsViewBinder implements SimpleCursorAdapter.ViewBinder {

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

            // Update elements accordingly
            if(view.getId() == R.id.list_item_created_at) {
                long timestamp = cursor.getLong(columnIndex);
                CharSequence relativeTime = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(new Date(timestamp));
                ((TextView) view).setText(relativeTime);
            } else if(view.getId() == R.id.list_item_object_name) {
                String name = cursor.getString(columnIndex);
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
                ((TextView) view).setText(name);
            } else if(view.getId() == R.id.list_item_background) {
                byte[] img = cursor.getBlob(columnIndex);
                Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
                ((ImageView)view).setImageBitmap(bitmap);
            }
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity#onCreate()");

        if(savedInstanceState == null) {
            // Create new cursor adapter
            setContentView(R.layout.activity_main);
            mAdapter = new SimpleCursorAdapter(this, R.layout.objects_list, null, FROM, TO, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            mAdapter.setViewBinder(new ObjectsViewBinder());

            GridView listView = findViewById(R.id.objects_list);
            listView.setAdapter(mAdapter);

            // On click, display details about the object
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    System.out.println(adapterView.getItemAtPosition(position));
                    DetailsFragment detailsFragment = (DetailsFragment) getFragmentManager().findFragmentById(R.id.fragment_details);

                    if(detailsFragment != null && detailsFragment.isVisible()) {
                        detailsFragment.updateView(id);
                    } else {
                        startActivity(new Intent(MainActivity.this, DetailsActivity.class).putExtra(ObjectContract.Column.ID, id));
                    }
                }
            });

            getLoaderManager().initLoader(LOADER_ID, null, this);

            bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottom_navigation);

            // Navigation menu for different activities/options
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch(item.getItemId()) {
                        case R.id.action_settings:
                            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                            return true;

                        case R.id.action_detect:
                            startActivity(new Intent(MainActivity.this, DetectActivity.class));
                            return true;

                        case R.id.action_cameras:
                            startActivity(new Intent(MainActivity.this, CamerasActivity.class));
                            return true;

                        default:
                            return false;
                    }
                }
            });

        }

        //Log.i(TAG, "objectFinderService#onHandleIntent()");

        /*

                try {
                    Intent requestLocateIntent = new Intent(MainActivity.this, ObjectRecoginzerService.class);

                    String filename = "img_locate.png";
                    FileOutputStream stream = MainActivity.this.openFileOutput(filename, Context.MODE_PRIVATE);

                    Bitmap bmp = null;
                    //String filename = intent.getStringExtra("locate_image");
                    try {
                        InputStream is = getAssets().open("alonso2017.png");//this.openFileInput(filename);
                        bmp = BitmapFactory.decodeStream(is);
                        is.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);

                    stream.close();
                    bmp.recycle();

                    requestLocateIntent.putExtra("locate_image", filename);
                    startService(requestLocateIntent);
                    //getActivity().startActivity(new Intent(getContext(), FindObjectActivity.class));
                } catch(IOException e) {
                    Toast.makeText(getApplicationContext(), "There was an error with sending the image to the ObjectFinderService!", Toast.LENGTH_LONG).show();
                }

        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.action_detect:
                startActivity(new Intent(this, DetectActivity.class));
                return true;

            case R.id.action_cameras:
                startActivity(new Intent(MainActivity.this, CamerasActivity.class));
                return true;

            default:
                return false;
        }
    }
}
