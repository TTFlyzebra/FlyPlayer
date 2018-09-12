package com.jancar.media.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.jancar.media.R;
import com.jancar.media.base.BaseActivity;
import com.jancar.media.data.Const;
import com.jancar.media.listener.IUsbMediaListener;
import com.jancar.media.utils.FlyLog;
import com.jancar.media.view.FlyTabTextView;
import com.jancar.media.view.FlyTabView;

import java.util.ArrayList;
import java.util.List;

import tcking.github.com.giraffeplayer.GiraffePlayer;

import static tcking.github.com.giraffeplayer.GiraffePlayer.isShowVideoPlayList;

public class VideoActivity extends BaseActivity implements
        IUsbMediaListener,
        View.OnClickListener,
        FlyTabView.OnItemClickListener {
    private ImageView play_fore, play_next, play_list;
    private RelativeLayout play_ll01_playlist;
    public int currenPos = 0;
    public GiraffePlayer player;
    public List<String> videoList = new ArrayList<>();
    private FlyTabView tabView;

    private String titles[] = new String[]{"磁盘列表","播放列表","文件列表"};
    private String fmName[] = new String[]{"StorageFragment","VideoPlayListFragment","VideoFloderFragment"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        player = new GiraffePlayer(this);
        /**
         * 需支持多国语言
         */
        titles = new String[]{getString(R.string.disk_list),getString(R.string.play_list),getString(R.string.file_list)};

        usbMediaScan.addListener(this);

        replaceFragment(fmName[0]);

        initView();

        Intent intent = getIntent();
        final String playUrl = intent.getStringExtra(Const.PLAYURL_KEY);
        FlyLog.d("playurl=%s",playUrl);
        if(!TextUtils.isEmpty(playUrl)) {
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
    protected void onDestroy() {
        usbMediaScan.removeListener(this);
        super.onDestroy();
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        player.stop();
        super.onStop();
    }

    @Override
    public void musicUrlList(List<String> musicUrlList) {

    }

    @Override
    protected void onNewIntent(Intent intent) {
        final String playUrl = intent.getStringExtra(Const.PLAYURL_KEY);
        FlyLog.d("playurl=%s",playUrl);
        if(!TextUtils.isEmpty(playUrl)&&!player.isPlaying()) {
            player.play(playUrl);
        }
        super.onNewIntent(intent);
    }

    private void initView() {
        play_fore = (ImageView) findViewById(R.id.ac_music_play_fore);
        play_next = (ImageView) findViewById(R.id.ac_music_play_next);
        play_list = (ImageView) findViewById(R.id.menu_play_list);
        play_ll01_playlist = (RelativeLayout) findViewById(R.id.play_ll01_playlist);
        tabView = (FlyTabView) findViewById(R.id.app_video_tabview);
        tabView.setTitles(titles);
        tabView.setOnItemClickListener(this);

        play_fore.setOnClickListener(this);
        play_next.setOnClickListener(this);
        play_list.setOnClickListener(this);

    }

    private void playNext() {
        if(videoList!=null&&!videoList.isEmpty()){
            if(currenPos<videoList.size()-1){
                currenPos++;;
                player.play(videoList.get(currenPos));
            }
        }
    }

    private void playFore() {
        if(videoList!=null&&!videoList.isEmpty()){
            if(currenPos>0){
                currenPos--;
                player.play(videoList.get(currenPos));
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_play_list:
                showOrHideVideoPlayListLayout();
                break;
            case R.id.ac_music_play_fore:
                playFore();
                break;
            case R.id.ac_music_play_next:
                playNext();
                break;
        }
    }

    @Override
    public void onItemClick(View v, int pos) {
        if(v instanceof FlyTabTextView){
            replaceFragment(fmName[pos]);
        }
    }

    private void showOrHideVideoPlayListLayout() {
        isShowVideoPlayList = !isShowVideoPlayList;
        play_ll01_playlist.animate().translationX(isShowVideoPlayList ? -341 : 0).setDuration(300).start();
    }

    @Override
    public void videoUrlList(List<String> videoUrlList) {
        FlyLog.d("get videos size=%d", videoUrlList == null ? 0 : videoUrlList.size());
        if(videoUrlList==null) return;
        if (videoUrlList.isEmpty()) {
            if (player!=null&&!player.isPlaying()) {
                player.stop();
            }
        } else {
            videoList.clear();
            videoList.addAll(videoUrlList);
            if (player!=null&&!player.isPlaying()) {
                currenPos = 0;
                player.play(videoList.get(currenPos));
            }
        }
    }

    @Override
    public void imageUrlList(List<String> imageUrlList) {

    }

    @Override
    public void usbRemove(String usbstore) {

    }

}
