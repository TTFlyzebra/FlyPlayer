package com.jancar.media.model;

import android.content.Context;
import android.media.MediaPlayer;

import com.jancar.media.listener.IMusicPlayerListener;


public interface IMusicPlayer {
    void addListener(IMusicPlayerListener iMusicPlayerListener);

    void removeListener(IMusicPlayerListener iMusicPlayerListener);

    void init(Context context);

    void play(String url);

    String getPlayUrl();

    void start();

    void puase();

    boolean isPuase();

    boolean isPlaying();

    void stop();

    MediaPlayer getMediaPlay();
}
