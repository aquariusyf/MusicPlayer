package com.example.android.musicplayer;

import android.media.MediaPlayer;
import android.os.Handler;
import android.provider.MediaStore;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

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
                mediaPlayer = MediaPlayer.create(MainActivity.this, playList.get(mediaIndex).getmMedia());
            }
            else{
                mediaIndex++;
                mediaPlayer = MediaPlayer.create(MainActivity.this, playList.get(mediaIndex).getmMedia());
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

        playList.add(new PlayList(R.raw.gaobaiqiqiu,"告白气球"));
        playList.add(new PlayList(R.raw.tiantiande, "甜甜的"));
        playList.add(new PlayList(R.raw.sugar, "Sugar"));
        playList.add(new PlayList(R.raw.daoxiang, "稻香"));
        playList.add(new PlayList(R.raw.chulianfensexi, "初恋粉色系"));
        playList.add(new PlayList(R.raw.maiyatang, "麦芽糖"));
        playList.add(new PlayList(R.raw.qingge, "情歌"));
        playList.add(new PlayList(R.raw.ruguowomenbucengxiangyu, "如果我们不曾相遇"));
        playList.add(new PlayList(R.raw.tingjianxiayudeshengyin, "听见下雨的声音"));
        playList.add(new PlayList(R.raw.xiaoqingge, "小情歌"));
        playList.add(new PlayList(R.raw.xuemaojiao, "学猫叫"));
        seekBar = (SeekBar)(findViewById(R.id.seekbar));
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
                mediaPlayer = MediaPlayer.create(MainActivity.this, playList.get(mediaIndex).getmMedia());
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
                if(mediaPlayer != null && mediaPlayer.getCurrentPosition() != 0){
                    mediaPlayer.start();
                    Toast.makeText(MainActivity.this, "Continue", Toast.LENGTH_SHORT).show();
                }
                else{
                    releaseMediaPlayer();
                    Toast.makeText(MainActivity.this, "Playing", Toast.LENGTH_SHORT).show();
                    mediaPlayer = MediaPlayer.create(MainActivity.this, playList.get(mediaIndex).getmMedia());
                    mediaPlayer.start();
                    Log.v("MainActivity", "Start Playing!!!");
                    mediaPlayer.setOnCompletionListener(mCompletionListener);
                    updateMarqueeText(playList.get(mediaIndex));
                    seekBar.setMax(mediaPlayer.getDuration());
                    updateSeekBar();
                }
            }
        });

        ImageView pauseButton = (ImageView) findViewById(R.id.pause_btn);
        pauseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Toast.makeText(MainActivity.this, "Paused", Toast.LENGTH_SHORT).show();
                if(mediaPlayer.isPlaying())
                    mediaPlayer.pause();
            }
        });

        ImageView stopButton = (ImageView) findViewById(R.id.stop_btn);
        stopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "STOPPED", Toast.LENGTH_SHORT).show();
                mediaPlayer.seekTo(0);
                mediaPlayer.pause();
            }
        });

        ImageView nextButton = (ImageView) (findViewById(R.id.next_btn));
        nextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Toast.makeText(MainActivity.this, "NEXT", Toast.LENGTH_SHORT).show();
                releaseMediaPlayer();
                if(mediaIndex == playList.size() - 1){
                    mediaIndex = 0;
                    mediaPlayer = MediaPlayer.create(MainActivity.this, playList.get(mediaIndex).getmMedia());
                }
                else{
                    mediaIndex++;
                    mediaPlayer = MediaPlayer.create(MainActivity.this, playList.get(mediaIndex).getmMedia());
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
                Toast.makeText(MainActivity.this, "PREVIOUS", Toast.LENGTH_SHORT).show();
                releaseMediaPlayer();
                if(mediaIndex == 0){
                    mediaIndex = playList.size() - 1;
                    mediaPlayer = MediaPlayer.create(MainActivity.this, playList.get(mediaIndex).getmMedia());
                }
                else{
                    mediaIndex--;
                    mediaPlayer = MediaPlayer.create(MainActivity.this, playList.get(mediaIndex).getmMedia());
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
                if(fromUser){
                    mediaPlayer.seekTo(progress);
                    updateTimer(mediaPlayer.getCurrentPosition());
                }
                if(!fromUser)
                    updateTimer(mediaPlayer.getCurrentPosition());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mediaPlayer.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
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
}

