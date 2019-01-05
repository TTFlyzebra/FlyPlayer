package com.jancar.player.video;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.jancar.media.base.BaseActivity;
import com.jancar.media.data.Video;
import com.jancar.media.model.listener.IMediaEventListerner;
import com.jancar.media.model.mediaSession.IMediaSession;
import com.jancar.media.model.mediaSession.MediaSession;
import com.jancar.media.utils.FlyLog;
import com.jancar.media.utils.RtlTools;
import com.jancar.media.utils.SystemPropertiesProxy;
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
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class VideoActivity_AP1 extends BaseActivity implements
        View.OnClickListener,
        FlyTabView.OnItemClickListener,
        GiraffePlayer.OnPlayStatusChangeLiseter,
        TouchEventRelativeLayout.OnTouchEventListener
,IMediaEventListerner {
    private ImageView play_fore, play_next, play_pause, leftMenu;
    private TouchEventRelativeLayout leftLayout;
    private TouchEventRelativeLayout controlLayout;
    public int currenPos = 0;
    public GiraffePlayer player;
    public List<Video> videoList = new ArrayList<>();
    protected FlyTabView tabView;
    private RelativeLayout liveBox;
    private RegisterMediaSession registerMediaSession;


    protected String titles[] = new String[]{"磁盘列表", "播放列表", "文件列表"};
    protected String fmName[] = new String[]{"VideoStorageFragment", "VideoPlayListFragment_AP1", "VideoFloderFragment"};

    private AudioManager mAudioManager;

    private boolean lostPause = false;
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
                if (countSavePlaySeek % SAVEPLAYSEEKTIME == 0 && player != null && player.isPlaying()) {
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

    private IMediaSession mediaSession;

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

        registerMediaSession = new RegisterMediaSession(this, player);
        initFragment();
        initView();

        mediaSession = new MediaSession(this);
        mediaSession.init();
        mediaSession.addEventListener(this);
    }

    public void initFragment() {
    }

    @Override
    protected void onStart() {
        registerMediaSession.requestMediaButton();
        touchTime = 0;
        showControlView(true);
        super.onStart();
        FlyLog.d("onStart");
        player.playSavePath(currenPath);
        mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
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
        FlyLog.d("onStop");
        mAudioManager.abandonAudioFocus(mAudioFocusListener);
        player.stop();
        mHandler.removeCallbacks(seekBarTask);
        registerMediaSession.releaseMediaButton();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mediaSession.removeEventListener(this);
        mediaSession.release();
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

    @Override
    protected void onNewIntent(Intent intent) {
//        final String playUrl = intent.getStringExtra(Const.PLAYURL_KEY);
//        FlyLog.d("playurl=%s", playUrl);
//        if (!TextUtils.isEmpty(playUrl) && !player.isPlaying()) {
//            player.start(playUrl);
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

    @Override
    public void playNext() {
        if (videoList != null && !videoList.isEmpty()) {
            currenPos = (currenPos + 1) % videoList.size();
            player.play(videoList.get(currenPos).url);
        }
    }

    @Override
    public void playPrev() {
        if (videoList != null && !videoList.isEmpty()) {
            currenPos = (currenPos - 1 + videoList.size()) % videoList.size();
            player.play(videoList.get(currenPos).url);
        }
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

    private void showLeftMenu(boolean flag) {
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
                        if (leftLayout.getX() > (screen_width - video_left_list_width)
                                || leftLayout.getX() < (-video_left_list_width)) {
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
                playNext();
                break;
            case GiraffePlayer.STATUS_PLAYING:
                setCurrentPos();
                player.savePathUrl(currenPath);
                mHandler.removeCallbacks(seekBarTask);
                mHandler.post(seekBarTask);
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
                playPrev();
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

    public long touchTime;

    @Override
    public void onFlyTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchTime = System.currentTimeMillis();
                break;
        }
    }


//    @Override
//    public void onUsbMounted(Activity activity, boolean flag) {
//        /**
//         * 正在播放的U盘被拔
//         */
//        if (!flag) {
//            FlyLog.e("palying usb is removed!");
//            player.stop();
//            FlyLog.e("is back palying andr current(%s) path is removed, finish appliction!", currenPath);
//            if (isStop) {
//                activity.finish();
//            }
//        }
//    }

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

}
