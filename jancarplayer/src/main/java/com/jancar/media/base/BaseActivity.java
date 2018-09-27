package com.jancar.media.base;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jancar.media.R;
import com.jancar.media.data.Image;
import com.jancar.media.data.Music;
import com.jancar.media.data.Video;
import com.jancar.media.model.listener.IUsbMediaListener;
import com.jancar.media.model.mediascan.IMediaScan;
import com.jancar.media.model.mediascan.MediaScan;
import com.jancar.media.utils.FlyLog;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity implements IUsbMediaListener{
    protected IMediaScan usbMediaScan = MediaScan.getInstance();
    public String currenPath = "NORMAL";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usbMediaScan.open();
    }

    @Override
    protected void onStart() {
        super.onStart();
        usbMediaScan.addListener(this);
    }

    @Override
    protected void onStop() {
        usbMediaScan.removeListener(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        usbMediaScan.close();
        super.onDestroy();
    }

    public void replaceFragment(String fName) {
        FlyLog.d("replaceFragment com.jancar.media.fragment.%s", fName);
        try {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Class<?> cls = Class.forName("com.jancar.media.fragment." + fName);
            Constructor<?> cons = cls.getConstructor();
            Fragment fragment = (Fragment) cons.newInstance();
            ft.replace(R.id.ac_replace_fragment, fragment).commit();
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }

    @Override
    public void musicUrlList(List<Music> musicUrlList) {
        for(IUsbMediaListener listener:listeners){
            listener.musicUrlList(musicUrlList);
        }
    }

    @Override
    public void musicID3UrlList(List<Music> musicUrlList) {
        for(IUsbMediaListener listener:listeners){
            listener.musicID3UrlList(musicUrlList);
        }

    }

    @Override
    public void videoUrlList(List<Video> videoUrlList) {
        for(IUsbMediaListener listener:listeners){
            listener.videoUrlList(videoUrlList);
        }

    }

    @Override
    public void imageUrlList(List<Image> imageUrlList) {
        for(IUsbMediaListener listener:listeners){
            listener.imageUrlList(imageUrlList);
        }

    }

    @Override
    public void changePath(String path) {
        currenPath = path;
        for(IUsbMediaListener listener:listeners){
            listener.changePath(path);
        }

    }


    private List<IUsbMediaListener> listeners = new ArrayList<>();

    public void addListener(IUsbMediaListener iUsbMediaListener) {
        listeners.add(iUsbMediaListener);
    }

    public void removeListener(IUsbMediaListener iUsbMediaListener) {
        listeners.remove(iUsbMediaListener);
    }
}
