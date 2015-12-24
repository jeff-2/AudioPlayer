package com.jmfoste2.audioplayer.favorite;

import android.os.AsyncTask;
import android.util.Log;

import com.jmfoste2.audioplayer.model.SearchItem;

/**
 * Async task for removing a favorite search item from the database.
 */
public class RemoveFavoriteTask extends AsyncTask<SearchItem, Void, Void> {

    private static final String TAG = "RemoveFavoriteTask";

    private FavoritesDbHelper dbHelper;

    /**
     * Creates a remove favorite task with the specified favorites db helper.
     *
     * @param dbHelper The db helper used to remove the favorite search item from the database
     */
    public RemoveFavoriteTask(FavoritesDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    protected Void doInBackground(SearchItem... params) {
        int ret = dbHelper.removeFavorite(params[0]);
        Log.d(TAG, "removeFavorite with: " + params[0]);
        Log.d(TAG, "removeFavorite returned:" + ret);
        return null;
    }
}
