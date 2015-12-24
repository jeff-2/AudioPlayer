package com.jmfoste2.audioplayer.favorite;

import android.provider.BaseColumns;

/**
 * Represents the contract for the favorites table in the database.
 */
public final class FavoritesContract {

    // should not be instantiated
    private FavoritesContract() {}

    /**
     * Represents the favorites table in the database. Provides
     * the columns of the table for usage in database queries.
     */
    public static abstract class Favorites implements BaseColumns {
        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_NAME_VIDEO_ID = "video_id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_THUMBNAIL_URL = "thumbnail_url";
        public static final String COLUMN_NAME_DURATION = "duration";
    }
}
