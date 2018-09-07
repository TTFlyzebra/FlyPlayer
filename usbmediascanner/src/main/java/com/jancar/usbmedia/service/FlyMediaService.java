package com.jancar.usbmedia.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;

import com.jancar.media.FlyMedia;
import com.jancar.media.Notify;
import com.jancar.usbmedia.data.Const;
import com.jancar.usbmedia.utils.FlyLog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class FlyMediaService extends Service {
    private static final Executor executor = Executors.newSingleThreadExecutor();
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private AtomicBoolean isStoped = new AtomicBoolean(false);
    private List<String> mMusicList = Collections.synchronizedList(new ArrayList<String>());
    private List<String> mVideoList = Collections.synchronizedList(new ArrayList<String>());
    private List<String> mImageList = Collections.synchronizedList(new ArrayList<String>());
    private RemoteCallbackList<Notify> mNotifys = new RemoteCallbackList<>();
    private static final MediaFileFilter filter = new MediaFileFilter(".mp4");


    private IBinder mBinder = new FlyMedia.Stub() {
        @Override
        public void scanDisk(String disk) throws RemoteException {

        }

        @Override
        public List<String> getMusics() throws RemoteException {
            return mMusicList;
        }

        @Override
        public List<String> getVideos() throws RemoteException {
            return mVideoList;
        }

        @Override
        public List<String> getImages() throws RemoteException {
            return mImageList;
        }

        @Override
        public void registerNotify(Notify notify) throws RemoteException {
            mNotifys.register(notify);
        }

        @Override
        public void unregisterNotify(Notify notify) throws RemoteException {
            mNotifys.unregister(notify);
        }

    };


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        FlyLog.d("start");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String str = intent.getStringExtra(Const.SCAN_PATH_KEY);
        if (TextUtils.isEmpty(str)) {
            FlyLog.e("Not get scan path!");
            str = "/storage";
        }
        final String scanPath = str;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                isStoped.set(true);
                while (isRunning.get()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                isRunning.set(true);
                isStoped.set(false);
                mVideoList.clear();
                scanPath(scanPath);
                isRunning.set(false);
                final int N = mNotifys.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    Notify l = mNotifys.getBroadcastItem(i);
                    if (l != null) {
                        try {
                            l.notifyVideo(mVideoList);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mNotifys.finishBroadcast();
            }
        });

        return Service.START_STICKY;
    }

    private boolean isFirst = true;

    private void scanPath(String path) {
        isFirst = true;
        try {
            getVideoFromPath(new File(path));
        } catch (Exception e) {
            FlyLog.e();
        }

    }

    private void getVideoFromPath(File file) throws Exception{
        if (isStoped.get()) return;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if(files==null) return;
            for (File tempFile : files) {
                getVideoFromPath(tempFile);
            }
        } else {
            String filename = file.getName();
            String url = file.getAbsolutePath();
            int ret = file.getName().lastIndexOf('.');
            if (ret < 0) return;
            String strSuffix = filename.substring(ret, filename.length()).toLowerCase();
            switch (strSuffix) {
                case ".mp4":
                case ".mkv":
                    if (isFirst) {
                        isFirst = false;
                        Intent intent = new Intent();
                        ComponentName cn = new ComponentName("com.jancar.media", "com.jancar.media.activity.VideoActivity");
                        intent.setComponent(cn);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(Const.PLAYURL_KEY, url);
                        startActivity(intent);
                        FlyLog.d("start intent playurl=%s", url);
                    }
                    mVideoList.add(url);
                    FlyLog.d("add a video=%s", url);
                    break;
                case ".mp3":
                    mMusicList.add(url);
                    FlyLog.d("add a music=%s", url);
                    break;
                case ".png":
                    mImageList.add(url);
                    FlyLog.d("add a image=%s", url);
                    break;
            }
        }
    }


}
