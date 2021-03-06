package tcking.github.com.giraffeplayer;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;

import com.jancar.media.utils.FlyLog;
import com.jancar.media.utils.SPUtil;
import com.jancar.media.utils.SystemPropertiesProxy;
import com.jancar.player.video.R;
import com.jancar.player.video.VideoActivity_AP1;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;

/**
 * Created by tcking on 15/10/27.
 */
public class GiraffePlayer {

    /**
     * fitParent:scale the video uniformly (maintain the video's aspect ratio) so that both dimensions (width and height) of the video will be equal to or **less** than the corresponding dimension of the view. like ImageView's `CENTER_INSIDE`.等比缩放,画面填满view。
     */
    public static final String SCALETYPE_FITPARENT = "fitParent";
    /**
     * fillParent:scale the video uniformly (maintain the video's aspect ratio) so that both dimensions (width and height) of the video will be equal to or **larger** than the corresponding dimension of the view .like ImageView's `CENTER_CROP`.等比缩放,直到画面宽高都等于或小于view的宽高。
     */
    public static final String SCALETYPE_FILLPARENT = "fillParent";
    /**
     * wrapContent:center the video in the view,if the video is less than view perform no scaling,if video is larger than view then scale the video uniformly so that both dimensions (width and height) of the video will be equal to or **less** than the corresponding dimension of the view. 将视频的内容完整居中显示，如果视频大于view,则按比例缩视频直到完全显示在view中。
     */
    public static final String SCALETYPE_WRAPCONTENT = "wrapContent";
    /**
     * fitXY:scale in X and Y independently, so that video matches view exactly.不剪裁,非等比例拉伸画面填满整个View
     */
    public static final String SCALETYPE_FITXY = "fitXY";
    /**
     * 16:9:scale x and y with aspect ratio 16:9 until both dimensions (width and height) of the video will be equal to or **less** than the corresponding dimension of the view.不剪裁,非等比例拉伸画面到16:9,并完全显示在View中。
     */
    public static final String SCALETYPE_16_9 = "16:9";
    /**
     * 4:3:scale x and y with aspect ratio 4:3 until both dimensions (width and height) of the video will be equal to or **less** than the corresponding dimension of the view.不剪裁,非等比例拉伸画面到4:3,并完全显示在View中。
     */
    public static final String SCALETYPE_4_3 = "4:3";

    private static final int MESSAGE_SHOW_PROGRESS = 1;
    private static final int MESSAGE_FADE_OUT = 2;
    private static final int MESSAGE_SEEK_NEW_POSITION = 3;
    private static final int MESSAGE_HIDE_CENTER_BOX = 4;
    private static final int MESSAGE_RESTART_PLAY = 5;
    private final VideoActivity_AP1 activity;
    private final IjkVideoView ijkVideoView;
    private final TableLayout mHudView;
    private final TextView mVideoInfoText;
    private final boolean isShowTestInfo;

    private final SeekBar seekBar;
    private final AudioManager audioManager;
    private final int mMaxVolume;
    private boolean playerSupport;
    private String mPlayUrl;
    private Query $;
    public static final int STATUS_ERROR = -1;
    public static final int STATUS_IDLE = 0;
    public static final int STATUS_LOADING = 1;
    public static final int STATUS_PLAYING = 2;
    public static final int STATUS_PAUSE = 3;
    public static final int STATUS_COMPLETED = 4;
    public static final int STATUS_START = 5;
    public int status = STATUS_IDLE;
    private boolean isLive = false;//是否为直播
    private OrientationEventListener orientationEventListener;
    final private int initHeight;
    private int defaultTimeout = 10000;
    private int screenWidthPixels;

