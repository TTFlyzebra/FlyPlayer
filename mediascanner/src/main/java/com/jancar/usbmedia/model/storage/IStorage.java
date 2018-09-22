package com.jancar.usbmedia.model.storage;



public interface IStorage {

    void addListener(IStorageListener iStorageListener);

    void removeListener(IStorageListener iStorageListener);

    void refresh();
}
