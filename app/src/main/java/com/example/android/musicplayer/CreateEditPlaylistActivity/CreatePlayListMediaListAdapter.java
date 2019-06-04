package com.example.android.musicplayer.CreateEditPlaylistActivity;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.musicplayer.PlayList;
import com.example.android.musicplayer.R;

import java.util.ArrayList;

public class CreatePlayListMediaListAdapter extends ArrayAdapter {

    public View mMediaList;

    public CreatePlayListMediaListAdapter(Activity context, ArrayList<PlayList> playList) {
        super(context, 0, playList);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        mMediaList = convertView;
        if (convertView == null) {
            mMediaList = LayoutInflater.from(getContext()).inflate(
                    R.layout.play_item_in_create_play_list, parent, false);
        }

        final PlayList currentMedia = (PlayList) getItem(position);
        TextView songNameTextView = mMediaList.findViewById(R.id.song_name);
        TextView artistNameTextView = mMediaList.findViewById(R.id.artist_name);
        ImageView albumImageView = mMediaList.findViewById(R.id.album_image);
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

        final TextView deleteButton = mMediaList.findViewById(R.id.delete_song_from_playlist_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteButton.setVisibility(View.GONE);
                CreateEditPlayListActivity.deleteSong(position);
                notifyDataSetChanged();
            }
        });

        return mMediaList;
    }
}
