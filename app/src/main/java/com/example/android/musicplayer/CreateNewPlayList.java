package com.example.android.musicplayer;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.content.Loader;
import android.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.musicplayer.PlayListDataBase.PlayListContract.PlayListEntry;
import com.example.android.musicplayer.PlayListDataBase.PlayListDbHelper;

import javax.xml.transform.URIResolver;

public class CreateNewPlayList extends AppCompatActivity {

    private final String LOG_TAG = CreateNewPlayList.this.getClass().getSimpleName();
    private final int CURRENT_PLAYLIST_LOADER = 1;
    private TextView mTitile;
    private EditText mInputName;
    private ImageView mConfirmButton;
    private ImageView mCancelButton;
    private ImageView mDelete;

    private PlayListDbHelper mDbHelper;
    private long newPlayListId;
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

        Intent intent = getIntent();
        mCurrentPlayListUri = intent.getData();
        Log.v(LOG_TAG, "Uri: " + mCurrentPlayListUri);
        mTitile = findViewById(R.id.play_list_editor_titile);
        if(mCurrentPlayListUri == null)
            mTitile.setText("New Playlist");
        else
            mTitile.setText("Edit");
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
                savePlayList();
                finish();
            }
        });

        mDelete = findViewById(R.id.delete_playlist);
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePlayList();
                finish();
            }
        });

        mDbHelper = new PlayListDbHelper(this);
    }

    private void savePlayList(){
        String nameField = mInputName.getText().toString().trim();
        String mediaIdField = "Sample Media ID";

        if(TextUtils.isEmpty(nameField) && TextUtils.isEmpty(mediaIdField))
            return;
        if(!TextUtils.isEmpty(mInputName.getText().toString().trim()))
            nameField = mInputName.getText().toString().trim();

        ContentValues newEntry = new ContentValues();
        newEntry.put(PlayListEntry.COLUMN_PLAYLIST_NAME, nameField);
        newEntry.put(PlayListEntry.COLUMN_MEDIA_ID, mediaIdField);

        if(mCurrentPlayListUri == null){
            Uri newPlayListUri = getContentResolver().insert(PlayListEntry.CONTENT_URI, newEntry);
            if(newPlayListUri != null)
                Toast.makeText(getApplicationContext(), "New PlayList added!", Toast.LENGTH_LONG).show();
        }
        else{
            String selection = PlayListEntry._ID;
            String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(mCurrentPlayListUri))};
            int updatePlayList = getContentResolver().update(mCurrentPlayListUri, newEntry, selection, selectionArgs);
        }
    }

    private void deletePlayList() {
        int rowsDeleted = getContentResolver().delete(mCurrentPlayListUri, null, null);
        if(rowsDeleted >= 1)
            Toast.makeText(getApplicationContext(), "PlayList deleted!", Toast.LENGTH_LONG).show();
    }
}
