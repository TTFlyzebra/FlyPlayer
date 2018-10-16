package com.jancar.media.base;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;

import com.jancar.media.data.Image;
import com.jancar.media.data.Music;
import com.jancar.media.data.StorageInfo;
import com.jancar.media.data.Video;
import com.jancar.media.model.listener.IUsbMediaListener;
import com.jancar.media.model.mediascan.IMediaScan;
import com.jancar.media.model.mediascan.MediaScan;
import com.jancar.media.utils.FlyLog;
import com.jancar.media.utils.SPUtil;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity implements IUsbMediaListener {
    protected IMediaScan usbMediaScan = MediaScan.getInstance();
    public String currenPath = "NORMAL";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usbMediaScan.open();
    }

    @Override
    protected void onDestroy() {
        usbMediaScan.close();
        super.onDestroy();
    }

    public void replaceFragment(String fName, @IdRes int resID) {
        FlyLog.d("replaceFragment %s.fragment.%s",getPackageName(), fName);
        try {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Class<?> cls = Class.forName(getPackageName()+".fragment." + fName);
            Constructor<?> cons = cls.getConstructor();
            Fragment fragment = (Fragment) cons.newInstance();
            ft.replace(resID, fragment).commit();
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
    public void stogrePathChange(String path) {
        currenPath = path;
        SPUtil.set(this, "SAVA_PATH", currenPath);
        for(IUsbMediaListener listener:listeners){
            listener.stogrePathChange(path);
        }

    }

    @Override
    public void scanFinish(String path) {
        for(IUsbMediaListener listener:listeners){
            listener.scanFinish(path);
        }
    }

    @Override
    public void scanServiceConneted() {
        String path = (String) SPUtil.get(this, "SAVA_PATH", "/storage/emulated/0");
        usbMediaScan.openStorager(new StorageInfo(path));
        for(IUsbMediaListener listener:listeners){
            listener.scanServiceConneted();
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
