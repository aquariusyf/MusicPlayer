package com.example.android.musicplayer.PlayListDataBase;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class PlayListContract {

    public final static String CONTENT_AUTHORITY = "com.example.android.musicplayer";
    public final static Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public final static String PATH_PLAYLISTS = "playlists";

    private PlayListContract(){
        //default constructor
    }

    public static final class PlayListEntry implements BaseColumns {
        public final static String TABLE_NAME = "playlists";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_PLAYLIST_NAME = "NAME";
        public final static String COLUMN_MEDIA_ID = "MEDIA_ID";

        public final static Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PLAYLISTS);
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PLAYLISTS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PLAYLISTS;
    }

}
