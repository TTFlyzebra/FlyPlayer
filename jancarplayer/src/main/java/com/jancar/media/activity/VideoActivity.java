package com.jancar.media.activity;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.github.chrisbanes.photoview.PhotoView;
import com.jancar.media.R;
import com.jancar.media.base.BaseActivity;
import com.jancar.media.data.Const;
import com.jancar.media.data.Video;
import com.jancar.media.utils.DisplayUtils;
import com.jancar.media.utils.FlyLog;
import com.jancar.media.utils.SPUtil;
import com.jancar.media.utils.SystemPropertiesProxy;
import com.jancar.media.view.FlyTabTextView;
import com.jancar.media.view.FlyTabView;
import com.jancar.media.view.TouchEventRelativeLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tcking.github.com.giraffeplayer.GiraffePlayer;

public class VideoActivity extends BaseActivity implements
        View.OnClickListener,
        FlyTabView.OnItemClickListener,
        GiraffePlayer.OnPlayStatusChangeLiseter,
        TouchEventRelativeLayout.OnTouchEventListener{
    private ImageView play_fore, play_next, leftMenu;
    private TouchEventRelativeLayout leftLayout;
    private ImageView menu_play_list;
    private TouchEventRelativeLayout controlLayout;
    public int currenPos = 0;
    public GiraffePlayer player;
    public List<Video> videoList = new ArrayList<>();
    private FlyTabView tabView;
    RelativeLayout liveBox;


    private String titles[] = new String[]{"磁盘列表", "播放列表", "文件列表"};
    private String fmName[] = new String[]{"StorageFragment", "VideoPlayListFragment", "VideoFloderFragment"};

    private AudioManager mAudioManager;

    private boolean lostPause = false;
    private boolean isShowLeftMenu = false;
    public boolean isShowControl = true;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int hideTime = 5000;
    private static final int REFRESH_SEEK_LRC_TIME = 1000;

    private Runnable seekBarTask = new Runnable() {
        @Override
        public void run() {
            try {
                player.setProgress();
                mHandler.postDelayed(seekBarTask, REFRESH_SEEK_LRC_TIME);
            }catch (Exception e){
                FlyLog.e(e.toString());
            }
        }
    };
    private Runnable hideControlTask = new Runnable() {
        @Override
        public void run() {
            long time = System.currentTimeMillis() - touchTime;
            if (time > hideTime && !videoList.isEmpty()) {
                showLeftMenu(false);
                isShowLeftMenu = false;
                controlLayout.animate()
                        .translationY(150 * DisplayUtils.getMetrices(VideoActivity.this).heightPixels / 600)
                        .setDuration(300).start();
                getWindow().getDecorView().setSystemUiVisibility(View.INVISIBLE);
                isShowControl = false;
                mHandler.removeCallbacks(seekBarTask);
            } else {
                mHandler.postDelayed(hideControlTask, time + 100);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.giraffe_player);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        player = new GiraffePlayer(this);
        player.setScaleType(GiraffePlayer.SCALETYPE_FITPARENT);
        player.addStatusChangeLiseter(this);

        initView();

        playSave();
    }

    @Override
    protected void onStart() {
        super.onStart();
        playSave();
        usbMediaScan.addListener(this);
        mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    private void playSave() {
        String playUrl = (String) SPUtil.get(this, "VIDEO_URL", "");
        int seek = (int) SPUtil.get(this, "VIDEO_SEEK", 0);
        if (!TextUtils.isEmpty(playUrl)) {
            File file = new File(playUrl);
            if (file.exists()) {
                player.play(playUrl, seek);
            }
        }
    }

    boolean isPause = false;

    @Override
    protected void onResume() {
        super.onResume();
        if (isPause) {
            player.start();
        }
        isPause = false;
    }

    @Override
    protected void onPause() {
        isPause = false;
        if (player.isPlaying()) {
            player.pause();
            isPause = true;
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        SPUtil.set(this, "VIDEO_URL", player.getPlayUrl());
        SPUtil.set(this, "VIDEO_SEEK", player.getCurrentPosition());
        mAudioManager.abandonAudioFocus(mAudioFocusListener);
        usbMediaScan.removeListener(this);
        super.onStop();
        player.stop();
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
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
        titles = new String[]{getString(R.string.disk_list), getString(R.string.play_list), getString(R.string.file_list)};
        play_fore = (ImageView) findViewById(R.id.ac_music_play_fore);
        play_next = (ImageView) findViewById(R.id.ac_music_play_next);
        leftMenu = (ImageView) findViewById(R.id.menu_play_list);
        controlLayout = (TouchEventRelativeLayout) findViewById(R.id.app_video_bottom_box);
        tabView = (FlyTabView) findViewById(R.id.app_video_tabview);
        leftLayout = (TouchEventRelativeLayout) findViewById(R.id.play_ll01_playlist);
        menu_play_list = (ImageView) findViewById(R.id.menu_play_list);
        liveBox = (RelativeLayout) findViewById(R.id.app_video_box);

        findViewById(R.id.video_view).setOnClickListener(this);
        tabView.setOnItemClickListener(this);
        play_fore.setOnClickListener(this);
        play_next.setOnClickListener(this);
        leftMenu.setOnClickListener(this);
        controlLayout.setOnClickListener(this);
        controlLayout.setOnTouchEventListener(this);
        leftLayout.setOnTouchEventListener(this);
        liveBox.setOnClickListener(this);

        tabView.setTitles(titles);
        replaceFragment(fmName[1]);
        tabView.setFocusPos(1);

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == 0) {
                    showControlView(true);
                }
            }
        });
    }

    private void playNext() {
        if (videoList != null && !videoList.isEmpty()) {
            currenPos = (currenPos + 1) % videoList.size();
            player.play(videoList.get(currenPos).url);
        }
    }

    private void playFore() {
        if (videoList != null && !videoList.isEmpty()) {
            currenPos = (currenPos - 1 + videoList.size()) % videoList.size();
            player.play(videoList.get(currenPos).url);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_play_list:
                isShowLeftMenu = !isShowLeftMenu;
                showLeftMenu(isShowLeftMenu);
                break;
            case R.id.ac_music_play_fore:
                playFore();
                break;
            case R.id.ac_music_play_next:
                playNext();
            case R.id.app_video_box:
            case R.id.video_view:
                touchTime = 0;
//                showControlView(!isShowControl);
                break;
        }
    }

    @Override
    public void onItemClick(View v, int pos) {
        if (v instanceof FlyTabTextView) {
            replaceFragment(fmName[pos]);
        }
    }


    public void showControlView(boolean flag) {
        isShowControl = flag;
        if(isShowControl){
            mHandler.removeCallbacks(seekBarTask);
            mHandler.post(seekBarTask);
        }
        if (flag) {
            controlLayout.animate().translationY(0).setDuration(300).start();
            getWindow().getDecorView().setSystemUiVisibility(View.VISIBLE);
            mHandler.removeCallbacks(hideControlTask);
            mHandler.postDelayed(hideControlTask, hideTime);
        } else {
            mHandler.removeCallbacks(hideControlTask);
            mHandler.post(hideControlTask);
        }
    }

    private void showLeftMenu(boolean flag) {
        leftLayout.setVisibility(View.VISIBLE);
        leftLayout.animate()
                .translationX(flag ? -394 * DisplayUtils.getMetrices(this).widthPixels / 1024:0)
                .setDuration(300)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (leftLayout.getX() > ((1024 - 394) * DisplayUtils.getMetrices(VideoActivity.this).widthPixels / 1024)) {
                            leftLayout.setVisibility(View.GONE);
                        } else {
                            leftLayout.setVisibility(View.VISIBLE);
                        }
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }
                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                })
                .start();
        leftMenu.setImageResource(flag ? R.drawable.media_list_menu_open : R.drawable.media_list_menu_close);
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
            mHandler.removeCallbacks(hideControlTask);
            controlLayout.animate().translationY(0).setDuration(300).start();
            getWindow().getDecorView().setSystemUiVisibility(View.VISIBLE);
            isShowControl = true;
            showLeftMenu(true);
        } else {
            showControlView(true);
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
        if (TextUtils.isEmpty(player.getPlayUrl())) {
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

        for (int i = 0; i < videoList.size(); i++) {
            currenPos = 0;
            if (videoList.get(i).url.equals(player.getPlayUrl())) {
                currenPos = i;
                break;
            }
        }
        if (currenPos == 0 && TextUtils.isEmpty(player.getPlayUrl())) {
            player.play(videoList.get(currenPos).url);
        }
        super.videoUrlList(videoUrlList);
    }

    @Override
    public void statusChange(int statu) {
        FlyLog.d("Statu = %d", statu);

        switch (statu) {
            case GiraffePlayer.STATUS_ERROR:
            case GiraffePlayer.STATUS_COMPLETED:
                playNext();
                break;
            case GiraffePlayer.STATUS_PLAYING:
                SPUtil.set(this, "VIDEO_URL", player.getPlayUrl());
                SPUtil.set(this, "VIDEO_SEEK", player.getCurrentPosition());
                setCurrentPos();
                mHandler.removeCallbacks(seekBarTask);
                mHandler.post(seekBarTask);
                break;
            case GiraffePlayer.STATUS_PAUSE:
            case GiraffePlayer.STATUS_LOADING:
                break;
        }
    }

    private void setCurrentPos() {
        for (int i = 0; i < videoList.size(); i++) {
            if (videoList.get(i).url.equals(player.getPlayUrl())) {
                currenPos = i;
                break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.start();
                }
                return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                player.start();
                return true;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                if (player.isPlaying()) {
                    player.pause();
                }
                return true;
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                return true;
            case KeyEvent.KEYCODE_MEDIA_STOP:
            case KeyEvent.KEYCODE_BACK:
                finish();
                return true;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                playNext();
                return true;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                playFore();
                return true;

        }
        return super.onKeyDown(keyCode, event);
    }

    private AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            try {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_LOSS:
                        player.pause();
                        finish();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        if (player.isPlaying()) {
                            player.pause();
                            lostPause = true;
                        }
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        /**
                         * 是否混音
                         */
                        boolean flag = SystemProperties.getBoolean(SystemPropertiesProxy.Property.PERSIST_KEY_GISMIX, true);
                        if (!flag && player.isPlaying()) {
                            player.pause();
                            lostPause = true;
                        }
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN:
                        if (lostPause) {
                            player.start();
                        }
                        break;
                }
            } catch (Exception e) {
                FlyLog.e(e.toString());
            }
        }
    };

    public long touchTime;
    @Override
    public void onFlyTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchTime = System.currentTimeMillis();
                break;
        }
    }
}
