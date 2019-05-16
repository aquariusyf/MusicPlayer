package com.example.android.musicplayer;

public class PlayList {
    private long mMedia;
    private String mSongName;
    private String mArtistName;
    private String mMediaDuration;
    private String mPlaying;

    public PlayList(long mediaSource, String songName, String artistName, String mediaDuration){
        this.mMedia = mediaSource;
        this.mSongName = songName;
        this.mArtistName = artistName;
        this.mMediaDuration = mediaDuration;
        this.mPlaying = "";
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
}
