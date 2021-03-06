package com.sma.smartfinder.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DB helper for cameras
 */
public class CameraDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "cameras.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + CameraContract.Column.TABLE_NAME + " (" +
                    CameraContract.Column._ID + " INTEGER PRIMARY KEY," +
                    CameraContract.Column.COLUMN_NAME_ADDRESS + " TEXT," +
                    CameraContract.Column.COLUMN_NAME_OWNER + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + CameraContract.Column.TABLE_NAME;


    public CameraDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }
}
