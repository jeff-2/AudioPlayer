package com.jmfoste2.audioplayer.playlist;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.jmfoste2.audioplayer.R;
import com.jmfoste2.audioplayer.model.Playlist;

import java.util.List;

/**
 * An activity for displaying and editing the list of playlists.
 */
public class PlaylistsActivity extends ListActivity implements AdapterView.OnItemLongClickListener {

    private static final String TAG = "PlaylistsActivity";

    private ProgressDialog progressDialog;
    private List<Playlist> playlists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists);

        progressDialog = new ProgressDialog(PlaylistsActivity.this);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        PlaylistsLoadTask playlistsLoadTask = new PlaylistsLoadTask();
        playlistsLoadTask.execute();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // start playlist activity with selected playlist
        Playlist selectedPlaylist = (Playlist) l.getItemAtPosition(position);
        Intent intent = new Intent(this, PlaylistActivity.class);
        intent.putExtra(PlaylistActivity.PLAYLIST, selectedPlaylist);
        startActivity(intent);
    }


    /**
     * Prompts user for confirmation to delete the selected playlist.
     * Deletes the playlist if confirmed, does nothing otherwise.
     *
     * @param playlist The playlist to delete if confirmed.
     */
    private void confirmPlaylistDeletion(final Playlist playlist) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Remove Playlist");
        alert.setMessage("Remove " + playlist.getTitle() + " from playlists?");
        alert.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "Removing playlist" + playlist);
                playlists.remove(playlist);
                ((PlaylistArrayAdapter) getListAdapter()).notifyDataSetChanged();

                RemovePlaylistTask removePlaylistTask = new RemovePlaylistTask(new PlaylistsDbHelper(PlaylistsActivity.this));
                removePlaylistTask.execute(playlist);
            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.show();
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Playlist selectedPlaylist = (Playlist) parent.getItemAtPosition(position);
        confirmPlaylistDeletion(selectedPlaylist);
        return true;
    }

    /**
     * An async task to load all of the playlists.
     */
    private final class PlaylistsLoadTask extends AsyncTask<Void, Void, List<Playlist>> {

        private static final String TAG = "PlaylistsLoadTask";

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(List<Playlist> playlists) {
            progressDialog.hide();
            PlaylistsActivity.this.playlists = playlists;
            PlaylistArrayAdapter adapter = new PlaylistArrayAdapter(PlaylistsActivity.this, playlists);
            setListAdapter(adapter);
            getListView().setOnItemLongClickListener(PlaylistsActivity.this);
        }


        @Override
        protected List<Playlist> doInBackground(Void... params) {
            PlaylistsDbHelper dbHelper = new PlaylistsDbHelper(PlaylistsActivity.this);
            List<Playlist> playlists = dbHelper.getAllPlaylists();
            Log.d(TAG, "Got playlists:" + playlists);
            return playlists;
        }
    }
}
