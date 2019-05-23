package com.example.android.musicplayer.PlayListDataBase;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.musicplayer.PlayListDataBase.PlayListContract;
import com.example.android.musicplayer.PlayListDataBase.PlayListContract.PlayListEntry;
import com.example.android.musicplayer.R;

public class PlayListCursorAdapter extends CursorAdapter {

    public PlayListCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.single_playlist_view, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView name = view.findViewById(R.id.single_playlist_name);
        String playListName = cursor.getString(cursor.getColumnIndexOrThrow(PlayListEntry.COLUMN_PLAYLIST_NAME));
        name.setText(playListName);
    }
}
