package com.jancar.media.model.mediascan;

import com.jancar.media.data.StorageInfo;
import com.jancar.media.model.listener.IUsbMediaListener;

public interface IMediaScan {
    void addListener(IUsbMediaListener iUsbMediaListener);

    void removeListener(IUsbMediaListener iUsbMediaListener);

    void open();

    void close();

    void openStorager(StorageInfo storageInfo);
}
