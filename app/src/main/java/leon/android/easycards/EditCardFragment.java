package leon.android.easycards;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.edwardvanraak.materialbarcodescanner.MaterialBarcodeScanner;
import com.edwardvanraak.materialbarcodescanner.MaterialBarcodeScannerBuilder;
import com.google.android.gms.vision.barcode.Barcode;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.IOException;
import java.util.List;

import leon.android.easycards.database.DatabaseHelper;
import leon.android.easycards.model.Card;
import leon.android.easycards.utils.ImagePickerActivity;
import leon.android.easycards.utils.OnPhotoReceivedListener;
import leon.android.easycards.utils.UniversalImageLoader;

import static leon.android.easycards.AddCardFragment.REQUEST_IMAGE;

public class EditCardFragment extends Fragment implements OnPhotoReceivedListener {
    private static final String TAG = "EditCardFragment";

    //This will evade the nullpointer exception whena adding to a new bundle from MainActivity
    public EditCardFragment() {
        super();
        setArguments(new Bundle());
    }

    private Card mCard;
    private EditText mCardName;
    private EditText mCardNumber;
    private ImageView mCardImageView;
    private ImageView mCardImageBarCode;
    private Toolbar toolbar;
    private String mSelectedImagePath;
    private Barcode barcodeResult;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_card, container, false);
        mCardName = (EditText) rootView.findViewById(R.id.etCardName);
        mCardNumber = (EditText) rootView.findViewById(R.id.etCardNumber);
        mCardImageView = (ImageView) rootView.findViewById(R.id.cardImage);
        mCardImageBarCode = (ImageView) rootView.findViewById(R.id.imageViewCardNumber);
        toolbar = rootView.findViewById(R.id.editCardToolbar);


        mSelectedImagePath = null;

        //set the heading the for the toolbar
        TextView heading = (TextView) rootView.findViewById(R.id.textCardToolbar);
        heading.setText(getString(R.string.edit_card));

        //required for setting up the toolbar
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        //get the contact from the bundle
        mCard = getCardFromBundle();

        if (mCard != null) {
            init();
        }

        //navigation for the backarrow
        ImageView imageViewBackArrow = (ImageView) rootView.findViewById(R.id.imageViewBackArrow);
        imageViewBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked back arrow.");
                //remove previous fragment from the backstack (therefore navigating back)
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // save changes to the contact
        ImageView imageViewCheckmark = (ImageView) rootView.findViewById(R.id.imageViewCheckmark);
        imageViewCheckmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: saving the edited contact.");
                //execute the save method for the database

                if (checkStringIfNull(mCardName.getText().toString())) {
                    Log.d(TAG, "onClick: saving changes to the cards: " + mCardName.getText().toString());

                    //get the database helper and save the contact
                    DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
                    Cursor cursor = databaseHelper.getCardID(mCard);

                    int cardID = -1;
                    while (cursor.moveToNext()) {
                        cardID = cursor.getInt(0);
                    }
                    if (cardID > -1) {
                        if (mSelectedImagePath != null) {
                            mCard.setImageCard(mSelectedImagePath);
                        }
                        mCard.setNameOfCard(mCardName.getText().toString());
                        mCard.setNumberOfCard(mCardNumber.getText().toString());

                        databaseHelper.updateCard(mCard, cardID);
                        Toast.makeText(getActivity(), "Contact Updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Database Error", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // initiate the dialog box for choosing an image
        ImageView imageViewCamera = (ImageView) rootView.findViewById(R.id.imageViewCamera);
        imageViewCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dexter.withActivity(getActivity())
                        .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                if (report.areAllPermissionsGranted()) {
                                    showImagePickerOptions();
                                    Toast.makeText(getActivity(), "1", Toast.LENGTH_SHORT).show();
                                }

                                if (report.isAnyPermissionPermanentlyDenied()) {
                                    showSettingsDialog();
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
            }
        });


        mCardImageBarCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScan();
            }
        });

        // Clearing older images from cache directory
        // don't call this line if you want to choose multiple images in the same activity
        // call this once the bitmap(s) usage is over
        ImagePickerActivity.clearCache(getActivity());

        return rootView;
    }

    private void startScan() {
        /**
         * Build a new MaterialBarcodeScanner
         */
        final MaterialBarcodeScanner materialBarcodeScanner = new MaterialBarcodeScannerBuilder()
                .withActivity(getActivity())
                .withEnableAutoFocus(true)
                .withBleepEnabled(true)
                .withBackfacingCamera()
                .withCenterTracker()
                .withText("Scanning...")
                .withResultListener(new MaterialBarcodeScanner.OnResultListener() {
                    @Override
                    public void onResult(Barcode barcode) {
                        barcodeResult = barcode;
                        mCardNumber.setText(barcode.rawValue);
                    }
                })
                .build();
        materialBarcodeScanner.startScan();
    }

    private boolean checkStringIfNull(String string) {
        if (string.equals("")) {
            return false;
        } else {
            return true;
        }
    }

    private void init() {
        mCardName.setText(mCard.getNameOfCard());
        mCardNumber.setText(mCard.getNumberOfCard());
        UniversalImageLoader.setImage(mCard.getImageCard(), mCardImageView, null, "");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.card_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_item_delete:
                Log.d(TAG, "onOptionsItemSelected: deleting card.");
                DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
                Cursor cursor = databaseHelper.getCardID(mCard);

                int cardID = -1;
                while (cursor.moveToNext()) {
                    cardID = cursor.getInt(0);
                }
                if (cardID > -1) {
                    if (databaseHelper.deleteCard(cardID) > 0) {
                        Toast.makeText(getActivity(), "Card Deleted", Toast.LENGTH_SHORT).show();

                        //clear the arguments ont he current bundle since the contact is deleted
                        this.getArguments().clear();

                        //remove previous fragemnt from the backstack (therefore navigating back)
                        getActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        Toast.makeText(getActivity(), "Database Error", Toast.LENGTH_SHORT).show();
                    }
                }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Retrieves the selected contact from the bundle (coming from MainActivity)
     *
     * @return
     */
    private Card getCardFromBundle() {
        Log.d(TAG, "getContactFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.card));
        } else {
            return null;
        }
    }

    /**
     * Retrieves the selected image from the bundle (coming from ChangePhotoDialog)
     *
     * @param bitmap
     */
    @Override
    public void getBitmapImage(Bitmap bitmap) {
        Log.d(TAG, "getBitmapImage: got the bitmap: " + bitmap);
        //get the bitmap from 'ChangePhotoDialog'
        if (bitmap != null) {
            //compress the image (if you like)
            ((MainActivity) getActivity()).compressBitmap(bitmap, 70);
            mCardImageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void getImagePath(String imagePath) {
        Log.d(TAG, "getImagePath: got the image path: " + imagePath);

        if (!imagePath.equals("")) {
            imagePath = imagePath.replace(":/", "://");
            mSelectedImagePath = imagePath;
            UniversalImageLoader.setImage(imagePath, mCardImageView, null, "");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getParcelableExtra("path");
                try {
                    // You can update this bitmap to your server
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);

                    // loading profile image from local cache
                    getBitmapImage(bitmap);

                    File file = new File(uri.toString());
                    getImagePath(file.getPath());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showImagePickerOptions() {
        ImagePickerActivity.showImagePickerOptions(getActivity(), new ImagePickerActivity.PickerOptionListener() {
            @Override
            public void onTakeCameraSelected() {
                launchCameraIntent();
            }

            @Override
            public void onChooseGallerySelected() {
                launchGalleryIntent();
            }
        });
    }

    private void launchCameraIntent() {
        Intent intent = new Intent(getActivity(), ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000);

        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void launchGalleryIntent() {
        Intent intent = new Intent(getActivity(), ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_permission_title));
        builder.setMessage(getString(R.string.dialog_permission_message));
        builder.setPositiveButton(getString(R.string.go_to_settings), (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();

    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

}
