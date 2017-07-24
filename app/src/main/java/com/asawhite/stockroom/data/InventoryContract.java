package com.asawhite.stockroom.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Asa on 18/07/2017.
 */

public final class InventoryContract {

    public static final String CONTENT_AUTHORITY = "com.asawhite.stockroom";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_STOCK = "stock";

    private InventoryContract() {}

    public static class StockEntry implements BaseColumns {
        // Name of the table
        public static final String TABLE_NAME = "stock";

        // Column titles
        public static final String COLUMN_TITLE_ID = "_id";
        public static final String COLUMN_TITLE_NAME = "name";
        public static final String COLUMN_TITLE_PRICE = "price";
        public static final String COLUMN_TITLE_QUANTITY = "quantity";
        public static final String COLUMN_TITLE_SUPPLIER_EMAIL = "supplier";
        public static final String COLUMN_TITLE_IMAGE = "image";

        // Column types
        public static final String COLUMN_TYPE_ID = "INTEGER";
        public static final String COLUMN_TYPE_NAME = "TEXT";
        public static final String COLUMN_TYPE_PRICE = "REAL";
        public static final String COLUMN_TYPE_QUANTITY = "INTEGER";
        public static final String COLUMN_TYPE_SUPPLIER_EMAIL = "TEXT";
        public static final String COLUMN_TYPE_IMAGE = "TEXT";

        // Uri for table
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_STOCK);

        // MIME type for list
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOCK;

        // MIME type for single item
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOCK;
    }
}
