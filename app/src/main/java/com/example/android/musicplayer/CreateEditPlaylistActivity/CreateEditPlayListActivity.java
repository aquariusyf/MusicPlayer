package com.example.android.musicplayer.CreateEditPlaylistActivity;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.content.Loader;
import android.content.CursorLoader;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.musicplayer.AddSongsToPlaylistActivity.AddSongsActivity;
import com.example.android.musicplayer.MainFragmentPlayList;
import com.example.android.musicplayer.PlayList;
import com.example.android.musicplayer.PlayListDataBase.PlayListContract.PlayListEntry;
import com.example.android.musicplayer.PlayListDataBase.PlayListDbHelper;
import com.example.android.musicplayer.R;

import java.util.ArrayList;
import java.util.Collections;

public class CreateEditPlayListActivity extends AppCompatActivity {

    private final String LOG_TAG = CreateEditPlayListActivity.this.getClass().getSimpleName();
    private final int CURRENT_PLAYLIST_LOADER = 1;
    private final int CREATE_NEW = 0;
    private final int EDIT = 1;
    private int state;
    private TextView mTitle;
    private EditText mInputName;
    private ImageView mConfirmButton;
    private ImageView mCancelButton;
    private TextView mAddSongsButton;
    private ListView mMediaListView;

    private PlayListDbHelper mDbHelper;
    private static String mMediaIds = "";
    private static String mAddedMediaIds = "";
    private long[] mIds;
    private static ArrayList<Integer> mDeleteButtonState = new ArrayList<>();
    private static ArrayList<PlayList> mCurrentPlayList = new ArrayList<>();
    private ArrayList<PlayList> mAddedPlayList = new ArrayList<>();
    private static boolean mIsFromAddSongs;
    private CreatePlayListMediaListAdapter mAdapter;
    private Uri mCurrentPlayListUri;
    private LoaderManager.LoaderCallbacks<Cursor> mCurrentPlayListCallBack
            = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            String[] projection = {PlayListEntry._ID,
                    PlayListEntry.COLUMN_PLAYLIST_NAME,
                    PlayListEntry.COLUMN_MEDIA_ID};
            if(mCurrentPlayListUri == null)
                return null;
            return new CursorLoader(getBaseContext(),
                                    mCurrentPlayListUri,
                                    projection,
                                   null,
                                   null,
                                   null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            if(cursor == null || cursor.getCount() < 1)
                return;

            if(cursor.moveToFirst()){
                String currentPlayListName = cursor.getString(cursor.getColumnIndexOrThrow(PlayListEntry.COLUMN_PLAYLIST_NAME));
                mMediaIds = cursor.getString(cursor.getColumnIndexOrThrow(PlayListEntry.COLUMN_MEDIA_ID));
                convertIdFromStringToLong(mMediaIds);
                if(state == EDIT)
                    loadSongList(mCurrentPlayList);
                setDeleteButtonState();
                Log.v(LOG_TAG, "currentPlayListName: " + currentPlayListName);
                mInputName.setText(currentPlayListName);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mInputName.setText("");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_play_list);

        final Intent intent = getIntent();
        mCurrentPlayListUri = intent.getData();
        Log.v(LOG_TAG, "Uri: " + mCurrentPlayListUri);
        mTitle = findViewById(R.id.play_list_editor_titile);
        if(mCurrentPlayListUri == null){
            state = CREATE_NEW;
            mTitle.setText("New Playlist");
        }
        else{
            state = EDIT;
            mTitle.setText("Edit");
        }

        getLoaderManager().initLoader(CURRENT_PLAYLIST_LOADER, null, mCurrentPlayListCallBack);

        mInputName = findViewById(R.id.input_name_edit_text);
        mCancelButton = findViewById(R.id.cancel_create);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mConfirmButton = findViewById(R.id.confirm_create);
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(savePlayList())
                    finish();
            }
        });

        mAddSongsButton = findViewById(R.id.add_songs_button);
        mAddSongsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addSongs = new Intent(CreateEditPlayListActivity.this, AddSongsActivity.class);
                mAddedMediaIds = "";
                mAddedPlayList = new ArrayList<>();
                startActivity(addSongs);
            }
        });

        mDbHelper = new PlayListDbHelper(this);
        mMediaListView = findViewById(R.id.media_pool_for_create);
        mAdapter = new CreatePlayListMediaListAdapter(this, mCurrentPlayList);
        mMediaListView.setAdapter(mAdapter);
        mMediaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteButton(position);
            }
        });

        Log.v(LOG_TAG, "Current media list before add songs: " + mCurrentPlayList);
        Log.v(LOG_TAG, "Media list before add songs: " + mMediaIds);
    }

    @Override
    protected void onDestroy() {
        mMediaIds = "";
        mAddedMediaIds = "";
        mCurrentPlayList = new ArrayList<>();
        mDeleteButtonState = new ArrayList<>();
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        if(mIsFromAddSongs && !mAddedMediaIds.isEmpty()) {
            super.onRestart();
            convertIdFromStringToLong(mAddedMediaIds);
            loadSongList(mAddedPlayList);
            mAdapter.addAll(mAddedPlayList);
            mMediaIds += mAddedMediaIds;
            setDeleteButtonState();
            Log.v(LOG_TAG, "Current media list after add songs: " + mCurrentPlayList);
            Log.v(LOG_TAG, "Media list after add songs: " + mMediaIds);
            mIsFromAddSongs = false;
            return;
        }
        super.onRestart();
    }

    public static void setIsFromAddSongsTrue() {
        mIsFromAddSongs = true;
    }

    private boolean savePlayList(){
        String nameField = "";
        String mediaIdField = mMediaIds;
        if(!TextUtils.isEmpty(mInputName.getText().toString().trim()))
            nameField = mInputName.getText().toString().trim();

        if(TextUtils.isEmpty(nameField)){
            Toast.makeText(getApplicationContext(), getString(R.string.empty_playlist_name_prompt), Toast.LENGTH_LONG).show();
            return false;
        }

        ContentValues newEntry = new ContentValues();
        newEntry.put(PlayListEntry.COLUMN_PLAYLIST_NAME, nameField);
        newEntry.put(PlayListEntry.COLUMN_MEDIA_ID, mediaIdField);

        if(mCurrentPlayListUri == null){
            Uri newPlayListUri = getContentResolver().insert(PlayListEntry.CONTENT_URI, newEntry);
            MainFragmentPlayList.addSelectedStateForNewPlaylist();
            if(newPlayListUri != null)
                Toast.makeText(getApplicationContext(), getString(R.string.playlist_added), Toast.LENGTH_LONG).show();
        }
        else{
            String selection = PlayListEntry._ID;
            String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(mCurrentPlayListUri))};
            int updatePlayList = getContentResolver().update(mCurrentPlayListUri, newEntry, selection, selectionArgs);
        }
        return true;
    }

    private void showDeleteButton(int pos) {
        int firstVisiblePosition = mMediaListView.getFirstVisiblePosition();
        int position = pos - firstVisiblePosition;
        View thisItem = mMediaListView.getChildAt(position);
        TextView deleteButton = thisItem.findViewById(R.id.delete_song_from_playlist_button);
        if(mDeleteButtonState.get(pos) == View.GONE){
            mDeleteButtonState.set(pos, View.VISIBLE);
            deleteButton.setVisibility(mDeleteButtonState.get(pos));
        }
        else {
            mDeleteButtonState.set(pos, View.GONE);
            deleteButton.setVisibility(mDeleteButtonState.get(pos));
        }
    }

    public static void deleteSong(int pos) {
        int position = pos;
        String id = Long.toString(mCurrentPlayList.get(position).getmMedia());
        id += " ";
        if(mMediaIds.contains(id)){
            mMediaIds =  mMediaIds.replaceFirst(id, "");
        }
        mCurrentPlayList.remove(position);
        mDeleteButtonState.remove(position);
    }

    private void setDeleteButtonState(){
        mDeleteButtonState = new ArrayList<>(Collections.nCopies(mCurrentPlayList.size(), View.GONE));
    }

    public static void getAddedSongIds(String ids) {
        mAddedMediaIds = ids;
    }

    private void loadSongList(ArrayList<PlayList> playlist) {

        if(mIds == null)
            return;

        ContentResolver songContentResolver = getContentResolver();
        for(int j = 0; j < mIds.length; j++){
            Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mIds[j]);
            Cursor songCursor = songContentResolver.query(songUri, null, null, null, null);

            if(!songCursor.moveToFirst()) {
                return;
            }

            long songId = songCursor.getLong(songCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
            String songName = songCursor.getString(songCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            String artistName = songCursor.getString(songCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            if(artistName.isEmpty() || artistName == null || artistName.equals("<unknown>")) {
                artistName = "Unknown Artist";
            }

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(getApplicationContext(),songUri);
            String songDurationRaw = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            String songDuration = getDuration(songDurationRaw);
            byte[] albumImageRawData = mmr.getEmbeddedPicture();
            Bitmap albumImageBitmap = null;
            if(albumImageRawData != null)
                albumImageBitmap = BitmapFactory.decodeByteArray(albumImageRawData, 0, albumImageRawData.length);
            else
                albumImageBitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.new_default_album_icon);
            playlist.add(new PlayList(songId, songName, artistName, songDuration, albumImageBitmap));
        }

    }

    private String getDuration(String rawData) {
        int raw = Integer.parseInt(rawData);
        raw /= 1000;
        int min = raw / 60;
        int sec = raw % 60;
        String minute = Integer.toString(min);
        String second = Integer.toString(sec);
        if (min < 10) {
            String temp = minute;
            minute = "0";
            minute += temp;
        }
        if (sec < 10) {
            String temp = second;
            second = "0";
            second += temp;
        }
        return minute + " : " + second;
    }

    private void convertIdFromStringToLong(String idStr){
        if(idStr.isEmpty() || idStr == null)
            return;
        String[] idString = idStr.split(" ");
        mIds = new long[idString.length];
        for(int i = 0; i < idString.length; i++){
            mIds[i] = Long.parseLong(idString[i]);
        }
    }
}
