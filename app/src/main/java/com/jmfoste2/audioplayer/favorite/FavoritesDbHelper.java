package com.jmfoste2.audioplayer.favorite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.jmfoste2.audioplayer.model.SearchItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class for interacting with the favorites table in
 * the database. Exposes operations for insertion/removal/retrieval
 * of favorites in the database. Also provides handle to
 * readable/writeable database.
 */
public class FavoritesDbHelper extends SQLiteOpenHelper {

    private static final String TAG = "FavoritesDbHelper";

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "Favorites.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FavoritesContract.Favorites.TABLE_NAME + " (" +
                    FavoritesContract.Favorites._ID + " INTEGER PRIMARY KEY," +
                    FavoritesContract.Favorites.COLUMN_NAME_VIDEO_ID + TEXT_TYPE + COMMA_SEP +
                    FavoritesContract.Favorites.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    FavoritesContract.Favorites.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    FavoritesContract.Favorites.COLUMN_NAME_DEFAULT_THUMBNAIL_URL + TEXT_TYPE + COMMA_SEP +
                    FavoritesContract.Favorites.COLUMN_NAME_HIGH_RES_THUMBNAIL_URL + TEXT_TYPE + COMMA_SEP +
                    FavoritesContract.Favorites.COLUMN_NAME_DURATION + TEXT_TYPE + " )";

    // may be useful in the future
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FavoritesContract.Favorites.TABLE_NAME;

    /**
     * Creats a favorites db helper with the specified context.
     *
     * @param context The context for the favorites db helper to use.
     */
    public FavoritesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "OnCreate:" + SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    // if db schema is later changed, implement this
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    // if db schema is later changed, implement this
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    /**
     * Inserts the specified search item as a favorite in the database.
     *
     * @param favorite The search item to add as a favorite.
     * @return The id of the favorite in the database, or -1 if an error occurred.
     */
    public long addFavorite(SearchItem favorite) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FavoritesContract.Favorites.COLUMN_NAME_VIDEO_ID, favorite.getVideoId());
        values.put(FavoritesContract.Favorites.COLUMN_NAME_TITLE, favorite.getTitle());
        values.put(FavoritesContract.Favorites.COLUMN_NAME_DESCRIPTION, favorite.getDescription());
        values.put(FavoritesContract.Favorites.COLUMN_NAME_DEFAULT_THUMBNAIL_URL, favorite.getDefaultThumbnailURL());
        values.put(FavoritesContract.Favorites.COLUMN_NAME_HIGH_RES_THUMBNAIL_URL, favorite.getHighResThumbnailURL());
        values.put(FavoritesContract.Favorites.COLUMN_NAME_DURATION, favorite.getDuration());

        long newRowId = db.insert(FavoritesContract.Favorites.TABLE_NAME, null, values);

        db.close();
        return newRowId;
    }

    /**
     * Removes the specified search item as a favorite from the database.
     *
     * @param favorite The favorite search item to remove from the database.
     * @return The number of rows affected by the deletion (0, if not deleted).
     */
    public int removeFavorite(SearchItem favorite) {
        SQLiteDatabase db = getWritableDatabase();

        String selection = FavoritesContract.Favorites.COLUMN_NAME_VIDEO_ID + " LIKE ?";
        String[] selectionArgs = { favorite.getVideoId() };
        int numRowsRemoved = db.delete(FavoritesContract.Favorites.TABLE_NAME, selection, selectionArgs);

        db.close();
        return numRowsRemoved;
    }

    /**
     * Indicates whether the specified search item is a favorite.
     *
     * @param searchItem The search item to check if it is a favorite.
     * @return Boolean indicating if the specified search item is a favorite.
     */
    public boolean isFavorite(SearchItem searchItem) {
        SQLiteDatabase db = getWritableDatabase();

        String selection = FavoritesContract.Favorites.COLUMN_NAME_VIDEO_ID + " LIKE ?";
        String[] selectionArgs = { searchItem.getVideoId() };
        long numMatchingEntries = DatabaseUtils.queryNumEntries(db, FavoritesContract.Favorites.TABLE_NAME, selection, selectionArgs);

        db.close();
        return numMatchingEntries > 0;
    }

    /**
     * Converts the cursor of favorites into a list of search items.
     *
     * @param cursor The cursor of favorites to convert to list of search items.
     * @return List of favorite search items stored in the cursor.
     */
    private List<SearchItem> cursorToList(Cursor cursor) {
        // get column indices for our data
        int videoIdColumnIndex = cursor.getColumnIndex(FavoritesContract.Favorites.COLUMN_NAME_VIDEO_ID);
        int titleColumnIndex = cursor.getColumnIndex(FavoritesContract.Favorites.COLUMN_NAME_TITLE);
        int descriptionColumnIndex = cursor.getColumnIndex(FavoritesContract.Favorites.COLUMN_NAME_DESCRIPTION);
        int defaultThumbnailURLColumnIndex = cursor.getColumnIndex(FavoritesContract.Favorites.COLUMN_NAME_DEFAULT_THUMBNAIL_URL);
        int highResThumbnailURLColumnIndex = cursor.getColumnIndex(FavoritesContract.Favorites.COLUMN_NAME_HIGH_RES_THUMBNAIL_URL);
        int durationColumnIndex = cursor.getColumnIndex(FavoritesContract.Favorites.COLUMN_NAME_DURATION);

        // add each search item to our list
        List<SearchItem> favorites = new ArrayList<>();
        while (cursor.moveToNext()) {
            String videoId = cursor.getString(videoIdColumnIndex);
            String title = cursor.getString(titleColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            String defaultThumbnailURL = cursor.getString(defaultThumbnailURLColumnIndex);
            String highResThumbnailURL = cursor.getString(highResThumbnailURLColumnIndex);
            String duration = cursor.getString(durationColumnIndex);
            SearchItem searchItem = new SearchItem(videoId, title, description, defaultThumbnailURL, highResThumbnailURL, duration);

            favorites.add(searchItem);
        }

        return favorites;
    }

    /**
     * Retrieves all of the favorites as a list.
     *
     * @return List of favorite search items.
     */
    public List<SearchItem> getAllFavorites() {
        SQLiteDatabase db = getReadableDatabase();

        // columns to retrieve
        String[] projection = {
                FavoritesContract.Favorites.COLUMN_NAME_VIDEO_ID,
                FavoritesContract.Favorites.COLUMN_NAME_TITLE,
                FavoritesContract.Favorites.COLUMN_NAME_DESCRIPTION,
                FavoritesContract.Favorites.COLUMN_NAME_DEFAULT_THUMBNAIL_URL,
                FavoritesContract.Favorites.COLUMN_NAME_HIGH_RES_THUMBNAIL_URL,
                FavoritesContract.Favorites.COLUMN_NAME_DURATION
        };

        // order alphabetically by title (A-Z)
        String sortOrder = FavoritesContract.Favorites.COLUMN_NAME_TITLE + " ASC";

        Cursor cursor = db.query(FavoritesContract.Favorites.TABLE_NAME, projection, null, null, null, null, sortOrder);
        List<SearchItem> favorites = cursorToList(cursor);

        db.close();

        return favorites;
    }
}
