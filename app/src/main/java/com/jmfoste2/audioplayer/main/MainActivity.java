package com.jmfoste2.audioplayer.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;

import com.jmfoste2.audioplayer.R;
import com.jmfoste2.audioplayer.favorite.FavoriteActivity;
import com.jmfoste2.audioplayer.playlist.PlaylistsActivity;
import com.jmfoste2.audioplayer.search.SearchActivity;
import com.jmfoste2.audioplayer.suggestion.SearchSuggestionProvider;

/**
 * The main entry point of the application. Provides access to the
 * favorites and playlists. Also provides search functionality in action bar.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button favoritesButton = (Button) findViewById(R.id.favorites_button);
        favoritesButton.setOnClickListener(favoritesListener);

        Button playlistsButton = (Button) findViewById(R.id.playlists_button);
        playlistsButton.setOnClickListener(playlistsListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        ComponentName searchableComponentName = new ComponentName(this, SearchActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(searchableComponentName));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_search_history:
                clearSearchHistory();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Displays a dialog confirming whether or not to clear
     * search history, and if confirmed, clears the search
     * history of the user.
     */
    private void clearSearchHistory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Clear Search History");
        builder.setMessage("Are you sure?");
        builder.setPositiveButton("YES", clearSearchHistoryListener);
        builder.setNegativeButton("NO", null);
        builder.show();
    }

    private final View.OnClickListener favoritesListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, FavoriteActivity.class);
            startActivity(intent);
        }
    };

    private final View.OnClickListener playlistsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, PlaylistsActivity.class);
            startActivity(intent);
        }
    };

    private final DialogInterface.OnClickListener clearSearchHistoryListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(MainActivity.this,
                    SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
            suggestions.clearHistory();
        }
    };
}
