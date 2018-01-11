package com.sma.smartfinder.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by octavian.salcianu on 12/19/2017.
 */

public class ObjectContract {
    public static final String DB_NAME = "rec_objects.db";
    public static final int DB_VERSION = 1;
    public static final String TABLE = "objects";

    public static final String DEFAULT_SORT = Column.CREATED_AT + " DESC";

    public static final String AUTHORITY = "com.sma.smartfinder.db.ObjectProvider";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE);
    public static final int STATUS_ITEM = 1;
    public static final int STATUS_DIR = 2;
    public static final String STATUS_TYPE_ITEM =
            "vnd.android.cursor.item/vnd.com.sma.smartfinder.provider.object";
    public static final String STATUS_TYPE_DIR =
            "vnd.android.cursor.dir/vnd.com.sma.smartfinder.provider.object";



    public class Column {
        public static final String ID = BaseColumns._ID;
        public static final String OBJECT_NAME = "object_name";
        public static final String IMG = "img";
        public static final String CREATED_AT =  "created_at";
    }
}
