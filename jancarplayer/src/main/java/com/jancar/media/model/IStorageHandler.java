package com.jancar.media.model;


import com.jancar.media.listener.IStorageListener;

public interface IStorageHandler {

    void addListener(IStorageListener iStorageListener);

    void removeListener(IStorageListener iStorageListener);
}
