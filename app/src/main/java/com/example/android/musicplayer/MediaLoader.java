package com.example.android.musicplayer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.content.AsyncTaskLoader;
import android.widget.Toast;

import java.util.ArrayList;

public class MediaLoader extends AsyncTaskLoader<ArrayList<PlayList>> {

    private static final String LOG_TAG = MediaLoader.class.getName();
    private Uri mUri;

    public MediaLoader(Context context, Uri uri) {
        super(context);
        this.mUri = uri;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public ArrayList<PlayList> loadInBackground() {
        if(mUri == null){
            return null;
        }
        ArrayList<PlayList> playlist = new ArrayList<>();
        ContentResolver musicContentResolver = getContext().getContentResolver();
        Cursor musicCursor = musicContentResolver.query(mUri, null, null, null, null);
        if(musicCursor == null){
            Toast.makeText(getContext(), getContext().getString(R.string.load_music_failed), Toast.LENGTH_LONG).show();
            return null;
        }
        else if(!musicCursor.moveToFirst()){
            Toast.makeText(getContext(), getContext().getString(R.string.toast_no_song_found), Toast.LENGTH_LONG).show();
            return null;
        }
        else{
            do{
                long songId = musicCursor.getLong(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                String songName = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String artistName = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                if(artistName.isEmpty() || artistName == null || artistName.equals("<unknown>")) {
                    artistName = "Unknown Artist";
                }
                Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId);
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(getContext(),songUri);
                String songDurationRaw = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                String songDuration = getDuration(songDurationRaw);
                byte[] albumImageRawData = mmr.getEmbeddedPicture();
                Bitmap albumImageBitmap = null;
                if(albumImageRawData != null)
                    albumImageBitmap = BitmapFactory.decodeByteArray(albumImageRawData, 0, albumImageRawData.length);
                else
                    albumImageBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.default_album_icon);
                playlist.add(new PlayList(songId, songName, artistName, songDuration, albumImageBitmap));
            } while(musicCursor.moveToNext());
        }
        return playlist;
    }

    private String getDuration(String rawData) {
        int raw = Integer.parseInt(rawData);
        raw /= 1000;
        int min = raw / 60;
        int sec = raw % 60;
        String minute = Integer.toString(min);
        String second = Integer.toString(sec);
        if (min < 10) {
            String temp = minute;
            minute = "0";
            minute += temp;
        }
        if (sec < 10) {
            String temp = second;
            second = "0";
            second += temp;
        }
        return minute + " : " + second;
    }
}
