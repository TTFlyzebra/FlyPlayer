package com.jancar.media.model.musicplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.jancar.media.data.Music;
import com.jancar.media.model.listener.IMusicPlayerListener;
import com.jancar.media.utils.FlyLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicPlayer implements IMusicPlayer,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener,
        IPlayStatus,
        ILoopStatus {
    private Context mContext;
    private List<IMusicPlayerListener> listeners = new ArrayList<>();
    private MediaPlayer mMediaPlayer;
    private int mPlayStatus = STATUS_IDLE;
    private int mLoopStatus = LOOP_ALL;
    private String mPlayUrl = "";
    private List<Music> mPlayUrls = new ArrayList<>();
    private int mPlayPos = -1;
    private Map<String, Integer> mPosMap = new HashMap<>();
    private AudioManager mAudioManager;

    private MusicPlayer() {
    }

    public void onPrepared(MediaPlayer mp) {
        start();
        mPlayStatus = STATUS_PLAYING;
        notifyStatus();
    }

    private static class MusicPlayerHolder {
        public static final MusicPlayer sInstance = new MusicPlayer();
    }

    public static MusicPlayer getInstance() {
        return MusicPlayerHolder.sInstance;
    }


    @Override
    public void addListener(IMusicPlayerListener iMusicPlayerListener) {
        listeners.add(iMusicPlayerListener);
    }

    @Override
    public void removeListener(IMusicPlayerListener iMusicPlayerListener) {
        listeners.remove(iMusicPlayerListener);
    }

    @Override
    public void init(Context context) {
        this.mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

    }

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnPreparedListener(this);
//        mMediaPlayer.setOnInfoListener(this);
    }

    @Override
    public void play(String url) {
        FlyLog.d("play url=%s", url);
        try {
            this.mPlayUrl = url;
            if (mMediaPlayer == null) {
                initMediaPlayer();
            } else {
                mMediaPlayer.reset();
            }
            mPlayStatus = STATUS_STARTPLAY;
            notifyStatus();
            mMediaPlayer.setDataSource(mPlayUrl);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getPlayUrl() {
        return mPlayUrl;
    }

    @Override
    public void start() {
        if (mMediaPlayer != null) {
            requestAudioFocus();
            mMediaPlayer.start();
            mPlayStatus = STATUS_PLAYING;
            notifyStatus();
        }
    }

    @Override
    public void pause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            abandonAudioFocus();
            mPlayStatus = STATUS_PAUSE;
            notifyStatus();
        }
    }

    @Override
    public boolean isPause() {
        return mPlayStatus == STATUS_PAUSE;
    }

    @Override
    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        abandonAudioFocus();
        mPlayStatus = STATUS_IDLE;
        notifyStatus();
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    @Override
    public int getCurrentPosition() {
        return mMediaPlayer == null ? 0 : mMediaPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return mMediaPlayer == null ? 0 : mMediaPlayer.getDuration();
    }

    @Override
    public void setPlayUrls(List<Music> urls) {
        mPlayUrls.clear();
        if (urls == null || urls.isEmpty()) {
            mPlayPos = -1;
            stop();
        } else {
            mPlayUrls.addAll(urls);
            mPosMap.clear();
            for (int i = 0; i < mPlayUrls.size(); i++) {
                mPosMap.put(mPlayUrls.get(i).url, i);
            }
            if (isPlaying()) {
                mPlayPos = getPlayPos();
                if (mPlayPos == -1) {
                    mPlayPos = 0;
                    play(mPlayUrls.get(0).url);
                }
            } else {
                mPlayPos = 0;
                play(mPlayUrls.get(0).url);
            }
        }
        FlyLog.d("setPlayUrls mPlayPos=%d", mPlayPos);
    }

    @Override
    public void playNext() {
        switch (mLoopStatus) {
            case LOOP_RAND:
                mPlayPos = (int) (Math.random() * mPlayUrls.size());
                break;
            case LOOP_ALL:
            case LOOP_ONE:
                mPlayPos = (mPlayPos + 1) % (mPlayUrls.size());
                break;
        }
        play(mPlayUrls.get(mPlayPos).url);
    }

    @Override
    public void playFore() {
        switch (mLoopStatus) {
            case LOOP_RAND:
                mPlayPos = (int) (Math.random() * mPlayUrls.size());
                break;
            case LOOP_ALL:
            case LOOP_ONE:
                mPlayPos = Math.max(0, mPlayPos - 1);
                break;
        }
        if (mPlayUrls != null && mPlayUrls.size() > mPlayPos) {
            play(mPlayUrls.get(mPlayPos).url);
        }
    }

    @Override
    public int getPlayPos() {
        if (mPosMap.get(mPlayUrl) == null) {
            return -1;
        }
        return mPosMap.get(mPlayUrl);
    }

    @Override
    public void switchLoopStatus() {
        mLoopStatus = (mLoopStatus + 1) % 3;
        for (IMusicPlayerListener iMusicPlayerListener : listeners) {
            iMusicPlayerListener.loopStatusChange(mLoopStatus);
        }
    }

    @Override
    public void seekTo(int seekPos) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(seekPos);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        FlyLog.d("onCompletion---");
        mPlayStatus = STATUS_COMPLETED;
        notifyStatus();
        switch (mLoopStatus) {
            case LOOP_RAND:
            case LOOP_ALL:
                playNext();
                break;
            case LOOP_ONE:
                play(mPlayUrl);
                break;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        FlyLog.d("onError what=%d,extra=%d", what, extra);
        mPlayStatus = STATUS_ERROR;
        notifyStatus();
        playNext();
        return false;
    }

    private void notifyStatus() {
        mPlayPos = getPlayPos();
        for (IMusicPlayerListener iMusicPlayerListener : listeners) {
            iMusicPlayerListener.playStatusChange(mPlayStatus);
        }
    }

    private AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            FlyLog.d("onAudioFocusChange focusChange=%d",focusChange);
            try {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_LOSS:
                        pause();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        if (isPlaying()) {
                            pause();
                        }
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN:
                        if (isPause()) {
                            start();
                        }
                        break;
                }
            } catch (Exception e) {
                FlyLog.e(e.toString());
            }
        }
    };


    private void requestAudioFocus() {
        mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    }


    private void abandonAudioFocus() {
        mAudioManager.abandonAudioFocus(mAudioFocusListener);
    }
}

