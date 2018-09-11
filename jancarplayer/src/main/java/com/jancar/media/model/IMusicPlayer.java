package com.jancar.media.model;

import android.content.Context;

import com.jancar.media.listener.IMusicPlayerListener;


public interface IMusicPlayer {
    void addListener(IMusicPlayerListener iMusicPlayerListener);

    void removeListener(IMusicPlayerListener iMusicPlayerListener);

    void init(Context context);

    void close();
}
