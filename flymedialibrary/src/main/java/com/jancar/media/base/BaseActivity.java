package com.jancar.media.base;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;

import com.jancar.media.data.Image;
import com.jancar.media.data.Music;
import com.jancar.media.data.StorageInfo;
import com.jancar.media.data.Video;
import com.jancar.media.model.listener.IStorageListener;
import com.jancar.media.model.listener.IUsbMediaListener;
import com.jancar.media.model.mediascan.IMediaScan;
import com.jancar.media.model.mediascan.MediaScan;
import com.jancar.media.model.storage.IStorage;
import com.jancar.media.model.storage.Storage;
import com.jancar.media.utils.CleanLeakUtils;
import com.jancar.media.utils.FlyLog;
import com.jancar.media.utils.SPUtil;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity implements IUsbMediaListener,IStorageListener {
    protected IMediaScan usbMediaScan = MediaScan.getInstance();
    public static final String DEF_PATH = "/storage/emulated/0";
    public static String currenPath = DEF_PATH;
    protected static boolean isStop = false;
    public static int StorageNum;
    private IStorage iStorage = Storage.getInstance();

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currenPath = getSavePath();

        iStorage.addListener(this);

        usbMediaScan.addListener(this);
        usbMediaScan.open();
    }

    @Override
    protected void onStart() {
        super.onStart();
        isStop = false;
        /**
         * 程序于后台被拔掉U盘，已经打不开当前盘符，刷新为当前磁盘
         */
        if (!(new File(currenPath).exists())) {
            usbMediaScan.openStorager(new StorageInfo("REFRESH"));
        }
    }


    @Override
    protected void onStop() {
        isStop = true;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        iStorage.removeListener(this);
        usbMediaScan.close();
        usbMediaScan.removeListener(this);
        CleanLeakUtils.fixInputMethodManagerLeak(this);
        super.onDestroy();
    }

    public void replaceFragment(String fName, @IdRes int resID) {
        FlyLog.d("replaceFragment %s.fragment.%s", getPackageName(), fName);
        try {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Class<?> cls = Class.forName(getPackageName() + ".fragment." + fName);
            Constructor<?> cons = cls.getConstructor();
            Fragment fragment = (Fragment) cons.newInstance();
            ft.replace(resID, fragment).commitAllowingStateLoss();
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

    @Override
    public void storageList(List<StorageInfo> storageList) {

    }

}
