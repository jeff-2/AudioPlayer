package com.jmfoste2.audioplayer.playlist;

import android.os.AsyncTask;
import android.util.Log;

import com.jmfoste2.audioplayer.model.Playlist;
import com.jmfoste2.audioplayer.model.SearchItem;

/**
 * An async task to add a playlist entry to a playlist
 */
public class AddEntryToPlaylistTask extends AsyncTask<SearchItem, Void, Void> {

    private static final String TAG = "AddEntryToPlaylistTask";

    private final PlaylistsDbHelper dbHelper;
    private final Playlist playlist;

    /**
     * Creates an add entry to playlist task with the specified playlists db helper and playlist.
     *
     * @param dbHelper The db helper used to add the playlist entry to the database
     * @param playlist The playlist for the playlist entry to be added to
     */
    public AddEntryToPlaylistTask(PlaylistsDbHelper dbHelper, Playlist playlist) {
        this.dbHelper = dbHelper;
        this.playlist = playlist;
    }

    @Override
    protected Void doInBackground(SearchItem... params) {
        long ret = dbHelper.addEntryToPlaylist(playlist, params[0]);
        Log.d(TAG, "Add entry to playlist with :" + params[0]);
        Log.d(TAG, "Add entry to playlist returned:" + ret);
        return null;
    }
}
