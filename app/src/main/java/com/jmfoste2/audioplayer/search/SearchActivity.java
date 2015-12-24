package com.jmfoste2.audioplayer.search;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.jmfoste2.audioplayer.R;
import com.jmfoste2.audioplayer.favorite.FavoritesDbHelper;
import com.jmfoste2.audioplayer.favorite.SetFavoritesTask;
import com.jmfoste2.audioplayer.model.Playlist;
import com.jmfoste2.audioplayer.model.SearchItem;
import com.jmfoste2.audioplayer.player.PlayerActivity;
import com.jmfoste2.audioplayer.playlist.PlaylistsDbHelper;
import com.jmfoste2.audioplayer.suggestion.SearchSuggestionProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for searching and displaying the results of a search for
 * a video. Receives an ACTION_SEARCH intent with the associated query,
 * and does the search, and then updates the activity with the results.
 */
public class SearchActivity extends ListActivity {

    private static final String TAG = "SearchActivity";

    private ProgressDialog progressDialog;
    private String query;
    private List<Playlist> playlists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        progressDialog = new ProgressDialog(SearchActivity.this);
        progressDialog.setCanceledOnTouchOutside(false);

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

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);

            retrieveAndDisplaySearchResults(query);
        }
    }

    /**
     * Retrieves playlists using an async task which will then
     * retrieve the search results using an async task upon completion
     * which populates this activity with the results after completion.
     *
     * @param query The query to search for.
     */
    private void retrieveAndDisplaySearchResults(String query) {
        Log.d(TAG, "Fetching results for query:" + query);
        this.query = query;
        PlaylistsLoadTask playlistsLoadTask = new PlaylistsLoadTask();
        playlistsLoadTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        SearchItem selectedItem = (SearchItem) l.getItemAtPosition(position);
        List<SearchItem> playlistEntries = new ArrayList<>();
        playlistEntries.add(selectedItem);
        Playlist playlist = new Playlist(selectedItem.getTitle(), playlistEntries);

        final Intent intent = new Intent(this, PlayerActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(PlayerActivity.PLAYLIST, playlist);

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


    private final class SearchResultTask extends AsyncTask<String, Void, List<SearchItem>> {

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(List<SearchItem> searchItems) {
            progressDialog.hide();
            SearchItemArrayAdapter adapter = new SearchItemArrayAdapter(SearchActivity.this, searchItems, playlists);
            setListAdapter(adapter);
        }

        @Override
        protected List<SearchItem> doInBackground(String... params) {
            try {
                List<SearchItem> searchItems = YouTubeSearchRequest.getSearchResults(params[0]);
                return searchItems;
            } catch (IOException e) {
                Log.d(TAG, "IOException", e);
            }
            return new ArrayList<>();
        }
    }

    private final class PlaylistsLoadTask extends AsyncTask<Void, Void, List<Playlist>> {

        private static final String TAG = "PlaylistsLoadTask";

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(List<Playlist> playlists) {
            progressDialog.hide();
            SearchActivity.this.playlists = playlists;

            SearchResultTask searchResultTask = new SearchResultTask();
            searchResultTask.execute(query);
        }

        @Override
        protected List<Playlist> doInBackground(Void... params) {
            PlaylistsDbHelper dbHelper = new PlaylistsDbHelper(SearchActivity.this);
            List<Playlist> playlists = dbHelper.getAllPlaylists();
            Log.d(TAG, "Got playlists:" + playlists);
            return playlists;
        }
    }
}
