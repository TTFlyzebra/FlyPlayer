package com.jancar.media.base;

import android.app.Fragment;
import android.os.Bundle;

import com.jancar.media.listener.IUsbMediaListener;
import com.jancar.media.model.IUsbMediaScan;
import com.jancar.media.model.UsbMedia;

import java.util.List;

public class BaseFragment extends Fragment implements IUsbMediaListener {
    protected IUsbMediaScan usbMediaScan = UsbMedia.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        usbMediaScan.addListener(this);
    }

    @Override
    public void onStop() {
        usbMediaScan.removeListener(this);
        super.onStop();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void musicUrlList(List<String> musicUrlList) {

    }

    @Override
    public void videoUrlList(List<String> videoUrlList) {

    }

    @Override
    public void imageUrlList(List<String> imageUrlList) {

    }

    @Override
    public void usbRemove(String usbstore) {

    }
}
