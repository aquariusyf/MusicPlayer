package com.example.android.musicplayer.AddSongsToPlaylistActivity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.musicplayer.CreateEditPlaylistActivity.CreateEditPlayListActivity;
import com.example.android.musicplayer.MainActivity;
import com.example.android.musicplayer.R;

import java.util.ArrayList;

public class AddSongsActivity extends AppCompatActivity {

    private final String LOG_TAG = AddSongsActivity.class.getSimpleName();
    private TextView mAddButton;
    private TextView mCancelButton;
    private ListView mAllSongsListView;
    private AddSongsListAdapter mAdapter;
    private static ArrayList<Boolean> mSongCheckState;
    private static String mAddedSongIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_songs);

        initSongCheckState();
        mAddedSongIds = "";

        mAddButton = findViewById(R.id.add_button);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveResult();
                mAddedSongIds = "";
                finish();
            }
        });

        mCancelButton = findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddedSongIds = "";
                finish();
            }
        });

        mAllSongsListView = findViewById(R.id.add_songs_list_view);
        mAdapter = new AddSongsListAdapter(this, MainActivity.getAllSongs());
        mAllSongsListView.setAdapter(mAdapter);
        mAllSongsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectMedia(position);
            }
        });
    }

    private void initSongCheckState() {
        mSongCheckState = new ArrayList<>();
        int size = MainActivity.getAllSongs().size();
        for(int i = 0; i < size; i++){
            mSongCheckState.add(false);
        }
    }

    public static ArrayList<Boolean> getSongCheckState() {
        return mSongCheckState;
    }

    private void selectMedia(int pos) {
        if(pos < 0 || pos > mSongCheckState.size() - 1)
            return;
        int firstVisiblePos = mAllSongsListView.getFirstVisiblePosition();
        int lastVisiblePos = mAllSongsListView.getLastVisiblePosition();
        if(mSongCheckState.get(pos)) {
            mSongCheckState.set(pos, false);
        }
        else {
            mSongCheckState.set(pos, true);
        }
        if(pos >= firstVisiblePos && pos <= lastVisiblePos){
            LinearLayout singleItem = mAllSongsListView.getChildAt(pos - firstVisiblePos).findViewById(R.id.play_item);
            if(mSongCheckState.get(pos))
                singleItem.setBackgroundColor(getResources().getColor(R.color.color_all_song_list_selected));
            else
                singleItem.setBackgroundColor(getResources().getColor(R.color.color_all_song_list_background));
        }
        if(mSongCheckState.get(pos)){
            addMediaId(pos);
        }
        else{
            removeMediaId(pos);
        }
    }

    private void addMediaId(int pos) {
        mAddedSongIds += MainActivity.getAllSongs().get(pos).getmMedia();
        mAddedSongIds += " ";
        Log.v(LOG_TAG, "Updated id list: " + mAddedSongIds);
    }

    private void removeMediaId(int pos) {
        String id = Long.toString(MainActivity.getAllSongs().get(pos).getmMedia());
        id += " ";
        if(mAddedSongIds.contains(id)){
            mAddedSongIds =  mAddedSongIds.replace(id, "");
            Log.v(LOG_TAG, "Updated id list: " + mAddedSongIds);
        }
    }

    private void saveResult(){
        CreateEditPlayListActivity.getAddedSongIds(mAddedSongIds);
        CreateEditPlayListActivity.setIsFromAddSongsTrue();
    }

}
