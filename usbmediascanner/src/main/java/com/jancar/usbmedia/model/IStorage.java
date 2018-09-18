package com.jancar.usbmedia.model;



public interface IStorage {

    void addListener(IStorageListener iStorageListener);

    void removeListener(IStorageListener iStorageListener);

    void refresh();
}
