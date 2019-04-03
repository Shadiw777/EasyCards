package leon.android.easycards;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import leon.android.easycards.database.DatabaseHelper;
import leon.android.easycards.model.Card;
import leon.android.easycards.utils.ChangePhotoDialog;
import leon.android.easycards.utils.Init;
import leon.android.easycards.utils.UniversalImageLoader;

public class EditCardFragment extends Fragment implements ChangePhotoDialog.onPhotoReceivedListener {
    private static final String TAG = "EditCardFragment";

    //This will evade the nullpointer exception whena adding to a new bundle from MainActivity
    public EditCardFragment() {
        super();
        setArguments(new Bundle());
    }

    private Card mCard;
    private EditText mNameCard;
    private ImageView mImageCard;
    private Toolbar toolbar;
    private String mSelectedImagePath;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_card, container, false);
        mNameCard = (EditText) rootView.findViewById(R.id.etCardName);
        mImageCard = (ImageView) rootView.findViewById(R.id.cardImage);
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

                if (checkStringIfNull(mNameCard.getText().toString())) {
                    Log.d(TAG, "onClick: saving changes to the contact: " + mNameCard.getText().toString());

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
                        mCard.setNameOfCard(mNameCard.getText().toString());

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

                /*
                Make sure all permissions have been verified before opening the dialog
                 */
                for (int i = 0; i < Init.PERMISSIONS.length; i++) {
                    String[] permission = {Init.PERMISSIONS[i]};
                    if (((MainActivity) getActivity()).checkPermission(permission)) {
                        if (i == Init.PERMISSIONS.length - 1) {
                            Log.d(TAG, "onClick: opening the 'image selection dialog box'.");
                            ChangePhotoDialog dialog = new ChangePhotoDialog();
                            dialog.show(getFragmentManager(), getString(R.string.change_photo_dialog));
                            dialog.setTargetFragment(EditCardFragment.this, 0);
                        }
                    } else {
                        ((MainActivity) getActivity()).verifyPermissions(permission);
                    }
                }


            }
        });

        return rootView;
    }

    private boolean checkStringIfNull(String string) {
        if (string.equals("")) {
            return false;
        } else {
            return true;
        }
    }

    private void init() {
        mNameCard.setText(mCard.getNameOfCard());
        UniversalImageLoader.setImage(mCard.getImageCard(), mImageCard, null, "");
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
                Log.d(TAG, "onOptionsItemSelected: deleting contact.");
                DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
                Cursor cursor = databaseHelper.getCardID(mCard);

                int cardID = -1;
                while (cursor.moveToNext()) {
                    cardID = cursor.getInt(0);
                }
                if (cardID > -1) {
                    if (databaseHelper.deleteCard(cardID) > 0) {
                        Toast.makeText(getActivity(), "Contact Deleted", Toast.LENGTH_SHORT).show();

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
            mImageCard.setImageBitmap(bitmap);
        }
    }

    @Override
    public void getImagePath(String imagePath) {
        Log.d(TAG, "getImagePath: got the image path: " + imagePath);

        if (!imagePath.equals("")) {
            imagePath = imagePath.replace(":/", "://");
            mSelectedImagePath = imagePath;
            UniversalImageLoader.setImage(imagePath, mImageCard, null, "");
        }
    }
}
