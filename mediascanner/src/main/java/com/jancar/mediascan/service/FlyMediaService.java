package com.jancar.mediascan.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;

import com.jancar.media.FlyMedia;
import com.jancar.media.Notify;
import com.jancar.media.data.Image;
import com.jancar.media.data.Music;
import com.jancar.media.data.Video;
import com.jancar.mediascan.R;
import com.jancar.mediascan.data.Const;
import com.jancar.mediascan.model.cache.ListFileDiskCache;
import com.jancar.mediascan.model.cache.MusicDoubleCache;
import com.jancar.mediascan.model.storage.IStorage;
import com.jancar.mediascan.model.storage.IStorageListener;
import com.jancar.mediascan.model.storage.Storage;
import com.jancar.mediascan.model.storage.StorageInfo;
import com.jancar.mediascan.utils.FlyLog;
import com.jancar.mediascan.utils.GsonUtils;
import com.jancar.mediascan.utils.SPUtil;
import com.jancar.mediascan.utils.StorageTools;
import com.jancar.mediascan.utils.StringTools;
import com.jancar.mediascan.utils.SystemPropertiesProxy;
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
import java.util.concurrent.atomic.AtomicInteger;

public class FlyMediaService extends Service implements IStorageListener {
    private static final Executor executor = Executors.newFixedThreadPool(1);

    static final HandlerThread sWorkerThread = new HandlerThread("notify-thread");

    static {
        sWorkerThread.start();
    }

    static final Handler sWorker = new Handler(sWorkerThread.getLooper());
    private static AtomicBoolean isRunning = new AtomicBoolean(false);
    private static AtomicBoolean isStoped = new AtomicBoolean(false);
    private static AtomicBoolean isNotifyVideo = new AtomicBoolean(false);
    private static AtomicBoolean isNotifyMusic = new AtomicBoolean(false);
    private static AtomicBoolean isNotifyImage = new AtomicBoolean(false);
    private static List<Music> mMusicList = Collections.synchronizedList(new ArrayList<Music>());
    private static List<Music> mMusicID3List = Collections.synchronizedList(new ArrayList<Music>());
    private static List<Video> mVideoList = Collections.synchronizedList(new ArrayList<Video>());
    private static List<Image> mImageList = Collections.synchronizedList(new ArrayList<Image>());
    private static AtomicInteger mMusicStart = new AtomicInteger(0);
    private static AtomicInteger mMusicID3Start = new AtomicInteger(0);
    private static AtomicInteger mVideoStart = new AtomicInteger(0);
    private static AtomicInteger mImageStart = new AtomicInteger(0);

    private static AtomicInteger mMusicEnd = new AtomicInteger(0);
    private static AtomicInteger mMusicID3End = new AtomicInteger(0);
    private static AtomicInteger mVideoEnd = new AtomicInteger(0);
    private static AtomicInteger mImageEnd = new AtomicInteger(0);

