package com.jancar.media.model.usbmediascan;

import com.jancar.media.data.StorageInfo;
import com.jancar.media.model.listener.IUsbMediaListener;

public interface IUsbMediaScan{
    void addListener(IUsbMediaListener iUsbMediaListener);

    void removeListener(IUsbMediaListener iUsbMediaListener);

    void open();

    void close();

    void openStorager(StorageInfo storageInfo);
}
