package com.sma.smartfinder.db;

import android.provider.BaseColumns;

/**
 * Created by octavian.salcianu on 6/19/2018.
 */

public class CameraContract {

    public static class CameraEntry implements BaseColumns {
        public static final String TABLE_NAME = "camera";
        public static final String COLUMN_NAME_ADDRESS = "address";
        public static final String COLUMN_NAME_OWNER = "owner";
    }
}
