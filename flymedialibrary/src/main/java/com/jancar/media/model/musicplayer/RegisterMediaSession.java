package com.jancar.media.model.musicplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.session.MediaSession;
import android.os.Build;
import android.view.KeyEvent;

import com.jancar.media.utils.FlyLog;

public class RegisterMediaSession {
    private MediaSession mMediaSession;
    private Context context;
    private IMusicPlayer musicPlayer;

    public RegisterMediaSession(Context context, IMusicPlayer bluetoothManager) {
        this.context = context;
        this.musicPlayer = bluetoothManager;
        setupMediaSession();
    }

    /**
     * 初始化并激活 MediaSession
     */
    @SuppressLint("WrongConstant")
    private void setupMediaSession() {
//        第二个参数 tag: 这个是用于调试用的,随便填写即可
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mMediaSession = new MediaSession(context, context.getPackageName());
            mMediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
//            mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
            mMediaSession.setCallback(new MediaSession.Callback() {
                @SuppressLint("Override")
                public boolean onMediaButtonEvent(Intent intent) {
                    // TODO Auto-generated method stub
                    KeyEvent keyEvent;
                    if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
                        keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                        if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                            FlyLog.d("media key keyEvent=%s", keyEvent.toString());
                            handleMediaButton(keyEvent);
                        }
                    }
                    return super.onMediaButtonEvent(intent);
                }
            });
        }
    }

    /**
     * @Title: requestMediaButton
     * @Description: 请求绑定系统媒体按键
     * @return: void
     */
    public void requestMediaButton() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (!mMediaSession.isActive()) {
                    mMediaSession.setActive(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @Title: releaseMediaButton
     * @Description: 释放系统媒体按键
     * @return: void
     */
    public void releaseMediaButton() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mMediaSession.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleMediaButton(KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();
        if (KeyEvent.KEYCODE_MEDIA_NEXT == keyCode) {
            FlyLog.d("media key next");
            musicPlayer.playNext();
        } else if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == keyCode) {
        } else if (KeyEvent.KEYCODE_HEADSETHOOK == keyCode) {
        } else if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == keyCode) {
            FlyLog.d("media key fore");
            musicPlayer.playPrev();
        } else if (KeyEvent.KEYCODE_MEDIA_STOP == keyCode) {
            FlyLog.d("media key stop");
            musicPlayer.pause();
        } else if (KeyEvent.KEYCODE_MEDIA_PAUSE == keyCode) {
            FlyLog.d("media key pause");
            musicPlayer.pause();
        } else if (KeyEvent.KEYCODE_MEDIA_PLAY == keyCode) {
            FlyLog.d("media key start");
            musicPlayer.start();
        }
    }
}
