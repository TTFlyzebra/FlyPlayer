package com.jancar.player.video;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.jancar.media.base.BaseActivity;
import com.jancar.media.data.StorageInfo;
import com.jancar.media.data.Video;
import com.jancar.media.model.listener.IMediaEventListerner;
import com.jancar.media.model.mediaSession.IMediaSession;
import com.jancar.media.model.mediaSession.MediaSession;
import com.jancar.media.model.storage.Storage;
import com.jancar.media.utils.FlyLog;
import com.jancar.media.utils.RtlTools;
import com.jancar.media.utils.SystemPropertiesProxy;
import com.jancar.media.utils.UriTools;
import com.jancar.media.view.FlyTabTextView;
import com.jancar.media.view.FlyTabView;
import com.jancar.media.view.ParkWarningView;
import com.jancar.media.view.TouchEventRelativeLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import tcking.github.com.giraffeplayer.GiraffePlayer;
import tcking.github.com.giraffeplayer.IRenderView;
import tcking.github.com.giraffeplayer.IjkVideoView;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class VideoActivity_AP1 extends BaseActivity implements
        View.OnClickListener,
        FlyTabView.OnItemClickListener,
        GiraffePlayer.OnPlayStatusChangeLiseter,
        TouchEventRelativeLayout.OnTouchEventListener,
        IMediaEventListerner {
    protected ImageView play_fore, play_next, play_pause, leftMenu, play_mode;
    private TouchEventRelativeLayout leftLayout;
    private TouchEventRelativeLayout controlLayout;
    public int currenPos = 0;
    public GiraffePlayer player;
    public List<Video> videoList = new ArrayList<>();
    protected FlyTabView tabView;
    private RelativeLayout liveBox;
    private IjkVideoView ijkVideoView;


    protected String titles[] = new String[]{"磁盘列表", "播放列表", "文件列表"};
    protected String fmName[] = new String[]{"VideoStorageFragment", "VideoPlayListFragment_AP1", "VideoFloderFragment"};
    private boolean isShowLeftMenu = false;
    public boolean isShowControl = true;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int hideTime = 5000;
    private static final int REFRESH_SEEK_LRC_TIME = 1000;
    private int countSavePlaySeek = 0;
    private int SAVEPLAYSEEKTIME = 10;

    public static boolean isScan = false;

    private int mAnimaDurtion = 300;
    private float screen_width = 1024;
    private float screen_height = 600;
    private float video_bottom_menu_height = 138;
    private float video_left_list_width = 400;

    private Runnable seekBarTask = new Runnable() {
        @Override
        public void run() {
            try {
                countSavePlaySeek++;
                if (countSavePlaySeek % SAVEPLAYSEEKTIME == 3 && player != null && player.isPlaying()) {
                    player.savePathUrl(currenPath);
                }
                player.setProgress();
                mHandler.removeCallbacks(seekBarTask);
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
                        .translationY(video_bottom_menu_height)
                        .setDuration(mAnimaDurtion).start();
                getWindow().getDecorView().setSystemUiVisibility(View.INVISIBLE);
                isShowControl = false;
//                mHandler.removeCallbacks(seekBarTask);
            } else {
                mHandler.postDelayed(hideControlTask, time + 100);
            }
        }
    };

    public IMediaSession mediaSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.giraffe_player);

        screen_width = getResources().getDimensionPixelSize(R.dimen.video_screen_width);
        screen_height = getResources().getDimensionPixelSize(R.dimen.video_screen_height);
        video_bottom_menu_height = getResources().getDimensionPixelSize(R.dimen.video_bottom_menu_height);
        video_left_list_width = getResources().getDimensionPixelSize(R.dimen.video_left_list_width);
        // init player
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        player = new GiraffePlayer(this);
        player.addStatusChangeLiseter(this);

        initFragment();
        initView();

        mediaSession = new MediaSession(this);

        playIntent(getIntent());

        requestAudioFocus();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        playIntent(intent);
    }

    public void initFragment() {
    }

    @Override
    protected void onStart() {
        FlyLog.d("onStart");
        touchTime = 0;
        showControlView(true);
        super.onStart();
        player.playSavePath(currenPath);
        mediaSession.init();
        if (setPause) {
            final int resetVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        player.pause();
                    } catch (Exception e) {
                        FlyLog.e(e.toString());
                    }
                }
            }, 1000);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, resetVolume, 0);
                }
            }, 1100);
        }
        mediaSession.addEventListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FlyLog.d("onResume setpause=" + setPause);
