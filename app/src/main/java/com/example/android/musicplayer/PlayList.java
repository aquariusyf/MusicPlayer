package com.example.android.musicplayer;

public class PlayList {
    private long mMedia;
    private String mSongName;

    public PlayList(long mediaSource, String songName){
        this.mMedia = mediaSource;
        this.mSongName = songName;
    }

    public long getmMedia(){
        return this.mMedia;
    }

    public String getmSongName(){
        return this.mSongName;
    }
}
