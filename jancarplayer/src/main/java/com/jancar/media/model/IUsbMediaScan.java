package com.jancar.media.model;

import android.content.Context;

import com.jancar.media.listener.IUsbMediaListener;

public interface IUsbMediaScan{
    void addListener(IUsbMediaListener iUsbMediaListener);

    void removeListener(IUsbMediaListener iUsbMediaListener);

    void init(Context context);

    void close();
}
