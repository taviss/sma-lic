package com.sma.smartfinder.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by octavian.salcianu on 12/19/2017.
 */

public class ObjectProvider extends ContentProvider {
    public static final String TAG = ObjectProvider.class.getName();
    private DbHelper dbHelper;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(ObjectContract.AUTHORITY, ObjectContract.TABLE, ObjectContract.STATUS_DIR);
        sURIMatcher.addURI(ObjectContract.AUTHORITY, ObjectContract.TABLE + "/#", ObjectContract.STATUS_ITEM);
    }

    @Override
    public boolean onCreate() {
        Log.i(TAG, "ObjectProvider#onCreate");
        dbHelper = new DbHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(ObjectContract.TABLE);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor;

        switch(sURIMatcher.match(uri)) {
            case ObjectContract.STATUS_DIR:
                cursor = db.rawQuery("SELECT * FROM " + ObjectContract.TABLE, null);
                //Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
                break;
            case ObjectContract.STATUS_ITEM:
                if(selection == null) {
                    qb.appendWhere(ObjectContract.Column.ID + "=" + uri.getLastPathSegment());
                }
                cursor = db.rawQuery("SELECT * FROM " + ObjectContract.TABLE + " WHERE " + ObjectContract.Column.ID + "=" + uri.getLastPathSegment(), null);
                //Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
                break;

            default:
                throw new IllegalArgumentException();
        }

        String orderBy = (TextUtils.isEmpty(sortOrder)) ? ObjectContract.DEFAULT_SORT : sortOrder;

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        //Log.d(TAG, "queried: " + cursor.getCount());
        //Log.d(TAG, "query: " + selection + " " + selectionArgs[0]);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case ObjectContract.STATUS_DIR:
                Log.d(TAG, "type:" + ObjectContract.STATUS_TYPE_DIR);
                return ObjectContract.STATUS_TYPE_DIR;
            case ObjectContract.STATUS_ITEM:
                Log.d(TAG, "type:" + ObjectContract.STATUS_TYPE_ITEM);
                return ObjectContract.STATUS_TYPE_ITEM;

            default:
                throw new IllegalArgumentException();
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Uri ret = null;

        if(sURIMatcher.match(uri) != ObjectContract.STATUS_DIR) {
            throw new IllegalArgumentException();
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insertWithOnConflict(ObjectContract.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        if(rowId != -1) {
            ret = ContentUris.withAppendedId(uri, rowId);
            Log.d(TAG, "inserted " + ret);

            getContext().getContentResolver().notifyChange(uri, null);
        }

        return ret;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        String where;

        switch(sURIMatcher.match(uri)) {
            case ObjectContract.STATUS_DIR:
                where = selection == null ? "1" : selection;
                break;
            case ObjectContract.STATUS_ITEM:
                where = selection;
                if(where == null) {
                    long id = ContentUris.parseId(uri);
                    where = ObjectContract.Column.ID +
                            "=" +
                            id +
                            (TextUtils.isEmpty(selection) ? "" : " AND ( " + selection + " )");
                }
                break;

            default:
                throw new IllegalArgumentException();
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int ret = db.delete(ObjectContract.TABLE, where, selectionArgs);

        if(ret>0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        Log.d(TAG, "updated: " + ret);
        return ret;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        String where;

        switch(sURIMatcher.match(uri)) {
            case ObjectContract.STATUS_DIR:
                where = selection;
                break;
            case ObjectContract.STATUS_ITEM:
                where = selection;
                if(where == null) {
                    long id = ContentUris.parseId(uri);
                    where = ObjectContract.Column.ID +
                            "=" +
                            id +
                            (TextUtils.isEmpty(selection) ? "" : " AND ( " + selection + " )");
                }
                break;

            default:
                throw new IllegalArgumentException();
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int ret = db.update(ObjectContract.TABLE, values, where, selectionArgs);

        if(ret>0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        Log.d(TAG, "updated: " + ret);
        return ret;
    }
}
