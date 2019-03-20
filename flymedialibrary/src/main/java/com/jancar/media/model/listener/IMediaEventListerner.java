package com.jancar.media.model.listener;

public interface IMediaEventListerner {
    void playNext();

    void playPrev();

    void playOrPause();

    void start();

    void pause();

    void KEY_FF();

    void KEY_FB();

    void KEY_REPEAT();

    void KEY_SHUFFLE();

    void KEY_LIST();
}
