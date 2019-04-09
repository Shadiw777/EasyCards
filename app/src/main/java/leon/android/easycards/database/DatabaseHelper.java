package leon.android.easycards.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import leon.android.easycards.model.Card;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "card.db";
    public static final String TABLE_NAME = "card_table";
    public static final String COL0 = "ID";
    public static final String COL1 = "NAME";
    public static final String COL2 = "CARD_IMAGE";
    public static final String COL3 = "CARD_NUMBER";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 3);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " +
                TABLE_NAME + " ( " +
                COL0 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL1 + " TEXT, " +
                COL2 + " TEXT, " +
                COL3 + " TEXT )";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Insert a new card into the database
     *
     * @param card
     * @return
     */
    public boolean addCard(Card card) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, card.getNameOfCard());
        contentValues.put(COL2, card.getImageCard());
        contentValues.put(COL3, card.getNumberOfCard());

        long result = db.insert(TABLE_NAME, null, contentValues);

        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Retrieve all cards from database
     *
     * @return
     */
    public Cursor getAllCards() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    /**
     * Update a card where id = @param 'id'
     * Replace the current contact with @param 'contact'
     *
     * @param card
     * @param id
     * @return
     */
    public boolean updateCard(Card card, int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, card.getNameOfCard());
        contentValues.put(COL2, card.getImageCard());
        contentValues.put(COL3, card.getNumberOfCard());

        int update = db.update(TABLE_NAME,
                contentValues,
                COL0 + " = ? ",
                new String[]{String.valueOf(id)});

        if (update != 1) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Retrieve the card unique id from the database using @param
     *
     * @param card
     * @return
     */
    public Cursor getCardID(Card card) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + COL1 + " = '" + card.getNameOfCard() + "'" +
                " AND " + COL3 + " = '" + card.getNumberOfCard() + "'";

        return db.rawQuery(sql, null);
    }

    public Integer deleteCard(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "ID = ?", new String[]{String.valueOf(id)});
    }
}
