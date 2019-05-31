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
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import com.example.android.musicplayer.PlayListDataBase.PlayListContract.PlayListEntry;

public class MediaLoader extends AsyncTaskLoader<ArrayList<PlayList>> {

    private static final String LOG_TAG = MediaLoader.class.getName();
    private Uri mUri;

    public MediaLoader(Context context, Uri uri) {
        super(context);
        this.mUri = uri;
    }

    @Override
    protected void onStartLoading() {
        Log.v(LOG_TAG, "onStartLoading Called with Uri = " + mUri);
        if(!(mUri == null))
            forceLoad();
    }

    @Override
    public ArrayList<PlayList> loadInBackground() {
        Log.v(LOG_TAG, "loadInBackground Called with Uri = " + mUri);
        if(mUri == null){
            return null;
        }

        ArrayList<PlayList> playlist = new ArrayList<>();
        if(mUri == MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
            loadAllSongsAtStart(playlist);
        else
            loadSongsOfPlaylist(playlist);
        mUri = null;
        return playlist;
    }

    private void loadAllSongsAtStart(ArrayList<PlayList> playlist) {
        ContentResolver musicContentResolver = getContext().getContentResolver();
        Cursor musicCursor = musicContentResolver.query(mUri, null, null, null, null);
        if(musicCursor == null){
            Toast.makeText(getContext(), getContext().getString(R.string.load_music_failed), Toast.LENGTH_LONG).show();
            return;
        }
        else if(!musicCursor.moveToFirst()){
            Toast.makeText(getContext(), getContext().getString(R.string.toast_no_song_found), Toast.LENGTH_LONG).show();
            return;
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
    }

    private void loadSongsOfPlaylist(ArrayList<PlayList> playlist){
        ContentResolver playlistContentResolver = getContext().getContentResolver();
        Cursor playlistCursor = playlistContentResolver.query(mUri, null, null, null, null);
        if(!playlistCursor.moveToFirst())
            return;
        String songIdsRaw = playlistCursor.getString(playlistCursor.getColumnIndexOrThrow(PlayListEntry.COLUMN_MEDIA_ID));
        if(songIdsRaw == null || songIdsRaw.isEmpty())
            return;
        String[] songIdArray = songIdsRaw.split(" ");
        long[] songIds = new long[songIdArray.length];
        for(int i = 0; i < songIdArray.length; i++)
            songIds[i] = Long.parseLong(songIdArray[i]);

        for(int j = 0; j < songIds.length; j++) {
            Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songIds[j]);
            ContentResolver songContentResolver = getContext().getContentResolver();
            Cursor songCursor = songContentResolver.query(songUri, null, null, null, null);
            if(!songCursor.moveToFirst()) {
                return;
            }

            long songId = songCursor.getLong(songCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
            String songName = songCursor.getString(songCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            String artistName = songCursor.getString(songCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            if(artistName.isEmpty() || artistName == null || artistName.equals("<unknown>")) {
                artistName = "Unknown Artist";
            }

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
        }
        Log.v(LOG_TAG, "loadSongOfPlaylist Called");
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
