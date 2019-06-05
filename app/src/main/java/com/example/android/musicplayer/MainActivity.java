package com.example.android.musicplayer;

import android.Manifest;
import android.app.LoaderManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.RemoteViews;

import com.example.android.musicplayer.PlayItemFragmentViewPager.ZoomOutPageTransformer;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<PlayList>> {

    private final String LOG_TAG = MainActivity.this.getClass().getSimpleName();
    private static final int MY_PERMISSION_REQUEST = 1;
    private static final int ALL_SONG_LOADER = 0;
    private static MainFragmentPlayConsole mConsoleFragment;
    private MainFragmentPlayList mPlayListsFragment;
    private List<Fragment> mFragmentList;
    private static ViewPager mFragmentContainerMain;

    private NotificationManager mNotificationManager;
    private NotificationChannel mNotificationChannel;
    private RemoteViews mNotificationView;
    private Notification mNotification;
    private static final int NOTIFICATION_ID = 100;
    private static final String CHANNEL_ID = "channel01";
    private static final String CHANNEL_NAME = "Notification channel";

    public static ArrayList<PlayList> mAllSongs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }
        }
        getLoaderManager().initLoader(ALL_SONG_LOADER, null, this);
        initFragmentList();
        initNotification();
        showNotification();
    }

    private void initFragmentList(){
        mFragmentContainerMain = findViewById(R.id.main_activity_viewpager_container);
        mFragmentContainerMain.setPageTransformer(true, new ZoomOutPageTransformer());
        mPlayListsFragment = new MainFragmentPlayList();
        mConsoleFragment = new MainFragmentPlayConsole();
        mFragmentList = new ArrayList<>();
        mFragmentList.add(mPlayListsFragment);
        mFragmentList.add(mConsoleFragment);
        mFragmentContainerMain.setAdapter(new MainScreenViewPagerAdapter(getSupportFragmentManager(), mFragmentList));
    }

    public static void backToPlaylistPage() {
        mFragmentContainerMain.setCurrentItem(0, true);
    }

    public static MainFragmentPlayConsole getConsoleFragment() {
        return mConsoleFragment;
    }

    public static ArrayList<PlayList> getAllSongs(){
        return mAllSongs;
    }

    private void initNotification(){
        mNotificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationChannel.enableVibration(false);
            mNotificationChannel.setVibrationPattern(new long[]{ 0 });
            mNotificationManager.createNotificationChannel(mNotificationChannel);
        }

        Intent mainIntent = new Intent(MainActivity.this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, mainIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        mNotificationView = new RemoteViews(getPackageName(), R.layout.notification_layout);
        builder.setContentIntent(pendingIntent);
        builder.setContent(mNotificationView);
        builder.setChannelId(CHANNEL_ID);
        builder.setSmallIcon(R.drawable.pets_icon);
        builder.setOngoing(true);
        mNotification = builder.build();
    }

    private void showNotification(){
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        Log.v(LOG_TAG, "Show notification called");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        mConsoleFragment.onKeyDownFragment(keyCode);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @NonNull
    @Override
    public Loader<ArrayList<PlayList>> onCreateLoader(int i, @Nullable Bundle bundle) {
        return new MediaLoader(getBaseContext(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<PlayList>> loader, ArrayList<PlayList> playList) {
        if(playList == null || playList.isEmpty()){
            mPlayListsFragment.setTotalNumberOfSongs(0);
            return;
        }
        mAllSongs = playList;
        mPlayListsFragment.setTotalNumberOfSongs(mAllSongs.size());
    }

    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<PlayList>> loader) {

    }
}