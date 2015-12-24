package com.jmfoste2.audioplayer.favorite;

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
import com.jmfoste2.audioplayer.model.SearchItem;
import com.jmfoste2.audioplayer.player.PlayerActivity;
import com.jmfoste2.audioplayer.search.SearchItemArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * The main activity of the application. Consists of
 * menu items, and their associated actions. Displays favorite search items
 * for easy access to favorite songs.
 */
public class FavoriteActivity extends ListActivity implements AdapterView.OnItemLongClickListener {

    private static final String TAG = "FavoriteActivity";

    private List<SearchItem> favorites;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        progressDialog = new ProgressDialog(FavoriteActivity.this);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FavoritesLoaderTask loaderTask = new FavoritesLoaderTask();
        loaderTask.execute();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // start player activity and pass on the selected item
        SearchItem selectedItem = (SearchItem) l.getItemAtPosition(position);
        selectedItem.setFavorite(true);
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        List<SearchItem> playlistEntries = new ArrayList<>();
        playlistEntries.add(selectedItem);
        Playlist playlist = new Playlist(selectedItem.getTitle(), playlistEntries);
        intent.putExtra(PlayerActivity.PLAYLIST, playlist);
        startActivity(intent);
    }

    /**
     * Display alert dialog to confirm removing the provided favorite search item.
     * Deletes favorite search item if confirmed, does nothing otherwise.
     *
     * @param favorite The favorite search item to remove if confirmed.
     */
    private void confirmFavoriteDeletion(final SearchItem favorite) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Remove Favorite");
        alert.setMessage("Remove " + favorite.getTitle() + " from favorites?");
        alert.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "Removing favorite" + favorite);
                favorites.remove(favorite);
                ((SearchItemArrayAdapter)getListAdapter()).notifyDataSetChanged();
                RemoveFavoriteTask removeFavoriteTask = new RemoveFavoriteTask(new FavoritesDbHelper(FavoriteActivity.this));
                removeFavoriteTask.execute(favorite);
            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.show();
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        SearchItem selectedItem = (SearchItem) parent.getItemAtPosition(position);
        confirmFavoriteDeletion(selectedItem);
        return true;
    }

    private final class FavoritesLoaderTask extends AsyncTask<Void, Void, List<SearchItem>> {

        @Override
        protected List<SearchItem> doInBackground(Void... params) {
            FavoritesDbHelper dbHelper = new FavoritesDbHelper(FavoriteActivity.this);
            return dbHelper.getAllFavorites();
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(List<SearchItem> searchItems) {
            progressDialog.hide();
            Log.d(TAG, "Got items: " + searchItems);
            favorites = searchItems;
            SearchItemArrayAdapter adapter = new SearchItemArrayAdapter(FavoriteActivity.this, searchItems);
            setListAdapter(adapter);
            getListView().setOnItemLongClickListener(FavoriteActivity.this);
        }
    }
}