//        player.onResume();
    }

    @Override
    protected void onPause() {
        FlyLog.d("onPause");
//        player.pause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mediaSession.removeEventListener(this);
        mediaSession.release();
        FlyLog.d("onStop");
        player.stop();
        mHandler.removeCallbacks(seekBarTask);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        abandonAudioFocus();
        mHandler.removeCallbacksAndMessages(null);
        player.removeStatusChangeLiseter(this);
        player.onDestroy();
        try {
            ((ParkWarningView) Objects.requireNonNull(findViewById(R.id.layout_parking))).onDestory();
        } catch (Exception e) {
            FlyLog.e();
        }
        super.onDestroy();
    }

    private void playIntent(Intent intent) {
        FlyLog.d("intent=" + intent);
        if (intent == null) return;
        String test = intent.getStringExtra("test");
        FlyLog.d("playOpenIntent test=" + test);
        List<String> openList = intent.getStringArrayListExtra("music_list");
        FlyLog.d("openList=%s", openList == null ? "" : openList.toString());
        String url;
        if (openList == null) {
            Uri uri = intent.getData();
            if (uri != null) {
                url = UriTools.getFilePath(this, uri);
                if (!TextUtils.isEmpty(url)) {
                    FlyLog.d("open uri=%s", url);
                    openList = new ArrayList<>();
                    openList.add(url);
                }
            }
        }
        if (openList != null && !openList.isEmpty()) {
            currenPath = Storage.ALL_STORAGE;
            player.play(openList.get(0));
            usbMediaScan.openStorager(new StorageInfo(currenPath));
        }
    }

    private void initView() {
        titles = new String[]{getString(R.string.disk_list), getString(R.string.play_list), getString(R.string.file_list)};
        play_fore = (ImageView) findViewById(R.id.ac_video_play_fore);
        play_pause = (ImageView) findViewById(R.id.ac_video_play_pause);
        play_next = (ImageView) findViewById(R.id.ac_video_play_next);
        leftMenu = (ImageView) findViewById(R.id.menu_play_list);
        play_mode = (ImageView) findViewById(R.id.menu_play_mode);
        controlLayout = (TouchEventRelativeLayout) findViewById(R.id.app_video_bottom_box);
        tabView = (FlyTabView) findViewById(R.id.app_video_tabview);
        leftLayout = (TouchEventRelativeLayout) findViewById(R.id.play_ll01_playlist);
        liveBox = (RelativeLayout) findViewById(R.id.app_video_box);
        ijkVideoView = (IjkVideoView) findViewById(R.id.video_view);

        ijkVideoView.setOnClickListener(this);
        tabView.setOnItemClickListener(this);
        play_fore.setOnClickListener(this);
        play_next.setOnClickListener(this);
        play_pause.setOnClickListener(this);
        leftMenu.setOnClickListener(this);
        play_mode.setOnClickListener(this);
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

    @Override
    public void playNext() {
        if (play_next.isEnabled() && videoList != null && !videoList.isEmpty()) {
            setViewEnable(false);
            setPlayForeImg();
            currenPos = (currenPos + 1) % videoList.size();
            player.play(videoList.get(currenPos).url);
        }
    }

    public void setPlayForeImg() {
        play_fore.setImageResource(R.drawable.media_fore_01);
    }

    @Override
    public void playPrev() {
        if (play_fore.isEnabled() && videoList != null && !videoList.isEmpty()) {
            setViewEnable(false);
            setPlayNextImg();
            currenPos = (currenPos - 1 + videoList.size()) % videoList.size();
            player.play(videoList.get(currenPos).url);
        }
    }

    public void setPlayNextImg() {
        play_next.setImageResource(R.drawable.media_next_01);
    }


    @Override
    public void playOrPause() {
        if (player.isPlaying()) {
            setPause = true;
            player.pause();
        } else {
            setPause = false;
            player.start();
        }
    }

    @Override
    public void start() {
        player.start();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void KEY_FF() {
        player.forward(0.10f);
    }

    @Override
    public void KEY_FB() {
        player.forward(-0.10f);
    }

    @Override
    public void KEY_REPEAT() {

    }

    @Override
    public void KEY_SHUFFLE() {

    }

    @Override
    public void KEY_LIST() {
        showControlView(true);
        isShowLeftMenu = !isShowLeftMenu;
        showLeftMenu(isShowLeftMenu);
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
                playPrev();
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
            case R.id.menu_play_mode:
                try {
                    int mode = player.switchMode();
                    switch (mode) {
                        case IRenderView.AR_MATCH_PARENT:

                            setPlayModeImg(2);
                            break;
                        case IRenderView.AR_16_9_FIT_PARENT:

                            setPlayModeImg(3);
                            break;
                        case IRenderView.AR_4_3_FIT_PARENT:
                            setPlayModeImg(4);
                            break;
                        case IRenderView.AR_ASPECT_FIT_PARENT:
                        default:
                            setPlayModeImg(1);
                            break;
                    }
                } catch (Exception e) {
                    FlyLog.e(e.toString());
                }
                break;
        }
    }

    public void setPlayModeImg(int type) {
        switch (type) {
            case 1:
                play_mode.setImageResource(R.drawable.media_video_mode1);
                break;
            case 2:
                play_mode.setImageResource(R.drawable.media_video_mode2);
                break;
            case 3:
                play_mode.setImageResource(R.drawable.media_video_mode3);
                break;
            case 4:
                play_mode.setImageResource(R.drawable.media_video_mode4);
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
            controlLayout.animate().translationY(0).setDuration(mAnimaDurtion).start();
            getWindow().getDecorView().setSystemUiVisibility(View.VISIBLE);
            mHandler.removeCallbacks(hideControlTask);
            mHandler.postDelayed(hideControlTask, hideTime);
        } else {
            mHandler.removeCallbacks(hideControlTask);
            mHandler.post(hideControlTask);
        }
    }

    private void showLeftMenu(final boolean flag) {
        leftLayout.setVisibility(View.VISIBLE);
        boolean isRtl = RtlTools.isLayoutRtl(leftLayout);
        leftLayout.animate()
                .translationX(flag ?
                        isRtl ? video_left_list_width : -(video_left_list_width)
                        : 0)
                .setDuration(mAnimaDurtion)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!flag) {
                            leftLayout.setVisibility(View.INVISIBLE);
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
        setLeftImg(flag);

    }

    public void setLeftImg(boolean flag) {
        leftMenu.setImageResource(flag ? R.drawable.media_list_menu_open : R.drawable.media_list_menu_close);
    }

    @Override
    public void notifyPathChange(String path) {
        FlyLog.d("notifyPathChange path=%s", path);
        if (isStop) return;
        isScan = true;
        if (player.isPlaying()) {
            player.savePathUrl(currenPath);
        }
        videoList.clear();
        if (!player.getPlayUrl().startsWith(path)) {
            player.stop();
            currenPos = -1;
        }
        player.playSavePath(path);
        super.notifyPathChange(path);
    }

    @Override
    public void videoUrlList(List<Video> videoUrlList) {
        FlyLog.d("get videos size=%d", videoUrlList == null ? 0 : videoUrlList.size());
        if (isStop) return;
        if (videoUrlList != null && !videoUrlList.isEmpty()) {
            videoList.addAll(videoUrlList);
            if ((new File(player.getPlayUrl()).exists()) && player.getPlayUrl().startsWith(currenPath)) {
                /**
                 * 已经在播放，更新播放了哪一集
                 */
                for (int i = 0; i < videoList.size(); i++) {
                    currenPos = -1;
                    if (videoList.get(i).url.equals(player.getPlayUrl())) {
                        currenPos = i;
                        break;
                    }
                }
            } else {
                currenPos = 0;
                player.play(videoList.get(0).url);
            }
        }
        try {
            mediaSession.notifyPlayId(Math.max(0, currenPos), videoList.size());
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
        super.videoUrlList(videoUrlList);
    }

    @Override
    public void scanFinish(String path) {
        FlyLog.d("scanFinish path=%s", path);
        if (isStop) return;
        isScan = false;
        if (videoList == null || videoList.isEmpty()) {
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
                setViewEnable(true);
                playNext();
                break;
            case GiraffePlayer.STATUS_PLAYING:
                setCurrentPos();
                mHandler.removeCallbacks(seekBarTask);
                mHandler.post(seekBarTask);
                mHandler.removeCallbacks(enableViewTask);
                mHandler.postDelayed(enableViewTask, 5000);
                break;
            case GiraffePlayer.STATUS_PAUSE:
                player.savePathUrl(currenPath);
                break;
            case GiraffePlayer.STATUS_START:
                setViewEnable(true);
                break;
            case GiraffePlayer.STATUS_LOADING:
            default:
                break;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setPlayImg();
            }
        }, 100);
        try {
            mediaSession.notifyPlayId(Math.max(0, currenPos), videoList.size());
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }

    public void setPlayImg() {
        play_pause.setImageResource(player.isPlaying() ? R.drawable.media_pause : R.drawable.media_play);
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
                    setPause = true;
                    player.pause();
                } else {
                    setPause = false;
                    player.start();
                }
                return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                setPause = false;
                player.start();
                return true;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                if (player.isPlaying()) {
                    setPause = true;
                    player.pause();
                }
                return true;
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                player.forward(0.10f);
                return true;
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                player.forward(-0.10f);
                return true;
            case KeyEvent.KEYCODE_MEDIA_STOP:
            case KeyEvent.KEYCODE_BACK:
                finish();
                return true;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                playNext();
                return true;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                playPrev();
                return true;

        }
        return super.onKeyDown(keyCode, event);
    }

    public long touchTime;

    @Override
    public void onFlyTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchTime = System.currentTimeMillis();
                break;
        }
    }


    protected int getFloderSum() {
        Set<String> set = new HashSet<>();
        for (Video video : videoList) {
            String url = video.url;
            int last = url.lastIndexOf(File.separator);
            String path = url.substring(0, last).intern();
            set.add(path);
        }
        return set.size();
    }

    private boolean lostPause = false;
    private AudioManager mAudioManager;
    private AudioFocusRequest audioFocusRequest;
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
                    String flag = SystemPropertiesProxy.get(VideoActivity_AP1.this, SystemPropertiesProxy.Property.PERSIST_KEY_GISMIX, "100");
                    if (flag.equals("0") && player != null && player.isPlaying()) {
                        FlyLog.d("lost Focus2 pause!");
                        player.pause();
                        lostPause = true;
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (player != null && lostPause && !setPause) {
                        FlyLog.d("get Focus start!");
                        player.start();
                    }
                    break;
            }
        }
    };

    private void requestAudioFocus() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(true)
                    .setOnAudioFocusChangeListener(mAudioFocusListener)
                    .build();
            mAudioManager.requestAudioFocus(audioFocusRequest);
        } else {
            mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    private void abandonAudioFocus() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mAudioManager.abandonAudioFocusRequest(audioFocusRequest);
        } else {
            mAudioManager.abandonAudioFocus(mAudioFocusListener);
        }
    }

    private void setViewEnable(boolean flag) {
        ijkVideoView.setEnabled(flag);
        tabView.setEnabled(flag);
        play_fore.setEnabled(flag);
        play_next.setEnabled(flag);
        play_pause.setEnabled(flag);
        leftMenu.setEnabled(flag);
        controlLayout.setEnabled(flag);
        controlLayout.setEnabled(flag);
        leftLayout.setEnabled(flag);
        liveBox.setEnabled(flag);
        if (flag) {
            setPlayNextPressedImg();
            setPlayForePressedImg();
        }
    }

    public void setPlayNextPressedImg() {
        play_next.setImageResource(R.drawable.media_next);
    }

    public void setPlayForePressedImg() {
        play_fore.setImageResource(R.drawable.media_fore);

    }

    private Runnable enableViewTask = new Runnable() {
        @Override
        public void run() {
            setViewEnable(true);
        }
    };

}
