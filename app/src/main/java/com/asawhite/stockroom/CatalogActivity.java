package com.asawhite.stockroom;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.asawhite.stockroom.data.InventoryContract.StockEntry;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.asawhite.stockroom.R.id.fab;

public class CatalogActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = CatalogActivity.class.getSimpleName();
    public static final int STOCK_LOADER_ID = 1;

    @BindView(R.id.list)
    ListView mListView;
    @BindView(R.id.empty_view)
    RelativeLayout mEmptyView;
    @BindView(fab)
    FloatingActionButton mFab;

    private StockCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        ButterKnife.bind(this);

        mListView.setEmptyView(mEmptyView);
        mAdapter = new StockCursorAdapter(this, null);
        mListView.setAdapter(mAdapter);

        // Opens the EditorActivity when a list item is clicked, the relevant info is displayed in
        // activity.
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                intent.setData(ContentUris.withAppendedId(StockEntry.CONTENT_URI, id));
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(STOCK_LOADER_ID, null, this);

        // Opens the EditorActivity to add a new stock item
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] columns = new String[]{StockEntry.COLUMN_TITLE_ID, StockEntry.COLUMN_TITLE_NAME,
                StockEntry.COLUMN_TITLE_PRICE, StockEntry.COLUMN_TITLE_QUANTITY};

        return new CursorLoader(this, StockEntry.CONTENT_URI, columns, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