    private final static Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.app_video_replay_icon) {
                ijkVideoView.seekTo(0);
                ijkVideoView.start();
                statusChange(STATUS_PLAYING);
                doPauseResume();
            }
        }
    };
    private boolean isShowing;
    private boolean portrait;
    private float brightness = -1;
    private int volume = -1;
    private long newPosition = -1;
    private long defaultRetryTime = 5000;
    private OnErrorListener onErrorListener = new OnErrorListener() {
        @Override
        public void onError(int what, int extra) {
        }
    };
    private Runnable oncomplete = new Runnable() {
        @Override
        public void run() {

        }
    };
    private OnInfoListener onInfoListener = new OnInfoListener() {
        @Override
        public void onInfo(int what, int extra) {

        }
    };
    private OnControlPanelVisibilityChangeListener onControlPanelVisibilityChangeListener = new OnControlPanelVisibilityChangeListener() {
        @Override
        public void change(boolean isShowing) {

        }
    };


    /**
     * try to start when error(only for live video)
     *
     * @param defaultRetryTime millisecond,0 will stop retry,default is 5000 millisecond
     */
    public void setDefaultRetryTime(long defaultRetryTime) {
        this.defaultRetryTime = defaultRetryTime;
    }

    private int currentPosition;
    private boolean fullScreenOnly;

    private void doPauseResume() {
        if (status == STATUS_COMPLETED) {
            $.id(R.id.app_video_replay).gone();
            ijkVideoView.seekTo(0);
            ijkVideoView.start();
            statusChange(STATUS_PLAYING);
        } else if (ijkVideoView.isPlaying()) {
            ijkVideoView.pause();
            statusChange(STATUS_PAUSE);
        } else {
            ijkVideoView.start();
            statusChange(STATUS_PLAYING);
        }
    }

    /**
     * @param timeout
     */
    public void show(int timeout) {
        if (!isShowing) {
            isShowing = true;
            onControlPanelVisibilityChangeListener.change(true);
        }
        handler.removeMessages(MESSAGE_FADE_OUT);
        if (timeout != 0) {
            handler.sendMessageDelayed(handler.obtainMessage(MESSAGE_FADE_OUT), timeout);
        }
    }

    private long duration;
    private boolean instantSeeking;
    private boolean isDragging;
    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser)
                return;
            $.id(R.id.app_video_status).gone();//移动时隐藏掉状态image
            int newPosition = (int) ((duration * progress * 1.0) / 1000);
            String time = generateTime(newPosition);
            if (instantSeeking) {
                ijkVideoView.seekTo(newPosition);
            }
            $.id(R.id.app_video_currentTime).text(time);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isDragging = true;
            show(3600000);
            handler.removeMessages(MESSAGE_SHOW_PROGRESS);
            if (instantSeeking) {
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (!instantSeeking) {
                ijkVideoView.seekTo((int) ((duration * seekBar.getProgress() * 1.0) / 1000));
            }
            show(defaultTimeout);
            handler.removeMessages(MESSAGE_SHOW_PROGRESS);
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            isDragging = false;
            handler.sendEmptyMessageDelayed(MESSAGE_SHOW_PROGRESS, 1000);
        }
    };

    @SuppressWarnings("HandlerLeak")
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_FADE_OUT:
                    hide(false);
                    break;
                case MESSAGE_HIDE_CENTER_BOX:
                    $.id(R.id.app_video_volume_box).gone();
                    $.id(R.id.app_video_brightness_box).gone();
                    $.id(R.id.app_video_fastForward_box).gone();
                    break;
                case MESSAGE_SEEK_NEW_POSITION:
                    if (!isLive && newPosition >= 0) {
                        ijkVideoView.seekTo((int) newPosition);
                        newPosition = -1;
                    }
                    break;
                case MESSAGE_SHOW_PROGRESS:
                    setProgress();
                    if (!isDragging && isShowing) {
                        msg = obtainMessage(MESSAGE_SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000);
                    }
                    break;
                case MESSAGE_RESTART_PLAY:
                    play(mPlayUrl);
                    break;
            }
        }
    };


    public GiraffePlayer(final Activity activity) {
        try {
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
            playerSupport = true;
        } catch (Throwable e) {
            Log.e("GiraffePlayer", "loadLibraries error", e);
        }
        this.activity = (VideoActivity_AP1) activity;
//        app_video_bottom_box = (RelativeLayout) activity.findViewById(R.id.app_video_bottom_box);
        screenWidthPixels = activity.getResources().getDisplayMetrics().widthPixels;
        $ = new Query(activity);
        ijkVideoView = (IjkVideoView) activity.findViewById(R.id.video_view);
        mHudView = (TableLayout) activity.findViewById(R.id.hud_view);
        mVideoInfoText = (TextView) activity.findViewById(R.id.video_info);
        String str = SystemPropertiesProxy.get(activity, SystemPropertiesProxy.Property.PERSIST_KEY_VIDEOTEST, "0");
        isShowTestInfo = "1".equals(str);
        if (isShowTestInfo) {
            mHudView.setVisibility(View.VISIBLE);
            ijkVideoView.setHudView(mHudView);
            mVideoInfoText.setVisibility(View.VISIBLE);
        } else {
            mHudView.setVisibility(View.GONE);
            mVideoInfoText.setVisibility(View.GONE);
        }

        ijkVideoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer mp) {
                statusChange(STATUS_COMPLETED);
                oncomplete.run();
            }
        });
        ijkVideoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                statusChange(STATUS_ERROR);
                onErrorListener.onError(what, extra);
                return true;
            }
        });
        ijkVideoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                int width = mp.getVideoWidth();
                int height = mp.getVideoHeight();
                if (width > 2880 && height > 1620) {
                    FlyLog.e("no support video, width=%d,height=%d", width, height);
                    stop();
                    statusChange(STATUS_ERROR);
                    return false;
                }
                switch (what) {
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                        statusChange(STATUS_LOADING);
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                        statusChange(STATUS_PLAYING);
                        break;
                    case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                        //显示 下载速度
//                        Toaster.show("download rate:" + extra);
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                        if (isShowTestInfo) {
                            ITrackInfo[] iTrackInfos = ijkVideoView.getTrackInfo();
                            String viewInfo = "";
                            for (ITrackInfo iTrackInfo : iTrackInfos) {
                                viewInfo = viewInfo + iTrackInfo.getInfoInline() + "\n";
                            }
                            if (!TextUtils.isEmpty(viewInfo)) {
                                viewInfo = viewInfo.substring(0, viewInfo.length() - 1);
                                mVideoInfoText.setText(viewInfo);
                            }
                        }
                        onInfoListener.onInfo(what, extra);
                        statusChange(STATUS_PLAYING);
                        statusChange(STATUS_START);
                        break;
                }
                return false;
            }
        });

        seekBar = (SeekBar) activity.findViewById(R.id.app_video_seekBar);
        seekBar.setMax(1000);
        seekBar.setOnSeekBarChangeListener(mSeekListener);
        $.id(R.id.app_video_replay_icon).clicked(onClickListener);


        audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        final GestureDetector gestureDetector = new GestureDetector(activity, new PlayerGestureListener());


        View liveBox = activity.findViewById(R.id.app_video_box);
        liveBox.setClickable(true);
        liveBox.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (gestureDetector.onTouchEvent(motionEvent))
                    return true;

                // 处理手势结束
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        endGesture();
                        break;
                }

                return false;
            }
        });


        orientationEventListener = new OrientationEventListener(activity) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation >= 0 && orientation <= 30 || orientation >= 330 || (orientation >= 150 && orientation <= 210)) {
                    //竖屏
                    if (portrait) {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        orientationEventListener.disable();
                    }
                } else if ((orientation >= 90 && orientation <= 120) || (orientation >= 240 && orientation <= 300)) {
                    if (!portrait) {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        orientationEventListener.disable();
                    }
                }
            }
        };
        if (fullScreenOnly) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        portrait = getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        initHeight = activity.findViewById(R.id.app_video_box).getLayoutParams().height;
        hideAll();
        if (!playerSupport) {
            showStatus(activity.getResources().getString(R.string.not_support));
        }
    }

    /**
     * 手势结束
     */
    private void endGesture() {
        volume = -1;
        brightness = -1f;
        if (newPosition >= 0) {
            handler.removeMessages(MESSAGE_SEEK_NEW_POSITION);
            handler.sendEmptyMessage(MESSAGE_SEEK_NEW_POSITION);
        }
        handler.removeMessages(MESSAGE_HIDE_CENTER_BOX);
        handler.sendEmptyMessageDelayed(MESSAGE_HIDE_CENTER_BOX, 500);

    }

    private void statusChange(int newStatus) {
        status = newStatus;
        for (OnPlayStatusChangeLiseter onPlayStatusChangeLiseter : onPlayStatusChangeLiseters) {
            onPlayStatusChangeLiseter.statusChange(newStatus);
        }
        if (!isLive && newStatus == STATUS_COMPLETED) {
            handler.removeMessages(MESSAGE_SHOW_PROGRESS);
            hideAll();
//            $.id(R.id.app_video_replay).visible();
        } else if (newStatus == STATUS_ERROR) {
            handler.removeMessages(MESSAGE_SHOW_PROGRESS);
            hideAll();
            showStatus(activity.getResources().getString(R.string.small_problem));
        } else if (newStatus == STATUS_LOADING) {
            hideAll();
            $.id(R.id.app_video_loading).visible();
        } else if (newStatus == STATUS_PLAYING) {
            $.id(R.id.app_video_replay).gone();
            $.id(R.id.app_video_loading).gone();
            $.id(R.id.app_video_status).gone();
        }
    }

    private void hideAll() {
        $.id(R.id.app_video_replay).gone();
        $.id(R.id.app_video_loading).gone();
        $.id(R.id.app_video_status).gone();
        onControlPanelVisibilityChangeListener.change(false);
    }

    public void onPause() {
        show(0);//把系统状态栏显示出来
        if (status == STATUS_PLAYING) {
            ijkVideoView.pause();
            if (!isLive) {
                currentPosition = ijkVideoView.getCurrentPosition();
            }
        }
    }

    public void onResume() {
        if (status == STATUS_PLAYING) {
            if (isLive) {
                ijkVideoView.seekTo(0);
            } else {
                if (currentPosition > 0) {
                    ijkVideoView.seekTo(currentPosition);
                }
            }
            ijkVideoView.start();
            statusChange(STATUS_PLAYING);
        }
    }

    public void onConfigurationChanged(final Configuration newConfig) {
        portrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT;
        doOnConfigurationChanged(portrait);
    }

    private void doOnConfigurationChanged(final boolean portrait) {
        if (ijkVideoView != null && !fullScreenOnly) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    tryFullScreen(!portrait);
                    if (portrait) {
                        $.id(R.id.app_video_box).height(initHeight, false);
                    } else {
                        int heightPixels = activity.getResources().getDisplayMetrics().heightPixels;
                        int widthPixels = activity.getResources().getDisplayMetrics().widthPixels;
                        $.id(R.id.app_video_box).height(Math.min(heightPixels, widthPixels), false);
                    }
                }
            });
            orientationEventListener.enable();
        }
    }

    private void tryFullScreen(boolean fullScreen) {
        if (activity instanceof AppCompatActivity) {
            ActionBar supportActionBar = ((AppCompatActivity) activity).getSupportActionBar();
            if (supportActionBar != null) {
                if (fullScreen) {
                    supportActionBar.hide();
                } else {
                    supportActionBar.show();
                }
            }
        }
        setFullScreen(fullScreen);
    }

    private void setFullScreen(boolean fullScreen) {
        if (activity != null) {
            WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
            if (fullScreen) {
                attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                activity.getWindow().setAttributes(attrs);
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            } else {
                attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
                activity.getWindow().setAttributes(attrs);
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            }
        }

    }

    public void onDestroy() {
        orientationEventListener.disable();
        handler.removeCallbacksAndMessages(null);
        ijkVideoView.stopPlayback();
    }


    public void showStatus(String statusText) {
        $.id(R.id.app_video_status).visible();
        $.id(R.id.app_video_status_text).text("");
    }

    public void play(String url) {
        FlyLog.d("start url=%s", url);
        this.mPlayUrl = url;
        if (playerSupport) {
            $.id(R.id.app_video_loading).visible();
            ijkVideoView.setVideoPath(url);
            ijkVideoView.start();
            statusChange(STATUS_PLAYING);
        }
    }


    public void play(String url, int seek) {
        FlyLog.d("start url=%s,seek=%d", url, seek);
        this.mPlayUrl = url;
        if (playerSupport) {
            $.id(R.id.app_video_loading).visible();
            ijkVideoView.setVideoPath(url);
            ijkVideoView.seekTo(seek);
            ijkVideoView.start();
            statusChange(STATUS_PLAYING);
        }
    }

    public String getPlayUrl() {
        return mPlayUrl == null ? "" : mPlayUrl;
    }

    public void setPlayUrl(String url) {
        mPlayUrl = url;
    }

    private String generateTime(long time) {
        time = Math.min(Math.max(time, 0), 359999000);
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    private int getScreenOrientation() {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        return orientation;
    }

    /**
     * 滑动改变声音大小
     *
     * @param percent
     */
    private void onVolumeSlide(float percent) {
        if (volume == -1) {
            volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (volume < 0)
                volume = 0;
        }

        int index = (int) (percent * mMaxVolume) + volume;
        if (index > mMaxVolume)
            index = mMaxVolume;
        else if (index < 0)
            index = 0;

        // 变更声音
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

        // 变更进度条
        int i = (int) (index * 1.0 / mMaxVolume * 100);
        String s = i + "%";
        if (i == 0) {
            s = "off";
        }
        // 显示
        $.id(R.id.app_video_volume_icon).image(i == 0 ? R.drawable.ic_volume_off_white_36dp : R.drawable.ic_volume_up_white_36dp);
        $.id(R.id.app_video_brightness_box).gone();
        $.id(R.id.app_video_volume_box).visible();
        $.id(R.id.app_video_volume_box).visible();
        $.id(R.id.app_video_volume).text(s).visible();
    }

    private void onProgressSlide(float percent) {
        long position = ijkVideoView.getCurrentPosition();
        long duration = ijkVideoView.getDuration();
        long deltaMax = Math.min(100 * 1000, duration - position);
        long delta = (long) (deltaMax * percent);

        newPosition = delta + position;
        if (newPosition > duration) {
            newPosition = duration;
        } else if (newPosition <= 0) {
            newPosition = 0;
            delta = -position;
        }
        int showDelta = (int) delta / 1000;
        if (showDelta != 0) {
            $.id(R.id.app_video_fastForward_box).visible();
            String text = showDelta > 0 ? ("+" + showDelta) : "" + showDelta;
            $.id(R.id.app_video_fastForward).text(text + "s");
            $.id(R.id.app_video_fastForward_target).text(generateTime(newPosition) + "/");
            $.id(R.id.app_video_fastForward_all).text(generateTime(duration));
        }
    }

    /**
     * 滑动改变亮度
     *
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        if (brightness < 0) {
            brightness = activity.getWindow().getAttributes().screenBrightness;
            if (brightness <= 0.00f) {
                brightness = 0.50f;
            } else if (brightness < 0.01f) {
                brightness = 0.01f;
            }
        }
        Log.d(this.getClass().getSimpleName(), "brightness:" + brightness + ",percent:" + percent);
        $.id(R.id.app_video_brightness_box).visible();
        WindowManager.LayoutParams lpa = activity.getWindow().getAttributes();
        lpa.screenBrightness = brightness + percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
        $.id(R.id.app_video_brightness).text(((int) (lpa.screenBrightness * 100)) + "%");
        activity.getWindow().setAttributes(lpa);

    }

    public long setProgress() {
        long position = ijkVideoView.getCurrentPosition();
        long duration = ijkVideoView.getDuration();
        /**
         * #NOTE:时间大于总时长，已经播放完毕，(解决有些视频播放完了还一直播放的问题)
         */
        if (duration != -1 && position > duration) {
            FlyLog.e("curentpos=%d,sumPos=%d,playing is finish, but is already playing....", position, duration);
            ijkVideoView.pause();
            statusChange(STATUS_COMPLETED);
            oncomplete.run();
            return 0;
        }

        if (isDragging) {
            return 0;
        }

        if (seekBar != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                seekBar.setProgress((int) pos);
            }
            int percent = ijkVideoView.getBufferPercentage();
            seekBar.setSecondaryProgress(percent * 10);
        }

        this.duration = duration;
        $.id(R.id.app_video_currentTime).text(generateTime(position));
        $.id(R.id.app_video_endTime).text(generateTime(this.duration));
        try {
            if(isPlaying()){
                activity.mediaSession.notifyProgress((int) position,(int) duration);
            }
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
        return position;
    }

    public void hide(boolean force) {
        if (force) {
            handler.removeCallbacksAndMessages(null);
        }
        if (force || isShowing) {
            handler.removeMessages(MESSAGE_SHOW_PROGRESS);
            isShowing = false;
            onControlPanelVisibilityChangeListener.change(false);
        }

    }


    public void setFullScreenOnly(boolean fullScreenOnly) {
        this.fullScreenOnly = fullScreenOnly;
        tryFullScreen(fullScreenOnly);
        if (fullScreenOnly) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }

    /**
     * using constants in GiraffePlayer,eg: GiraffePlayer.SCALETYPE_FITPARENT
     *
     * @param scaleType
     */
    public void setScaleType(String scaleType) {
        if (SCALETYPE_FITPARENT.equals(scaleType)) {
            ijkVideoView.setAspectRatio(IRenderView.AR_ASPECT_FIT_PARENT);
        } else if (SCALETYPE_FILLPARENT.equals(scaleType)) {
            ijkVideoView.setAspectRatio(IRenderView.AR_ASPECT_FILL_PARENT);
        } else if (SCALETYPE_WRAPCONTENT.equals(scaleType)) {
            ijkVideoView.setAspectRatio(IRenderView.AR_ASPECT_WRAP_CONTENT);
        } else if (SCALETYPE_FITXY.equals(scaleType)) {
            ijkVideoView.setAspectRatio(IRenderView.AR_MATCH_PARENT);
        } else if (SCALETYPE_16_9.equals(scaleType)) {
            ijkVideoView.setAspectRatio(IRenderView.AR_16_9_FIT_PARENT);
        } else if (SCALETYPE_4_3.equals(scaleType)) {
            ijkVideoView.setAspectRatio(IRenderView.AR_4_3_FIT_PARENT);
        }
    }

    public void start() {
        if (ijkVideoView != null) {
            ijkVideoView.start();
            statusChange(STATUS_PLAYING);
        }
    }

    public void pause() {
        if (ijkVideoView != null) {
            ijkVideoView.pause();
            statusChange(STATUS_PAUSE);
        }
    }

    public boolean onBackPressed() {
        if (!fullScreenOnly && getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return true;
        }
        return false;
    }

    public void savePathUrl(final String path) {
        final String url = mPlayUrl;
        final int seek = getCurrentPosition();
        if (!TextUtils.isEmpty(path) && !TextUtils.isEmpty(url)) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!TextUtils.isEmpty(url) && url.startsWith(path)) {
                            SPUtil.set(activity, path + "VIDEO_URL", url);
                            SPUtil.set(activity, path + "VIDEO_SEEK", seek);
                            FlyLog.d("savePathUrl seek=%d,path=%s,url=%s", seek, path, url);
                        } else {
                            FlyLog.e("save failed! seek=%d,path=%s,url=%s", seek, path, url);
                        }
                    } catch (Exception e) {
                        FlyLog.e(e.toString());
                    }
                }
            });
        } else {
            FlyLog.e("save failed! seek=%d,path=%s,url=%s", seek, path, url);
        }
    }

    public String playPath = "";

    public void playSavePath(String path) {
        FlyLog.d("playSavePath path=%s", path);
        playPath = path;
        String url = (String) SPUtil.get(activity, path + "VIDEO_URL", "");
        int seek = (int) SPUtil.get(activity, path + "VIDEO_SEEK", 0);
        FlyLog.d("get Save url=%s,seek=%d", url, seek);
        if (TextUtils.isEmpty(url) || url.equals(mPlayUrl)) {
            FlyLog.e("start save is playing or empty so return, start url=%s", url);
            return;
        }
        if (!TextUtils.isEmpty(url)) {
            File file = new File(url);
            if (file.exists()) {
                play(url, seek);
            }
        } else {
            FlyLog.e("start file no exists url=%s", url);
            mPlayUrl = "";
        }
    }

    public void playNext() {
        activity.playNext();
    }

    public void playFore() {
        activity.playPrev();
    }

    class Query {
        private final Activity activity;
        public View view;

        public Query(Activity activity) {
            this.activity = activity;
        }

        public Query id(int id) {
            view = activity.findViewById(id);
            return this;
        }

        public Query image(int resId) {
            if (view instanceof ImageView) {
                ((ImageView) view).setImageResource(resId);
            }
            return this;
        }

        public Query visible() {
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }
            return this;
        }

        public Query gone() {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
            return this;
        }

        public Query invisible() {
            if (view != null) {
                view.setVisibility(View.INVISIBLE);
            }
            return this;
        }

        public Query clicked(View.OnClickListener handler) {
            if (view != null) {
                view.setOnClickListener(handler);
            }
            return this;
        }

        public Query text(CharSequence text) {
            if (view != null && view instanceof TextView) {
                ((TextView) view).setText(text);
            }
            return this;
        }

        public Query visibility(int visible) {
            if (view != null) {
                view.setVisibility(visible);
            }
            return this;
        }

        private void size(boolean width, int n, boolean dip) {

            if (view != null) {

                ViewGroup.LayoutParams lp = view.getLayoutParams();


                if (n > 0 && dip) {
                    n = dip2pixel(activity, n);
                }

                if (width) {
                    lp.width = n;
                } else {
                    lp.height = n;
                }

                view.setLayoutParams(lp);

            }

        }

        public void height(int height, boolean dip) {
            size(false, height, dip);
        }

        public int dip2pixel(Context context, float n) {
            int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, n, context.getResources().getDisplayMetrics());
            return value;
        }

        public float pixel2dip(Context context, float n) {
            Resources resources = context.getResources();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            float dp = n / (metrics.densityDpi / 160f);
            return dp;

        }
    }


    public class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {
        private boolean firstTouch;
        private boolean volumeControl;
        private boolean toSeek;

        @Override
        public boolean onDown(MotionEvent event) {
            try {
                if (event.getY() > 60) {
                    firstTouch = true;
                }
            }catch (Exception ee){
                FlyLog.e(ee.toString());
            }
            return super.onDown(event);

        }

        /**
         * 滑动
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            try {
                if (e1.getY() > 60) {
                    float mOldX = e1.getX(), mOldY = e1.getY();
                    float deltaY = mOldY - e2.getY();
                    float deltaX = mOldX - e2.getX();
                    if (firstTouch) {
                        toSeek = Math.abs(distanceX) >= Math.abs(distanceY);
                        volumeControl = mOldX > screenWidthPixels * 0.5f;
                        firstTouch = false;
                    }

                    if (toSeek) {
                        if (!isLive) {
                            onProgressSlide(-deltaX / ijkVideoView.getWidth());
                        }
                    } else {
                        float percent = deltaY / ijkVideoView.getHeight();
                        if (volumeControl) {
                            onVolumeSlide(percent);
                        } else {
                            onBrightnessSlide(percent);
                        }


                    }
                }
            }catch (Exception e){
                FlyLog.e(e.toString());
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (isShowing) {
                hide(false);
            } else {
                show(defaultTimeout);
            }
            activity.touchTime = 0;
            activity.showControlView(!activity.isShowControl);
            return true;
        }

        /**
         * 双击
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
//            ijkVideoView.toggleAspectRatio();
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }
    }

    /**
     * is player support this device
     *
     * @return
     */
    public boolean isPlayerSupport() {
        return playerSupport;
    }

    /**
     * 是否正在播放
     *
     * @return
     */
    public boolean isPlaying() {
        return ijkVideoView != null && ijkVideoView.isPlaying();
    }

    public void stop() {
        this.mPlayUrl = "";
        ijkVideoView.pause();
        ijkVideoView.stopPlayback();
        statusChange(STATUS_IDLE);
        seekBar.setProgress(0);
        showStatus("");
    }

    /**
     * seekTo position
     *
     * @param msec millisecond
     */
    public GiraffePlayer seekTo(int msec, boolean showControlPanle) {
        ijkVideoView.seekTo(msec);
        if (showControlPanle) {
            show(defaultTimeout);
        }
        return this;
    }

    public GiraffePlayer forward(float percent) {
        if (isLive || percent > 1 || percent < -1) {
            return this;
        }
        onProgressSlide(percent);
        handler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS);
        endGesture();
        return this;
    }

    public int getCurrentPosition() {
        return ijkVideoView == null ? 0 : ijkVideoView.getCurrentPosition();
    }

    /**
     * get video duration
     *
     * @return
     */
    public int getDuration() {
        return ijkVideoView.getDuration();
    }

    public GiraffePlayer playInFullScreen(boolean fullScreen) {
        if (fullScreen) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        return this;
    }


    public interface OnErrorListener {
        void onError(int what, int extra);
    }

    public interface OnControlPanelVisibilityChangeListener {
        void change(boolean isShowing);
    }

    public interface OnInfoListener {
        void onInfo(int what, int extra);
    }

    public GiraffePlayer onError(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
        return this;
    }

    public GiraffePlayer onComplete(Runnable complete) {
        this.oncomplete = complete;
        return this;
    }

    public GiraffePlayer onInfo(OnInfoListener onInfoListener) {
        this.onInfoListener = onInfoListener;
        return this;
    }

    public GiraffePlayer onControlPanelVisibilityChang(OnControlPanelVisibilityChangeListener listener) {
        this.onControlPanelVisibilityChangeListener = listener;
        return this;
    }

    /**
     * set is live (can't seek forward)
     *
     * @param isLive
     * @return
     */
    public GiraffePlayer live(boolean isLive) {
        this.isLive = isLive;
        return this;
    }

    public GiraffePlayer toggleAspectRatio() {
        if (ijkVideoView != null) {
            ijkVideoView.toggleAspectRatio();
        }
        return this;
    }

    public GiraffePlayer onControlPanelVisibilityChange(OnControlPanelVisibilityChangeListener listener) {
        this.onControlPanelVisibilityChangeListener = listener;
        return this;
    }

    public interface OnPlayStatusChangeLiseter {
        void statusChange(int statu);
    }

    private List<OnPlayStatusChangeLiseter> onPlayStatusChangeLiseters = new ArrayList<>();

    public void addStatusChangeLiseter(OnPlayStatusChangeLiseter onPlayStatusChangeLiseter) {
        onPlayStatusChangeLiseters.add(onPlayStatusChangeLiseter);
    }

    public void removeStatusChangeLiseter(OnPlayStatusChangeLiseter onPlayStatusChangeLiseter) {
        onPlayStatusChangeLiseters.remove(onPlayStatusChangeLiseter);
    }

    public int switchMode() {
        return ijkVideoView.toggleAspectRatio();
    }

}
