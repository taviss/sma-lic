package com.sma.smartfinder.db;

import android.provider.BaseColumns;

public class CameraContract {

    public static class Column implements BaseColumns {
        public static final String TABLE_NAME = "camera";
        public static final String COLUMN_NAME_ADDRESS = "address";
        public static final String COLUMN_NAME_OWNER = "owner";
    }
}
