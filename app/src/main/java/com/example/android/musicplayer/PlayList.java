package com.example.android.musicplayer;

public class PlayList {
    private int mMedia;
    private String mSongName;

    public PlayList(int mediaSource, String songName){
        this.mMedia = mediaSource;
        this.mSongName = songName;
    }

    public int getmMedia(){
        return this.mMedia;
    }

    public String getmSongName(){
        return this.mSongName;
    }
}
