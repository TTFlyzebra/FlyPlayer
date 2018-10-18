package com.jancar.player.video;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.jancar.JancarManager;
import com.jancar.media.base.BaseActivity;
import com.jancar.media.data.Video;
import com.jancar.media.utils.DisplayUtils;
import com.jancar.media.utils.FlyLog;
import com.jancar.media.utils.SystemPropertiesProxy;
import com.jancar.media.view.FlyTabTextView;
import com.jancar.media.view.FlyTabView;
import com.jancar.media.view.TouchEventRelativeLayout;
import com.jancar.state.JacState;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tcking.github.com.giraffeplayer.GiraffePlayer;

public class VideoActivity extends BaseActivity implements
        View.OnClickListener,
        FlyTabView.OnItemClickListener,
        GiraffePlayer.OnPlayStatusChangeLiseter,
        TouchEventRelativeLayout.OnTouchEventListener {
    private ImageView play_fore, play_next, play_pause, leftMenu;
    private TouchEventRelativeLayout leftLayout;
    private TouchEventRelativeLayout controlLayout;
    public int currenPos = 0;
    public GiraffePlayer player;
    public List<Video> videoList = new ArrayList<>();
    private FlyTabView tabView;
    RelativeLayout liveBox;


    private String titles[] = new String[]{"磁盘列表", "播放列表", "文件列表"};
    private String fmName[] = new String[]{"VideoStorageFragment", "VideoPlayListFragment", "VideoFloderFragment"};

    private AudioManager mAudioManager;

    private boolean lostPause = false;
    private boolean isShowLeftMenu = false;
    public boolean isShowControl = true;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int hideTime = 5000;
    private static final int REFRESH_SEEK_LRC_TIME = 1000;
    private int countSavePlaySeek = 0;
    private int SAVEPLAYSEEKTIME = 10;

    private JacState jacState = null;
    private JancarManager jancarManager;

    private Runnable seekBarTask = new Runnable() {
        @Override
        public void run() {
            try {
                countSavePlaySeek++;
                if (countSavePlaySeek % SAVEPLAYSEEKTIME == 0 && player != null) {
                    player.savePathUrl(currenPath);
                }
                player.setProgress();
                mHandler.postDelayed(seekBarTask, REFRESH_SEEK_LRC_TIME);
            } catch (Exception e) {
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
//                mHandler.removeCallbacks(seekBarTask);
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
        jacState = new SystemStates();
        jancarManager = (JancarManager) getSystemService("jancar_manager");
        jancarManager.registerJacStateListener(jacState.asBinder());
    }

    @Override
    protected void onStart() {
        touchTime = 0;
        showControlView(true);
        super.onStart();
        FlyLog.d("onStart");
        player.playSavePath(currenPath);
        mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    boolean isPause = false;

    @Override
    protected void onResume() {
        super.onResume();
        FlyLog.d("onResume setpause=" + setPause);
        if (isPause) {
            player.start();
        }
        isPause = false;
        if (setPause) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    player.pause();
                }
            }, 1000);
        }
    }

    @Override
    protected void onPause() {
        FlyLog.d("onPause");
        isPause = false;
        if (player.isPlaying()) {
            player.pause();
            isPause = true;
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        FlyLog.d("onStop");
        mAudioManager.abandonAudioFocus(mAudioFocusListener);
        super.onStop();
        player.stop();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        jancarManager.unregisterJacStateListener(jacState.asBinder());
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
//        final String playUrl = intent.getStringExtra(Const.PLAYURL_KEY);
//        FlyLog.d("playurl=%s", playUrl);
//        if (!TextUtils.isEmpty(playUrl) && !player.isPlaying()) {
//            player.play(playUrl);
//        }
        super.onNewIntent(intent);
    }

    private void initView() {
        titles = new String[]{getString(R.string.disk_list), getString(R.string.play_list), getString(R.string.file_list)};
        play_fore = (ImageView) findViewById(R.id.ac_video_play_fore);
        play_pause = (ImageView) findViewById(R.id.ac_video_play_pause);
        play_next = (ImageView) findViewById(R.id.ac_video_play_next);
        leftMenu = (ImageView) findViewById(R.id.menu_play_list);
        controlLayout = (TouchEventRelativeLayout) findViewById(R.id.app_video_bottom_box);
        tabView = (FlyTabView) findViewById(R.id.app_video_tabview);
        leftLayout = (TouchEventRelativeLayout) findViewById(R.id.play_ll01_playlist);
        liveBox = (RelativeLayout) findViewById(R.id.app_video_box);

        findViewById(R.id.video_view).setOnClickListener(this);
        tabView.setOnItemClickListener(this);
        play_fore.setOnClickListener(this);
        play_next.setOnClickListener(this);
        play_pause.setOnClickListener(this);
        leftMenu.setOnClickListener(this);
        controlLayout.setOnClickListener(this);
        controlLayout.setOnTouchEventListener(this);
        leftLayout.setOnTouchEventListener(this);
        liveBox.setOnClickListener(this);

        tabView.setTitles(titles);
        replaceFragment(fmName[1], R.id.ac_replace_fragment);
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

    private boolean setPause = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_play_list:
                isShowLeftMenu = !isShowLeftMenu;
                showLeftMenu(isShowLeftMenu);
                break;
            case R.id.ac_video_play_fore:
                playFore();
                break;
            case R.id.ac_video_play_next:
                playNext();
                break;
            case R.id.ac_video_play_pause:
                if (player.isPlaying()) {
                    setPause = true;
                    player.pause();
                } else {
                    setPause = false;
                    player.start();
                }
                break;
            case R.id.app_video_box:
            case R.id.video_view:
                touchTime = 0;
                break;
        }
    }

    @Override
    public void onItemClick(View v, int pos) {
        if (v instanceof FlyTabTextView) {
            replaceFragment(fmName[pos], R.id.ac_replace_fragment);
        }
    }


    public void showControlView(boolean flag) {
        isShowControl = flag;
        if (isShowControl) {
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
                .translationX(flag ? -394 * DisplayUtils.getMetrices(this).widthPixels / 1024 : 0)
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
    public void notifyPathChange(String path) {
        FlyLog.d("notifyPathChange path=%s", path);
        if (player.isPlaying()) {
            player.savePathUrl(currenPath);
        }
        videoList.clear();
        player.playSavePath(path);
        super.notifyPathChange(path);
    }

    @Override
    public void videoUrlList(List<Video> videoUrlList) {
        FlyLog.d("get videos size=%d", videoUrlList == null ? 0 : videoUrlList.size());

        if (videoUrlList == null || videoUrlList.isEmpty()) {
            FlyLog.d("musicUrlList = null return");
            super.videoUrlList(videoUrlList);
            return;
        }

        videoList.addAll(videoUrlList);
        if ((new File(player.getPlayUrl()).exists())) {
            for (int i = 0; i < videoList.size(); i++) {
                currenPos = -1;
                if (videoList.get(i).url.equals(player.getPlayUrl())) {
                    currenPos = i;
                    break;
                }
            }
        } else {
            player.setPlayUrl("");
            currenPos = 0;
            player.play(videoList.get(0).url);
        }
        super.videoUrlList(videoUrlList);
    }

    @Override
    public void scanFinish(String path) {
        FlyLog.d("scanFinish path=%s", path);
        if (videoList == null || videoList.isEmpty()) {
            player.stop();
            player.showStatus("");
            replaceFragment(fmName[0], R.id.ac_replace_fragment);
            tabView.setFocusPos(0);
            showControlView(true);
            showLeftMenu(true);
        }
        if (isShowControl) {
            showControlView(true);
        }
        super.scanFinish(path);
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
                setCurrentPos();
                mHandler.removeCallbacks(seekBarTask);
                mHandler.post(seekBarTask);
                player.savePathUrl(currenPath);
                break;
            case GiraffePlayer.STATUS_PAUSE:
                player.savePathUrl(currenPath);
                break;
            case GiraffePlayer.STATUS_LOADING:
            default:
                break;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                play_pause.setImageResource(player.isPlaying() ? R.drawable.media_pause : R.drawable.media_play);
            }
        }, 100);
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
            FlyLog.d("onAudioFocusChange focusChange=%d", focusChange);
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    FlyLog.d("lost Focus finish!");
                    finish();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (player != null && player.isPlaying()) {
                        FlyLog.d("lost Focus1 pause!");
                        player.pause();
                        lostPause = true;
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    /**
                     * 是否混音,高德其它软件在后台播报的情景
                     */
                    boolean flag = SystemProperties.getBoolean(SystemPropertiesProxy.Property.PERSIST_KEY_GISMIX, true);
                    if (!flag && player != null && player.isPlaying()) {
                        FlyLog.d("lost Focus2 pause!");
                        player.pause();
                        lostPause = true;
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (player != null && lostPause && !setPause) {
                        FlyLog.d("get Focus play!");
                        player.start();
                    }
                    break;
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

    public class SystemStates extends JacState {
        @Override
        public void OnBackCar(boolean bState) {
            super.OnBackCar(bState);
        }

        @Override
        public void OnStorage(StorageState state) {
            FlyLog.d("usb state:" + state.isUsbMounted());
            if (!state.isUsbMounted()) {
                if (!(new File(player.getPlayUrl()).exists())) {
                    FlyLog.d("play file is no exists");
                    player.stop();
                    player.showStatus("");
                }
            }
            super.OnStorage(state);
        }
    }
}
