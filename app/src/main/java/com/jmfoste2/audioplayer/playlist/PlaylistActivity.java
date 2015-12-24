package com.jmfoste2.audioplayer.playlist;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.jmfoste2.audioplayer.R;
import com.jmfoste2.audioplayer.favorite.FavoritesDbHelper;
import com.jmfoste2.audioplayer.favorite.SetFavoritesTask;
import com.jmfoste2.audioplayer.model.Playlist;
import com.jmfoste2.audioplayer.model.SearchItem;
import com.jmfoste2.audioplayer.player.PlayerActivity;
import com.jmfoste2.audioplayer.search.SearchItemArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for displaying and editing a playlist.
 */
public class PlaylistActivity extends ListActivity implements AdapterView.OnItemLongClickListener {

    private static final String TAG = "PlaylistActivity";

    public static final String PLAYLIST = "playlist";

    private Playlist playlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    /**
     * Handles the ACTION_SEARCH intent which started this activity.
     * Saves the search query as a recent query, and retrieves
     * and displays the search results for that query.
     *
     * @param intent The intent which started this activity
     */
    private void handleIntent(Intent intent) {
        playlist = intent.getParcelableExtra(PLAYLIST);
        SearchItemArrayAdapter adapter = new SearchItemArrayAdapter(this, playlist.getEntries());
        setListAdapter(adapter);
        getListView().setOnItemLongClickListener(PlaylistActivity.this);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        playlist.setCurrentEntryPosition(position);

        final Intent intent = new Intent(this, PlayerActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(PlayerActivity.PLAYLIST, playlist);


        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        SetFavoritesTask setFavoritesTask = new SetFavoritesTask(new FavoritesDbHelper(this), new Runnable() {
            @Override
            public void run() {
                progressDialog.hide();
                startActivity(intent);
            }
        });
        setFavoritesTask.execute(playlist.getEntries());
    }

    /**
     * Prompts user with dialog to confirm removing an entry
     * from the playlist. Removes entry from playlist upon confirmation,
     * does nothing otherwise.
     *
     * @param entry The SearchItem to remove if confirmed.
     */
    private void confirmPlaylistEntryDeletion(final SearchItem entry) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Remove Playlist Entry");
        alert.setMessage("Remove " + entry.getTitle() + " from this playlist?");
        alert.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "Removing playlist entry" + entry);
                playlist.getEntries().remove(entry);
                ((SearchItemArrayAdapter) getListAdapter()).notifyDataSetChanged();

                RemoveEntryFromPlaylistTask removeEntryFromPlaylistTask = new RemoveEntryFromPlaylistTask(
                        new PlaylistsDbHelper(PlaylistActivity.this), playlist);
                removeEntryFromPlaylistTask.execute(entry);
            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.show();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        SearchItem selectedItem = (SearchItem) parent.getItemAtPosition(position);
        confirmPlaylistEntryDeletion(selectedItem);
        return true;
    }
}
