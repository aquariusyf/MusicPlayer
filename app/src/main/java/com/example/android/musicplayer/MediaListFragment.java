package com.example.android.musicplayer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MediaListFragment extends Fragment {

    private static ListView mListView;
    private static PlayItemAdapter mPlayItemAdapter;
    private static AdapterView.OnItemClickListener mListener;

    public MediaListFragment() {
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
        return inflater.inflate(R.layout.media_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mListView = view.findViewById(R.id.play_list_fragment_layout);
        mListView.setAdapter(mPlayItemAdapter);
        mListView.setOnItemClickListener(getListener());
        super.onViewCreated(view, savedInstanceState);
    }

    public static void initAdapter(ArrayList<PlayList> playList, Activity context){
        mPlayItemAdapter = new PlayItemAdapter(context, playList);
    }

    public static ListView getListView(){
        return mListView;
    }

    public static AdapterView.OnItemClickListener getListener(){
        return mListener;
    }

    public static void setListener(AdapterView.OnItemClickListener listener){
        mListener = listener;
    }

    public static void updateText(int currentPos, int newPos, ArrayList<PlayList> mPlayList){
        int firstVisiblePosition = mListView.getFirstVisiblePosition();
        int lastVisiblePosition = mListView.getLastVisiblePosition();
        if(currentPos >= firstVisiblePosition && currentPos <= lastVisiblePosition){
            TextView oldItemTextView = mListView.getChildAt(currentPos - firstVisiblePosition).findViewById(R.id.is_playing);
            oldItemTextView.setText(mPlayList.get(currentPos).getPlayingState());
        }
        if(newPos >= firstVisiblePosition && newPos <= lastVisiblePosition){
            TextView newItemTextView = mListView.getChildAt(newPos - firstVisiblePosition).findViewById(R.id.is_playing);
            newItemTextView.setText(mPlayList.get(newPos).getPlayingState());
        }
    }
}
