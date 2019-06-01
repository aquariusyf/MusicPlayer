package com.example.android.musicplayer.AddSongsToPlaylistActivity;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.musicplayer.PlayList;
import com.example.android.musicplayer.R;

import java.util.ArrayList;

public class AddSongsListAdapter extends ArrayAdapter {

    public View mMediaList;
    public ArrayList<Boolean> mSongCheckState;

    public AddSongsListAdapter(Activity context, ArrayList<PlayList> playList) {
        super(context, 0, playList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mMediaList = convertView;
        if (convertView == null) {
            mMediaList = LayoutInflater.from(getContext()).inflate(
                    R.layout.play_item_in_create_play_list, parent, false);
        }

        final PlayList currentMedia = (PlayList) getItem(position);

        TextView songNameTextView = mMediaList.findViewById(R.id.song_name);
        TextView artistNameTextView = mMediaList.findViewById(R.id.artist_name);
        ImageView albumImageView = mMediaList.findViewById(R.id.album_image);
        LinearLayout thisItem = mMediaList.findViewById(R.id.play_item);
        mSongCheckState = AddSongsActivity.getSongCheckState();

        if(mSongCheckState.get(position))
            thisItem.setBackgroundColor(getContext().getResources().getColor(R.color.color_all_song_list_selected));
        else
            thisItem.setBackgroundColor(getContext().getResources().getColor(R.color.color_all_song_list_background));

        songNameTextView.setText(currentMedia.getmSongName());
        artistNameTextView.setText(currentMedia.getmArtistName());
        if (currentMedia.getAlbumBitMap() != null) {
            albumImageView.setImageBitmap(currentMedia.getAlbumBitMap());
            albumImageView.setScaleX(1.0f);
            albumImageView.setScaleY(1.0f);
        } else {
            albumImageView.setImageResource(R.drawable.new_default_album_icon);
            albumImageView.setScaleX(1.26f);
            albumImageView.setScaleY(1.2f);
        }
        albumImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        return mMediaList;
    }
}
