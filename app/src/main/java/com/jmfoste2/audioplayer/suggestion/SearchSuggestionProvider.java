package com.jmfoste2.audioplayer.suggestion;

import android.app.SearchManager;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import java.io.IOException;
import java.util.List;

/**
 * A ContentProvider for search suggestions. Provides suggestions for recent
 * searches if no query entered, or retrieves suggestions from a remote
 * server if query specified.
 */
public class SearchSuggestionProvider extends SearchRecentSuggestionsProvider {

    public static final String AUTHORITY = "com.jmfoste2.audioplayer.suggestion.SearchSuggestionProvider";
    public static final int MODE = DATABASE_MODE_QUERIES;

    private static final String TAG = "SearchSuggestionProvidr";
    private static final String[] COLUMNS = { BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_QUERY };

    public SearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String queryText = selectionArgs[0];
        
        if (queryText.isEmpty()) {
            return super.query(uri, projection, selection, selectionArgs, sortOrder);
        } else {
            MatrixCursor cursor = new MatrixCursor(COLUMNS);

            try {
                List<String> suggestions = SearchSuggestionRequest.getSuggestions(queryText);
                if (suggestions != null) {
                    for (int i = 0; i < suggestions.size(); i++) {
                        cursor.addRow(new String[]{Integer.toString(i), suggestions.get(i), suggestions.get(i)});
                    }
                }
            } catch (IOException e) {
                Log.d(TAG, "IOException", e);
            }

            return cursor;
        }
    }
}
