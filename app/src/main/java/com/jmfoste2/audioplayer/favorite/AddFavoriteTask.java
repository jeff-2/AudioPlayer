package com.jmfoste2.audioplayer.favorite;

import android.os.AsyncTask;
import android.util.Log;

import com.jmfoste2.audioplayer.model.SearchItem;

/**
 * Async task for adding a search item to the database as a favorite.
 */
public class AddFavoriteTask extends AsyncTask<SearchItem, Void, Void> {

    private static final String TAG = "AddFavoriteTask";

    private FavoritesDbHelper dbHelper;

    /**
     * Creates an add favorite task with the specified favorites db helper.
     *
     * @param dbHelper The db helper used to add the search item as a favorite into the database
     */
    public AddFavoriteTask(FavoritesDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    protected Void doInBackground(SearchItem... params) {
        long ret = dbHelper.addFavorite(params[0]);
        Log.d(TAG, "Add favorite with :" + params[0]);
        Log.d(TAG, "Add favorite returned:" + ret);
        return null;
    }
}
