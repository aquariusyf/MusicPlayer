package com.example.android.musicplayer.PlayItemFragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.musicplayer.PlayList;
import com.example.android.musicplayer.R;

import java.util.ArrayList;

public class CurrentMediaFragment extends Fragment {

    public static ImageView mAlbumImageView;
    public static TextView mMediaNameTextView;

    public CurrentMediaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.current_media_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mAlbumImageView = view.findViewById(R.id.current_media_fragment_image);
        mMediaNameTextView = view.findViewById(R.id.current_media_name);
        super.onViewCreated(view, savedInstanceState);
    }

    public static void updateAlbumDisplay(int newPos, ArrayList<PlayList> playList){
        Bitmap newAlbumImage = playList.get(newPos).getAlbumBitMap();
        mAlbumImageView.setImageBitmap(newAlbumImage);
    }

    public static void updateMediaName(PlayList playList){
        String mediaName = playList.getmSongName();
        mMediaNameTextView.setText(mediaName);
        mMediaNameTextView.setSelected(true);
    }
}
