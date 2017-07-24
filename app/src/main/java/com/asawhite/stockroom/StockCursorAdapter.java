package com.asawhite.stockroom;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.asawhite.stockroom.data.InventoryContract.StockEntry;

import java.text.NumberFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Asa on 19/07/2017.
 */

public class StockCursorAdapter extends CursorAdapter {

    public static final String LOG_TAG = StockCursorAdapter.class.getSimpleName();

    @BindView(R.id.name_tv)
    TextView nameTv;
    @BindView(R.id.price_tv)
    TextView priceTv;
    @BindView(R.id.quantity_tv)
    TextView quantityTv;
    @BindView(R.id.sale_button)
    Button saleButton;

    private int mQuantity;

    public StockCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        ButterKnife.bind(this, view);

        String name = cursor.getString(cursor.getColumnIndex(StockEntry.COLUMN_TITLE_NAME));
        float price = cursor.getFloat(cursor.getColumnIndex(StockEntry.COLUMN_TITLE_PRICE));
        mQuantity = cursor.getInt(cursor.getColumnIndex(StockEntry.COLUMN_TITLE_QUANTITY));
        String priceFormatted = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(price);

        nameTv.setText(name);
        priceTv.setText(priceFormatted);
        quantityTv.setText(String.valueOf(mQuantity));
        saleButton.setTag(cursor.getPosition());

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = (int) view.getTag();
                Cursor c = getCursor();
                c.moveToPosition(position);
                int q = c.getInt(c.getColumnIndex(StockEntry.COLUMN_TITLE_QUANTITY));

                if (q > 0) {
                    q--;
                    ContentValues values = new ContentValues();
                    values.put(StockEntry.COLUMN_TITLE_QUANTITY, q);

                    context.getContentResolver().update(ContentUris.
                                    withAppendedId(StockEntry.CONTENT_URI, c.getInt(c
                                            .getColumnIndex(StockEntry.COLUMN_TITLE_ID))),
                            values, null, null);

                } else {
                    Toast.makeText(
                            context, R.string.stock_zero_toast, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
