package leon.android.easycards.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import leon.android.easycards.R;
import leon.android.easycards.model.Card;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    private List<Card> mCards = null;
    private ArrayList<Card> arrayList; // used for searchBar
    private Context mContext;
    private String mAppend;

    private OnRecyclerListener onRecyclerListener;

    public CardAdapter(Context mContext, List<Card> mCards, String mAppend, OnRecyclerListener onRecyclerListener) {
        this.mCards = mCards;
        this.mContext = mContext;
        this.mAppend = mAppend;
        arrayList = new ArrayList<>();
        this.arrayList.addAll(mCards);
        this.onRecyclerListener = onRecyclerListener;
    }

    @NonNull
    @Override
    public CardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.layout_card_list, parent, false);
        return new ViewHolder(rootView, onRecyclerListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final CardAdapter.ViewHolder holder, int position) {
        Card card = mCards.get(position);
//        holder.mCardName.setText(card.getNameOfCard());

        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(mAppend + card.getImageCard(), holder.mCardImage, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                holder.mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                holder.mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                holder.mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                holder.mProgressBar.setVisibility(View.GONE);
            }
        });


    }

    @Override
    public int getItemCount() {
        return mCards.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mCardName;
        private ImageView mCardImage;
        private ProgressBar mProgressBar;

        OnRecyclerListener onRecyclerListener;

        public ViewHolder(@NonNull View itemView, OnRecyclerListener onRecyclerListener) {
            super(itemView);
            this.onRecyclerListener = onRecyclerListener;

            mCardName = itemView.findViewById(R.id.cardName);
            mCardImage = itemView.findViewById(R.id.cardImage);
            mProgressBar = itemView.findViewById(R.id.cardProgressBar);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onRecyclerListener.onClickRecyclerPosition(getAdapterPosition());
        }
    }

    public interface OnRecyclerListener {

        void onClickRecyclerPosition(int position);

    }

    //Filter class
    public void filter(String characterText) {
        characterText = characterText.toLowerCase(Locale.getDefault());
        mCards.clear();
        if (characterText.length() == 0) {
            mCards.addAll(arrayList);
        } else {
            mCards.clear();
            for (Card card : arrayList) {
                if (card.getNameOfCard().toLowerCase(Locale.getDefault()).contains(characterText)) {
                    mCards.add(card);
                }
            }
        }
        notifyDataSetChanged();
    }
}
