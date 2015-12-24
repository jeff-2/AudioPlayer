package com.jmfoste2.audioplayer.playlist;

import android.os.AsyncTask;
import android.util.Log;

import com.jmfoste2.audioplayer.model.Playlist;
import com.jmfoste2.audioplayer.model.SearchItem;

/**
 * An async task to remove an entry from a playlist.
 */
public class RemoveEntryFromPlaylistTask extends AsyncTask<SearchItem, Void, Void> {

    private static final String TAG = "AddEntryToPlaylistTask";

    private final PlaylistsDbHelper dbHelper;
    private final Playlist playlist;

    /**
     * Constructs a RemoveEntryFromPlaylistTask with the specified db helper and playlist
     *
     * @param dbHelper The dbHelper to use.
     * @param playlist The playlist to remove an entry from.
     */
    public RemoveEntryFromPlaylistTask(PlaylistsDbHelper dbHelper, Playlist playlist) {
        this.dbHelper = dbHelper;
        this.playlist = playlist;
    }

    @Override
    protected Void doInBackground(SearchItem... params) {
        int ret = dbHelper.removeEntryFromPlaylist(playlist, params[0]);
        Log.d(TAG, "Remove entry from playlist :" + params[0]);
        Log.d(TAG, "Remove entry to playlist returned:" + ret);
        return null;
    }
}
