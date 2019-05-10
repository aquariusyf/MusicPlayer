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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSION_REQUEST = 1;
    private TextView marqueeText;
    private TextView timerText;
    private SeekBar seekBar;
    private Handler seekHandler = new Handler();
    private Runnable run = new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
        }
    };
    private MediaPlayer mediaPlayer;
    private int mediaIndex = 0;
    private ArrayList<PlayList> playList = new ArrayList<PlayList>();
    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            releaseMediaPlayer();
            if(mediaIndex == playList.size() - 1){
                mediaIndex = 0;
                mediaPlayer = setMediaPlayer(mediaPlayer, playList.get(mediaIndex).getmMedia());
            }
            else{
                mediaIndex++;
                mediaPlayer = setMediaPlayer(mediaPlayer, playList.get(mediaIndex).getmMedia());
            }
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mCompletionListener);
            seekBar.setMax(mediaPlayer.getDuration());
            updateMarqueeText(playList.get(mediaIndex));
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
        seekBar = (SeekBar)(findViewById(R.id.seekbar));
        seekBar.setEnabled(false);
        marqueeText = (TextView) findViewById(R.id.marquee_text);
        marqueeText.setText("------");
        marqueeText.setSelected(true);
        timerText = findViewById(R.id.timer_text_view);

        PlayItemAdapter playItemAdapter = new PlayItemAdapter(this, playList);
        ListView listView = (ListView) findViewById(R.id.play_list);
        listView.setAdapter(playItemAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mediaPlayer != null){
                    mediaPlayer.pause();
                    releaseMediaPlayer();
                }
                mediaIndex = position;
                mediaPlayer = setMediaPlayer(mediaPlayer, playList.get(mediaIndex).getmMedia());
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mCompletionListener);
                updateMarqueeText(playList.get(mediaIndex));
                seekBar.setMax(mediaPlayer.getDuration());
                updateSeekBar();
            }
        });

        final ImageView playButton = (ImageView) findViewById(R.id.play_btn);
        playButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mediaPlayer != null && mediaPlayer.isPlaying())
                    return;
                if(mediaPlayer != null && mediaPlayer.getCurrentPosition() != 0) {
                    mediaPlayer.start();
                    Toast.makeText(MainActivity.this, "Continue", Toast.LENGTH_SHORT).show();
                }
                else if((mediaPlayer == null && !playList.isEmpty()) ||
                        (mediaPlayer != null && mediaPlayer.getCurrentPosition() == 0)){
                    releaseMediaPlayer();
                    Toast.makeText(MainActivity.this, "Playing", Toast.LENGTH_SHORT).show();
                    mediaPlayer = setMediaPlayer(mediaPlayer, playList.get(mediaIndex).getmMedia());
                    mediaPlayer.start();
                    Log.v("MainActivity", "Start Playing!!!");
                    mediaPlayer.setOnCompletionListener(mCompletionListener);
                    updateMarqueeText(playList.get(mediaIndex));
                    seekBar.setMax(mediaPlayer.getDuration());
                    updateSeekBar();
                }
                else{
                    Toast.makeText(MainActivity.this, "No songs found!", Toast.LENGTH_LONG).show();
                }
            }
        });

        ImageView pauseButton = (ImageView) findViewById(R.id.pause_btn);
        pauseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    Toast.makeText(MainActivity.this, "Paused", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageView stopButton = (ImageView) findViewById(R.id.stop_btn);
        stopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(mediaPlayer != null){
                    Toast.makeText(MainActivity.this, "STOPPED", Toast.LENGTH_SHORT).show();
                    mediaPlayer.seekTo(0);
                    mediaPlayer.pause();
                }
            }
        });

        ImageView nextButton = (ImageView) (findViewById(R.id.next_btn));
        nextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mediaPlayer == null)
                    return;
                Toast.makeText(MainActivity.this, "NEXT", Toast.LENGTH_SHORT).show();
                releaseMediaPlayer();
                if(mediaIndex == playList.size() - 1){
                    mediaIndex = 0;
                    mediaPlayer = setMediaPlayer(mediaPlayer, playList.get(mediaIndex).getmMedia());
                }
                else{
                    mediaIndex++;
                    mediaPlayer = setMediaPlayer(mediaPlayer, playList.get(mediaIndex).getmMedia());
                }
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mCompletionListener);
                updateMarqueeText(playList.get(mediaIndex));
                seekBar.setMax(mediaPlayer.getDuration());
                updateSeekBar();
            }
        });

        ImageView previousButton = (ImageView) (findViewById(R.id.previous_btn));
        previousButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mediaPlayer == null)
                    return;
                Toast.makeText(MainActivity.this, "PREVIOUS", Toast.LENGTH_SHORT).show();
                releaseMediaPlayer();
                if(mediaIndex == 0){
                    mediaIndex = playList.size() - 1;
                    mediaPlayer = setMediaPlayer(mediaPlayer, playList.get(mediaIndex).getmMedia());
                }
                else{
                    mediaIndex--;
                    mediaPlayer = setMediaPlayer(mediaPlayer, playList.get(mediaIndex).getmMedia());
                }
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mCompletionListener);
                updateMarqueeText(playList.get(mediaIndex));
                seekBar.setMax(mediaPlayer.getDuration());
                updateSeekBar();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaPlayer == null)
                    return;
                seekBar.setEnabled(true);
                if(fromUser){
                    mediaPlayer.seekTo(progress);
                    updateTimer(mediaPlayer.getCurrentPosition());
                }
                if(!fromUser)
                    updateTimer(mediaPlayer.getCurrentPosition());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(mediaPlayer != null)
                    mediaPlayer.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(mediaPlayer != null)
                    mediaPlayer.start();
            }
        });
    }

    private void releaseMediaPlayer(){
        if(mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void updateMarqueeText(PlayList playItem){
        String str = playItem.getmSongName();
        marqueeText.setText(str + "          ");
        updateTimer(mediaPlayer.getCurrentPosition());
    }

    private void updateSeekBar(){
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        seekHandler.postDelayed(run, 1000);
    }

    private void updateTimer(int position){
        int p = position/1000;
        int min = p/60;
        Log.v("Main", "min: " + min);
        String minute = Integer.toString(min);
        if(min < 10){
            String temp = minute;
            minute = "0";
            minute += temp;
        }
        Log.v("Main", "minute: " + minute);
        int sec = p%60;
        Log.v("Main", "sec: " + sec);
        String second = Integer.toString(sec);
        if(sec < 10){
            String temp = second;
            second = "0";
            second += temp;
        }
        Log.v("Main", "second: " + second);
        timerText.setText(minute + " : " + second);
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
            Toast.makeText(getApplicationContext(), getString(R.string.no_music_found), Toast.LENGTH_LONG).show();
            return;
        }
        else{
            do{
                long songId = musicCursor.getLong(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                String songName = musicCursor.getString(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                playList.add(new PlayList(songId, songName));
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

