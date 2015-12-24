package com.jmfoste2.audioplayer.playlist;

import android.os.AsyncTask;
import android.util.Log;

import com.jmfoste2.audioplayer.model.Playlist;

/**
 * An async task to remove a playlist.
 */
public class RemovePlaylistTask extends AsyncTask<Playlist, Void, Void> {

    private static final String TAG = "RemovePlaylistTask";

    private final PlaylistsDbHelper dbHelper;

    /**
     * Constructs a RemovePlaylistTask with the specified db helper.
     *
     * @param dbHelper The db helper to use
     */
    public RemovePlaylistTask(PlaylistsDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    protected Void doInBackground(Playlist... params) {
        int ret = dbHelper.removePlaylist(params[0]);
        Log.d(TAG, "Remove playlist :" + params[0]);
        Log.d(TAG, "Remove playlist returned:" + ret);
        return null;
    }
}