    private static RemoteCallbackList<Notify> mNotifys = new RemoteCallbackList<>();
    private MusicDoubleCache mDoubleMusicCache;
    private ListFileDiskCache mListDiskCache;
    private static String localPaths = "T";
    private static final String DEF_PATH = "/storage/emulated/0";
    private String currentPath = DEF_PATH;
    private static final int UPDATE_DENSITY = 100;
    private static final int ID3_UPDATE_DENSITY = 50;
    private static final int THREAD_WAIT_TIME = 100;
    private IStorage iStorage = Storage.getInstance();
    private long startScanTime;
    private int tryCount = 0;
    private int TRY_MAX = 5;
    private IBinder mBinder = new FlyMedia.Stub() {
        @Override
        public void scanDisk(final String disk) throws RemoteException {
            FlyLog.d("scanDisk=%s", disk);
            startScanPath(disk);
        }

        @Override
        public void registerNotify(final Notify notify) throws RemoteException {
            mNotifys.register(notify);
            FlyLog.d("registerNotify client=%s", notify.toString());
        }

        @Override
        public void unregisterNotify(Notify notify) throws RemoteException {
            mNotifys.unregister(notify);
            FlyLog.d("unregisterNotify client=%s", notify.toString());
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FlyLog.d("onCreate");
        mDoubleMusicCache = MusicDoubleCache.getInstance(getApplicationContext());
        mListDiskCache = new ListFileDiskCache(this);
        Storage.getInstance().init(getApplicationContext());
        iStorage.addListener(this);
        iStorage.refresh();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FlyLog.d("onStartCommand");
        try {
            String str1 = intent.getStringExtra(Const.SCAN_PATH_KEY);
            if (!TextUtils.isEmpty(str1) && StorageTools.isRemoved(this, str1)) {
                String str = SystemPropertiesProxy.get(this, SystemPropertiesProxy.Property.PERSIST_KEY_AUTOPLAY, "false");
                if (str.equals("true")) {
                    FlyLog.d("autoplay will scan path=%s", str1);
                    startScanPath(str1);
                }
            }

            String str2 = intent.getStringExtra(Const.UMOUNT_STORE);
            if (!TextUtils.isEmpty(str2)) {
                FlyLog.d();
                FlyLog.d("remove path=%s", str2);
                removePath(str2);
            }
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        iStorage.removeListener(this);
        sWorker.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void scanPath(final String path) {
        FlyLog.d("scan mPath=%s", path);
        clearData();
        currentPath = path;
        SPUtil.set(this, "SAVE_PATH", currentPath);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (isRunning.get()) {
                    FlyLog.d("wait another scan finish=%s", path);
                    try {
                        Thread.sleep(THREAD_WAIT_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                isRunning.set(true);
                isStoped.set(false);
                notifyPathListener();
                FlyLog.d("start scan mPath=%s", path);
                File file = new File(path);
                if (file.exists()) {
                    File files[] = file.listFiles();
                    if (files == null || files.length < 1) {
                        FlyLog.d("start scan %s no file list", path);
                        try {
                            Thread.sleep(1000);
                            if (!isStoped.get()) {
                                tryCount++;
                                if (tryCount <= TRY_MAX) {
                                    scanPath(path);
                                } else {
                                    scanPath(DEF_PATH);
                                }
                            }
                            isRunning.set(false);
                            return;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    FlyLog.d("start scan %s no file", path);
                    try {
                        Thread.sleep(1000);
                        if (!isStoped.get()) {
                            tryCount++;
                            if (tryCount <= TRY_MAX) {
                                scanPath(path);
                            } else {
                                scanPath(DEF_PATH);
                            }
                        }
                        isRunning.set(false);
                        return;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                isNotifyMusic.set(false);
                List<Music> musics = mListDiskCache.get(path + "player.music", Music.class);
                if (musics != null && !musics.isEmpty()) {
                    if ((new File(musics.get(0).url).exists())) {
                        isNotifyMusic.set(true);
                        notifyMusicListener(musics);
                        FlyLog.d("loading save musics size=%d", musics.size());
                    }
                }

                isNotifyVideo.set(false);
                List<Video> videos = mListDiskCache.get(path + "video", Video.class);
                if (videos != null && !videos.isEmpty()) {
                    if ((new File(videos.get(0).url).exists())) {
                        isNotifyVideo.set(true);
                        notifyVideoListener(videos);
                        FlyLog.d("loading save videos size=%d", videos.size());
                    }
                }

                isNotifyImage.set(false);
                List<Image> images = mListDiskCache.get(path + "image", Image.class);
                if (images != null && !images.isEmpty()) {
                    if ((new File(images.get(0).url).exists())) {
                        isNotifyImage.set(true);
                        notifyImageListener(images);
                        FlyLog.d("loading save images size=%d", images.size());
                    }
                }

                try {
                    getVideoFromPath(new File(path));
                } catch (Exception e) {
                    FlyLog.e(e.toString());
                }

                if (isNotifyMusic.get() || isNotifyVideo.get() || isNotifyImage.get()) {
                    notifyPathListener();
                }

                if (isNotifyMusic.get()) {
                    synchronized (mMusicList) {
                        notifyMusicListener(mMusicList);
                    }
                } else {
                    notifyMusicListener(mMusicStart.get());
                }
                if (isNotifyVideo.get()) {
                    synchronized (mVideoList) {
                        notifyVideoListener(mVideoList);
                    }
                } else {
                    notifyVideoListener(mVideoStart.get());
                }
                if (isNotifyImage.get()) {
                    synchronized (mImageList) {
                        notifyImageListener(mImageList);
                    }
                } else {
                    notifyImageListener(mImageStart.get());
                }

                notifyFinishListener();

                if (localPaths.contains(path)) {
                    synchronized (mVideoList) {
                        mListDiskCache.put(path + "video", GsonUtils.obj2Json(mVideoList));
                    }
                    synchronized (mMusicList) {
                        mListDiskCache.put(path + "player.music", GsonUtils.obj2Json(mMusicList));
                    }
                    synchronized (mImageList) {
                        mListDiskCache.put(path + "image", GsonUtils.obj2Json(mImageList));
                    }
                    FlyLog.d("finish save path=%s", path);
                }

                getMusicID3Info();
                if (!isStoped.get()) {
                    notifyMusicID3Listener(mMusicID3Start.get());
                }
                isRunning.set(false);
                FlyLog.d("finish scan mPath=%s", path);
                FlyLog.d("scan path %s use %d Millis", path, (int) (System.currentTimeMillis() - startScanTime));
            }
        });
    }

    private void clearData() {
        FlyLog.d("clear all data--------");
        synchronized (mVideoList) {
            mVideoList.clear();
        }
        synchronized (mImageList) {
            mImageList.clear();
        }
        synchronized (mMusicList) {
            mMusicList.clear();
        }
        synchronized (mMusicID3List) {
            mMusicID3List.clear();
        }
        mVideoStart.set(0);
        mMusicStart.set(0);
        mImageStart.set(0);
        mMusicID3Start.set(0);

        mVideoEnd.set(0);
        mMusicEnd.set(0);
        mImageEnd.set(0);
        mMusicID3End.set(0);
    }

    private void removePath(String path) {
        FlyLog.d("remove mPath=%s", path);
        if (currentPath.equals(path)) {
            FlyLog.d("clear all list");
            clearData();
            currentPath = DEF_PATH;
            startScanPath(currentPath);
        }
//        iStorage.refresh();
    }

    private void getVideoFromPath(File file) throws Exception {
//        FlyLog.d("getVideoFromPath-------------");
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
                    synchronized (mVideoList) {
                        mVideoList.add(new Video(url, mVideoEnd.get()));
                        mVideoEnd.getAndIncrement();
                        if (!isNotifyVideo.get() && (mVideoList.size() % UPDATE_DENSITY) == 1) {
                            notifyVideoListener(mVideoStart.get());
                            mVideoStart.set(mVideoList.size());
                        }
                    }
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
                    synchronized (mMusicList) {
                        mMusicList.add(new Music(url, mMusicEnd.get()));
//                        if (mMusicList.size() == 1 &&
//                                StorageTools.isRemoved(FlyMediaService.this, currentPath)) {
//                            FlyLog.d("start playmusic acitivty");
//                            Utils.startActivity(this, "com.jancar.media", "com.jancar.media.activity.MusicActivity");
//                        }
                        mMusicEnd.getAndIncrement();
                        if (!isNotifyMusic.get() && (mMusicList.size() % UPDATE_DENSITY) == 1) {
                            notifyMusicListener(mMusicStart.get());
                            mMusicStart.set(mMusicList.size());
                        }
                    }
//                    FlyLog.d("add a player.music=%s", url);
                    break;
                case ".png":
                case ".bmp":
                case ".gif":
                case ".jpg":
                case ".ico":
                    synchronized (mImageList) {
                        mImageList.add(new Image(url, mImageEnd.get()));
                        mImageEnd.getAndIncrement();
                        if (!isNotifyImage.get() && (mImageList.size() % UPDATE_DENSITY) == 1) {
                            notifyImageListener(mImageStart.get());
                            mImageStart.set(mImageList.size());
                        }
                    }
//                    FlyLog.d("add a image=%s", url);
                    break;
            }
        }
    }

    @Override
    public void storageList(List<StorageInfo> storageList) {
        if (storageList != null && !storageList.isEmpty()) {
            localPaths = "T";
//            String savePath = (String) SPUtil.get(this, "SAVE_PATH", "");
//            String scanPath = "";
            for (StorageInfo storageInfo : storageList) {
//                if (!storageInfo.isRemoveable) {
                localPaths = localPaths + "#T#S#Y#" + storageInfo.mPath;
//                }
//                if (savePath.endsWith(storageInfo.mPath)) {
//                    scanPath = savePath;
//                }
            }
//            FlyLog.d("localPaths=%s", localPaths);
//            if (TextUtils.isEmpty(scanPath)) {
//                scanPath = storageList.get(0).mPath;
//                FlyLog.d("scan first path =%s", scanPath);
//            } else {
//                FlyLog.d("scan save path =%s", scanPath);
//            }
//            startScanPath(scanPath);
        }
    }

    private void startScanPath(String disk) {
        FlyLog.d("start scan disk!");
        startScanTime = System.currentTimeMillis();
        tryCount = 0;
        if (!currentPath.endsWith(disk)) {
            isStoped.set(true);
            scanPath(disk);
        } else {
            FlyLog.d("notifyPath path=%s", currentPath);
            notifyPathListener();
            synchronized (mMusicList) {
                notifyMusicListener(mMusicList);
            }
            synchronized (mVideoList) {
                notifyVideoListener(mVideoList);
            }
            synchronized (mImageList) {
                notifyImageListener(mImageList);
            }
            synchronized (mMusicID3List) {
                notifyID3MusicListener(mMusicID3List);
            }
            notifyFinishListener();
            FlyLog.d("notifyFinish path=%s", currentPath);
        }
    }

    private void notifyMusic(List<Music> mMusicList, Notify notify) {
        FlyLog.d("notifyMusic size=%d", mMusicList.size());
        int start = 0;
        int sum = mMusicList.size();
        while (start < sum) {
            int end = Math.min(sum, start + UPDATE_DENSITY);
            try {
                FlyLog.d("notifyMusic start=%d,end=%d", start, end);
                notify.notifyMusic(mMusicList.subList(start, end));
            } catch (RemoteException e) {
                FlyLog.e();
                break;
            }
            start = start + UPDATE_DENSITY;
        }
    }

    private void notifyVideo(List<Video> mVideoList, Notify notify) {
        FlyLog.d("notifyVideo size=%d", mVideoList.size());
        int start = 0;
        int sum = mVideoList.size();
        while (start < sum) {
            int end = Math.min(sum, start + UPDATE_DENSITY);
            try {
                FlyLog.d("notifyVideo start=%d,end=%d", start, end);
                notify.notifyVideo(mVideoList.subList(start, end));
            } catch (RemoteException e) {
                FlyLog.e();
                break;
            }
            start = start + UPDATE_DENSITY;
        }
    }

    private void notifyImage(List<Image> mImageList, Notify notify) {
        FlyLog.d("notifyImage size=%d", mVideoList.size());
        int start = 0;
        int sum = mImageList.size();
        while (start < sum) {
            int end = Math.min(sum, start + UPDATE_DENSITY);
            try {
                FlyLog.d("notifyImage start=%d,end=%d", start, end);
                notify.notifyImage(mImageList.subList(start, end));
            } catch (RemoteException e) {
                FlyLog.e();
                break;
            }
            start = start + UPDATE_DENSITY;
        }
    }

    private void notifyID3Music(List<Music> mMusicID3List, Notify notify) {
        FlyLog.d("notifyID3Music size=%d", mVideoList.size());
        int start = 0;
        int sum = mMusicID3List.size();
        while (start < sum) {
            int end = Math.min(sum, start + UPDATE_DENSITY);
            try {
                FlyLog.d("notifyID3Music start=%d,end=%d", start, end);
                notify.notifyID3Music(mMusicID3List.subList(start, end));
            } catch (RemoteException e) {
                FlyLog.e();
                break;
            }
            start = start + UPDATE_DENSITY;
        }
    }

    private void notifyVideoListener(final int start) {
        sWorker.post(new Runnable() {
            @Override
            public void run() {
                synchronized (mVideoList) {
                    try {
                        FlyLog.d("notify video list start=%d", start);
                        final int N = mNotifys.beginBroadcast();
//                FlyLog.d("start notify client, client sum=%d", N);
                        for (int i = 0; i < N; i++) {
                            Notify l = mNotifys.getBroadcastItem(i);
                            if (l != null) {
                                l.notifyVideo(mVideoList.subList(start, mVideoList.size()));
                            }
                        }
                        mNotifys.finishBroadcast();
                    } catch (Exception e) {
                        FlyLog.e(e.toString());
                    }
                }
            }
        });
    }

    private void notifyMusicListener(final int start) {
        sWorker.post(new Runnable() {
            @Override
            public void run() {
                synchronized (mMusicList) {
                    try {
                        FlyLog.d("notify player.music list start=%d", start);
                        final int N = mNotifys.beginBroadcast();
//                FlyLog.d("start notify client, client sum=%d", N);
                        for (int i = 0; i < N; i++) {
                            Notify l = mNotifys.getBroadcastItem(i);
                            if (l != null) {
                                l.notifyMusic(mMusicList.subList(start, mMusicList.size()));
                            }
                        }
                        mNotifys.finishBroadcast();
                    } catch (Exception e) {
                        FlyLog.e(e.toString());
                    }
                }
            }
        });
    }

    private void notifyImageListener(final int start) {
        sWorker.post(new Runnable() {
            @Override
            public void run() {
                synchronized (mImageList) {
                    try {
                        FlyLog.d("notify player.music list start=%d", start);
                        final int N = mNotifys.beginBroadcast();
//                FlyLog.d("start notify client, client sum=%d", N);
                        for (int i = 0; i < N; i++) {
                            Notify l = mNotifys.getBroadcastItem(i);
                            if (l != null) {
                                l.notifyImage(mImageList.subList(start, mImageList.size()));
                            }
                        }
                        mNotifys.finishBroadcast();
                    } catch (Exception e) {
                        FlyLog.e(e.toString());
                    }
                }
            }
        });
    }

    private void notifyMusicID3Listener(final int start) {
        sWorker.post(new Runnable() {
            @Override
            public void run() {
                synchronized (mMusicID3List) {
                    try {
                        FlyLog.d("notify player.music list start=%d", start);
                        final int N = mNotifys.beginBroadcast();
//                FlyLog.d("start notify client, client sum=%d", N);
                        for (int i = 0; i < N; i++) {
                            Notify l = mNotifys.getBroadcastItem(i);
                            if (l != null) {
                                l.notifyID3Music(mMusicID3List.subList(start, mMusicID3List.size()));
                            }
                        }
                        mNotifys.finishBroadcast();
                    } catch (Exception e) {
                        FlyLog.e(e.toString());
                    }
                }
            }
        });
    }

    public void notifyVideoListener(final List<Video> list) {
        sWorker.post(new Runnable() {
            @Override
            public void run() {
                try {
                    final int N = mNotifys.beginBroadcast();
                    for (int i = 0; i < N; i++) {
                        Notify l = mNotifys.getBroadcastItem(i);
                        if (l != null) {
                            notifyVideo(list, l);
                        }
                    }
                    mNotifys.finishBroadcast();
                } catch (Exception e) {
                    FlyLog.e(e.toString());
                }
            }
        });
    }

    public void notifyMusicListener(final List<Music> mMusicList) {
        sWorker.post(new Runnable() {
            @Override
            public void run() {
                try {
                    FlyLog.d("notify player.music list size=%d", mMusicList.size());
                    final int N = mNotifys.beginBroadcast();
//            FlyLog.d("start notify client, client sum=%d", N);
                    for (int i = 0; i < N; i++) {
                        Notify l = mNotifys.getBroadcastItem(i);
                        if (l != null) {
                            notifyMusic(mMusicList, l);
                        }
                    }
                    mNotifys.finishBroadcast();
                } catch (Exception e) {
                    FlyLog.e(e.toString());
                }
            }
        });
    }

    public void notifyID3MusicListener(final List<Music> mID3MusicList) {
        sWorker.post(new Runnable() {
            @Override
            public void run() {
                try {
                    FlyLog.d("notify player.music list size=%d", mID3MusicList.size());
                    final int N = mNotifys.beginBroadcast();
//            FlyLog.d("start notify client, client sum=%d", N);
                    for (int i = 0; i < N; i++) {
                        Notify l = mNotifys.getBroadcastItem(i);
                        if (l != null) {
                            notifyID3Music(mID3MusicList, l);
                        }
                    }
                    mNotifys.finishBroadcast();
                } catch (Exception e) {
                    FlyLog.e(e.toString());
                }
            }
        });
    }

    public void notifyImageListener(final List<Image> mImageList) {
        sWorker.post(new Runnable() {
            @Override
            public void run() {
                try {
                    FlyLog.d("notify image list size=%d", mImageList == null ? 0 : mImageList.size());
                    final int N = mNotifys.beginBroadcast();
//            FlyLog.d("start notify client, client sum=%d", N);
                    for (int i = 0; i < N; i++) {
                        Notify l = mNotifys.getBroadcastItem(i);
                        if (l != null) {
                            notifyImage(mImageList, l);
                        }
                    }
                    mNotifys.finishBroadcast();
                } catch (Exception e) {
                    FlyLog.e(e.toString());
                }
            }
        });
    }

    private void notifyPathListener() {
        sWorker.post(new Runnable() {
            @Override
            public void run() {
                try {
                    FlyLog.d("notify mPath=%s", currentPath);
                    final int N = mNotifys.beginBroadcast();
//            FlyLog.d("start notify client, client sum=%d", N);
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
                } catch (Exception e) {
                    FlyLog.e(e.toString());
                }
            }
        });
    }

    private void notifyFinishListener() {
        sWorker.post(new Runnable() {
            @Override
            public void run() {
                try {
                    FlyLog.d("notify finish mPath=%s", currentPath);
                    final int N = mNotifys.beginBroadcast();
//            FlyLog.d("start notify client, client sum=%d", N);
                    try {
                        for (int i = 0; i < N; i++) {
                            Notify l = mNotifys.getBroadcastItem(i);
                            if (l != null) {
                                l.notifyFinish(currentPath);
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
        });
    }

    private void getMusicID3Info() {
        if (isStoped.get()) return;
        synchronized (mMusicList) {
            FlyLog.d("getMusicID3Info mMusicList size=%d", mMusicList.size());
            synchronized (mMusicID3List) {
                mMusicID3List.clear();
            }
            for (int i = 0; i < mMusicList.size(); i++) {
                if (isStoped.get()) return;
                try {
                    Music music = mMusicList.get(i);
                    Music id3music = mDoubleMusicCache.get(music.url);
                    if (id3music == null) {
                        if (music.url.toLowerCase().endsWith(".mp3")) {
                            try {
                                Mp3File mp3file = new Mp3File(music.url);
                                if (mp3file.hasId3v2Tag()) {
                                    ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                                    music.artist = TextUtils.isEmpty(id3v2Tag.getArtist()) ? getString(R.string.no_artist) : id3v2Tag.getArtist();
                                    music.album = TextUtils.isEmpty(id3v2Tag.getAlbum()) ? getString(R.string.no_album) : id3v2Tag.getAlbum();
                                    music.name = TextUtils.isEmpty(id3v2Tag.getTitle()) ? StringTools.getNameByPath(music.url) : id3v2Tag.getTitle();
                                } else if (mp3file.hasId3v1Tag()) {
                                    ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                                    music.artist = TextUtils.isEmpty(id3v1Tag.getArtist()) ? getString(R.string.no_artist) : id3v1Tag.getArtist();
                                    music.album = TextUtils.isEmpty(id3v1Tag.getAlbum()) ? getString(R.string.no_album) : id3v1Tag.getAlbum();
                                    music.name = TextUtils.isEmpty(id3v1Tag.getTitle()) ? StringTools.getNameByPath(music.url) : id3v1Tag.getTitle();
                                }
                            } catch (Exception e) {
                                music.artist = getString(R.string.no_artist);
                                music.album = getString(R.string.no_album);
                                music.name = StringTools.getNameByPath(music.url);
                                FlyLog.d(e.toString());
                            }
                        } else {
                            music.artist = getString(R.string.no_artist);
                            music.album = getString(R.string.no_album);
                            music.name = StringTools.getNameByPath(music.url);
                        }
                        mDoubleMusicCache.put(music.url, music);
                    } else {
                        music.artist = TextUtils.isEmpty(id3music.artist) ? getString(R.string.no_artist) : id3music.artist;
                        music.album = TextUtils.isEmpty(id3music.album) ? getString(R.string.no_album) : id3music.album;
                        music.name = StringTools.getNameByPath(music.url);
                    }
                    synchronized (mMusicID3List) {
                        mMusicID3List.add(music);
                        if (!isStoped.get() && (mMusicID3List.size() % ID3_UPDATE_DENSITY == 1)) {
                            notifyMusicID3Listener(mMusicID3Start.get());
                            mMusicID3Start.set(mMusicID3List.size());
                        }
                    }
                } catch (Exception e) {
                    FlyLog.e(e.toString());
                }
            }
        }
    }

}
