package com.jancar.media.model.musicplayer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.media.session.MediaSession;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.KeyEvent;

import com.jancar.media.model.musicplayer.IMusicPlayer;

public class RegisterMusicSession {
    private String TAG = "BluetoothMusic";
    private MediaSession mMediaSession;
    private Context context;
    private IMusicPlayer musicPlayer;

    public RegisterMusicSession(Context context, IMusicPlayer bluetoothManager) {
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
            mMediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS|MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
            //指明支持的按键信息类型
            mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS|MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
            Log.e(TAG, "setupMediaSession");
            mMediaSession.setCallback(new MediaSession.Callback() {

                @SuppressLint("Override")
                public boolean onMediaButtonEvent(Intent intent) {
                    // TODO Auto-generated method stub
                    KeyEvent keyEvent;
                    if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
                        keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                        if (keyEvent != null) {
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
            Log.e(TAG, "requestMediaButton:" + context.getPackageName());
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
            Log.e(TAG, "releaseMediaButton:" + context.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleMediaButton(KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();
        if (KeyEvent.KEYCODE_MEDIA_NEXT == keyCode) {
            musicPlayer.playFore();
        } else if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == keyCode) {
        } else if (KeyEvent.KEYCODE_HEADSETHOOK == keyCode) {
        } else if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == keyCode) {
            musicPlayer.playNext();
        } else if (KeyEvent.KEYCODE_MEDIA_STOP == keyCode) {
            musicPlayer.pause();
        } else if (KeyEvent.KEYCODE_MEDIA_PAUSE == keyCode) {
            musicPlayer.pause();
        } else if (KeyEvent.KEYCODE_MEDIA_PLAY == keyCode) {
            musicPlayer.start();
        }
    }
}
