package com.example.android.musicplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.support.v4.app.Fragment;
import com.example.android.musicplayer.PlayItemFragmentViewPager.ZoomOutPageTransformer;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<PlayList>> {

    private final String LOG_TAG = MainActivity.this.getClass().getSimpleName();
    private static final int MY_PERMISSION_REQUEST = 1;
    private static final int ALL_SONG_LOADER = 0;
    private MainFragmentPlayConsole mConsoleFragment;
    private MainFragmentPlayList mPlayListsFragment;
    private List<Fragment> mFragmentList;
    private ViewPager mFragmentContainer;
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
    }

    private void initFragmentList(){
        mFragmentContainer = findViewById(R.id.main_activity_viewpager_container);
        mFragmentContainer.setPageTransformer(true, new ZoomOutPageTransformer());
        mPlayListsFragment = new MainFragmentPlayList();
        mConsoleFragment = new MainFragmentPlayConsole();
        mFragmentList = new ArrayList<>();
        mFragmentList.add(mPlayListsFragment);
        mFragmentList.add(mConsoleFragment);
        mFragmentContainer.setAdapter(new MainScreenViewPagerAdapter(getSupportFragmentManager(), mFragmentList));
    }

    public static ArrayList<PlayList> getAllSongs(){
        return mAllSongs;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        mConsoleFragment.onKeyDownFragment(keyCode);
        return super.onKeyDown(keyCode, event);
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