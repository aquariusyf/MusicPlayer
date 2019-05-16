package com.example.android.musicplayer;

import android.app.Activity;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class PlayItemAdapter extends ArrayAdapter<PlayList> {

    public View mPlayListView;

    public PlayItemAdapter(Activity context, ArrayList<PlayList> playList){
        super(context, 0, playList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mPlayListView = convertView;
        if(convertView == null){
            mPlayListView = LayoutInflater.from(getContext()).inflate(
                    R.layout.play_item, parent, false);
        }

        final PlayList currentMedia = getItem(position);
        TextView songNameTextView = mPlayListView.findViewById(R.id.song_name);
        TextView artistNameTextView = mPlayListView.findViewById(R.id.artist_name);
        TextView songDurationTextView = mPlayListView.findViewById(R.id.song_duration);
        TextView playingTextView = mPlayListView.findViewById(R.id.is_playing);
        songNameTextView.setText(currentMedia.getmSongName());
        artistNameTextView.setText(currentMedia.getmArtistName());
        songDurationTextView.setText(currentMedia.getmMediaDuration());
        playingTextView.setText(currentMedia.getPlayingState());
        return mPlayListView;
    }
}
