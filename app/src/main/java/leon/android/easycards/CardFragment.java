package leon.android.easycards;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import leon.android.easycards.database.DatabaseHelper;
import leon.android.easycards.model.Card;
import leon.android.easycards.utils.UniversalImageLoader;

public class CardFragment extends Fragment {
    private static final String TAG = "CardFragment";

    public interface onEditCardListener {
        public void onEditCardSelected(Card card);
    }

    onEditCardListener mOnEditCardListener;

    //This will evade the nullpointer exception whena adding to a new bundle from MainActivity
    public CardFragment() {
        super();
        setArguments(new Bundle());
    }

    private android.support.v7.widget.Toolbar toolbar;
    private Card mCard;
    private TextView mCardName;
    private TextView mCardNumber;
    private ImageView mCardImageView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_card, container, false);
        toolbar = rootView.findViewById(R.id.cardToolbar);
        mCardName = (TextView) rootView.findViewById(R.id.cardName);
        mCardNumber = rootView.findViewById(R.id.cardNumber);
        mCardImageView = (ImageView) rootView.findViewById(R.id.cardImage);
        mCard = getCardFromBundle();

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        init();

        ImageView imageViewBackArrow = (ImageView) rootView.findViewById(R.id.imageViewBackArrow);
        imageViewBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        ImageView imageViewEdit = (ImageView) rootView.findViewById(R.id.imageViewEdit);
        imageViewEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnEditCardListener.onEditCardSelected(mCard);
            }
        });

        return rootView;
    }

    private void init() {
        mCardName.setText("Name of card: " + mCard.getNameOfCard());
        mCardNumber.setText("Number of card: " + mCard.getNumberOfCard());
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
                        Toast.makeText(getActivity(), "Database error", Toast.LENGTH_SHORT).show();
                    }
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        Cursor cursor = databaseHelper.getCardID(mCard);

        int cardID = -1;
        while (cursor.moveToNext()) {
            cardID = cursor.getInt(0);
        }

        if (cardID > -1) {
            init();
        } else {
            this.getArguments().clear();
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    /**
     * Retrieves the selected contact from the bundle (coming from MainActivity)
     *
     * @return
     */
    private Card getCardFromBundle() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.card));
        } else {
            return null;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mOnEditCardListener = (onEditCardListener) getActivity();
        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
    }
}
