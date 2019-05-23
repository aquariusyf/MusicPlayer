package com.example.android.musicplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;

import com.example.android.musicplayer.PlayItemFragmentViewPager.FragmentViewPagerAdapter;
import com.example.android.musicplayer.PlayItemFragmentViewPager.ZoomOutPageTransformer;
import com.example.android.musicplayer.PlayListDataBase.PlayListContract.PlayListEntry;
import com.example.android.musicplayer.PlayListDataBase.PlayListCursorAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.this.getClass().getSimpleName();
    private static final int MY_PERMISSION_REQUEST = 1;
    private MainFragmentPlayConsole mConsoleFragment;
    private MainFragmentPlayList mPlayListsFragment;
    private List<android.support.v4.app.Fragment> mFragmentList;
    private ViewPager mFragmentContainer;
    private PlayListCursorAdapter mCursorAdapter;

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
        mFragmentContainer.setAdapter(new FragmentViewPagerAdapter(getSupportFragmentManager(), mFragmentList));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        mConsoleFragment.onKeyDownFragment(keyCode);
        return super.onKeyDown(keyCode, event);
    }
}