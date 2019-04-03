package leon.android.easycards;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import leon.android.easycards.database.DatabaseHelper;
import leon.android.easycards.model.Card;
import leon.android.easycards.utils.ChangePhotoDialog;
import leon.android.easycards.utils.Init;
import leon.android.easycards.utils.UniversalImageLoader;

public class AddCardFragment extends Fragment implements ChangePhotoDialog.onPhotoReceivedListener {

    private static final String TAG = "AddContactFragment";

    private EditText mCardName;
    private ImageView mCardImageView;
    private Toolbar toolbar;
    private String mSelectedImagePath;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_card, container, false);
        mCardName = (EditText) rootView.findViewById(R.id.etCardName);
        mCardImageView = (ImageView) rootView.findViewById(R.id.cardImage);
        toolbar = (Toolbar) rootView.findViewById(R.id.editCardToolbar);

        mSelectedImagePath = null;

        //load the default images by causing an error
        UniversalImageLoader.setImage(null, mCardImageView, null, "");

        //set the heading the for the toolbar
        TextView heading = (TextView) rootView.findViewById(R.id.textCardToolbar);
        heading.setText(getString(R.string.add_card));

        //required for setting up the toolbar
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

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
                            dialog.setTargetFragment(AddCardFragment.this, 0);
                        }
                    } else {
                        ((MainActivity) getActivity()).verifyPermissions(permission);
                    }
                }
            }
        });

        //set onclicklistenre to the 'checkmar' icon for saving a contact
        ImageView confirmNewContact = (ImageView) rootView.findViewById(R.id.imageViewCheckmark);
        confirmNewContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to save new contact.");
                if (checkStringIfNull(mCardName.getText().toString())) {
                    Log.d(TAG, "onClick: saving new contact. " + mCardName.getText().toString());

                    DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
                    Card card = new Card(mCardName.getText().toString(),
                            mSelectedImagePath);
                    if (databaseHelper.addCard(card)) {
                        Toast.makeText(getActivity(), "Card Saved", Toast.LENGTH_SHORT).show();
                        getActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        Toast.makeText(getActivity(), "Error Saving", Toast.LENGTH_SHORT).show();
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

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.card_menu, menu);
//        super.onCreateOptionsMenu(menu, inflater);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        switch (item.getItemId()) {
//            case R.id.menu_item_delete:
//                Log.d(TAG, "onOptionsItemSelected: deleting card.");
//        }
//        return super.onOptionsItemSelected(item);
//    }

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
}
