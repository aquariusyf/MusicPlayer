package com.example.android.musicplayer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.android.musicplayer.Notification.NotificationActions;
import com.example.android.musicplayer.PlayItemFragment.MediaListFragment;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.android.musicplayer.PlayItemFragment.CurrentMediaFragment;
import com.example.android.musicplayer.PlayItemFragmentViewPager.FragmentViewPagerAdapter;
import com.example.android.musicplayer.PlayItemFragmentViewPager.ZoomOutPageTransformer;

import java.io.IOException;

public class MainFragmentPlayConsole extends Fragment {

    private final String LOG_TAG = MainFragmentPlayConsole.class.getSimpleName();
    public static final int MEDIA_LOADER_INITIATE_ID = 1;
    public static final int MEDIA_LOADER_UPDATE_ID = 2;
    private boolean isNewPlaylist = false;
    private Uri mPlaylistUri;
    private static FragmentActivity mMyContext;
    private TextView mBackToPlaylist;
    private TextView mTimerText;
    private SeekBar mSeekBar;
    private TextView mTotalTime;
    private static ImageView mPlayButton;
    private static ImageView mNextButton;
    private static ImageView mPreviousButton;
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
                    if(mMediaIndex == mPlayList.size() - 1){
                        updatePlayingItemDisplay(mMediaIndex, 0);
                        mMediaIndex = 0;
                        mMediaPlayer = setMediaPlayer(mMediaPlayer, mPlayList.get(mMediaIndex).getmMedia());
                    }
                    else{
                        updatePlayingItemDisplay(mMediaIndex, mMediaIndex + 1);
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

    private List<android.support.v4.app.Fragment> mFragmentList;
    private ViewPager mFragmentContainer;
    private TabLayout mTab;

    private LoaderCallbacks<ArrayList<PlayList>> songLoaderCallBack
            = new LoaderCallbacks<ArrayList<PlayList>>() {
        @Override
        public Loader<ArrayList<PlayList>> onCreateLoader(int i, Bundle bundle) {
            if(i == MEDIA_LOADER_INITIATE_ID)
                return new MediaLoader(getActivity(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            else
                return new MediaLoader(getActivity(), mPlaylistUri);
        }

        @Override
        public void onLoadFinished(Loader<ArrayList<PlayList>> loader, ArrayList<PlayList> playlist) {
            if(playlist == null || playlist.isEmpty()){
                MainActivity.setTotal(playlist);
                return;
            }
            mPlayList = playlist;
            if(!isNewPlaylist){
                MainActivity.setTotal(mPlayList);
            }
            initShuffleSeed();
            MediaListFragment.updateAdapter(playlist);
            if(isNewPlaylist){
                Toast.makeText(getActivity(), getString(R.string.toast_playing), Toast.LENGTH_SHORT).show();
                updatePlayingItemDisplay(mPlayList.size() - 1, mMediaIndex);
                mMediaPlayer = setMediaPlayer(mMediaPlayer, mPlayList.get(mMediaIndex).getmMedia());
                mMediaPlayer.start();
                mPlayButton.setImageResource(R.mipmap.pause_button_icon);
                MainActivity.changePlayPauseIcon(R.mipmap.pause_button_icon);
                Log.v(LOG_TAG, "Start Playing!!!" + mPlayList.get(mMediaIndex).getmSongName());
                mMediaPlayer.setOnCompletionListener(mCompletionListener);
                updateMarqueeText(mPlayList.get(mMediaIndex));
                mSeekBar.setMax(mMediaPlayer.getDuration());
                updateTotalTime(mMediaPlayer.getDuration());
                updateSeekBar();
            }
            isNewPlaylist = true;
        }

        @Override
        public void onLoaderReset(Loader<ArrayList<PlayList>> loader) {
            MediaListFragment.resetAdapter();
        }
    };

    public MainFragmentPlayConsole() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        mMyContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.play_console_screen_fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.v(LOG_TAG, "Console fragment onCreate called");
        initFragmentList();
        initRepeatAndShuffleButton();
        mSeekBar = getView().findViewById(R.id.seekbar);
        mSeekBar.setEnabled(false);
        mTimerText = getView().findViewById(R.id.timer_text_view);
        mTotalTime = getView().findViewById(R.id.total_time_text_view);

        MediaListFragment.initAdapter(mPlayList, getActivity());
        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
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
                mPlayButton.setImageResource(R.mipmap.pause_button_icon);
                MainActivity.changePlayPauseIcon(R.mipmap.pause_button_icon);
                mMediaPlayer.setOnCompletionListener(mCompletionListener);
                updateMarqueeText(mPlayList.get(mMediaIndex));
                mSeekBar.setMax(mMediaPlayer.getDuration());
                updateTotalTime(mMediaPlayer.getDuration());
                updateSeekBar();
            }
        };
        MediaListFragment.setListener(listener);
        getActivity().getLoaderManager().initLoader(MEDIA_LOADER_INITIATE_ID, null, songLoaderCallBack);

        mBackToPlaylist = getView().findViewById(R.id.back_to_playlist_button);
        mBackToPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.backToPlaylistPage();
            }
        });

        mPlayButton = getView().findViewById(R.id.play_btn);
        mPlayButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
                    mMediaPlayer.pause();
                    mPlayButton.setImageResource(R.mipmap.play_button_icon);
                    MainActivity.changePlayPauseIcon(R.mipmap.play_button_icon);
                    Toast.makeText(getActivity(), getString(R.string.toast_pause), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(mMediaPlayer != null && mMediaPlayer.getCurrentPosition() != 0) {
                    mMediaPlayer.start();
                    mPlayButton.setImageResource(R.mipmap.pause_button_icon);
                    MainActivity.changePlayPauseIcon(R.mipmap.pause_button_icon);
                    Toast.makeText(getActivity(), getString(R.string.toast_continue), Toast.LENGTH_SHORT).show();
                }
                else if((mMediaPlayer == null && !mPlayList.isEmpty()) ||
                        (mMediaPlayer != null && mMediaPlayer.getCurrentPosition() == 0)){
                    releaseMediaPlayer();
                    Toast.makeText(getActivity(), getString(R.string.toast_playing), Toast.LENGTH_SHORT).show();
                    updatePlayingItemDisplay(mPlayList.size() - 1, mMediaIndex);
                    mMediaPlayer = setMediaPlayer(mMediaPlayer, mPlayList.get(mMediaIndex).getmMedia());
                    mMediaPlayer.start();
                    mPlayButton.setImageResource(R.mipmap.pause_button_icon);
                    MainActivity.changePlayPauseIcon(R.mipmap.pause_button_icon);
                    Log.v(LOG_TAG, "Start Playing!!!" + mPlayList.get(mMediaIndex).getmSongName());
                    mMediaPlayer.setOnCompletionListener(mCompletionListener);
                    updateMarqueeText(mPlayList.get(mMediaIndex));
                    mSeekBar.setMax(mMediaPlayer.getDuration());
                    updateTotalTime(mMediaPlayer.getDuration());
                    updateSeekBar();
                }
                else{
                    Toast.makeText(getActivity(), getString(R.string.toast_no_song_found), Toast.LENGTH_LONG).show();
                }
            }
        });

        mNextButton = getView().findViewById(R.id.next_btn);
        mNextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mMediaPlayer == null)
                    return;
                releaseMediaPlayer();
                int oldIndex = mMediaIndex;
                Log.v(LOG_TAG, "Old Index: " + oldIndex);
                Log.v(LOG_TAG, "Play State: " + mPlayState);
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
                        mPlayButton.setImageResource(R.mipmap.pause_button_icon);
                        mMediaPlayer.setOnCompletionListener(mCompletionListener);
                        updateMarqueeText(mPlayList.get(mMediaIndex));
                        mSeekBar.setMax(mMediaPlayer.getDuration());
                        updateTotalTime(mMediaPlayer.getDuration());
                        updateSeekBar();
                        Toast.makeText(getActivity(), getString(R.string.toast_next), Toast.LENGTH_SHORT).show();
                        break;
                    case REPEAT_ONE:
                        mMediaPlayer = setMediaPlayer(mMediaPlayer, mPlayList.get(mMediaIndex).getmMedia());
                        mMediaPlayer.start();
                        mPlayButton.setImageResource(R.mipmap.pause_button_icon);
                        mMediaPlayer.setOnCompletionListener(mCompletionListener);
                        updateMarqueeText(mPlayList.get(mMediaIndex));
                        mSeekBar.setMax(mMediaPlayer.getDuration());
                        updateTotalTime(mMediaPlayer.getDuration());
                        updateSeekBar();
                        break;
                    case SHUFFLE:
                        setShuffleIndex();
                        Log.v(LOG_TAG, "New Index: " + mMediaIndex);
                        updatePlayingItemDisplay(oldIndex, mMediaIndex);
                        mMediaPlayer = setMediaPlayer(mMediaPlayer, mPlayList.get(mMediaIndex).getmMedia());
                        mMediaPlayer.start();
                        Log.v(LOG_TAG, "Media started");
                        mPlayButton.setImageResource(R.mipmap.pause_button_icon);
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

        mPreviousButton = getView().findViewById(R.id.previous_btn);
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
                        mPlayButton.setImageResource(R.mipmap.pause_button_icon);
                        mMediaPlayer.setOnCompletionListener(mCompletionListener);
                        updateMarqueeText(mPlayList.get(mMediaIndex));
                        mSeekBar.setMax(mMediaPlayer.getDuration());
                        updateTotalTime(mMediaPlayer.getDuration());
                        updateSeekBar();
                        Toast.makeText(getActivity(), getString(R.string.toast_previous), Toast.LENGTH_SHORT).show();
                        break;
                    case REPEAT_ONE:
                        mMediaPlayer = setMediaPlayer(mMediaPlayer, mPlayList.get(mMediaIndex).getmMedia());
                        mMediaPlayer.start();
                        mPlayButton.setImageResource(R.mipmap.pause_button_icon);
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
                        mPlayButton.setImageResource(R.mipmap.pause_button_icon);
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
                    mPlayButton.setImageResource(R.mipmap.pause_button_icon);
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
        CurrentMediaFragment.updateMediaName(playItem);
        updateTimer(mMediaPlayer.getCurrentPosition());
    }

    private void updateSeekBar(){
        if(mMediaPlayer == null)
            return;
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

    private void initRepeatAndShuffleButton(){
        mPlayState = REPEAT_ALL;
        mRepeat = getView().findViewById(R.id.repeat_button);
        mShuffle = getView().findViewById(R.id.shuffle_button);

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
                mRepeat.setImageResource(R.mipmap.repeat_one_selected_icon);
                break;
            case REPEAT_ONE:
                mPlayState = REPEAT_ALL;
                mRepeat.setImageResource(R.mipmap.repeat_all_selected_icon);
                break;
            case SHUFFLE:
                mPlayState = REPEAT_ALL;
                mRepeat.setImageResource(R.mipmap.repeat_all_selected_icon);
                mShuffle.setImageResource(R.mipmap.shuffle_not_selected_icon);
                break;
            default: break;
        }
    }

    private void setShuffle(){
        switch (mPlayState){
            case REPEAT_ALL:
                mPlayState = SHUFFLE;
                mRepeat.setImageResource(R.mipmap.repeat_all_not_selected_icon);
                mShuffle.setImageResource(R.mipmap.shuffle_selected_icon);
                break;
            case REPEAT_ONE:
                mPlayState = SHUFFLE;
                mRepeat.setImageResource(R.mipmap.repeat_all_not_selected_icon);
                mShuffle.setImageResource(R.mipmap.shuffle_selected_icon);
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
        if((currentPosition < 0 || currentPosition > mPlayList.size() - 1) ||
                (newPosition < 0 || newPosition > mPlayList.size() - 1)){
            Log.v(LOG_TAG, "Index Out of Bound: " + currentPosition + ", " + newPosition);
            return;
        }
        mPlayList.get(currentPosition).setPlayingState("");
        mPlayList.get(newPosition).setPlayingState(getString(R.string.playing_indicator));
        MediaListFragment.updateText(currentPosition, newPosition, mPlayList);
        CurrentMediaFragment.updateAlbumDisplay(newPosition, mPlayList);
        MainActivity.updateSongName(mPlayList.get(newPosition).getmSongName());
        MainActivity.updateArtistName(mPlayList.get(newPosition).getmArtistName());
        MainActivity.updateAlbumImage(mPlayList.get(newPosition).getAlbumBitMap());
    }

    public MediaPlayer setMediaPlayer(MediaPlayer mediaPlayer, long songId) {
        Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId);
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(getContext(), songUri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mediaPlayer;
    }

    private void initFragmentList(){
        mFragmentContainer = getView().findViewById(R.id.vp_fragment_list_container);
        mFragmentContainer.setPageTransformer(true, new ZoomOutPageTransformer());
        mTab = getView().findViewById(R.id.vp_tab_dots);
        mTab.setupWithViewPager(mFragmentContainer,false);
        CurrentMediaFragment currentMediaFragment = new CurrentMediaFragment();
        MediaListFragment mediaListFragment = new MediaListFragment();
        mFragmentList = new ArrayList<>();
        mFragmentList.add(mediaListFragment);
        mFragmentList.add(currentMediaFragment);
        mFragmentContainer.setAdapter(new FragmentViewPagerAdapter(mMyContext.getSupportFragmentManager(), mFragmentList));
    }

    public void changePlaylist(Uri newPlaylistUri) {
        if(mMediaPlayer != null) {
           releaseMediaPlayer();
        }
        mMediaIndex = 0;
        mPlaylistUri = newPlaylistUri;
        getActivity().getLoaderManager().restartLoader(MEDIA_LOADER_UPDATE_ID, null, songLoaderCallBack);
        Log.v(LOG_TAG, "Loader restarted new playlist Uri: " + mPlaylistUri);
    }

    public static class NotificationBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case NotificationActions.ACTION_PLAY_PAUSE:
                    mPlayButton.callOnClick();
                    break;
                case NotificationActions.ACTION_NEXT:
                    mNextButton.callOnClick();
                    break;
                case NotificationActions.ACTION_PREVIOUS:
                    mPreviousButton.callOnClick();
                    break;
                default: break;
            }
        }
    }
}
