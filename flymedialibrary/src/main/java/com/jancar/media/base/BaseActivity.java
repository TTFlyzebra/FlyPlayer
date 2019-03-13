package com.jancar.media.base;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

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

public class BaseActivity extends AppCompatActivity implements IUsbMediaListener, IStorageListener {
    protected IMediaScan usbMediaScan = MediaScan.getInstance();
    public static final String DEF_PATH = "/storage/emulated/0";
    public static String currenPath = DEF_PATH;
    protected static boolean isStop = false;
    public static int StorageNum;
    private IStorage iStorage = Storage.getInstance();
    private DiskReceiver diskReceiver;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currenPath = getSavePath();
        if (!(new File(currenPath).exists())) {
            currenPath = DEF_PATH;
        }

        iStorage.addListener(this);

        usbMediaScan.addListener(this);
        usbMediaScan.open();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addDataScheme("file");
        diskReceiver = new DiskReceiver();
        registerReceiver(diskReceiver, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        isStop = false;
        /**
         * 程序于后台被拔掉U盘，已经打不开当前盘符，刷新为当前磁盘
         */
        if (!(new File(currenPath).exists())) {
            currenPath = DEF_PATH;
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
        unregisterReceiver(diskReceiver);
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

    public class DiskReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            FlyLog.d("DiskReceiver intent=" + intent.toUri(0));
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            switch (action) {
                case Intent.ACTION_MEDIA_MOUNTED:
                    break;
                case Intent.ACTION_MEDIA_EJECT:
                case Intent.ACTION_MEDIA_REMOVED:
                case Intent.ACTION_MEDIA_BAD_REMOVAL:
                case Intent.ACTION_MEDIA_UNMOUNTED:
                    final Uri uri = intent.getData();
                    if (uri == null) return;
                    if (!("file".equals(uri.getScheme()))) return;
                    String path = uri.getPath();
                    if (path == null) return;
                    FlyLog.d("MEDIA_UNMOUNTED path=%s", path);
                    if (isStop) {
                        if (currenPath.equals(Storage.ALL_STORAGE) || currenPath.equals(path)) {
                            finish();
                        }
                    }
                    break;
                default:
                    break;
            }

        }
    }


}
