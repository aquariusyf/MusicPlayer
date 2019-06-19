package com.example.android.musicplayer;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.android.musicplayer.CreateEditPlaylistActivity.CreateEditPlayListActivity;
import com.example.android.musicplayer.PlayListDataBase.PlayListCursorAdapter;
import com.example.android.musicplayer.PlayListDataBase.PlayListContract.PlayListEntry;

import java.util.ArrayList;

public class MainFragmentPlayList extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static String LOG_TAG = MainFragmentPlayList.class.getSimpleName();
    private final int PLAYLIST_LOADER = 0;
    private static ListView mListView;
    private ImageView mOptionsMenu;
    private static PlayListCursorAdapter mCursorAdapter;
    private TextView mDoneButton;
    private Button mPlayAllButton;
    private static ArrayList<Boolean> mPlaylistSelectState = new ArrayList<>();

    public TextView mTotalNumberOfSongs;

    public MainFragmentPlayList() {
        // empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getLoaderManager().initLoader(PLAYLIST_LOADER, null, this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.playlist_screen_fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mPlayAllButton = view.findViewById(R.id.play_all_button);
        mPlayAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getConsoleFragment().changePlaylist(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            }
        });

        mDoneButton = view.findViewById(R.id.done_with_edit_button);
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayListCursorAdapter.setDeleteButtonState(false);
                mCursorAdapter.notifyDataSetChanged();
                mDoneButton.setVisibility(View.GONE);
                deletePlaylist();
            }
        });
        mOptionsMenu = view.findViewById(R.id.options_nemu);
        mOptionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsMenu(v);
            }
        });
        mTotalNumberOfSongs = view.findViewById(R.id.number_of_total_songs);
        mCursorAdapter = new PlayListCursorAdapter(getActivity(), null);
        mListView = view.findViewById(R.id.playlists_listview);
        ImageView emptyView = view.findViewById(R.id.playlists_listview_empty_view);
        mListView.setEmptyView(emptyView);
        mListView.setAdapter(mCursorAdapter);
        super.onViewCreated(view, savedInstanceState);
    }


    private void deletePlaylist() {
        if(mPlaylistSelectState == null || mPlaylistSelectState.isEmpty())
            return;
        for(int i = 0; i < mPlaylistSelectState.size(); i++){
            if(mPlaylistSelectState.get(i)){
                Uri uriForDeletion = ContentUris.withAppendedId(PlayListEntry.CONTENT_URI, getIdByPosition(i));
                int rowsDeleted = getActivity().getContentResolver().delete(uriForDeletion, null, null);
                mCursorAdapter.notifyDataSetChanged();
            }
            Log.v(LOG_TAG, "Selected: " + i + " " + mPlaylistSelectState.get(i));
        }

        for(int j = 0; j < mPlaylistSelectState.size();) {
            if(mPlaylistSelectState.get(j))
                mPlaylistSelectState.remove(j);
            else
                j++;
        }
    }

    private static long getIdByPosition(int pos) {
        Cursor c = mCursorAdapter.getCursor();
        if(c.moveToFirst()){
            while(pos > 0){
                c.moveToNext();
                pos--;
            }
        }
        long result = c.getLong(c.getColumnIndexOrThrow(PlayListEntry._ID));
        Log.v(LOG_TAG, "result: " + result);
        return result;
    }

    public static ArrayList<Boolean> getSelectState() {
        return mPlaylistSelectState;
    }

    public static void setSelectedState(int pos, boolean value) {
        mPlaylistSelectState.set(pos, value);
    }

    public static void addSelectedStateForNewPlaylist() {
        mPlaylistSelectState.add(false);
    }

    private void showOptionsMenu(View v){
        PopupMenu popup = new PopupMenu(getActivity(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getTitle().toString()){
                    case "New Playlist":
                        Intent newPlaylist = new Intent(getActivity(), CreateEditPlayListActivity.class);
                        startActivity(newPlaylist);
                        break;
                    case "Edit":
                        mDoneButton.setVisibility(View.VISIBLE);
                        PlayListCursorAdapter.setDeleteButtonState(true);
                        mCursorAdapter.notifyDataSetChanged();
                        break;
                    default: break;
                }
                return true;
            }
        });
        popup.show();
    }

    public void setTotalNumberOfSongs(int total){
        Log.v(LOG_TAG, "Set number of total songs: " + total);
        if(total < 1){
            mTotalNumberOfSongs.setText(getString(R.string.no_songs));
            return;
        }
        mTotalNumberOfSongs.setText(total + " Songs");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {PlayListEntry._ID,
                PlayListEntry.COLUMN_PLAYLIST_NAME,
                PlayListEntry.COLUMN_MEDIA_ID};
        return new CursorLoader(getActivity(),
                PlayListEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data == null)
            return;
        mCursorAdapter.swapCursor(data);
        if(mPlaylistSelectState == null || mPlaylistSelectState.isEmpty())
            for(int i = 0; i < data.getCount(); i++)
                mPlaylistSelectState.add(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

}
