package com.jancar.music.model.storage;


import com.jancar.media.model.listener.IStorageListener;

public interface IStorage {

    void addListener(IStorageListener iStorageListener);

    void removeListener(IStorageListener iStorageListener);

    void refresh();
}
