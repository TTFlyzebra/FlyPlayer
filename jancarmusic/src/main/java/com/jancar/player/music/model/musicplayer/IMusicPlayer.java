package com.jancar.player.music.model.musicplayer;

import android.content.Context;

import com.jancar.media.data.Music;
import com.jancar.player.music.model.listener.IMusicPlayerListener;

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

    void playSave(String path);

    void setVolume(float v, float v1);

    void savePlayUrl(String path);
}
