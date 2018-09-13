package com.jancar.media.activity;

import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.jancar.media.R;
import com.jancar.media.base.BaseActivity;
import com.jancar.media.listener.IUsbMediaListener;

import java.util.List;

public class PhotoActivity extends BaseActivity implements IUsbMediaListener{
    private PhotoView photoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        photoView = (PhotoView)findViewById(R.id.ac_photo_pv01);
        usbMediaScan.addListener(this);
    }

    @Override
    protected void onDestroy() {
        usbMediaScan.removeListener(this);
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
        Glide.with(this).load(imageUrlList.get(0)).placeholder(R.drawable.media_default_image).into(photoView);
    }

    @Override
    public void usbRemove(String usbstore) {

    }
}
