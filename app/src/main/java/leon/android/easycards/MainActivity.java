package leon.android.easycards;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.ByteArrayOutputStream;

import leon.android.easycards.model.Card;
import leon.android.easycards.utils.UniversalImageLoader;

public class MainActivity extends AppCompatActivity implements
        ViewCardFragment.onCardSelectedListener,
        CardFragment.onEditCardListener,
        ViewCardFragment.onAddCardListener {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE = 1;


    @Override
    public void onEditCardSelected(Card card) {
        Log.d(TAG, "onEditCardSelected: card selected from "
                + getString(R.string.edit_card_fragment)
                + " " + card.getNameOfCard());

        EditCardFragment fragment = new EditCardFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.card), card);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(getString(R.string.edit_card_fragment));
        transaction.commit();
    }

    @Override
    public void onCardSelected(Card card) {
        Log.d(TAG, "onCardSelected: contact selected from "
                + getString(R.string.view_card_fragment)
                + " " + card.getNameOfCard());

        CardFragment fragment = new CardFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.card), card);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(getString(R.string.card_fragment));
        transaction.commit();
    }

    @Override
    public void onAddCard() {
        Log.d(TAG, "onAddCard: navigating to " + getString(R.string.add_card_fragment));

        AddCardFragment fragment = new AddCardFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(getString(R.string.add_card_fragment));
        transaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initImageLoader();

        init();
    }

    /**
     * initialize the first fragment (ViewContactsFragment)
     */
    private void init() {
        ViewCardFragment fragment = new ViewCardFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // replace what ever is in the fragment_container view with this fragment,
        // amd add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, fragment);
       // transaction.addToBackStack(null);
        transaction.commit();
    }

    private void initImageLoader() {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(MainActivity.this);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    /**
     * Compress a bitmap by the @param "quality"
     * Quality can be anywhere from 1-100 : 100 being the highest quality.
     *
     * @param bitmap
     * @param quality
     * @return
     */
    public Bitmap compressBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return bitmap;
    }

    /**
     * Generalized method for asking permission. Can pass any array of permissions
     *
     * @param permissions
     */
    public void verifyPermissions(String[] permissions) {
        Log.d(TAG, "verifyPermissions: asking user for permissions.");
        ActivityCompat.requestPermissions(
                MainActivity.this,
                permissions,
                REQUEST_CODE
        );
    }

    /**
     * Checks to see if permission was granted for the passed parameters
     * ONLY ONE PERMISSION MAYT BE CHECKED AT A TIME
     *
     * @param permission
     * @return
     */
    public boolean checkPermission(String[] permission) {
        Log.d(TAG, "checkPermission: checking permissions for:" + permission[0]);

        int permissionRequest = ActivityCompat.checkSelfPermission(
                MainActivity.this,
                permission[0]);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkPermission: \n Permissions was not granted for: " + permission[0]);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: requestCode: " + requestCode);

        switch (requestCode) {
            case REQUEST_CODE:
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "onRequestPermissionsResult: User has allowed permission to access: " + permissions[i]);
                    } else {
                        break;
                    }
                }
                break;
        }
    }

}
