package com.example.android.musicplayer.PlayListDataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.example.android.musicplayer.PlayListDataBase.PlayListContract.PlayListEntry;

public class PlayListDbHelper extends SQLiteOpenHelper {

    private final static String LOG_TAG = PlayListDbHelper.class.getSimpleName();

    private final static String DATABASE_NAME = "playlist.db";
    private final static int DATABASE_VERSION = 1;

    public PlayListDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_PLAYLIST_TABLE = "CREATE TABLE " + PlayListEntry.TABLE_NAME + " ("
                                            + PlayListEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                                            + PlayListEntry.COLUMN_PLAYLIST_NAME + " TEXT NOT NULL, "
                                            + PlayListEntry.COLUMN_MEDIA_ID + " TEXT NOT NULL);";
        db.execSQL(SQL_CREATE_PLAYLIST_TABLE);
        Log.v(LOG_TAG, "Create Table: " + SQL_CREATE_PLAYLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //To be implemented;
    }
}
