package com.example.android.musicplayer.PlayListDataBase;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.musicplayer.CreateEditPlaylistActivity.CreateEditPlayListActivity;
import com.example.android.musicplayer.MainActivity;
import com.example.android.musicplayer.MainFragmentPlayList;
import com.example.android.musicplayer.PlayListDataBase.PlayListContract.PlayListEntry;
import com.example.android.musicplayer.R;

import java.util.ArrayList;

public class PlayListCursorAdapter extends CursorAdapter {

    public static boolean mShowDeleteButton = false;
    private Context mContext = null;

    public PlayListCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        mContext = context;
        return LayoutInflater.from(context).inflate(R.layout.single_playlist_view, parent, false);
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        TextView name = view.findViewById(R.id.single_playlist_name);
        final String playListName = cursor.getString(cursor.getColumnIndexOrThrow(PlayListEntry.COLUMN_PLAYLIST_NAME));
        name.setText(playListName);
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(PlayListEntry._ID));
        final Uri currentPlayListUri = ContentUris.withAppendedId(PlayListEntry.CONTENT_URI, id);
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editPlayList = new Intent(mContext, CreateEditPlayListActivity.class);
                editPlayList.setData(currentPlayListUri);
                mContext.startActivity(editPlayList);
            }
        });

        TextView numOfSongs = view.findViewById(R.id.number_of_songs_in_playlist);
        String songIdsRawData = cursor.getString(cursor.getColumnIndexOrThrow(PlayListEntry.COLUMN_MEDIA_ID));
        String[] songIds = songIdsRawData.split(" ");
        if(songIds == null || (songIds.length == 1 && songIds[0].isEmpty()) || songIds.length == 0)
            numOfSongs.setText(mContext.getString(R.string.no_songs));
        else
            numOfSongs.setText(songIds.length + " Songs");

        final ImageView deleteButton = view.findViewById(R.id.delete_button);
        final ArrayList<Boolean> mSelectedForDeletion = MainFragmentPlayList.getSelectState();
        final int position = cursor.getPosition();
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSelectedForDeletion.get(position)) {
                    deleteButton.setImageResource(R.mipmap.delete_playlist_unselected_icon);
                    MainFragmentPlayList.setSelectedState(position, false);
                }
                else {
                    deleteButton.setImageResource(R.mipmap.delete_playlist_selected_icon);
                    MainFragmentPlayList.setSelectedState(position, true);
                }
            }
        });

        Button playButton = view.findViewById(R.id.play_this_list_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getConsoleFragment().changePlaylist(currentPlayListUri);
            }
        });

        if(mShowDeleteButton)
            deleteButton.setVisibility(View.VISIBLE);
        else {
            deleteButton.setVisibility(View.GONE);
            deleteButton.setImageResource(R.mipmap.delete_playlist_unselected_icon);
        }
    }

    public static void setDeleteButtonState(boolean state) {
        mShowDeleteButton = state;
    }

}
