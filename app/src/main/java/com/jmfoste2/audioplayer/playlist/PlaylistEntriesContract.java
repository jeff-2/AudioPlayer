package com.jmfoste2.audioplayer.playlist;

import android.provider.BaseColumns;

/**
 * Represents the contract for the playlist_entries table in the database.
 */
public class PlaylistEntriesContract {

    // should not be instantiated
    private PlaylistEntriesContract() {}

    /**
     * Represents the playlist_entries table in the database. Provides
     * the columns of the table for usage in database queries.
     */
    public static abstract class PlaylistEntry implements BaseColumns {
        public static final String TABLE_NAME = "playlist_entries";
        public static final String COLUMN_NAME_PLAYLIST_TITLE = "playlist_title";
        public static final String COLUMN_NAME_VIDEO_ID = "video_id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_DEFAULT_THUMBNAIL_URL = "default_thumbnail_url";
        public static final String COLUMN_NAME_HIGH_RES_THUMBNAIL_URL = "high_res_thumbnail_url";
        public static final String COLUMN_NAME_DURATION = "duration";
    }
}
