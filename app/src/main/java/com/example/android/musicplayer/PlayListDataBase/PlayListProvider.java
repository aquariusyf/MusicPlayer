package com.example.android.musicplayer.PlayListDataBase;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.musicplayer.PlayListDataBase.PlayListContract.PlayListEntry;

public class PlayListProvider extends ContentProvider {

    public static final String LOG_TAG = PlayListProvider.class.getSimpleName();
    private PlayListDbHelper mDbHelper;

    private static final int PLAYLIST = 100;
    private static final int PLAYLIST_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(PlayListContract.CONTENT_AUTHORITY, PlayListContract.PATH_PLAYLISTS, PLAYLIST);
        sUriMatcher.addURI(PlayListContract.CONTENT_AUTHORITY, PlayListContract.PATH_PLAYLISTS + "/#", PLAYLIST_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new PlayListDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PLAYLIST:
                cursor = database.query(PlayListEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case PLAYLIST_ID:
                selection = PlayListEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(PlayListEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PLAYLIST:
                return insertPlayList(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertPlayList(Uri uri, ContentValues values) {
        String name = values.getAsString(PlayListEntry.COLUMN_PLAYLIST_NAME);
        if (name == null)
            throw new IllegalArgumentException("PlayList requires a name");

        String mediaId = values.getAsString(PlayListEntry.COLUMN_MEDIA_ID);
        if (mediaId == null)
            throw new IllegalArgumentException("PlayList requires valid ID");

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long newPlayListId = db.insert(PlayListEntry.TABLE_NAME, null, values);
        if (newPlayListId == -1) {
            Log.e(LOG_TAG, "Failed to insert a row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, newPlayListId);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PLAYLIST:
                return updatePlayList(uri, contentValues, selection, selectionArgs);
            case PLAYLIST_ID:
                selection = PlayListEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePlayList(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updatePlayList(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.size() == 0)
            return 0;

        if (values.containsKey(PlayListEntry.COLUMN_PLAYLIST_NAME)) {
            String name = values.getAsString(PlayListEntry.COLUMN_PLAYLIST_NAME);
            if (name == null)
                throw new IllegalArgumentException("PlayList requires a name");
        }

        if (values.containsKey(PlayListEntry.COLUMN_MEDIA_ID)) {
            String mediaId = values.getAsString(PlayListEntry.COLUMN_MEDIA_ID);
            if (mediaId == null)
                throw new IllegalArgumentException("PlayList requires valid ID");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsUpdated = db.update(PlayListEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case PLAYLIST:
                rowsDeleted = database.delete(PlayListEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0)
                    getContext().getContentResolver().notifyChange(uri, null);
                return rowsDeleted;
            case PLAYLIST_ID:
                selection = PlayListEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                getContext().getContentResolver().notifyChange(uri, null);
                return database.delete(PlayListEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PLAYLIST:
                return PlayListEntry.CONTENT_LIST_TYPE;
            case PLAYLIST_ID:
                return PlayListEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

}
