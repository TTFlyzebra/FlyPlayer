package com.jancar.media.activity;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.jancar.media.R;
import com.jancar.media.base.BaseActivity;
import com.jancar.media.data.Const;
import com.jancar.media.data.Video;
import com.jancar.media.utils.DisplayUtils;
import com.jancar.media.utils.FlyLog;
import com.jancar.media.view.FlyTabTextView;
import com.jancar.media.view.FlyTabView;

import java.util.ArrayList;
import java.util.List;

import tcking.github.com.giraffeplayer.GiraffePlayer;
import tcking.github.com.giraffeplayer.GiraffePlayerActivity;

import static tcking.github.com.giraffeplayer.GiraffePlayer.isShowVideoPlayList;

public class VideoActivity extends BaseActivity implements
        View.OnClickListener,
        FlyTabView.OnItemClickListener,
        GiraffePlayer.OnPlayStatusChangeLiseter {
    private ImageView play_fore, play_next, play_list;
    private RelativeLayout play_ll01_playlist;
    private RelativeLayout app_video_bottom_box;
    public int currenPos = 0;
    public GiraffePlayer player;
    public List<Video> videoList = new ArrayList<>();
    private FlyTabView tabView;


    private String titles[] = new String[]{"磁盘列表", "播放列表", "文件列表"};
    private String fmName[] = new String[]{"StorageFragment", "VideoPlayListFragment", "VideoFloderFragment"};

    private float scaleX = 1.0f;
    private float scaleY = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.giraffe_player);
        player = new GiraffePlayer(this);
        player.setScaleType(GiraffePlayer.SCALETYPE_FITPARENT);
        player.addStatusChangeLiseter(this);


        DisplayMetrics dm = DisplayUtils.getMetrices(this);
        FlyLog.d("DisplayMetrics width=%d,heigth=%d",dm.widthPixels,dm.heightPixels);
        scaleX = dm.widthPixels/1024f;
        scaleY = dm.heightPixels/600f;

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
        app_video_bottom_box = (RelativeLayout) findViewById(R.id.app_video_bottom_box);
        tabView = (FlyTabView) findViewById(R.id.app_video_tabview);
        tabView.setOnItemClickListener(this);

        play_fore.setOnClickListener(this);
        play_next.setOnClickListener(this);
        play_list.setOnClickListener(this);
        app_video_bottom_box.setOnClickListener(this);

        tabView.setTitles(titles);
        replaceFragment(fmName[1]);
        tabView.setFocusPos(1);
    }

    private void playNext() {
        if (videoList != null && !videoList.isEmpty()) {
            if (currenPos < videoList.size() - 1) {
                currenPos++;
                player.play(videoList.get(currenPos).url);
            }
        }
    }

    private void playFore() {
        if (videoList != null && !videoList.isEmpty()) {
            if (currenPos > 0) {
                currenPos--;
                player.play(videoList.get(currenPos).url);
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
        play_ll01_playlist.setVisibility(View.VISIBLE);
        isShowVideoPlayList = !isShowVideoPlayList;
        play_ll01_playlist.animate().translationX(isShowVideoPlayList
                ? -394 * DisplayUtils.getMetrices(this).widthPixels / 1024
                : 0)
                .setDuration(300)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (play_ll01_playlist.getX() > ((1024 - 394) * DisplayUtils.getMetrices(VideoActivity.this).widthPixels / 1024)) {
                            play_ll01_playlist.setVisibility(View.GONE);
                        } else {
                            play_ll01_playlist.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
        play_list.setImageResource(isShowVideoPlayList ? R.drawable.media_list_menu_open : R.drawable.media_list_menu_close);
    }

    @Override
    public void changePath(String path) {
        videoList.clear();
        super.changePath(path);
    }

    @Override
    public void videoUrlList(List<Video> videoUrlList) {
        FlyLog.d("get videos size=%d", videoUrlList == null ? 0 : videoUrlList.size());
        if (videoUrlList == null) {
            FlyLog.d("musicUrlList = null return");
            super.videoUrlList(videoUrlList);
            return;
        }
        if (videoUrlList.isEmpty()) {
            currenPos = 0;
            if (player.isPlaying()) {
                player.stop();
            }
            FlyLog.d("musicPlayer stop");
            super.videoUrlList(videoUrlList);
            return;
        }
        videoList.addAll(videoUrlList);
        //TODO:判断当前列表有没更新，确定播放哪首歌曲
        if (!player.isPlaying()) {
            currenPos = 0;
            player.play(videoList.get(currenPos).url);
            super.videoUrlList(videoUrlList);
            return;
        }

        if (currenPos >= videoList.size()) {
            currenPos = 0;
            player.play(videoList.get(currenPos).url);
            super.videoUrlList(videoUrlList);
            return;
        }
        String currentUrl = player.getPlayUrl();
        if (!videoList.get(currenPos).equals(currentUrl)) {
            currenPos = 0;
            player.play(videoList.get(currenPos).url);
        }
        super.videoUrlList(videoUrlList);
    }

    @Override
    public void statusChange(int statu) {
        FlyLog.d("Statu = %d",statu);

        switch (statu) {
            case GiraffePlayer.STATUS_ERROR:
            case GiraffePlayer.STATUS_COMPLETED:
                playNext();
                break;
            case GiraffePlayer.STATUS_PLAYING:
                setCurrentPos();
                break;
            case GiraffePlayer.STATUS_PAUSE:
            case GiraffePlayer.STATUS_LOADING:
                break;
        }
    }

    private void setCurrentPos() {
        for (int i = 0; i < videoList.size(); i++) {
            if (videoList.get(i).equals(player.getPlayUrl())) {
                currenPos = i;
                break;
            }
        }
    }
}
