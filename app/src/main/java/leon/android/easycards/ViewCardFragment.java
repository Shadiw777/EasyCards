package leon.android.easycards;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import leon.android.easycards.adapter.CardAdapter;
import leon.android.easycards.database.DatabaseHelper;
import leon.android.easycards.model.Card;

public class ViewCardFragment extends Fragment implements CardAdapter.OnRecyclerListener {
    private static final String TAG = "ViewCardFragment";

    public interface onCardSelectedListener {
        public void onCardSelected(Card card);
    }

    onCardSelectedListener mCardListener;

    public interface onAddCardListener {
        public void onAddCard();
    }

    onAddCardListener mOnAddCard;

    //variables and widgets
    private static final int STANDARD_APPBAR = 0;
    private static final int SEARCH_APPBAR = 1;
    private int mAppBarState;

    private AppBarLayout viewCardsBar, searchBar;
    private RecyclerView mRecyclerView;
    private CardAdapter mAdapter;
    private EditText mSearchCards;
    private List<Card> mCards = new ArrayList<>();
    private Card mCard;

    int[] animationList = {R.anim.layout_animation_up_to_down, R.anim.layout_animation_right_to_left, R.anim.layout_animation_down_to_up, R.anim.layout_animation_left_to_right};
    int i = 0;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_view_cards, container, false);
        viewCardsBar = (AppBarLayout) rootView.findViewById(R.id.viewCardToolbar);
        searchBar = (AppBarLayout) rootView.findViewById(R.id.searchToolbar);
        mRecyclerView = rootView.findViewById(R.id.cardRecyclerView);
        mSearchCards = rootView.findViewById(R.id.etSearchCards);

        setAppBarState(STANDARD_APPBAR);

        initRecyclerView();

        // navigate to add contacts fragment
        FloatingActionButton fab = rootView.findViewById(R.id.fabAddContact);
        fab.setOnClickListener(v -> {
            Log.d(TAG, "onClick: clicked fab.");
            mOnAddCard.onAddCard();
        });

        ImageView imageViewSearchIcon = rootView.findViewById(R.id.imageViewSearchIcon);
        imageViewSearchIcon.setOnClickListener(v -> {
            Log.d(TAG, "onClick: clicked search icon.");
            toggleToolBarState();
        });

        ImageView imageViewBackArrow = rootView.findViewById(R.id.imageViewBackArrow);
        imageViewBackArrow.setOnClickListener(v -> {
            Log.d(TAG, "onClick: clicked back arrow.");
            toggleToolBarState();
        });


        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        databaseHelper.close();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (i < animationList.length - 1) {
            i++;
        } else {
            i = 0;
        }
        runAnimationAgain();
    }

    private void runAnimationAgain() {

        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(getActivity(), animationList[i]);

        mRecyclerView.setLayoutAnimation(controller);
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scheduleLayoutAnimation();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCardListener = (onCardSelectedListener) getActivity();
            mOnAddCard = (onAddCardListener) getActivity();
        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
    }

    private void initRecyclerView() {
        GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 2, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        List<Card> cards = getAllCards();
        mAdapter = new CardAdapter(getActivity(), cards, "", this);
        mRecyclerView.setAdapter(mAdapter);

    }

    private List<Card> getAllCards() {
        mCards = new ArrayList<>();
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());

        SQLiteDatabase mDatabase;
        mDatabase = databaseHelper.getWritableDatabase();
        Cursor c = mDatabase.query(DatabaseHelper.TABLE_NAME, null, null, null, null, null, null);

        if (c.moveToFirst()) {
            int idCol1 = c.getColumnIndex(DatabaseHelper.COL1);
            int idCol2 = c.getColumnIndex(DatabaseHelper.COL2);
            int idCol3 = c.getColumnIndex(DatabaseHelper.COL3);

            do {
                Card card = new Card();
                card.setNameOfCard(c.getString(idCol1));
                card.setImageCard(c.getString(idCol2));
                card.setNumberOfCard(c.getString(idCol3));

                mCards.add(card);
            } while (c.moveToNext());
        }


        c.close();

        //sort the arraylist based on the contact name
        Collections.sort(mCards, (o1, o2) -> o1.getNameOfCard().compareToIgnoreCase(o2.getNameOfCard()));

        mSearchCards.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                String text = mSearchCards.getText().toString().toLowerCase(Locale.getDefault());
                mAdapter.filter(text);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return mCards;
    }

    @Override
    public void onClickRecyclerPosition(int position) {
        Log.d(TAG, "onClick: navigating to " + getString(R.string.card_fragment));
        mCardListener.onCardSelected(mCards.get(position));
    }

    /**
     * Initiates the appbar state toggle
     */
    public void toggleToolBarState() {
        Log.d(TAG, "toggleToolBarState: toggling AppBarState.");
        if (mAppBarState == STANDARD_APPBAR) {
            setAppBarState(SEARCH_APPBAR);
        } else {
            setAppBarState(STANDARD_APPBAR);
        }
    }


    /**
     * Sets the appbar state for either the search 'mode' or 'standard' mode
     *
     * @param state
     */
    private void setAppBarState(int state) {
        Log.d(TAG, "setAppBarState: changing app bar state to: " + state);

        mAppBarState = state;

        if (mAppBarState == STANDARD_APPBAR) {
            searchBar.setVisibility(View.GONE);
            viewCardsBar.setVisibility(View.VISIBLE);

            //hide the keyboard
            View view = getView();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            try {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            } catch (NullPointerException e) {
                Log.d(TAG, "setAppBarState: NullPointerException: " + e.getMessage());
            }

        } else if (mAppBarState == SEARCH_APPBAR) {
            viewCardsBar.setVisibility(View.GONE);
            searchBar.setVisibility(View.VISIBLE);
            mSearchCards.requestFocus();

            //open the keyboard
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        setAppBarState(STANDARD_APPBAR);
    }


}
