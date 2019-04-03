package leon.android.easycards.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Card implements Parcelable {

    private String nameOfCard;
    private String imageCard;

    public Card(String nameOfCard, String imageCard) {
        this.nameOfCard = nameOfCard;
        this.imageCard = imageCard;
    }

    protected Card(Parcel in){
        nameOfCard = in.readString();
        imageCard = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(nameOfCard);
        parcel.writeString(imageCard);
    }

    public static final Creator<Card> CREATOR = new Creator<Card>() {
        @Override
        public Card createFromParcel(Parcel parcel) {
            return new Card(parcel);
        }

        @Override
        public Card[] newArray(int i) {
            return new Card[i];
        }
    };

    public String getNameOfCard() {
        return nameOfCard;
    }

    public void setNameOfCard(String nameOfCard) {
        this.nameOfCard = nameOfCard;
    }

    public String getImageCard() {
        return imageCard;
    }

    public void setImageCard(String imageCard) {
        this.imageCard = imageCard;
    }
}
