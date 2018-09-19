package com.jancar.usbmedia.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;

import com.jancar.media.FlyMedia;
import com.jancar.media.Notify;
import com.jancar.media.data.Music;
import com.jancar.usbmedia.R;
import com.jancar.usbmedia.data.Const;
import com.jancar.usbmedia.model.IStorage;
import com.jancar.usbmedia.model.IStorageListener;
import com.jancar.usbmedia.model.Storage;
import com.jancar.usbmedia.model.StorageInfo;
import com.jancar.usbmedia.utils.FlyLog;
import com.jancar.usbmedia.utils.StringTools;
import com.jancar.usbmedia.utils.Utils;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class FlyMediaService extends Service implements IStorageListener {
    private static final Executor executor = Executors.newSingleThreadExecutor();
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private AtomicBoolean isStoped = new AtomicBoolean(false);
    private List<String> mMusicList = Collections.synchronizedList(new ArrayList<String>());
    private List<Music> mMusicID3List = Collections.synchronizedList(new ArrayList<Music>());
    private List<String> mVideoList = Collections.synchronizedList(new ArrayList<String>());
    private List<String> mImageList = Collections.synchronizedList(new ArrayList<String>());
    private RemoteCallbackList<Notify> mNotifys = new RemoteCallbackList<>();
    private boolean isFirst = true;
    private static String NORMAL = "NORMAL";
    private String currentPath = NORMAL;

    private IStorage iStorage = Storage.getInstance();


    private IBinder mBinder = new FlyMedia.Stub() {
        @Override
        public void scanDisk(final String disk) throws RemoteException {
            isFirst = false;
            FlyLog.d("start scan disk!");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    FlyLog.d("start scan disk!");
                    scanPath(disk);
                }
            });

        }

        @Override
        public String getPath() throws RemoteException {
            return currentPath;
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
        public void notify(final Notify notify) throws RemoteException {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        FlyLog.d("notify client=%s", notify.toString());
                        notify.notifyID3Music(mMusicID3List);
                        notify.notifyMusic(mMusicList);
                        notify.notifyVideo(mVideoList);
                        notify.notifyImage(mImageList);
                        notify.notifyPath(currentPath);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void registerNotify(final Notify notify) throws RemoteException {
            mNotifys.register(notify);
            FlyLog.d("registerNotify client=%s", notify.toString());
            notify.notifyID3Music(mMusicID3List);
            notify.notifyMusic(mMusicList);
            notify.notifyVideo(mVideoList);
            notify.notifyImage(mImageList);
            notify.notifyPath(currentPath);
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
        Storage.getInstance().init(getApplicationContext());
        iStorage.refresh();
        iStorage.addListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            String str1 = intent.getStringExtra(Const.SCAN_PATH_KEY);
            if (!TextUtils.isEmpty(str1)) {
                isFirst = true;
                scanPath(str1);
            }

            String str2 = intent.getStringExtra(Const.UMOUNT_STORE);
            if (!TextUtils.isEmpty(str2)) {
                removePath(str2);
            }
        } catch (Exception e) {
            //TODO:检测此处抛出空异常
            FlyLog.e(e.toString());
        }

        return Service.START_STICKY;
    }


    @Override
    public void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        iStorage.removeListener(this);
        super.onDestroy();
    }

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private Runnable notifyAllTask = new Runnable() {
        @Override
        public synchronized void run() {
            FlyLog.d("notify all list");
            try {
                final int N = mNotifys.beginBroadcast();
                FlyLog.d("start notify client, client sum=%d", N);
                try {
                    for (int i = 0; i < N; i++) {
                        Notify l = mNotifys.getBroadcastItem(i);
                        if (l != null) {
                            FlyLog.d("notify video list size=%d", mVideoList == null ? 0 : mVideoList.size());
                            l.notifyVideo(mVideoList);
                            FlyLog.d("notify music list size=%d", mMusicList == null ? 0 : mMusicList.size());
                            l.notifyMusic(mMusicList);
                            FlyLog.d("notify image list size=%d", mImageList == null ? 0 : mImageList.size());
                            l.notifyImage(mImageList);
                            FlyLog.d("notify id3music list size=%d", mMusicID3List == null ? 0 : mMusicID3List.size());
                            l.notifyID3Music(mMusicID3List);
                            FlyLog.d("notify mPath =%s", currentPath);
                            l.notifyPath(currentPath);
                        }
                    }
                } catch (Exception e) {
                    FlyLog.e(e.toString());
                }
                mNotifys.finishBroadcast();
            } catch (Exception e) {
                FlyLog.e(e.toString());
            }
        }
    };

    public void notifyAllListener() {
        mHandler.removeCallbacks(notifyAllTask);
        mHandler.post(notifyAllTask);
    }


    private Runnable notifyID3Task = new Runnable() {
        @Override
        public synchronized void run() {
            FlyLog.d("notify id3 music list size=%d", mMusicID3List == null ? 0 : mMusicID3List.size());
            final int N = mNotifys.beginBroadcast();
            FlyLog.d("start notify client, client sum=%d", N);
            try {
                for (int i = 0; i < N; i++) {
                    Notify l = mNotifys.getBroadcastItem(i);
                    if (l != null) {
                        l.notifyID3Music(mMusicID3List);
                    }
                }
            } catch (Exception e) {
                FlyLog.e(e.toString());
            }
            mNotifys.finishBroadcast();
        }
    };

    public void notifyID3Listener() {
        mHandler.removeCallbacks(notifyID3Task);
        mHandler.post(notifyID3Task);
    }

    private Runnable notifyPathTask = new Runnable() {
        @Override
        public synchronized void run() {
            FlyLog.d("notify mPath=%s", currentPath);
            final int N = mNotifys.beginBroadcast();
            FlyLog.d("start notify client, client sum=%d", N);
            try {
                for (int i = 0; i < N; i++) {
                    Notify l = mNotifys.getBroadcastItem(i);
                    if (l != null) {
                        l.notifyPath(currentPath);
                    }
                }
            } catch (Exception e) {
                FlyLog.e(e.toString());
            }
            mNotifys.finishBroadcast();
        }
    };

    private void notifyPath() {
        mHandler.removeCallbacks(notifyPathTask);
        mHandler.post(notifyPathTask);
    }

    private void scanPath(final String path) {
        FlyLog.d("scan mPath=%s", path);
        isStoped.set(true);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                FlyLog.d("start scan mPath=%s", path);
                while (isRunning.get()) {
                    FlyLog.d("wait another scan finish=%s", path);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                currentPath = path;
                notifyPath();
                isRunning.set(true);
                isStoped.set(false);
                mVideoList.clear();
                mImageList.clear();
                mMusicList.clear();
                mMusicID3List.clear();
                try {
                    getVideoFromPath(new File(path));
                } catch (Exception e) {
                    FlyLog.e(e.toString());
                }
                notifyAllListener();
                getMusicID3Info(mMusicList);
//                if (!isStoped.get()) {
//                    notifyID3Listener();
//                }
                isRunning.set(false);
                FlyLog.d("finish scan mPath=%s", path);
            }
        });
    }

    private void removePath(String path) {
        FlyLog.d("remove mPath=%s", path);
        if (currentPath.equals(path)) {
            FlyLog.d("clear all list");
            mVideoList.clear();
            mImageList.clear();
            mMusicList.clear();
            currentPath = NORMAL;
            notifyAllListener();
        }
        iStorage.refresh();
    }

    private void getVideoFromPath(File file) throws Exception {
        if (isStoped.get()) return;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) return;
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
                case ".mov":
                case ".ts":
                case ".avi":
                case ".3gp":
                case ".3gpp":
                case ".3g2":
                case ".flv":
                case ".mpeg":
                case ".mpg":
                case ".rm":
                case ".tp":
                case ".vop":
                case ".wmv":
                    mVideoList.add(url);
