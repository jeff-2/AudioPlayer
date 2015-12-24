package com.jmfoste2.audioplayer.playlist;

import android.os.AsyncTask;
import android.util.Log;

import com.jmfoste2.audioplayer.model.Playlist;

/**
 * Async task to add a playlist to the database.
 */
public class AddPlaylistTask extends AsyncTask<Playlist, Void, Void> {

    private static final String TAG = "AddPlaylistTask";

    private PlaylistsDbHelper dbHelper;

    /**
     * Creates an add playlist task with the specified playlists db helper.
     *
     * @param dbHelper The db helper used to add the playlist to the database
     */
    public AddPlaylistTask(PlaylistsDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    protected Void doInBackground(Playlist... params) {
        long ret = dbHelper.addPlaylist(params[0]);
        Log.d(TAG, "Add playlist with :" + params[0]);
        Log.d(TAG, "Add playlist returned:" + ret);
        return null;
    }
}
