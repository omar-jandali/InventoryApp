package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
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

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import java.io.File;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = EditorActivity.class.getSimpleName();

    private static final int EXISTING_PRODUCT_LOADER = 0;

    private Uri mCurrentProductUri;

    private EditText mNameEditText;

    private EditText mStockEditText;

    private EditText mPriceEditText;

    private ImageView mPictureEditImage;

    private Uri pictureUri;

    private boolean mProductHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_product));

            invalidateOptionsMenu();
        } else {

            setTitle(getString(R.string.editor_activity_title_edit_product));

            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mStockEditText = (EditText) findViewById(R.id.edit_product_stock);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mPictureEditImage = (ImageView) findViewById(R.id.edit_product_picture);

        mNameEditText.setOnTouchListener(mTouchListener);
        mStockEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mPictureEditImage.setOnTouchListener(mTouchListener);
    }

    private void saveProduct() {

        String nameString = mNameEditText.getText().toString().trim();
        String stockString = mStockEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        String pictureString = "";
        if (pictureUri != null) {
            pictureString = pictureUri.toString();
        }
        Log.v(LOG_TAG, "saveProduct pictureString: " + pictureString);

        // Check if this is supposed to be a new product and all the fields are blank
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(stockString) &&
                TextUtils.isEmpty(priceString) && pictureUri == null) {
            //New product saved without changes. Don't create a database entry
            return;
        }
        //Test for valid data in the Editor, prior to saving or replace by 0 value
        if (TextUtils.isEmpty(nameString)) {
            nameString = getString(R.string.needs_name);
        }
        int zero = 0;
        if (TextUtils.isEmpty(stockString)) {
            stockString = new Integer(0).toString();
        }
        if (TextUtils.isEmpty(priceString)) {
            priceString = new Integer(0).toString();
        }

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_STOCK, stockString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
        values.put(ProductEntry.COLUMN_PRODUCT_PICTURE, pictureString);

        // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not
        if (mCurrentProductUri == null) {

            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {

            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_editor, menu);

        // Set up listener for "Decrease Stock"
        Button decreaseButton = (Button) findViewById(R.id.stock_decrease);

        decreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText mStockTextView = (EditText) findViewById(R.id.edit_product_stock);
                int currentStock = Integer.parseInt(mStockTextView.getText().toString().trim());

                //only decrease, if there is stock
                if (currentStock > 0) {
                    currentStock -= 1;
                    mStockTextView.setText(Integer.toString(currentStock));
                    Toast.makeText(EditorActivity.this, getString(R.string.toast_stock_decrease) + currentStock, Toast.LENGTH_SHORT).show();
                    mProductHasChanged = true;
                } else {
                    Toast.makeText(EditorActivity.this, R.string.toast_no_stock, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set up listener for "Increase Stock"
        Button increaseButton = (Button) findViewById(R.id.stock_increase);

        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText mStockTextView = (EditText) findViewById(R.id.edit_product_stock);
                int currentStock = Integer.parseInt(mStockTextView.getText().toString().trim());
                currentStock += 1;
                mStockTextView.setText(Integer.toString(currentStock));
                Toast.makeText(EditorActivity.this, getString(R.string.toast_increase_stock) + currentStock, Toast.LENGTH_SHORT).show();
                mProductHasChanged = true;
            }
        });

        ImageView selectPicture = (ImageView) findViewById(R.id.edit_product_picture);

        selectPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pictureIntent;

                if (Build.VERSION.SDK_INT < 19) {
                    pictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
                } else {
                    pictureIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    pictureIntent.addCategory(Intent.CATEGORY_OPENABLE);
                }
                pictureIntent.setType("image/*");
                startActivityForResult(Intent.createChooser(pictureIntent, "Select Picture"), 1);
                mProductHasChanged = true;
            }
//            @Override
//            protected void onActivityResult(int requestCode, int resultCode, Intent data)
//            {
//                if(resultCode==RESULT_CANCELED)
//                {
//                    // action cancelled
//                }
//                if(resultCode==RESULT_OK)
//                {
//                    Uri selectedPicture = data.getData();
//                    mPictureEditImage.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedPicture));
//                }
//            }
        });

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            // action cancelled
        }
        if (resultCode == RESULT_OK) {
            Uri selectedPicture = data.getData();
            pictureUri = selectedPicture;
            Log.v(LOG_TAG, "file path from OnActivityResult pictureUri: " + pictureUri);

            mPictureEditImage.setImageURI(pictureUri);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" and "Order" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
            menuItem = menu.findItem(R.id.action_order);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                saveProduct();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Order" menu option
            case R.id.action_order:
                showOrderConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_STOCK,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_PICTURE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int stockColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_STOCK);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int pictureColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PICTURE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int stock = cursor.getInt(stockColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            String picture = cursor.getString(pictureColumnIndex);
            // Uri pictureUri = Uri.parse(picture);
            pictureUri = Uri.parse(picture);
            Log.v(LOG_TAG, "onLoadFinished picture: " + picture);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mStockEditText.setText(Integer.toString(stock));
            mPriceEditText.setText(Integer.toString(price));
            mPictureEditImage.setImageURI(pictureUri);
            Log.v(LOG_TAG, "onLoadFinished picture: " + picture);
            Log.v(LOG_TAG, "onLoadFinished pictureURI: " + pictureUri);


            //hide Sell Button, if stock is zero
            Button sellButton = (Button) findViewById(R.id.stock_decrease);
            if (stock == 0) {
                sellButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mStockEditText.setText("");
        mPriceEditText.setText("");
        //TODO fis the picture impression
        File pictureFile = new File(ProductEntry.NO_IMAGE);
        if (pictureFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
            mPictureEditImage.setImageBitmap(myBitmap);
        }
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {

            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    private void showOrderConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.order_dialog_msg);
        builder.setPositiveButton(R.string.order, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Order" button, so delete the product.
                orderProduct();
                return;
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void orderProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // get the Product information from the editor_activity
            String nameString = mNameEditText.getText().toString().trim();
            Log.v(LOG_TAG, "Product to order: " + nameString);

            //prepare the message text
            String orderMessage = getString(R.string.order_greetings);
            orderMessage += "\n\n" + getString(R.string.order_body);
            orderMessage += "\n\n" + nameString;
            orderMessage += "\n\n" + getString(R.string.order_address) + "\n";

            //set up the e-mail Intent with subject line and text
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.order_subject) + nameString);
            intent.putExtra(Intent.EXTRA_TEXT, orderMessage);
            if (intent.resolveActivity(getPackageManager()) != null) {
                //send off the email
                startActivity(intent);
            }
        }
        // Close the activity
        finish();
    }
}
