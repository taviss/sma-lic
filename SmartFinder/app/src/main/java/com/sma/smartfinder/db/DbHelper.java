package com.sma.smartfinder.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sma.smartfinder.SmartFinderApplicationHolder;

/**
 * Created by octavian.salcianu on 12/19/2017.
 *
 * DB helper for Objects
 */

public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = DbHelper.class.getName();

    public DbHelper(Context context) {
        super(context, ObjectContract.DB_NAME, null, ObjectContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = String
                .format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s text, %s text, %s blob, %s blob, %s int)",
                        ObjectContract.TABLE,
                        ObjectContract.Column.ID,
                        ObjectContract.Column.OWNER,
                        ObjectContract.Column.OBJECT_NAME,
                        ObjectContract.Column.IMG,
                        ObjectContract.Column.CROPPED_IMG,
                        ObjectContract.Column.CREATED_AT);
        Log.d(TAG, sql);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ObjectContract.TABLE);
        onCreate(db);
    }
}