//                    FlyLog.d("add a video=%s", url);
                    break;
                case ".aac":
//                case ".ac3":
//                case ".aiff":
                case ".amr":
                case ".ape":
//                case ".au":
                case ".flac":
                case ".m4a":
                case ".mka":
                case ".mp3":
                case ".ogg":
//                case ".ra":
                case ".wav":
//                case ".wma":
                    if (isFirst) {
                        isFirst = false;
                        //OPEN FILE
                        Utils.startActivity(this, "com.jancar.media", "com.jancar.media.activity.MusicActivity");
                    }
                    mMusicList.add(url);
//                    FlyLog.d("add a music=%s", url);
                    break;
                case ".png":
                case ".bmp":
                case ".gif":
                case ".jpg":
                case ".ico":
                    mImageList.add(url);
//                    FlyLog.d("add a image=%s", url);
                    break;
            }
        }
    }

    private void getMusicID3Info(List<String> mMusicList) {
        if (isStoped.get()) return;
        FlyLog.d("getMusicID3Info mMusicList size=%d", mMusicList.size());
        mMusicID3List.clear();
        for (int i = 0; i < mMusicList.size(); i++) {
            if (isStoped.get()) return;
            String url = mMusicList.get(i);
            Music music = new Music();
            music.url = url;
            music.sort = i;
            try {
                if (url.toLowerCase().endsWith(".mp3")) {
                    Mp3File mp3file = new Mp3File(url);
                    if (mp3file.hasId3v2Tag()) {
                        ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                        music.artist = TextUtils.isEmpty(id3v2Tag.getArtist()) ? "" : id3v2Tag.getArtist();
                        music.album = TextUtils.isEmpty(id3v2Tag.getAlbum()) ? "" : id3v2Tag.getAlbum();
                        music.name = TextUtils.isEmpty(id3v2Tag.getTitle()) ? "" : id3v2Tag.getTitle();
                    } else if (mp3file.hasId3v1Tag()) {
                        ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                        music.artist = TextUtils.isEmpty(id3v1Tag.getArtist()) ? "" : id3v1Tag.getArtist();
                        music.album = TextUtils.isEmpty(id3v1Tag.getAlbum()) ? "" : id3v1Tag.getAlbum();
                        music.name = TextUtils.isEmpty(id3v1Tag.getTitle()) ? "" : id3v1Tag.getTitle();
                    }
                } else {
                    music.artist = getString(R.string.no_artist);
                    music.album = getString(R.string.no_album);
                    music.name = StringTools.getNameByPath(url);
                }
            } catch (Exception e) {
                FlyLog.e(e.toString());
            }
//            FlyLog.d("add id3info url=%s", url);
            mMusicID3List.add(music);
            if(!isStoped.get()){
                notifyID3Listener();
            }
        }
    }

    @Override
    public void storageList(List<StorageInfo> storageList) {
        if (storageList != null && !storageList.isEmpty()) {
            scanPath(storageList.get(0).mPath);
        }
    }
}
