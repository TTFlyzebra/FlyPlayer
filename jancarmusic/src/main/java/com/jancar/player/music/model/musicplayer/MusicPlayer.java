package com.jancar.player.music.model.musicplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.text.TextUtils;

import com.jancar.media.data.Music;
import com.jancar.media.utils.FlyLog;
import com.jancar.media.utils.SPUtil;
import com.jancar.player.music.model.listener.IMusicPlayerListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MusicPlayer implements IMusicPlayer,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener,
        IPlayStatus,
        ILoopStatus {
    private final static Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private Context mContext;
    private List<IMusicPlayerListener> listeners = new ArrayList<>();
    private MediaPlayer mMediaPlayer;
    private int mPlayStatus = STATUS_IDLE;
    private int mLoopStatus = LOOP_ALL;
    private String mPlayUrl = "";
    private List<Music> mPlayUrls = new ArrayList<>();
    private int mPlayPos = -1;
    private String playPath = "";
    private Map<String, Integer> mPosMap = new HashMap<>();

    private MusicPlayer() {
    }

    public void onPrepared(MediaPlayer mp) {
        if (saveSeek > 0) {
            seekTo(saveSeek);
            saveSeek = 0;
        }
        start();
        savePathUrl(playPath);
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
            mPlayStatus = STATUS_STARTPLAY;
            notifyStatus();
            mMediaPlayer.setDataSource(mPlayUrl);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int saveSeek = 0;

    private void play(String mPlayUrl, int seek) {
        FlyLog.d("play url=%s,seek=%d", mPlayUrl, seek);
        saveSeek = seek;
        try {
            this.mPlayUrl = mPlayUrl;
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
            mMediaPlayer.start();
            mPlayStatus = STATUS_PLAYING;
            notifyStatus();
        }
    }

    @Override
    public void pause() {
        if (mMediaPlayer != null) {
            savePathUrl(playPath);
            mMediaPlayer.pause();
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
        FlyLog.d();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mPlayUrl = "";
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
        if (!(new File(mPlayUrl).exists())) {
            mPlayUrl = "";
        }
        if(urls==null||urls.isEmpty()) return;

        mPlayUrls.addAll(urls);
        mPosMap.clear();
        for (int i = 0; i < mPlayUrls.size(); i++) {
            mPosMap.put(mPlayUrls.get(i).url, i);
        }
        if (!TextUtils.isEmpty(mPlayUrl)) {
            mPlayPos = getPlayPos();
        } else {
            mPlayPos = 0;
            play(mPlayUrls.get(0).url);
        }
        FlyLog.d("setPlayUrls mPlayPos=%d", mPlayPos);
    }

    @Override
    public void playNext() {
        FlyLog.d("playNext");
        switch (mLoopStatus) {
            case LOOP_RAND:
                mPlayPos = (int) (Math.random() * mPlayUrls.size());
                break;
            case LOOP_ALL:
            case LOOP_ONE:
                if (mPlayUrls != null && !mPlayUrls.isEmpty()) {
                    mPlayPos = (mPlayPos + 1) % (mPlayUrls.size());
                } else {
                    mPlayPos = -1;
                }
                break;
        }
        if (mPlayPos >= 0 && mPlayUrls != null && mPlayUrls.size() > mPlayPos) {
            play(mPlayUrls.get(mPlayPos).url);
        }
    }

    @Override
    public void playFore() {
        FlyLog.d("playFore");
        switch (mLoopStatus) {
            case LOOP_RAND:
                mPlayPos = (int) (Math.random() * mPlayUrls.size());
                break;
            case LOOP_ALL:
            case LOOP_ONE:
                if (mPlayUrls != null && !mPlayUrls.isEmpty()) {
                    mPlayPos = (mPlayPos - 1 + mPlayUrls.size()) % mPlayUrls.size();
                } else {
                    mPlayPos = -1;
                }
                break;
        }
        if (mPlayPos >= 0 && mPlayUrls != null && mPlayUrls.size() > mPlayPos) {
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
    public void savePathUrl(final String path) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                int seek = getCurrentPosition();
                SPUtil.set(mContext, path + "MUSIC_URL", mPlayUrl);
                SPUtil.set(mContext, path + "MUSIC_SEEK", seek);
                FlyLog.d("savePathUrl seek=%d,path=%s,url=%s",seek, path, mPlayUrl);
            }
        });
    }


    @Override
    public void playSavePath(String path) {
        FlyLog.d("playSavePath path=%s", path);
        playPath = path;
        String url = (String) SPUtil.get(mContext, playPath + "MUSIC_URL", "");
        int seek = (int) SPUtil.get(mContext, playPath + "MUSIC_SEEK", 0);
        FlyLog.d("get Save url=%s,seek=%d", url, seek);
        if (TextUtils.isEmpty(url) || url.equals(mPlayUrl)) {
            FlyLog.e("play save is playing so return, play url=%s", url);
            return;
        }
        if (!TextUtils.isEmpty(url)) {
            File file = new File(url);
            if (file.exists()) {
                play(url, seek);
            }
        } else {
            FlyLog.e("play file no exists url=%s", url);
            mPlayUrl = "";
            saveSeek = 0;
            savePathUrl(playPath);
        }
    }

    @Override
    public void setVolume(float v, float v1) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(v, v1);
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
        FlyLog.d("onCompletion url=%s", mPlayUrl);
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


}

