package com.jmfoste2.audioplayer.favorite;

import android.os.AsyncTask;

import com.jmfoste2.audioplayer.model.SearchItem;

import java.util.List;


/**
 * Async task to mark which search items are favorites.
 */
public class SetFavoritesTask extends AsyncTask<List<SearchItem>, Void, Void> {

    private final FavoritesDbHelper dbHelper;
    private final Runnable onPostExecuteRunnable;

    /**
     * Creates a set favorites task with specified db helper and runnable to
     * be executed after execution completion.
     *
     * @param dbHelper The db helper to use.
     * @param onPostExecuteRunnable The runnable to execute after completion.
     */
    public SetFavoritesTask(FavoritesDbHelper dbHelper, Runnable onPostExecuteRunnable) {
        this.dbHelper = dbHelper;
        this.onPostExecuteRunnable = onPostExecuteRunnable;
    }

    @Override
    protected Void doInBackground(List<SearchItem>... params) {
        for (SearchItem searchItem : params[0]) {
            searchItem.setFavorite(dbHelper.isFavorite(searchItem));
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        onPostExecuteRunnable.run();
    }
}
