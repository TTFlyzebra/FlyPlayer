package com.jancar.media.model.mediaSession;

import com.jancar.media.model.listener.IMediaEventListerner;

public interface IMediaSession {

    void notifyPlayState(int playStatus);

    void init();

    void release();

    void notifyRepeat(int staut);

    void notifyPlayId(int current,int total);

    void notifyProgress(int position, int duration);

    void notifyPlayUri(String title);

    void notifyId3(String title, String artist, String album, byte[] albumImageData);

    void addEventListener(IMediaEventListerner listerner);

    void removeEventListener(IMediaEventListerner listerner);
}
