package com.jancar.media.model.listener;

public interface IMediaEventListerner {
    void playNext();

    void playPrev();

    void playOrPause();

    void start();

    void pause();
}
