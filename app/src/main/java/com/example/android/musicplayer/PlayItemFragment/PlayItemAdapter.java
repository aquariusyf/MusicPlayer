package com.example.android.musicplayer.PlayItemFragment;

import android.app.Activity;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.musicplayer.PlayList;
import com.example.android.musicplayer.R;
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
        ImageView albumImageView = mPlayListView.findViewById(R.id.album_image);
        songNameTextView.setText(currentMedia.getmSongName());
        artistNameTextView.setText(currentMedia.getmArtistName());
        songDurationTextView.setText(currentMedia.getmMediaDuration());
        playingTextView.setText(currentMedia.getPlayingState());
        if(currentMedia.getAlbumBitMap() != null){
            albumImageView.setImageBitmap(currentMedia.getAlbumBitMap());
            albumImageView.setScaleX(1.0f);
            albumImageView.setScaleY(1.0f);
        }
        else{
            albumImageView.setImageResource(R.drawable.new_default_album_icon);
            albumImageView.setScaleX(1.26f);
            albumImageView.setScaleY(1.2f);
        }
        albumImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        return mPlayListView;
    }
}
