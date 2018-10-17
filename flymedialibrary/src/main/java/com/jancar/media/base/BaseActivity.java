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
    public static final String DEF_PATH = "/storage/emulated/0";
    public String currenPath = DEF_PATH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currenPath = getSavePath();
        usbMediaScan.addListener(this);
        usbMediaScan.open();
    }

    @Override
    protected void onDestroy() {
        usbMediaScan.close();
        usbMediaScan.removeListener(this);
        super.onDestroy();
    }

    public void replaceFragment(String fName, @IdRes int resID) {
        FlyLog.d("replaceFragment %s.fragment.%s", getPackageName(), fName);
        try {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Class<?> cls = Class.forName(getPackageName() + ".fragment." + fName);
            Constructor<?> cons = cls.getConstructor();
            Fragment fragment = (Fragment) cons.newInstance();
            ft.replace(resID, fragment).commit();
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }

    @Override
    public void musicUrlList(List<Music> musicUrlList) {
        for (IUsbMediaListener listener : fragmentListeners) {
            listener.musicUrlList(musicUrlList);
        }
    }

    @Override
    public void musicID3UrlList(List<Music> musicUrlList) {
        for (IUsbMediaListener listener : fragmentListeners) {
            listener.musicID3UrlList(musicUrlList);
        }

    }

    @Override
    public void videoUrlList(List<Video> videoUrlList) {
        for (IUsbMediaListener listener : fragmentListeners) {
            listener.videoUrlList(videoUrlList);
        }

    }

    @Override
    public void imageUrlList(List<Image> imageUrlList) {
        for (IUsbMediaListener listener : fragmentListeners) {
            listener.imageUrlList(imageUrlList);
        }

    }

    @Override
    public void notifyPathChange(String path) {
        //TODO::是切换了盘符保存还是播放了歌曲保存
        currenPath = path;
        setSavePath(currenPath);
        for (IUsbMediaListener listener : fragmentListeners) {
            listener.notifyPathChange(path);
        }

    }

    @Override
    public void scanFinish(String path) {
        for (IUsbMediaListener listener : fragmentListeners) {
            listener.scanFinish(path);
        }
    }

    @Override
    public void scanServiceConneted() {
        usbMediaScan.openStorager(new StorageInfo(currenPath));
        FlyLog.d("scanServiceConneted start scan path=%s", currenPath);
        for (IUsbMediaListener listener : fragmentListeners) {
            listener.scanServiceConneted();
        }
    }

    public void setSavePath(String path) {
        SPUtil.set(this, "SAVA_PATH", path);
    }

    public String getSavePath() {
        return (String) SPUtil.get(this, "SAVA_PATH", DEF_PATH);
    }

    private List<IUsbMediaListener> fragmentListeners = new ArrayList<>();

    public void addListener(IUsbMediaListener iUsbMediaListener) {
        fragmentListeners.add(iUsbMediaListener);
    }

    public void removeListener(IUsbMediaListener iUsbMediaListener) {
        fragmentListeners.remove(iUsbMediaListener);
    }
}
