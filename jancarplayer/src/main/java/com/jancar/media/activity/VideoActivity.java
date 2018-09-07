package com.jancar.media.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.jancar.media.R;
import com.jancar.media.base.BaseActivity;
import com.jancar.media.data.Const;
import com.jancar.media.fragment.PlayListFragment;
import com.jancar.media.utils.FlyLog;
import com.jancar.media.view.FlyTabView;

import java.util.ArrayList;
import java.util.List;

import tcking.github.com.giraffeplayer.GiraffePlayer;

import static tcking.github.com.giraffeplayer.GiraffePlayer.isShowVideoPlayList;

public class VideoActivity extends BaseActivity implements View.OnClickListener {
    private ImageView play_fore, play_next, play_list;
    private LinearLayout play_ll01_playlist;
    public String currentPlayUrl;
    public int currenPos = 0;
    public GiraffePlayer player;
    public List<String> videoList = new ArrayList<>();
    private FlyTabView tabView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        player = new GiraffePlayer(this);
        getFragmentManager().beginTransaction().replace(R.id.ac_video_fragment,new PlayListFragment()).commit();

        initView();

        Intent intent = getIntent();
        final String playUrl = intent.getStringExtra(Const.PLAYURL_KEY);
        FlyLog.d("playurl=%s",playUrl);
        if(!TextUtils.isEmpty(playUrl)) {
            currentPlayUrl = playUrl;
            player.play(playUrl);
        }

        player.onComplete(new Runnable() {
            @Override
            public void run() {
                playNext();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        final String playUrl = intent.getStringExtra(Const.PLAYURL_KEY);
        FlyLog.d("playurl=%s",playUrl);
        if(!TextUtils.isEmpty(playUrl)&&!player.isPlaying()) {
            currentPlayUrl = playUrl;
            player.play(playUrl);
        }
        super.onNewIntent(intent);
    }

    private void initView() {
        play_fore = (ImageView) findViewById(R.id.menu_play_fore);
        play_next = (ImageView) findViewById(R.id.menu_play_next);
        play_list = (ImageView) findViewById(R.id.menu_play_list);
        play_ll01_playlist = (LinearLayout) findViewById(R.id.play_ll01_playlist);
        tabView = (FlyTabView) findViewById(R.id.app_video_tabview);
        tabView.setTitles(new String[]{getString(R.string.disk_list),getString(R.string.play_list),getString(R.string.file_list)});

        play_fore.setOnClickListener(this);
        play_next.setOnClickListener(this);
        play_list.setOnClickListener(this);

    }

    private void playNext() {
        if(videoList!=null&&!videoList.isEmpty()){
            if(currenPos<videoList.size()-1){
                currenPos++;;
                currentPlayUrl = videoList.get(currenPos);
                player.play(currentPlayUrl);
            }
        }
    }

    private void playFore() {
        if(videoList!=null&&!videoList.isEmpty()){
            if(currenPos>0){
                currenPos--;
                currentPlayUrl = videoList.get(currenPos);
                player.play(currentPlayUrl);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_play_list:
                showOrHideVideoPlayListLayout();
                break;
            case R.id.menu_play_fore:
                playFore();
                break;
            case R.id.menu_play_next:
                playNext();
                break;
        }
    }

    private void showOrHideVideoPlayListLayout() {
        isShowVideoPlayList = !isShowVideoPlayList;
        play_ll01_playlist.animate().translationX(isShowVideoPlayList ? -341 : 0).setDuration(300).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
