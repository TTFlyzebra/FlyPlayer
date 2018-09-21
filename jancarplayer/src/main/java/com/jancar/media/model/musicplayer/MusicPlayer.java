package com.jancar.media.model.musicplayer;

import android.content.Context;
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
//        MediaPlayer.OnInfoListener,
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

    private MusicPlayer() {
    }

    public void onPrepared(MediaPlayer mp) {
        mp.start();
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
            mPlayStatus=STATUS_STARTPLAY;
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
            mMediaPlayer.start();
            mPlayStatus = STATUS_PLAYING;
            notifyStatus();
        }
    }

    @Override
    public void puase() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            mPlayStatus = STATUS_PAUSE;
            notifyStatus();
        }
    }

    @Override
    public boolean isPuase() {
        return mPlayStatus == STATUS_PAUSE;
    }

    @Override
    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
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
                mPlayPos = (mPlayPos + 1) % (mPlayUrls.size() - 1);
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
        play(mPlayUrls.get(mPlayPos).url);
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
//
//    @Override
//    public boolean onInfo(MediaPlayer mp, int what, int extra) {
//        FlyLog.d("onInfo what=%d,extra=%d", what, extra);
//        switch (what) {
//            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
//                mPlayStatus = STATUS_LOADING;
//                break;
//            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
//            case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
//                mPlayStatus = STATUS_PLAYING;
//                break;
//        }
//        notifyStatus();
//        return false;
//    }

    private void notifyStatus() {
        mPlayPos = getPlayPos();
        for (IMusicPlayerListener iMusicPlayerListener : listeners) {
            iMusicPlayerListener.playStatusChange(mPlayStatus);
        }
    }
}

