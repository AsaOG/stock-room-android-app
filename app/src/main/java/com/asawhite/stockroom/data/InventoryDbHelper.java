package com.asawhite.stockroom.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.asawhite.stockroom.data.InventoryContract.StockEntry;

/**
 * Created by Asa on 18/07/2017.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = InventoryDbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_STOCK_TABLE = "CREATE TABLE " + StockEntry.TABLE_NAME + " (" +
                StockEntry.COLUMN_TITLE_ID + " " + StockEntry.COLUMN_TYPE_ID +
                " PRIMARY KEY AUTOINCREMENT, " +
                StockEntry.COLUMN_TITLE_NAME + " " + StockEntry.COLUMN_TYPE_NAME + " NOT NULL, " +
                StockEntry.COLUMN_TITLE_PRICE + " " + StockEntry.COLUMN_TYPE_PRICE + " NOT NULL, " +
                StockEntry.COLUMN_TITLE_QUANTITY + " " + StockEntry.COLUMN_TYPE_QUANTITY +
                " NOT NULL, " +
                StockEntry.COLUMN_TITLE_SUPPLIER_EMAIL + " " +
                StockEntry.COLUMN_TYPE_SUPPLIER_EMAIL + " NOT NULL, " +
                StockEntry.COLUMN_TITLE_IMAGE + " " + StockEntry.COLUMN_TYPE_IMAGE + " NOT NULL);";

        db.execSQL(SQL_CREATE_STOCK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }
}
