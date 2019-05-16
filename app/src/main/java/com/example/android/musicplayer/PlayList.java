package com.example.android.musicplayer;

public class PlayList {
    private long mMedia;
    private String mSongName;
    private String mArtistName;

    public PlayList(long mediaSource, String songName, String artistName){
        this.mMedia = mediaSource;
        this.mSongName = songName;
        this.mArtistName = artistName;
    }

    public long getmMedia(){
        return this.mMedia;
    }

    public String getmSongName(){
        return this.mSongName;
    }

    public String getmArtistName(){ return this.mArtistName; }
}
