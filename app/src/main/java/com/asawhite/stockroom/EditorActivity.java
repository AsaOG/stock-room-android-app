package com.asawhite.stockroom;

import android.Manifest;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.asawhite.stockroom.data.InventoryContract.StockEntry;

import java.io.File;
import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

import static java.lang.Integer.parseInt;

public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = EditorActivity.class.getSimpleName();
    public static final int PICK_FROM_GALLERY = 0;
    public static final int PICK_FROM_CAMERA = 1;
    public static final int CAMERA_REQUEST = 0;
    public static final int MANAGE_DOCS_REQUEST = 1;
    public static final int STOCK_ITEM_LOADER_ID = 1;

    @BindView(R.id.name_et)
    EditText mNameEt;
    @BindView(R.id.price_et)
    EditText mPriceEt;
    @BindView(R.id.quantity_et)
    EditText mQuantityEt;
    @BindView(R.id.supplier_et)
    EditText mSupplierEt;
    @BindView(R.id.product_image_iv)
    ImageView mImageIv;
    @BindView(R.id.decrease_quantity_button)
    Button mDecreaseButton;
    @BindView(R.id.increase_quantity_button)
    Button mIncreaseButton;
    @BindView(R.id.order_button)
    Button mOrderButton;

    private Uri mImageUri = null;
    private Uri mCurrentStockUri = null;
    private boolean mHasInfoChanged = false;

    private View.OnClickListener onImageClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showImageSelectionDialog();
        }
    };

    private Button.OnClickListener onQuantityClick = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            changeQuantity(view);
        }
    };

    private Button.OnClickListener onOrderClick = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            emailSupplier();
        }
    };

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mHasInfoChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setListeners();
        mImageIv.setImageResource(R.drawable.no_image_available);
        Intent intent = getIntent();
        mCurrentStockUri = intent.getData();

        // Sets the title and options menu and initiates a loader depending on whether a list item
        // was clicked or the Floating Action Button
        if (mCurrentStockUri == null) {
            setTitle(getString(R.string.title_add_stock));
            invalidateOptionsMenu();

        } else {
            setTitle(getString(R.string.title_edit_stock));
            getLoaderManager().initLoader(STOCK_ITEM_LOADER_ID, null, this);
            Log.i(LOG_TAG, "Loader init");
        }
    }

    /**
     * Sets all the listeners required for the activity
     */
    private void setListeners() {
        mNameEt.setOnTouchListener(onTouchListener);
        mPriceEt.setOnTouchListener(onTouchListener);
        mQuantityEt.setOnTouchListener(onTouchListener);
        mDecreaseButton.setOnTouchListener(onTouchListener);
        mIncreaseButton.setOnTouchListener(onTouchListener);
        mSupplierEt.setOnTouchListener(onTouchListener);
        mDecreaseButton.setOnClickListener(onQuantityClick);
        mIncreaseButton.setOnClickListener(onQuantityClick);
        mOrderButton.setOnClickListener(onOrderClick);
        mImageIv.setOnClickListener(onImageClick);
    }

    /**
     * Shows the dialog for the user to choose between gallery or camera
     */
    private void showImageSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.image_source_dialog);

        builder.setPositiveButton(R.string.gallery_dialog_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                openGallery();
            }
        });

        builder.setNegativeButton(R.string.camera_dialog_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                openCamera();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Opens the gallery to pick an image
     */
    private void openGallery() {
        Intent intent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent,
                getString(R.string.gallery_intent_title)), PICK_FROM_GALLERY);
    }

    /**
     * Checks permission for camera, if it passes then opens the camera. If fails then requests
     * permission
     */
    private void openCamera() {
        int permCheckCamera = ContextCompat.checkSelfPermission(EditorActivity.this,
                Manifest.permission.CAMERA);

        if (permCheckCamera == PermissionChecker.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(EditorActivity.this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);

        } else {
            File dir = new File(getFilesDir() + "/images");

            if (!dir.exists()) {
                dir.mkdir();
            }

            String fileName = System.currentTimeMillis() + ".jpeg";
            File file = new File(dir, fileName);
            mImageUri = FileProvider.getUriForFile(getApplicationContext(),
                    "com.asawhite.stockroom.fileprovider", file);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
            startActivityForResult(intent, PICK_FROM_CAMERA);
        }
    }

    /**
     * Saves the inputted information to the database using the ContentResolver
     */
    private void saveStockRecord() {
        String name = mNameEt.getText().toString().trim();
        String price = mPriceEt.getText().toString().trim();
        String quantity = mQuantityEt.getText().toString().trim();
        String supplier = mSupplierEt.getText().toString().trim();

        if (name.isEmpty() || price.isEmpty() || quantity.isEmpty() || supplier.isEmpty() ||
                mImageUri == null) {
            Toast.makeText(this, R.string.empty_fields_toast,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        float priceFloat;
        int quantityInt;

        try {
            priceFloat = getPriceFloat(price);
            quantityInt = parseInt(quantity);

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error parsing numbers", e);
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(supplier).matches()) {
            Toast.makeText(this, R.string.incorrect_email_toast, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(StockEntry.COLUMN_TITLE_NAME, name);
        values.put(StockEntry.COLUMN_TITLE_PRICE, priceFloat);
        values.put(StockEntry.COLUMN_TITLE_QUANTITY, quantityInt);
        values.put(StockEntry.COLUMN_TITLE_SUPPLIER_EMAIL, supplier);
        values.put(StockEntry.COLUMN_TITLE_IMAGE, mImageUri.toString());

        if (mCurrentStockUri == null) {
            Uri uri = getContentResolver().insert(StockEntry.CONTENT_URI, values);
            long id = ContentUris.parseId(uri);

            if (id == -1) {
                Toast.makeText(this, R.string.error_insert_entry_toast,
                        Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, R.string.insert_success_toast, Toast.LENGTH_SHORT).show();
                finish();
            }

        } else {
            int rows = getContentResolver().update(mCurrentStockUri, values, null, null);

            if (rows == 0) {
                Toast.makeText(this, R.string.update_error_toast, Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, R.string.update_success_toast, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * Converts the price value from a String to float
     *
     * @param price is the price as a String
     * @return a float value
     */
    private float getPriceFloat(String price) {
        DecimalFormat df = new DecimalFormat("#.00");
        float rawPriceFloat = Float.parseFloat(price);
        String priceFormatted = df.format(rawPriceFloat);
        return Float.parseFloat(priceFormatted);
    }

    private void changeQuantity(View view) {
        int id = view.getId();
        String quantityString = mQuantityEt.getText().toString().trim();

        if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, R.string.quanity_field_empty_toast,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = Integer.parseInt(quantityString);

        switch (id) {

            case R.id.increase_quantity_button:
                quantity++;
                mQuantityEt.setText(String.valueOf(quantity));
                break;

            case R.id.decrease_quantity_button:
                if (quantity > 0) {
                    quantity--;
                    mQuantityEt.setText(String.valueOf(quantity));

                } else {
                    Toast.makeText(this, R.string.no_negative_quantity_toast,
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * Checks to see if the email address is valid, then creates and starts the implicit Intent to
     * send an email
     */
    private void emailSupplier() {
        String supplier = mSupplierEt.getText().toString().trim();
        String name = mNameEt.getText().toString().trim();

        if (TextUtils.isEmpty(supplier) ||
                !android.util.Patterns.EMAIL_ADDRESS.matcher(supplier).matches()) {
            Toast.makeText(this, R.string.invalid_email_address_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, R.string.no_name_toast, Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{supplier});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_intent_subject) + name);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * Shows a dialog to confirm with the user whether they want to delete the stock entry
     */
    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_dialog_title);
        builder.setIcon(R.drawable.ic_delete_forever_black_48dp);
        builder.setMessage(R.string.delete_dialog_message);

        builder.setNegativeButton(R.string.delete_dialog_delete,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int rows = getContentResolver().delete(mCurrentStockUri, null, null);

                if (rows == 1) {
                    Toast.makeText(getApplicationContext(), R.string.delete_success_toast,
                            Toast.LENGTH_SHORT).show();
                    getLoaderManager().destroyLoader(STOCK_ITEM_LOADER_ID);
                    finish();

                } else {
                    Toast.makeText(getApplicationContext(), R.string.delete_error_toast,
                            Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                }
            }
        });

        builder.setPositiveButton(R.string.delete_dialog_keep,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Handles the response from the gallery or camera
     *
     * @param requestCode is the request code used to start the activity
     * @param resultCode indicates whether the result was a success
     * @param data is the data returned from the activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                case PICK_FROM_GALLERY:
                    mImageUri = data.getData();
                    mImageIv.setImageURI(mImageUri);
                    mHasInfoChanged = true;
                    break;

                case PICK_FROM_CAMERA:
                    mImageIv.setImageURI(mImageUri);
                    mHasInfoChanged = true;
                    break;
            }
        }
    }

    /**
     * Handles the result of the permission requests
     *
     * @param requestCode is the request code inputted when triggering the permission request
     * @param permissions is an Array containing the permissions requested
     * @param grantResults is the result of the request
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {

                case CAMERA_REQUEST:
                    File dir = new File(getFilesDir() + "/images");

                    if (!dir.exists()) {
                        dir.mkdir();
                    }

                    String fileName = System.currentTimeMillis() + ".jpeg";
                    File file = new File(dir, fileName);
                    mImageUri = FileProvider.getUriForFile(getApplicationContext(),
                            "com.asawhite.stockroom.fileprovider", file);
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                    startActivityForResult(cameraIntent, PICK_FROM_CAMERA);
                    break;

                case MANAGE_DOCS_REQUEST:
                    Intent galleryIntent = new Intent();
                    galleryIntent.setType("image/*");
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(galleryIntent,
                            getString(R.string.gallery_intent_title)), PICK_FROM_GALLERY);
                    break;
            }

        } else {
            Toast.makeText(this, R.string.perms_required_toast,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_save:
                saveStockRecord();
                return true;
            case R.id.action_delete:
                showDeleteDialog();
                return true;
            case android.R.id.home:
                if (!mHasInfoChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                showDiscardChangesDialog();
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mCurrentStockUri == null) {
            MenuItem delete = menu.findItem(R.id.action_delete);
            delete.setVisible(false);
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        if (!mHasInfoChanged) {
            super.onBackPressed();
            return;
        }
        showDiscardChangesDialog();
    }

    /**
     * Shows dialog to inform user that there are unsaved changes and handles their response
     */
    private void showDiscardChangesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.discard_dialog_title);
        builder.setIcon(R.drawable.ic_save_black_48dp);
        builder.setMessage(R.string.discard_dialog_message);

        builder.setPositiveButton(R.string.discard_dialog_keep,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.setNegativeButton(R.string.discard_dialog_save,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                saveStockRecord();
            }
        });

        builder.setNeutralButton(R.string.discard_dialog_discard,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] columns = new String[]{
                StockEntry.COLUMN_TITLE_ID,
                StockEntry.COLUMN_TITLE_NAME,
                StockEntry.COLUMN_TITLE_PRICE,
                StockEntry.COLUMN_TITLE_QUANTITY,
                StockEntry.COLUMN_TITLE_SUPPLIER_EMAIL,
                StockEntry.COLUMN_TITLE_IMAGE};

        return new CursorLoader(this, mCurrentStockUri, columns, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            String name = cursor.getString(cursor.getColumnIndex(StockEntry.COLUMN_TITLE_NAME));
            float price = cursor.getFloat(cursor.getColumnIndex(StockEntry.COLUMN_TITLE_PRICE));
            int quantity = cursor.getInt(cursor.getColumnIndex(StockEntry.COLUMN_TITLE_QUANTITY));
            String supplier = cursor.getString(
                    cursor.getColumnIndex(StockEntry.COLUMN_TITLE_SUPPLIER_EMAIL));
            Uri imageUri = Uri.parse(cursor.getString(
                    cursor.getColumnIndex(StockEntry.COLUMN_TITLE_IMAGE)));

            String priceString = new DecimalFormat("0.00").format(price);

            mNameEt.setText(name);
            mPriceEt.setText(priceString);
            mQuantityEt.setText(String.valueOf(quantity));
            mSupplierEt.setText(supplier);
            mImageUri = imageUri;
            mImageIv.setImageURI(imageUri);

        } else {
            Toast.makeText(this, R.string.loader_failed_toast, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEt.setText(null);
        mPriceEt.setText(null);
        mQuantityEt.setText(null);
        mSupplierEt.setText(null);
        mImageIv.setImageResource(R.drawable.no_image_available);
    }
}
