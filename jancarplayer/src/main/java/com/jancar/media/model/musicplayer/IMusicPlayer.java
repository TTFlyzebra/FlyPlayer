package com.jancar.media.model.musicplayer;

import android.content.Context;

import com.jancar.media.data.Music;
import com.jancar.media.model.listener.IMusicPlayerListener;

import java.util.List;


public interface IMusicPlayer {

    void addListener(IMusicPlayerListener iMusicPlayerListener);

    void removeListener(IMusicPlayerListener iMusicPlayerListener);

    void init(Context context);

    String getPlayUrl();

    void play(String url);

    void start();

    void pause();

    void stop();

    boolean isPause();

    boolean isPlaying();

    void seekTo(int seekPos);

    int getCurrentPosition();

    int getDuration();

    void setPlayUrls(List<Music> urls);

    void playNext();

    void playFore();

    int getPlayPos();

    void switchLoopStatus();

    void playSave();

    void setVolume(float v, float v1);
}
