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

    public PlayItemAdapter(Activity context, ArrayList<PlayList> playList){
        super(context, 0, playList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View playListView = convertView;
        if(convertView == null){
            playListView = LayoutInflater.from(getContext()).inflate(
                    R.layout.play_item, parent, false);
        }

        final PlayList currentMedia = getItem(position);

        TextView songNameTextView = playListView.findViewById(R.id.song_name);
        TextView artistNameTextView = playListView.findViewById(R.id.artist_name);
        songNameTextView.setText(currentMedia.getmSongName());
        artistNameTextView.setText(currentMedia.getmArtistName());
        songNameTextView.setSelected(true);
        artistNameTextView.setSelected(true);
        return playListView;
    }
}
