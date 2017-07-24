package com.asawhite.stockroom.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.asawhite.stockroom.data.InventoryContract.StockEntry;

/**
 * Created by Asa on 18/07/2017.
 */

public class StockProvider extends ContentProvider {

    public static final String LOG_TAG = StockProvider.class.getSimpleName();

    private InventoryDbHelper mDbHelper;
    public static final int STOCK = 100;
    public static final int STOCK_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,
                InventoryContract.PATH_STOCK, STOCK);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,
                InventoryContract.PATH_STOCK + "/#", STOCK_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    /**
     * Performs a query on the SQLite database
     *
     * @param uri is the Uri pointing to the table to query
     * @param columns is a String array containing the titles of the columns to be returned
     * @param selection is the column to be searched when using a WHERE SQLite command
     * @param selectionArgs is the value for the WHERE command
     * @param sortOrder is the sort order for the returned results
     * @return a Cursor object containing the results from the query
     */
    @Override
    public Cursor query(Uri uri, String[] columns, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor result;
        int matchCode = sUriMatcher.match(uri);

        switch (matchCode) {

            case STOCK:
                result = db.query(StockEntry.TABLE_NAME, columns, selection, selectionArgs, null,
                        null, sortOrder);
                break;

            case STOCK_ID:
                selection = StockEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                Log.d(LOG_TAG, selectionArgs[0]);

                result = db.query(StockEntry.TABLE_NAME, columns, selection, selectionArgs, null,
                        null, sortOrder);
                Log.d(LOG_TAG, String.valueOf(result.getCount()));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        result.setNotificationUri(getContext().getContentResolver(), uri);
        return result;
    }

    /**
     * Performs an insert command on the SQLite database
     *
     * @param uri is the Uri pointing to the table to insert into
     * @param contentValues is a ContentValues object containing the key value pairs to be inserted
     *                      into the table
     * @return a Uri object which points to the new entry in the table
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        float price = contentValues.getAsFloat(StockEntry.COLUMN_TITLE_PRICE);

        if (price <= 0) {
            throw new IllegalArgumentException("Price must be more than 0");
        }

        int quantity = contentValues.getAsInteger(StockEntry.COLUMN_TITLE_QUANTITY);

        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must not be negative");
        }

        final int match = sUriMatcher.match(uri);

        if (match == STOCK) {
            return insertStock(uri, contentValues);

        } else {
            throw new IllegalArgumentException("Cannot insert for uri " + uri);
        }
    }

    /**
     * A helper method for inserting a new entry into the table
     *
     * @param uri is the Uri pointing to the table to insert into
     * @param contentValues is a ContentValues object containing the key value pairs to be inserted
     *                      into the table
     * @return a Uri object which points to the new entry in the table
     */
    private Uri insertStock(Uri uri, ContentValues contentValues) {
        long id = mDbHelper.getWritableDatabase()
                .insert(StockEntry.TABLE_NAME, null, contentValues);

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Deletes rows from the table
     *
     * @param uri is the Uri pointing to the table to delete from
     * @param selection is the column to be searched when using a WHERE SQLite command
     * @param selectionArgs is the value for the WHERE command
     * @return an int value which equals the number of rows deleted
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rows;

        switch (match) {

            case STOCK:
                rows = db.delete(StockEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case STOCK_ID:
                selection = StockEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rows = db.delete(StockEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rows;
    }

    /**
     * Updates rows in the table
     *
     * @param uri is a Uri pointing to the table to update
     * @param contentValues is a ContentValues object containing the key value pairs to update
     * @param selection is the column to be searched when using a WHERE SQLite command
     * @param selectionArgs is the value for the WHERE command
     * @return an int value which equals the number of rows affected by the update
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);

        switch (match) {

            case STOCK:
                return updateStock(uri, contentValues, selection, selectionArgs);

            case STOCK_ID:
                selection = StockEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateStock(uri, contentValues, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update not supported for uri " + uri);
        }
    }

    /**
     * Helper method to update rows in the table
     *
     * @param uri is a Uri pointing to the table to update
     * @param contentValues is a ContentValues object containing the key value pairs to update
     * @param selection is the column to be searched when using a WHERE SQLite command
     * @param selectionArgs is the value for the WHERE command
     * @return an int value which equals the number of rows affected by the update
     */
    private int updateStock(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        if (contentValues.size() == 0) {
            return 0;
        }

        if (contentValues.containsKey(StockEntry.COLUMN_TITLE_NAME)) {
            String name = contentValues.getAsString(StockEntry.COLUMN_TITLE_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Stock item must have a name");
            }
        }

        if (contentValues.containsKey(StockEntry.COLUMN_TITLE_PRICE)) {
            Float price = contentValues.getAsFloat(StockEntry.COLUMN_TITLE_PRICE);
            if (price == null || price <= 0) {
                throw new IllegalArgumentException("Stock item must have a price more than 0");
            }
        }

        if (contentValues.containsKey(StockEntry.COLUMN_TITLE_QUANTITY)) {
            Integer quantity = contentValues.getAsInteger(StockEntry.COLUMN_TITLE_QUANTITY);
            if (quantity == null || quantity < 0) {
                throw new IllegalArgumentException("Stock item must not have a negative quantity");
            }
        }

        if (contentValues.containsKey(StockEntry.COLUMN_TITLE_SUPPLIER_EMAIL)) {
            String supplier = contentValues.getAsString(StockEntry.COLUMN_TITLE_SUPPLIER_EMAIL);
            if (supplier == null) {
                throw new IllegalArgumentException("Stock item must have a supplier email");
            }
        }

        if (contentValues.containsKey(StockEntry.COLUMN_TITLE_IMAGE)) {
            String imageString = contentValues.getAsString(StockEntry.COLUMN_TITLE_IMAGE);
            Uri imageUri = Uri.parse(imageString);
            if (imageString == null) {
                throw new IllegalArgumentException("Stock item must have a image." +
                        " Linked image may have been moved or deleted");
            }
        }

        int rows = mDbHelper.getWritableDatabase().update(StockEntry.TABLE_NAME, contentValues,
                selection, selectionArgs);

        if (rows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rows;
    }

    /**
     * Gets the MIME type for a given Uri
     *
     * @param uri the Uri pointing to a table or items in a table
     * @return a String containing the MIME type
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {

            case STOCK:
                return StockEntry.CONTENT_ITEM_TYPE;

            case STOCK_ID:
                return StockEntry.CONTENT_LIST_TYPE;

            default:
                throw new IllegalStateException("Unknown Uri " + uri + " with match " + match);
        }
    }
}
