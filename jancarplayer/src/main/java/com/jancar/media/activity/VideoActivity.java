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
import com.jancar.media.model.MusicPlayer;
import com.jancar.media.utils.DisplayUtils;
import com.jancar.media.utils.FlyLog;
import com.jancar.media.view.FlyTabTextView;
import com.jancar.media.view.FlyTabView;

import java.util.ArrayList;
import java.util.List;

import tcking.github.com.giraffeplayer.GiraffePlayer;

import static tcking.github.com.giraffeplayer.GiraffePlayer.isShowVideoPlayList;

public class VideoActivity extends BaseActivity implements
        View.OnClickListener,
        FlyTabView.OnItemClickListener,
        GiraffePlayer.OnPlayStatusChangeLiseter {
    private ImageView play_fore, play_next, play_list;
    private RelativeLayout play_ll01_playlist;
    public int currenPos = 0;
    public GiraffePlayer player;
    public List<String> videoList = new ArrayList<>();
    private FlyTabView tabView;

    private String titles[] = new String[]{"磁盘列表", "播放列表", "文件列表"};
    private String fmName[] = new String[]{"StorageFragment", "VideoPlayListFragment", "VideoFloderFragment"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        player = new GiraffePlayer(this);
        player.addStatusChangeLiseter(this);

        initView();

        Intent intent = getIntent();
        final String playUrl = intent.getStringExtra(Const.PLAYURL_KEY);
        FlyLog.d("playurl=%s", playUrl);
        if (!TextUtils.isEmpty(playUrl)) {
            player.play(playUrl);
        }
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
    protected void onNewIntent(Intent intent) {
        final String playUrl = intent.getStringExtra(Const.PLAYURL_KEY);
        FlyLog.d("playurl=%s", playUrl);
        if (!TextUtils.isEmpty(playUrl) && !player.isPlaying()) {
            player.play(playUrl);
        }
        super.onNewIntent(intent);
    }

    private void initView() {
        /**
         * 需支持多国语言
         */
        titles = new String[]{getString(R.string.disk_list), getString(R.string.play_list), getString(R.string.file_list)};
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

        replaceFragment(fmName[0]);
    }

    private void playNext() {
        if (videoList != null && !videoList.isEmpty()) {
            if (currenPos < videoList.size() - 1) {
                currenPos++;
                ;
                player.play(videoList.get(currenPos));
            }
        }
    }

    private void playFore() {
        if (videoList != null && !videoList.isEmpty()) {
            if (currenPos > 0) {
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
        if (v instanceof FlyTabTextView) {
            replaceFragment(fmName[pos]);
        }
    }

    private void showOrHideVideoPlayListLayout() {
        isShowVideoPlayList = !isShowVideoPlayList;
        play_ll01_playlist.animate().translationX(isShowVideoPlayList
                ? -394* DisplayUtils.getMetrices(this).widthPixels/1024
                : 0)
                .setDuration(300).start();
    }

    @Override
    public void videoUrlList(List<String> videoUrlList) {
        FlyLog.d("get videos size=%d", videoUrlList == null ? 0 : videoUrlList.size());
        if (videoUrlList == null) {
            FlyLog.d("musicUrlList = null return");
            return;
        }
        videoList.clear();
        if (videoUrlList.isEmpty() ) {
            currenPos = 0;
            if(player.isPlaying()){
                player.stop();
            }
            FlyLog.d("musicPlayer stop");
            return;
        }
        videoList.addAll(videoUrlList);
        //TODO:判断当前列表有没更新，确定播放哪首歌曲
        if (!player.isPlaying()) {
            currenPos = 0;
            player.play(videoList.get(currenPos));
            return;
        }

        if (currenPos >= videoList.size()) {
            currenPos = 0;
            player.play(videoList.get(currenPos));
            return;
        }
        String currentUrl = player.getPlayUrl();
        if (!videoList.get(currenPos).equals(currentUrl)) {
            currenPos = 0;
            player.play(videoList.get(currenPos));
        }
    }

    @Override
    public void statusChange(int statu) {

        switch (statu) {
            case MusicPlayer.STATUS_COMPLETED:
                playNext();
                break;
            case MusicPlayer.STATUS_PLAYING:
                setCurrentPos();
                break;
            case MusicPlayer.STATUS_ERROR:
            case MusicPlayer.STATUS_PAUSE:
            case MusicPlayer.STATUS_LOADING:
                playNext();
                break;
        }
    }

    private void setCurrentPos() {
        for(int i=0;i<videoList.size();i++){
            if(videoList.get(i).equals(player.getPlayUrl())){
                currenPos = i;
                break;
            }
        }
    }
}
