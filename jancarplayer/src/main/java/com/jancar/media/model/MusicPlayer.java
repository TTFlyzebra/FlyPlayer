package com.jancar.media.model;

import android.content.Context;
import android.media.MediaPlayer;

import com.jancar.media.listener.IMusicPlayerListener;
import com.jancar.media.utils.FlyLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayer implements IMusicPlayer,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnInfoListener {
    private Context mContext;
    private List<IMusicPlayerListener> listeners = new ArrayList<>();
    private MediaPlayer mMediaPlayer;
    public static final int STATUS_ERROR = -1;
    public static final int STATUS_IDLE = 0;
    public static final int STATUS_LOADING = 1;
    public static final int STATUS_PLAYING = 2;
    public static final int STATUS_PAUSE = 3;
    public static final int STATUS_COMPLETED = 4;
    private int status = STATUS_IDLE;
    private String playUrl = "";


    private MusicPlayer() {
    }

    public void onPrepared(MediaPlayer mp) {
        mp.start();
        status = STATUS_PLAYING;
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
        mMediaPlayer.setOnInfoListener(this);
    }

    @Override
    public void play(String url) {
        FlyLog.d("play url=%s",url);
        try {
            this.playUrl = url;
            if (mMediaPlayer == null) {
                initMediaPlayer();
            }else{
                mMediaPlayer.reset();
            }
            mMediaPlayer.setDataSource(playUrl);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getPlayUrl() {
        return playUrl;
    }

    @Override
    public void start() {
        if(mMediaPlayer!=null) {
            mMediaPlayer.start();
            status = STATUS_PLAYING;
            notifyStatus();
        }
    }

    @Override
    public void puase() {
        if(mMediaPlayer!=null){
            mMediaPlayer.pause();
            status = STATUS_PAUSE;
            notifyStatus();
        }
    }

    @Override
    public boolean isPuase() {
        return status==STATUS_PAUSE;
    }

    @Override
    public void stop() {
        if(mMediaPlayer!=null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        status = STATUS_IDLE;
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    @Override
    public int getCurrentPosition() {
        return mMediaPlayer==null?0:mMediaPlayer.getCurrentPosition();
    }

    @Override
    public MediaPlayer getMediaPlay() {
        return mMediaPlayer;
    }

    @Override
    public void seekTo(int seekPos) {
        if(mMediaPlayer!=null){
            mMediaPlayer.seekTo(seekPos);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        FlyLog.d("onCompletion---");
        status = STATUS_COMPLETED;
        notifyStatus();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        FlyLog.d("onError what=%d,extra=%d",what,extra);
        status = STATUS_ERROR;
        notifyStatus();
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        FlyLog.d("onInfo what=%d,extra=%d",what,extra);
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                status = STATUS_LOADING;
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
            case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                status = STATUS_PLAYING;
                break;
        }
        notifyStatus();
        return false;
    }

    private void notifyStatus() {
        for (IMusicPlayerListener iMusicPlayerListener : listeners) {
            iMusicPlayerListener.statusChange(status);
        }
    }
}

