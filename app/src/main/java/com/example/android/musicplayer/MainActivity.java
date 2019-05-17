package com.example.android.musicplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
    private ListView mListView;
    private TextView mMarqueeText;
    private TextView mTimerText;
    private SeekBar mSeekBar;
    private TextView mTotalTime;
    private SeekBar mVolumeSeekBar;
    private AudioManager mAudioManager;
    private TextView mVolume;
    private ImageView mMute;
    private int mPreviousVolume;
    private boolean mIsMute;
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
            switch (mPlayState){
                case REPEAT_ALL:
                    updatePlayingItemDisplay(mMediaIndex, mMediaIndex + 1);
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
                    break;
                case REPEAT_ONE:
                    mMediaPlayer = setMediaPlayer(mMediaPlayer, mPlayList.get(mMediaIndex).getmMedia());
                    mMediaPlayer.start();
                    mMediaPlayer.setOnCompletionListener(mCompletionListener);
                    mSeekBar.setMax(mMediaPlayer.getDuration());
                    updateTotalTime(mMediaPlayer.getDuration());
                    updateMarqueeText(mPlayList.get(mMediaIndex));
                    break;
                case SHUFFLE:
                    int oldIndex = mMediaIndex;
                    setShuffleIndex();
                    updatePlayingItemDisplay(oldIndex, mMediaIndex);
                    mMediaPlayer = setMediaPlayer(mMediaPlayer, mPlayList.get(mMediaIndex).getmMedia());
                    mMediaPlayer.start();
                    mMediaPlayer.setOnCompletionListener(mCompletionListener);
                    mSeekBar.setMax(mMediaPlayer.getDuration());
                    updateTotalTime(mMediaPlayer.getDuration());
                    updateMarqueeText(mPlayList.get(mMediaIndex));
                    break;
                default: break;
            }
        }
    };
    private ImageView mRepeat;
    private ImageView mShuffle;
    private int mPlayState;
    private static final int REPEAT_ALL = 0;
    private static final int REPEAT_ONE = 1;
    private static final int SHUFFLE = 2;
    private int mShuffleScope;
    private int mShuffleSelector;

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
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        initVolumeSeekBar();
        initRepeatAndShuffleButton();
        loadMusic();
        initShuffleSeed();
        mSeekBar = (findViewById(R.id.seekbar));
        mSeekBar.setEnabled(false);
        mMarqueeText = findViewById(R.id.marquee_text);
        mMarqueeText.setText(getString(R.string.marquee_default));
        mMarqueeText.setSelected(true);
        mTimerText = findViewById(R.id.timer_text_view);
        mTotalTime = findViewById(R.id.total_time_text_view);
        PlayItemAdapter playItemAdapter = new PlayItemAdapter(this, mPlayList);
        mListView = findViewById(R.id.play_list);
        mListView.setAdapter(playItemAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mMediaPlayer != null){
                    mMediaPlayer.pause();
                    releaseMediaPlayer();
                }
                updatePlayingItemDisplay(mMediaIndex, position);
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
                    updatePlayingItemDisplay(mPlayList.size() - 1, mMediaIndex);
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
                releaseMediaPlayer();
                int oldIndex = mMediaIndex;
                switch (mPlayState){
                    case REPEAT_ALL:
                        if(mMediaIndex == mPlayList.size() - 1){
                            mMediaIndex = 0;
                            mMediaPlayer = setMediaPlayer(mMediaPlayer, mPlayList.get(mMediaIndex).getmMedia());
                        }
                        else{
                            mMediaIndex++;
                            mMediaPlayer = setMediaPlayer(mMediaPlayer, mPlayList.get(mMediaIndex).getmMedia());
                        }
                        updatePlayingItemDisplay(oldIndex, mMediaIndex);
                        mMediaPlayer.start();
                        mPlayButton.setImageResource(R.drawable.baseline_pause_circle_outline_white_18dp);
                        mMediaPlayer.setOnCompletionListener(mCompletionListener);
                        updateMarqueeText(mPlayList.get(mMediaIndex));
                        mSeekBar.setMax(mMediaPlayer.getDuration());
                        updateTotalTime(mMediaPlayer.getDuration());
                        updateSeekBar();
                        Toast.makeText(MainActivity.this, getString(R.string.toast_next), Toast.LENGTH_SHORT).show();
                        break;
                    case REPEAT_ONE:
                        mMediaPlayer = setMediaPlayer(mMediaPlayer, mPlayList.get(mMediaIndex).getmMedia());
                        mMediaPlayer.start();
                        mPlayButton.setImageResource(R.drawable.baseline_pause_circle_outline_white_18dp);
                        mMediaPlayer.setOnCompletionListener(mCompletionListener);
                        updateMarqueeText(mPlayList.get(mMediaIndex));
                        mSeekBar.setMax(mMediaPlayer.getDuration());
                        updateTotalTime(mMediaPlayer.getDuration());
                        updateSeekBar();
                        break;
                    case SHUFFLE:
                        setShuffleIndex();
                        updatePlayingItemDisplay(oldIndex, mMediaIndex);
                        mMediaPlayer = setMediaPlayer(mMediaPlayer, mPlayList.get(mMediaIndex).getmMedia());
                        mMediaPlayer.start();
                        mPlayButton.setImageResource(R.drawable.baseline_pause_circle_outline_white_18dp);
                        mMediaPlayer.setOnCompletionListener(mCompletionListener);
                        updateMarqueeText(mPlayList.get(mMediaIndex));
                        mSeekBar.setMax(mMediaPlayer.getDuration());
                        updateTotalTime(mMediaPlayer.getDuration());
                        updateSeekBar();
                        break;
                    default: break;
                }
            }
        });

        mPreviousButton = (findViewById(R.id.previous_btn));
        mPreviousButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mMediaPlayer == null)
                    return;
                releaseMediaPlayer();
                int oldIndex = mMediaIndex;
                switch (mPlayState){
                    case REPEAT_ALL:
                        if(mMediaIndex == 0){
                            mMediaIndex = mPlayList.size() - 1;
                            mMediaPlayer = setMediaPlayer(mMediaPlayer, mPlayList.get(mMediaIndex).getmMedia());
                        }
                        else{
                            mMediaIndex--;
                            mMediaPlayer = setMediaPlayer(mMediaPlayer, mPlayList.get(mMediaIndex).getmMedia());
                        }
                        updatePlayingItemDisplay(oldIndex, mMediaIndex);
                        mMediaPlayer.start();
                        mPlayButton.setImageResource(R.drawable.baseline_pause_circle_outline_white_18dp);
                        mMediaPlayer.setOnCompletionListener(mCompletionListener);
                        updateMarqueeText(mPlayList.get(mMediaIndex));
                        mSeekBar.setMax(mMediaPlayer.getDuration());
                        updateTotalTime(mMediaPlayer.getDuration());
                        updateSeekBar();
                        Toast.makeText(MainActivity.this, getString(R.string.toast_previous), Toast.LENGTH_SHORT).show();
                        break;
                    case REPEAT_ONE:
                        mMediaPlayer = setMediaPlayer(mMediaPlayer, mPlayList.get(mMediaIndex).getmMedia());
                        mMediaPlayer.start();
                        mPlayButton.setImageResource(R.drawable.baseline_pause_circle_outline_white_18dp);
                        mMediaPlayer.setOnCompletionListener(mCompletionListener);
                        updateMarqueeText(mPlayList.get(mMediaIndex));
                        mSeekBar.setMax(mMediaPlayer.getDuration());
                        updateTotalTime(mMediaPlayer.getDuration());
                        updateSeekBar();
                        break;
                    case SHUFFLE:
                        setShuffleIndex();
                        updatePlayingItemDisplay(oldIndex, mMediaIndex);
                        mMediaPlayer = setMediaPlayer(mMediaPlayer, mPlayList.get(mMediaIndex).getmMedia());
                        mMediaPlayer.start();
                        mPlayButton.setImageResource(R.drawable.baseline_pause_circle_outline_white_18dp);
                        mMediaPlayer.setOnCompletionListener(mCompletionListener);
                        updateMarqueeText(mPlayList.get(mMediaIndex));
                        mSeekBar.setMax(mMediaPlayer.getDuration());
                        updateTotalTime(mMediaPlayer.getDuration());
                        updateSeekBar();
                        break;
                    default: break;
                }
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
                String artistName = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                if(artistName.isEmpty() || artistName == null || artistName.equals("<unknown>")) {
                    artistName = "Unknown Artist";
                }
                Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId);
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(getApplicationContext(),songUri);
                String songDurationRaw = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                String songDuration = getDuration(songDurationRaw);
                byte[] albumImageRawData = mmr.getEmbeddedPicture();
                Bitmap albumImageBitmap = null;
                if(albumImageRawData != null)
                    albumImageBitmap = BitmapFactory.decodeByteArray(albumImageRawData, 0, albumImageRawData.length);
                mPlayList.add(new PlayList(songId, songName, artistName, songDuration, albumImageBitmap));
            } while(musicCursor.moveToNext());
        }
    }

    private void initVolumeSeekBar(){
        try{
            mPreviousVolume = 5;
            mIsMute = false;
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mVolumeSeekBar = findViewById(R.id.volume_seekbar);
            mMute = findViewById(R.id.mute_image_view);
            mVolume = findViewById(R.id.volume_text_view);
            mVolumeSeekBar.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            mVolumeSeekBar.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            mVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                    updateVolumeText();
                    setVolumeIcon();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            mMute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mIsMute)
                        setUnmute();
                    else
                        setMute();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        updateVolumeText();
        setVolumeIcon();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(mAudioManager == null)
            return super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            try{
                mVolumeSeekBar.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) - 1);
            } catch (Error e) {

            }
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            try{
                mVolumeSeekBar.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + 1);
            } catch (Error e) {

            }
        }
        updateVolumeText();
        setVolumeIcon();
        return super.onKeyDown(keyCode, event);
    }

    private void setVolumeIcon(){
        if(mVolumeSeekBar.getProgress() == 0){
            mMute.setImageResource(R.drawable.volume_off_icon);
            mIsMute = true;
        }

        else{
            mMute.setImageResource(R.drawable.volume_on_icon);
            mIsMute = false;
        }
    }

    private void setMute(){
        if(mIsMute)
            return;
        mPreviousVolume = mVolumeSeekBar.getProgress();
        mVolumeSeekBar.setProgress(0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        setVolumeIcon();
        Toast.makeText(getApplicationContext(), getString(R.string.mute), Toast.LENGTH_LONG).show();
    }

    private void setUnmute(){
        if(!mIsMute)
            return;
        mVolumeSeekBar.setProgress(mPreviousVolume);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mPreviousVolume, 0);
        setVolumeIcon();
        mPreviousVolume = 5;
        Toast.makeText(getApplicationContext(), getString(R.string.unmute), Toast.LENGTH_LONG).show();
    }

    private void updateVolumeText(){
        String volume = new String();
        double currentVolume = (double) mVolumeSeekBar.getProgress();
        double maxVolume = (double) mVolumeSeekBar.getMax();
        Log.v(LOG_TAG, "current volume: " + currentVolume);
        Log.v(LOG_TAG, "Max volume: " + maxVolume);
        int percentage = (int) (currentVolume/maxVolume * 100);
        Log.v(LOG_TAG, "Percentage: " + percentage);
        volume = Integer.toString(percentage);
        volume += "%";
        mVolume.setText(volume);
    }

    private void initRepeatAndShuffleButton(){
        mPlayState = REPEAT_ALL;
        mRepeat = findViewById(R.id.repeat_button);
        mShuffle = findViewById(R.id.shuffle_button);
        mRepeat.setBackgroundResource(R.drawable.round_shape_selected);
        mShuffle.setBackgroundResource(R.drawable.round_shape_not_selected);

        mRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRepeatState();
            }
        });

        mShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setShuffle();
            }
        });
    }

    private void setRepeatState(){
        switch (mPlayState){
            case REPEAT_ALL:
                mPlayState = REPEAT_ONE;
                mRepeat.setImageResource(R.drawable.repeat_one_icon);
                break;
            case REPEAT_ONE:
                mPlayState = REPEAT_ALL;
                mRepeat.setImageResource(R.drawable.repeat_all_icon);
                break;
            case SHUFFLE:
                mPlayState = REPEAT_ALL;
                mRepeat.setImageResource(R.drawable.repeat_all_icon);
                mRepeat.setBackgroundResource(R.drawable.round_shape_selected);
                mShuffle.setBackgroundResource(R.drawable.round_shape_not_selected);
                break;
            default: break;
        }
    }

    private void setShuffle(){
        switch (mPlayState){
            case REPEAT_ALL:
                mPlayState = SHUFFLE;
                mRepeat.setImageResource(R.drawable.repeat_all_icon);
                mRepeat.setBackgroundResource(R.drawable.round_shape_not_selected);
                mShuffle.setBackgroundResource(R.drawable.round_shape_selected);
                break;
            case REPEAT_ONE:
                mPlayState = SHUFFLE;
                mRepeat.setImageResource(R.drawable.repeat_all_icon);
                mRepeat.setBackgroundResource(R.drawable.round_shape_not_selected);
                mShuffle.setBackgroundResource(R.drawable.round_shape_selected);
                break;
            default: break;
        }
    }

    private void initShuffleSeed(){
        mShuffleScope = mPlayList.size();
        mShuffleSelector = mShuffleScope/2;
    }

    private void setShuffleIndex(){
        do{
            mShuffleSelector = (int) (Math.random() * mShuffleScope);
        } while(mShuffleSelector == mMediaIndex);
        mMediaIndex = mShuffleSelector;
    }

    private void updatePlayingItemDisplay(int currentPosition, int newPosition){
        if(currentPosition == newPosition)
            return;
        if((currentPosition < 0 || currentPosition > mPlayList.size() - 1) ||
                (newPosition < 0 || newPosition > mPlayList.size() - 1)){
            Log.v(LOG_TAG, "Index Out of Bound: " + currentPosition + ", " + newPosition);
            return;
        }
        mPlayList.get(currentPosition).setPlayingState("");
        mPlayList.get(newPosition).setPlayingState(getString(R.string.playing_indicator));
        if(currentPosition >= mListView.getFirstVisiblePosition() && currentPosition <= mListView.getLastVisiblePosition()){
            TextView oldItemTextView = mListView.getChildAt(currentPosition - mListView.getFirstVisiblePosition()).findViewById(R.id.is_playing);
            oldItemTextView.setText(mPlayList.get(currentPosition).getPlayingState());
        }
        if(newPosition >= mListView.getFirstVisiblePosition() && newPosition <= mListView.getLastVisiblePosition()){
            TextView newItemTextView = mListView.getChildAt(newPosition - mListView.getFirstVisiblePosition()).findViewById(R.id.is_playing);
            newItemTextView.setText(mPlayList.get(newPosition).getPlayingState());
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

