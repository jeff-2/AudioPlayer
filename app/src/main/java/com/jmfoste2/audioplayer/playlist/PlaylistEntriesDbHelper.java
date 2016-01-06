package com.jmfoste2.audioplayer.playlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.jmfoste2.audioplayer.model.Playlist;
import com.jmfoste2.audioplayer.model.SearchItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class for interacting with the playlist_entries table in
 * the database. Exposes operations for insertion/removal/retrieval
 * of playlist entries in the database. Also provides handle to
 * readable/writeable database.
 */
public class PlaylistEntriesDbHelper extends SQLiteOpenHelper {

    private static final String TAG = "PlaylistEntriesDbHelper";

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "PlaylistEntries.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PlaylistEntriesContract.PlaylistEntry.TABLE_NAME + " (" +
                    PlaylistEntriesContract.PlaylistEntry._ID + " INTEGER PRIMARY KEY," +
                    PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_VIDEO_ID + TEXT_TYPE + COMMA_SEP +
                    PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_DEFAULT_THUMBNAIL_URL + TEXT_TYPE + COMMA_SEP +
                    PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_HIGH_RES_THUMBNAIL_URL + TEXT_TYPE + COMMA_SEP +
                    PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_DURATION + TEXT_TYPE + COMMA_SEP +
                    PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_PLAYLIST_TITLE + TEXT_TYPE + " )";

    // may be useful in the future
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PlaylistEntriesContract.PlaylistEntry.TABLE_NAME;

    /**
     * Creates a playlist_entries db helper with the specified context.
     *
     * @param context The context for the playlist_entries db helper to use.
     */
    public PlaylistEntriesDbHelper(Context context) {
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
     * Adds the specified entry to the specified playlist.
     *
     * @param playlist The playlist to add the entry to.
     * @param entry The entry to add to the playlist.
     * @return The id of the playlist entry in the database, or -1 if an error occurred.
     */
    public long addPlaylistEntry(Playlist playlist, SearchItem entry) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_VIDEO_ID, entry.getVideoId());
        values.put(PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_TITLE, entry.getTitle());
        values.put(PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_DESCRIPTION, entry.getDescription());
        values.put(PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_DEFAULT_THUMBNAIL_URL, entry.getDefaultThumbnailURL());
        values.put(PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_HIGH_RES_THUMBNAIL_URL, entry.getHighResThumbnailURL());
        values.put(PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_DURATION, entry.getDuration());
        values.put(PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_PLAYLIST_TITLE, playlist.getTitle());

        long newRowId = db.insert(PlaylistEntriesContract.PlaylistEntry.TABLE_NAME, null, values);

        db.close();
        return newRowId;
    }

    /**
     * Removes the specified entry from the specified playlist.
     *
     * @param playlist The playlist to remove the entry from.
     * @param entry The entry to remove from the playlist.
     * @return The number of rows affected by the deletion (0, if not deleted).
     */
    public int removePlaylistEntry(Playlist playlist, SearchItem entry) {
        SQLiteDatabase db = getWritableDatabase();

        String selection = PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_PLAYLIST_TITLE + " LIKE ? AND " +
                PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_VIDEO_ID + " LIKE ?";
        String[] selectionArgs = { playlist.getTitle(), entry.getVideoId() };
        int numRowsRemoved = db.delete(PlaylistEntriesContract.PlaylistEntry.TABLE_NAME, selection, selectionArgs);

        db.close();
        return numRowsRemoved;
    }

    /**
     * Removes the specified entries from the specified playlist.
     *
     * @param playlist The playlist to remove all the entries from.
     * @return The umber of rows affected by the deletion (0, if not deleted).
     */
    public int removePlaylistEntries(Playlist playlist) {
        SQLiteDatabase db = getWritableDatabase();

        String selection = PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_PLAYLIST_TITLE + " LIKE ?";
        String[] selectionArgs = { playlist.getTitle() };
        int numRowsRemoved = db.delete(PlaylistEntriesContract.PlaylistEntry.TABLE_NAME, selection, selectionArgs);

        db.close();
        return numRowsRemoved;
    }

    /**
     * Builds list of search items from the given cursor
     *
     * @param cursor The cursor to build the list of search items from.
     * @return List of search items within the cursor.
     */
    private List<SearchItem> cursorToList(Cursor cursor) {
        // get column indices for our data
        int videoIdColumnIndex = cursor.getColumnIndex(PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_VIDEO_ID);
        int titleColumnIndex = cursor.getColumnIndex(PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_TITLE);
        int descriptionColumnIndex = cursor.getColumnIndex(PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_DESCRIPTION);
        int defaultThumbnailURLColumnIndex = cursor.getColumnIndex(PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_DEFAULT_THUMBNAIL_URL);
        int highResThumbnailURLColumnIndex = cursor.getColumnIndex(PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_HIGH_RES_THUMBNAIL_URL);
        int durationColumnIndex = cursor.getColumnIndex(PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_DURATION);

        // add each search item to our list
        List<SearchItem> playlistEntries = new ArrayList<>();
        while (cursor.moveToNext()) {
            String videoId = cursor.getString(videoIdColumnIndex);
            String title = cursor.getString(titleColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            String defaultThumbnailURL = cursor.getString(defaultThumbnailURLColumnIndex);
            String highResThumbnailURL = cursor.getString(highResThumbnailURLColumnIndex);
            String duration = cursor.getString(durationColumnIndex);
            SearchItem searchItem = new SearchItem(videoId, title, description, defaultThumbnailURL, highResThumbnailURL, duration);

            playlistEntries.add(searchItem);
        }

        return playlistEntries;
    }

    /**
     * Retrieves all the entries for the playlist with the specified title.
     *
     * @param playlistTitle The title of the playlist to retrieve the entries for.
     * @return List of search items that are in the playlist with the specified title.
     */
    public List<SearchItem> getAllPlaylistEntries(String playlistTitle) {
        SQLiteDatabase db = getReadableDatabase();

        // columns to retrieve
        String[] projection = {
                PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_VIDEO_ID,
                PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_TITLE,
                PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_DESCRIPTION,
                PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_DEFAULT_THUMBNAIL_URL,
                PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_HIGH_RES_THUMBNAIL_URL,
                PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_DURATION
        };

        // order alphabetically by title (A-Z)
        String sortOrder = PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_TITLE + " ASC";

        String selection = PlaylistEntriesContract.PlaylistEntry.COLUMN_NAME_PLAYLIST_TITLE + " LIKE ?";
        String[] selectionArgs = { playlistTitle };

        Cursor cursor = db.query(PlaylistEntriesContract.PlaylistEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        List<SearchItem> playlistEntries = cursorToList(cursor);

        db.close();

        return playlistEntries;
    }
}
