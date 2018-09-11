package com.jancar.media.model;

import android.content.Context;
import android.media.MediaPlayer;

import com.jancar.media.listener.IMusicPlayerListener;

import java.util.ArrayList;
import java.util.List;

public class MusicPlayer implements IMusicPlayer{
    private Context mContext;
    private List<IMusicPlayerListener> listeners = new ArrayList<>();
    private MediaPlayer mediaPlayer;

    private MusicPlayer() {
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
        this.mContext = mContext;
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public void close() {

    }

    private static class MusicPlayerHolder {
        public static final MusicPlayer sInstance = new MusicPlayer();
    }
}
