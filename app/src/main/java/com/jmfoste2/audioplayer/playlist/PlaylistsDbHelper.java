package com.jmfoste2.audioplayer.playlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.jmfoste2.audioplayer.model.Playlist;
import com.jmfoste2.audioplayer.model.SearchItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class for interacting with the playlists table in
 * the database. Exposes operations for insertion/removal/retrieval
 * of playlists in the database. Also provides handle to
 * readable/writeable database.
 */
public class PlaylistsDbHelper extends SQLiteOpenHelper {

    private static final String TAG = "PlaylistsDbHelper";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Playlists.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PlaylistsContract.Playlists.TABLE_NAME + " (" +
                    PlaylistsContract.Playlists._ID + " INTEGER PRIMARY KEY," +
                    PlaylistsContract.Playlists.COLUMN_NAME_TITLE + TEXT_TYPE + " )";

    // may be useful in the future
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PlaylistsContract.Playlists.TABLE_NAME;

    private final Context context;

    /**
     * Creats a playlists db helper with the specified context.
     *
     * @param context The context for the playlists db helper to use.
     */
    public PlaylistsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "OnCreate:" + SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    // if db schema is later changed, implement this
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    // if db schema is later changed, implement this
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public long addPlaylist(Playlist playlist) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PlaylistsContract.Playlists.COLUMN_NAME_TITLE, playlist.getTitle());

        long newRowId = db.insert(PlaylistsContract.Playlists.TABLE_NAME, null, values);

        PlaylistEntriesDbHelper dbHelper = new PlaylistEntriesDbHelper(context);
        for (SearchItem entry : playlist.getEntries()) {
            dbHelper.addPlaylistEntry(playlist, entry);
        }

        db.close();
        return newRowId;
    }

    /**
     * Removes the specified playlist from the database.
     *
     * @param playlist The playlist to remove.
     * @return Number of rows affected by the deletion (0, if none deleted).
     */
    public int removePlaylist(Playlist playlist) {
        SQLiteDatabase db = getWritableDatabase();

        String selection = PlaylistsContract.Playlists.COLUMN_NAME_TITLE + " LIKE ?";
        String[] selectionArgs = { playlist.getTitle() };
        int numRowsRemoved = db.delete(PlaylistsContract.Playlists.TABLE_NAME, selection, selectionArgs);

        PlaylistEntriesDbHelper dbHelper = new PlaylistEntriesDbHelper(context);
        dbHelper.removePlaylistEntries(playlist);

        db.close();
        return numRowsRemoved;
    }

    /**
     * Removes the specified entry from the specified playlist.
     *
     * @param playlist The playlist to remove an entry from.
     * @param entry The entry to remove from the playlist.
     * @return Number of rows affected by the deletion (0, if none deleted).
     */
    public int removeEntryFromPlaylist(Playlist playlist, SearchItem entry) {
        PlaylistEntriesDbHelper dbHelper = new PlaylistEntriesDbHelper(context);
        return dbHelper.removePlaylistEntry(playlist, entry);
    }

    /**
     * Adds the specified entry to the specified playlist.
     *
     * @param playlist The playlist to add an entry to.
     * @param entry The entry to add to the playlist.
     * @return The row id of the playlist entry, or -1 if an error occurred.
     */
    public long addEntryToPlaylist(Playlist playlist, SearchItem entry) {
        PlaylistEntriesDbHelper dbHelper = new PlaylistEntriesDbHelper(context);
        return dbHelper.addPlaylistEntry(playlist, entry);
    }

    /**
     * Converts the cursor into a list of playlist titles.
     *
     * @param cursor The cursor representing the list of playlists
     * @return List representing the titles of the playlists
     */
    private List<String> cursorToList(Cursor cursor) {
        int titleColumnIndex = cursor.getColumnIndex(PlaylistsContract.Playlists.COLUMN_NAME_TITLE);

        // add each search item to our list
        List<String> playlistTitles = new ArrayList<>();
        while (cursor.moveToNext()) {
            String title = cursor.getString(titleColumnIndex);

            playlistTitles.add(title);
        }

        return playlistTitles;
    }

    /**
     * Retrieves all of the playlists.
     *
     * @return List of all the playlists.
     */
    public List<Playlist> getAllPlaylists() {
        SQLiteDatabase db = getReadableDatabase();

        // columns to retrieve
        String[] projection = {
                PlaylistsContract.Playlists.COLUMN_NAME_TITLE
        };

        // order alphabetically by title (A-Z)
        String sortOrder = PlaylistsContract.Playlists.COLUMN_NAME_TITLE + " ASC";

        Cursor cursor = db.query(PlaylistsContract.Playlists.TABLE_NAME, projection, null, null, null, null, sortOrder);
        List<String> playlistTitles = cursorToList(cursor);

        db.close();

        List<Playlist> playlists = new ArrayList<>();
        PlaylistEntriesDbHelper dbHelper = new PlaylistEntriesDbHelper(context);
        for (String playlistTitle : playlistTitles) {
            List<SearchItem> playlistEntries = dbHelper.getAllPlaylistEntries(playlistTitle);
            Playlist playlist = new Playlist(playlistTitle, playlistEntries);
            playlists.add(playlist);
        }

        return playlists;
    }

}
