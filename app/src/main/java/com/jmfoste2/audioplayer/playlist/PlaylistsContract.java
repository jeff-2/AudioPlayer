package com.jmfoste2.audioplayer.playlist;

import android.provider.BaseColumns;

/**
 * Represents the contract for the playlists table in the database.
 */
public class PlaylistsContract {

    // should not be instantiated
    private PlaylistsContract() {}

    /**
     * Represents the favorites table in the database. Provides
     * the columns of the table for usage in database queries.
     */
    public static abstract class Playlists implements BaseColumns {
        public static final String TABLE_NAME = "playlists";
        public static final String COLUMN_NAME_TITLE = "title";
    }
}
