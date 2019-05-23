package com.example.android.musicplayer;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.android.musicplayer.PlayListDataBase.PlayListCursorAdapter;
import com.example.android.musicplayer.PlayListDataBase.PlayListContract.PlayListEntry;


public class MainFragmentPlayList extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final int PLAYLIST_LOADER = 0;
    private static ListView mListView;
    private static PlayListCursorAdapter mCursorAdapter;
    private ImageView mCreateListButton;

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

        mCreateListButton = view.findViewById(R.id.create_play_list_imageview);
        mCreateListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createPlayList = new Intent(getActivity(), CreateNewPlayList.class);
                startActivity(createPlayList);
            }
        });

        mCursorAdapter = new PlayListCursorAdapter(getActivity(), null);
        mListView = view.findViewById(R.id.playlists_listview);
        ImageView emptyView = view.findViewById(R.id.playlists_listview_empty_view);
        mListView.setEmptyView(emptyView);
        mListView.setAdapter(mCursorAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent editPlayList = new Intent(getActivity(), CreateNewPlayList.class);
                Uri currentPlayListUri = ContentUris.withAppendedId(PlayListEntry.CONTENT_URI, id);
                editPlayList.setData(currentPlayListUri);
                startActivity(editPlayList);
            }
        });
        super.onViewCreated(view, savedInstanceState);
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
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }


}
