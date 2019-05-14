package com.example.android.musicplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.this.getClass().getSimpleName();
    private static final int MY_PERMISSION_REQUEST = 1;
    private TextView mMarqueeText;
    private TextView mTimerText;
    private SeekBar mSeekBar;
    private TextView mTotalTime;
    private ImageView mPlayButton;
    private ImageView mNextButton;
    private ImageView mPreviousButton;
    private Handler mSeekHandler = new Handler();
    private Runnable mRun = new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
        }
    };
    private MediaPlayer mMediaPlayer;
    private int mMediaIndex = 0;
    private ArrayList<PlayList> mPlayList = new ArrayList<PlayList>();
    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            releaseMediaPlayer();
            if(mMediaIndex == mPlayList.size() - 1){
                mMediaIndex = 0;
                mMediaPlayer = setMediaPlayer(mMediaPlayer, mPlayList.get(mMediaIndex).getmMedia());
            }
            else{
                mMediaIndex++;
                mMediaPlayer = setMediaPlayer(mMediaPlayer, mPlayList.get(mMediaIndex).getmMedia());
            }
            mMediaPlayer.start();
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mSeekBar.setMax(mMediaPlayer.getDuration());
            updateTotalTime(mMediaPlayer.getDuration());
            updateMarqueeText(mPlayList.get(mMediaIndex));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }
            else{
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }
        }
        loadMusic();
        mSeekBar = (SeekBar)(findViewById(R.id.seekbar));
        mSeekBar.setEnabled(false);
        mMarqueeText = (TextView) findViewById(R.id.marquee_text);
        mMarqueeText.setText(getString(R.string.marquee_default));
        mMarqueeText.setSelected(true);
        mTimerText = findViewById(R.id.timer_text_view);
        mTotalTime = findViewById(R.id.total_time_text_view);

        PlayItemAdapter playItemAdapter = new PlayItemAdapter(this, mPlayList);
        ListView listView = (ListView) findViewById(R.id.play_list);
        listView.setAdapter(playItemAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mMediaPlayer != null){
                    mMediaPlayer.pause();
                    releaseMediaPlayer();
                }
                mMediaIndex = position;
                mMediaPlayer = setMediaPlayer(mMediaPlayer, mPlayList.get(mMediaIndex).getmMedia());
                mMediaPlayer.start();
                mPlayButton.setImageResource(R.drawable.baseline_pause_circle_outline_white_18dp);
                mMediaPlayer.setOnCompletionListener(mCompletionListener);
                updateMarqueeText(mPlayList.get(mMediaIndex));
                mSeekBar.setMax(mMediaPlayer.getDuration());
                updateTotalTime(mMediaPlayer.getDuration());
                updateSeekBar();
            }
        });

        mPlayButton = findViewById(R.id.play_btn);
        mPlayButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
                    mMediaPlayer.pause();
                    mPlayButton.setImageResource(R.drawable.baseline_play_circle_outline_white_18dp);
                    Toast.makeText(MainActivity.this, getString(R.string.toast_pause), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(mMediaPlayer != null && mMediaPlayer.getCurrentPosition() != 0) {
                    mMediaPlayer.start();
                    mPlayButton.setImageResource(R.drawable.baseline_pause_circle_outline_white_18dp);
                    Toast.makeText(MainActivity.this, getString(R.string.toast_continue), Toast.LENGTH_SHORT).show();
                }
                else if((mMediaPlayer == null && !mPlayList.isEmpty()) ||
                        (mMediaPlayer != null && mMediaPlayer.getCurrentPosition() == 0)){
                    releaseMediaPlayer();
                    Toast.makeText(MainActivity.this, getString(R.string.toast_playing), Toast.LENGTH_SHORT).show();
                    mMediaPlayer = setMediaPlayer(mMediaPlayer, mPlayList.get(mMediaIndex).getmMedia());
                    mMediaPlayer.start();
                    mPlayButton.setImageResource(R.drawable.baseline_pause_circle_outline_white_18dp);
                    Log.v(LOG_TAG, "Start Playing!!!" + mPlayList.get(mMediaIndex).getmSongName());
                    mMediaPlayer.setOnCompletionListener(mCompletionListener);
                    updateMarqueeText(mPlayList.get(mMediaIndex));
                    mSeekBar.setMax(mMediaPlayer.getDuration());
                    updateTotalTime(mMediaPlayer.getDuration());
                    updateSeekBar();
                }
                else{
                    Toast.makeText(MainActivity.this, getString(R.string.toast_no_song_found), Toast.LENGTH_LONG).show();
                }
            }
        });

        mNextButton = (findViewById(R.id.next_btn));
        mNextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mMediaPlayer == null)
                    return;
                Toast.makeText(MainActivity.this, getString(R.string.toast_next), Toast.LENGTH_SHORT).show();
                releaseMediaPlayer();
                if(mMediaIndex == mPlayList.size() - 1){
                    mMediaIndex = 0;
                    mMediaPlayer = setMediaPlayer(mMediaPlayer, mPlayList.get(mMediaIndex).getmMedia());
                }
                else{
                    mMediaIndex++;
                    mMediaPlayer = setMediaPlayer(mMediaPlayer, mPlayList.get(mMediaIndex).getmMedia());
                }
                mMediaPlayer.start();
                mPlayButton.setImageResource(R.drawable.baseline_pause_circle_outline_white_18dp);
                mMediaPlayer.setOnCompletionListener(mCompletionListener);
                updateMarqueeText(mPlayList.get(mMediaIndex));
                mSeekBar.setMax(mMediaPlayer.getDuration());
                updateTotalTime(mMediaPlayer.getDuration());
                updateSeekBar();
            }
        });

        mPreviousButton = (findViewById(R.id.previous_btn));
        mPreviousButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mMediaPlayer == null)
                    return;
                Toast.makeText(MainActivity.this, getString(R.string.toast_previous), Toast.LENGTH_SHORT).show();
                releaseMediaPlayer();
                if(mMediaIndex == 0){
                    mMediaIndex = mPlayList.size() - 1;
                    mMediaPlayer = setMediaPlayer(mMediaPlayer, mPlayList.get(mMediaIndex).getmMedia());
                }
                else{
                    mMediaIndex--;
                    mMediaPlayer = setMediaPlayer(mMediaPlayer, mPlayList.get(mMediaIndex).getmMedia());
                }
                mMediaPlayer.start();
                mPlayButton.setImageResource(R.drawable.baseline_pause_circle_outline_white_18dp);
                mMediaPlayer.setOnCompletionListener(mCompletionListener);
                updateMarqueeText(mPlayList.get(mMediaIndex));
                mSeekBar.setMax(mMediaPlayer.getDuration());
                updateTotalTime(mMediaPlayer.getDuration());
                updateSeekBar();
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mMediaPlayer == null)
                    return;
                seekBar.setEnabled(true);
                if(fromUser){
                    mMediaPlayer.seekTo(progress);
                    updateTimer(mMediaPlayer.getCurrentPosition());
                }
                if(!fromUser)
                    updateTimer(mMediaPlayer.getCurrentPosition());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(mMediaPlayer != null)
                    mMediaPlayer.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(mMediaPlayer != null){
                    mMediaPlayer.start();
                    mPlayButton.setImageResource(R.drawable.baseline_pause_circle_outline_white_18dp);
                }
            }
        });
    }

    private void releaseMediaPlayer(){
        if(mMediaPlayer != null){
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void updateMarqueeText(PlayList playItem){
        String str = playItem.getmSongName();
        mMarqueeText.setText(str + "          ");
        updateTimer(mMediaPlayer.getCurrentPosition());
    }

    private void updateSeekBar(){
        mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
        mSeekHandler.postDelayed(mRun, 1000);
    }

    private void updateTimer(int position){
        int p = position/1000;
        int min = p/60;
        Log.v(LOG_TAG, "min: " + min);
        String minute = Integer.toString(min);
        if(min < 10){
            String temp = minute;
            minute = "0";
            minute += temp;
        }
        Log.v(LOG_TAG, "minute: " + minute);
        int sec = p%60;
        Log.v(LOG_TAG, "sec: " + sec);
        String second = Integer.toString(sec);
        if(sec < 10){
            String temp = second;
            second = "0";
            second += temp;
        }
        Log.v(LOG_TAG, "second: " + second);
        mTimerText.setText(minute + " : " + second);
    }

    private void updateTotalTime(int duration){
        int totalTime = duration/1000;
        int min = totalTime/60;
        int sec = totalTime%60;
        String minute = Integer.toString(min);
        String second = Integer.toString(sec);
        if(min < 10){
            String temp = minute;
            minute = "0";
            minute += temp;
        }
        if(sec < 10){
            String temp = second;
            second = "0";
            second += temp;
        }
        mTotalTime.setText(minute + " : " + second);
    }

    public void loadMusic(){
        ContentResolver musicContentResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicContentResolver.query(musicUri, null, null, null, null);
        if(musicCursor == null){
            Toast.makeText(getApplicationContext(), getString(R.string.load_music_failed), Toast.LENGTH_LONG).show();
            return;
        }
        else if(!musicCursor.moveToFirst()){
            Toast.makeText(getApplicationContext(), getString(R.string.toast_no_song_found), Toast.LENGTH_LONG).show();
            return;
        }
        else{
            do{
                long songId = musicCursor.getLong(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                String songName = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                mPlayList.add(new PlayList(songId, songName));
            } while(musicCursor.moveToNext());
        }
    }

    public MediaPlayer setMediaPlayer(MediaPlayer mediaPlayer, long songId) {
        Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId);
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(getApplicationContext(), songUri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mediaPlayer;
    }
}

