package com.example.android.musicplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class PlayList {
    private long mMedia;
    private String mSongName;
    private String mArtistName;
    private String mMediaDuration;
    private String mPlaying;
    private Bitmap mAlbumBitMap;

    public PlayList(long mediaSource, String songName, String artistName, String mediaDuration, Bitmap albumBitmap){
        this.mMedia = mediaSource;
        this.mSongName = songName;
        this.mArtistName = artistName;
        this.mMediaDuration = mediaDuration;
        this.mPlaying = "";
        this.mAlbumBitMap = albumBitmap;
    }

    public long getmMedia(){
        return this.mMedia;
    }

    public String getmSongName(){
        return this.mSongName;
    }

    public String getmArtistName(){ return this.mArtistName; }

    public String getmMediaDuration(){ return this.mMediaDuration; }

    public String getPlayingState() { return this.mPlaying; }

    public void setPlayingState(String p){
        this.mPlaying = p;
    }

    public Bitmap getAlbumBitMap(){ return this.mAlbumBitMap; }
}
