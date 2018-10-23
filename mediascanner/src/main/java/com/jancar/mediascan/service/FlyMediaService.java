package com.jancar.mediascan.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;

import com.jancar.JancarManager;
import com.jancar.media.FlyMedia;
import com.jancar.media.Notify;
import com.jancar.media.data.Image;
import com.jancar.media.data.Music;
import com.jancar.media.data.Video;
import com.jancar.mediascan.R;
import com.jancar.mediascan.data.Const;
import com.jancar.mediascan.model.cache.ListFileDiskCache;
import com.jancar.mediascan.model.cache.MusicDoubleCache;
import com.jancar.mediascan.utils.FlyLog;
import com.jancar.mediascan.utils.GsonUtils;
import com.jancar.mediascan.utils.StorageTools;
import com.jancar.mediascan.utils.StringTools;
import com.jancar.mediascan.utils.SystemPropertiesProxy;
import com.jancar.state.JacState;
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

public class FlyMediaService extends Service{
    private static final Executor executor = Executors.newFixedThreadPool(1);
    private static final HandlerThread sWorkerThread = new HandlerThread("notify-thread");

    static {
        sWorkerThread.start();
    }

    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());

    private JancarManager jancarManager = null;
    private JacState jacState = new JacSystemStates();

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
    private static String currentPath = "";
    private static final int UPDATE_DENSITY = 100;
    private static final int ID3_UPDATE_DENSITY = 50;
    private static final int THREAD_WAIT_TIME = 100;
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

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate() {
        super.onCreate();
        FlyLog.d("onCreate");
        jancarManager = (JancarManager) getSystemService("jancar_manager");
        jancarManager.registerJacStateListener(jacState.asBinder());
        mDoubleMusicCache = MusicDoubleCache.getInstance(getApplicationContext());
        mListDiskCache = new ListFileDiskCache(this);
        startScanPath(DEF_PATH);
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
        sWorker.removeCallbacksAndMessages(null);
        jancarManager.unregisterJacStateListener(jacState.asBinder());
        super.onDestroy();
    }

    private void scanPath(final String path) {
        FlyLog.d("scan mPath=%s", path);
        sWorker.post(new Runnable() {
            @Override
            public void run() {
                notifyPathListener();
            }
        });
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
                final List<Music> musics = mListDiskCache.get(path + "player.music", Music.class);
                if (musics != null && !musics.isEmpty()) {
                    if ((new File(musics.get(0).url).exists())) {
                        isNotifyMusic.set(true);
                        sWorker.post(new Runnable() {
                            @Override
                            public void run() {
                                notifyMusicListener(musics);
                            }
                        });
                        FlyLog.d("loading save musics size=%d", musics.size());
                    }
                }

                isNotifyVideo.set(false);
                final List<Video> videos = mListDiskCache.get(path + "video", Video.class);
                if (videos != null && !videos.isEmpty()) {
                    if ((new File(videos.get(0).url).exists())) {
                        isNotifyVideo.set(true);
                        sWorker.post(new Runnable() {
                            @Override
                            public void run() {
                                notifyVideoListener(videos);
                            }
                        });
                        FlyLog.d("loading save videos size=%d", videos.size());
                    }
                }

                isNotifyImage.set(false);
                final List<Image> images = mListDiskCache.get(path + "image", Image.class);
                if (images != null && !images.isEmpty()) {
                    if ((new File(images.get(0).url).exists())) {
                        isNotifyImage.set(true);
                        sWorker.post(new Runnable() {
                            @Override
                            public void run() {
                                notifyImageListener(images);
                            }
                        });
                        FlyLog.d("loading save images size=%d", images.size());
                    }
                }

                try {
                    getMediaFileFromPath(new File(path));
                } catch (Exception e) {
                    FlyLog.e(e.toString());
                }

                if (isStoped.get()) {
                    sWorker.removeCallbacksAndMessages(null);
                    FlyLog.d("stop scan in scanPath. path=%s", path);
                } else {
                    if (!isNotifyMusic.get()) {
                        sWorker.post(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (mMusicList) {
                                    notifyMusicListener(mMusicStart.get(), mMusicList.size());
                                }
                            }
                        });
                    }
                    if (!isNotifyVideo.get()) {
                        sWorker.post(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (mVideoList) {
                                    notifyVideoListener(mVideoStart.get(), mVideoList.size());
                                }
                            }
                        });
                    }
                    if (!isNotifyImage.get()) {
                        sWorker.post(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (mImageList) {
                                    notifyImageListener(mImageStart.get(), mImageList.size());
                                }
                            }
                        });
                    }

                    if (isNotifyMusic.get() || isNotifyVideo.get() || isNotifyImage.get()) {
                        sWorker.post(new Runnable() {
                            @Override
                            public void run() {
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
                            }
                        });
                    }

                    sWorker.post(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (mImageList) {
                                notifyFinishListener();
                            }
                        }
                    });

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
                        final int start = mMusicID3Start.get();
                        final int end = Math.min(start + ID3_UPDATE_DENSITY, mMusicID3List.size());
                        sWorker.post(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (mImageList) {
                                    notifyMusicID3Listener(start, end);
                                }
                            }
                        });
                    }else{
                        sWorker.removeCallbacksAndMessages(null);
                        FlyLog.d("stop scan in scanPath. path=%s", path);
                    }
                    FlyLog.d("finish scan mPath=%s", path);
                }
                isRunning.set(false);
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
        if (currentPath.equals(path) && mEState == JacState.ePowerState.ePowerOn) {
            FlyLog.d("clear all list");
            clearData();
            startScanPath(DEF_PATH);
        }
//        iStorage.refresh();
    }

    private void getMediaFileFromPath(File file) throws Exception {
        if (isStoped.get()) {
            sWorker.removeCallbacksAndMessages(null);
            FlyLog.d("stop scan in getMediaFileFromPath");
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) return;
            for (File tempFile : files) {
                getMediaFileFromPath(tempFile);
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
                            final int start = mVideoStart.get();
                            final int end = Math.min(start + UPDATE_DENSITY, mVideoList.size());
                            sWorker.post(new Runnable() {
                                @Override
                                public void run() {
                                    notifyVideoListener(start, end);
                                }
                            });
                            mVideoStart.set(mVideoList.size());
                        }
                    }
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
                        mMusicEnd.getAndIncrement();
                        if (!isNotifyMusic.get() && (mMusicList.size() % UPDATE_DENSITY) == 1) {
                            final int start = mMusicStart.get();
                            final int end = Math.min(start + UPDATE_DENSITY, mMusicList.size());
                            sWorker.post(new Runnable() {
                                @Override
                                public void run() {
                                    notifyMusicListener(start, end);
                                }
                            });
                            mMusicStart.set(mMusicList.size());
                        }
                    }
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
                            final int start = mImageStart.get();
                            final int end = Math.min(start + UPDATE_DENSITY, mImageList.size());
                            sWorker.post(new Runnable() {
                                @Override
                                public void run() {
                                    notifyImageListener(start, end);
                                }
                            });
                            mImageStart.set(mImageList.size());
                        }
                    }
                    break;
            }
        }
    }

    private void startScanPath(String disk) {
        FlyLog.d("start scan disk!");
        sWorker.removeCallbacksAndMessages(null);
        startScanTime = System.currentTimeMillis();
        tryCount = 0;
        if (!currentPath.endsWith(disk)) {
            isStoped.set(true);
            clearData();
            currentPath = disk;
            scanPath(disk);
        } else {
            FlyLog.d("notify path=%s", currentPath);
            sWorker.post(new Runnable() {
                @Override
                public void run() {
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
                }
            });
            FlyLog.d("notify Finish path=%s", currentPath);
        }
    }

    private void notifyMusic(List<Music> mMusicList, Notify notify) {
        FlyLog.d("notify Music size=%d", mMusicList.size());
        int start = 0;
        int sum = mMusicList.size();
        while (start < sum&&!isStoped.get()) {
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
        FlyLog.d("notify Video size=%d", mVideoList.size());
        int start = 0;
        int sum = mVideoList.size();
        while (start < sum&&!isStoped.get()) {
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
        FlyLog.d("notify Image size=%d", mVideoList.size());
        int start = 0;
        int sum = mImageList.size();
        while (start < sum&&!isStoped.get()) {
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
        FlyLog.d("notify id3 music size=%d", mVideoList.size());
        int start = 0;
        int sum = mMusicID3List.size();
        while (start < sum&&!isStoped.get()) {
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

    private void notifyVideoListener(final int start, final int end) {
        FlyLog.d("notify video list start=%d,list", start);
        synchronized (mVideoList) {
            try {
                final List<Video> list = mVideoList.subList(start, end);
                final int N = mNotifys.beginBroadcast();
                try {
                    for (int i = 0; i < N; i++) {
                        Notify l = mNotifys.getBroadcastItem(i);
                        if (l != null) {
                            l.notifyVideo(list);
                        }
                    }
                }catch (Exception e){
                    FlyLog.e(e.toString());
                }
                mNotifys.finishBroadcast();
            } catch (Exception e) {
                FlyLog.e(e.toString());
                e.printStackTrace();
            }
        }
    }

    private void notifyMusicListener(final int start, final int end) {
        FlyLog.d("notify music list start=%d", start);
        synchronized (mMusicList) {
            try {
                final List<Music> list = mMusicList.subList(start, end);
                final int N = mNotifys.beginBroadcast();
                try {
                    for (int i = 0; i < N; i++) {
                        Notify l = mNotifys.getBroadcastItem(i);
                        if (l != null) {
                            l.notifyMusic(list);
                        }
                    }
                } catch (Exception e) {
                    FlyLog.e(e.toString());
                }
                mNotifys.finishBroadcast();
            } catch (Exception e) {
                FlyLog.e(e.toString());
                e.printStackTrace();
            }
        }
    }

    private void notifyImageListener(final int start, final int end) {
        FlyLog.d("notify image list start=%d", start);
        synchronized (mImageList) {
            try {
                List<Image> list = mImageList.subList(start, end);
                final int N = mNotifys.beginBroadcast();
                try {
                    for (int i = 0; i < N; i++) {
                        Notify l = mNotifys.getBroadcastItem(i);
                        if (l != null) {
                            l.notifyImage(list);
                        }
                    }
                } catch (Exception e) {
                    FlyLog.e(e.toString());
                }
                mNotifys.finishBroadcast();
            } catch (Exception e) {
                FlyLog.e(e.toString());
                e.printStackTrace();
            }
        }
    }

    private void notifyMusicID3Listener(final int start, final int end) {
        FlyLog.d("notify id3 music list start=%d", start);
        synchronized (mMusicID3List) {
            try {
                List<Music> list = mMusicID3List.subList(start, end);
                final int N = mNotifys.beginBroadcast();
                try {
                    for (int i = 0; i < N; i++) {
                        Notify l = mNotifys.getBroadcastItem(i);
                        if (l != null) {
                            l.notifyID3Music(list);
                        }
                    }
                } catch (Exception e) {
                    FlyLog.e(e.toString());
                }
                mNotifys.finishBroadcast();
            } catch (Exception e) {
                FlyLog.e(e.toString());
                e.printStackTrace();
            }
        }
    }

    public void notifyVideoListener(final List<Video> list) {
        FlyLog.d("notify video list size=%d", list == null ? 0 : list.size());
        try {
            final int N = mNotifys.beginBroadcast();
            try {
                for (int i = 0; i < N; i++) {
                    Notify l = mNotifys.getBroadcastItem(i);
                    if (l != null) {
                        notifyVideo(list, l);
                    }
                }
            } catch (Exception e) {
                FlyLog.e(e.toString());
            }
            mNotifys.finishBroadcast();
        } catch (Exception e) {
            FlyLog.e(e.toString());
            e.printStackTrace();
        }
    }

    public void notifyMusicListener(final List<Music> list) {
        FlyLog.d("notify music list size=%d", list == null ? 0 : list.size());
        try {
            final int N = mNotifys.beginBroadcast();
            try {
                for (int i = 0; i < N; i++) {
                    Notify l = mNotifys.getBroadcastItem(i);
                    if (l != null) {
                        notifyMusic(list, l);
                    }
                }
            } catch (Exception e) {
                FlyLog.e(e.toString());
            }
            mNotifys.finishBroadcast();
        } catch (Exception e) {
            FlyLog.e(e.toString());
            e.printStackTrace();
        }
    }

    public void notifyID3MusicListener(final List<Music> list) {
        FlyLog.d("notify id3 music list size=%d", list == null ? 0 : list.size());
        try {
            final int N = mNotifys.beginBroadcast();
            try {
                for (int i = 0; i < N; i++) {
                    Notify l = mNotifys.getBroadcastItem(i);
                    if (l != null) {
                        notifyID3Music(list, l);
                    }
                }
            } catch (Exception e) {
                FlyLog.e(e.toString());
            }
            mNotifys.finishBroadcast();
        } catch (Exception e) {
            FlyLog.e(e.toString());
            e.printStackTrace();
        }
    }

    public void notifyImageListener(final List<Image> list) {
        FlyLog.d("notify image list size=%d", list == null ? 0 : list.size());
        try {
            final int N = mNotifys.beginBroadcast();
            try {
                for (int i = 0; i < N; i++) {
                    Notify l = mNotifys.getBroadcastItem(i);
                    if (l != null) {
                        notifyImage(list, l);
                    }
                }
            } catch (Exception e) {
                FlyLog.e(e.toString());
            }
            mNotifys.finishBroadcast();
        } catch (Exception e) {
            FlyLog.e(e.toString());
            e.printStackTrace();
        }
    }

    private void notifyPathListener() {
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
            e.printStackTrace();
        }
    }

    private void notifyFinishListener() {
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
            e.printStackTrace();
        }
    }

    private void getMusicID3Info() {
        if (isStoped.get()) {
            sWorker.removeCallbacksAndMessages(null);
            FlyLog.d("stop scan in getMusicID3Info");
            return;
        }
        synchronized (mMusicList) {
            FlyLog.d("start get music id3 info, music size=%d", mMusicList.size());
            synchronized (mMusicID3List) {
                mMusicID3List.clear();
            }
            for (int i = 0; i < mMusicList.size(); i++) {
                try {
                    if (isStoped.get()) {
                        sWorker.removeCallbacksAndMessages(null);
                        FlyLog.d("stop scan in getMusicID3Info");
                        return;
                    }
                    Music music = mMusicList.get(i);
                    FlyLog.d("get %d id3 info",i);
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
                        if (!isStoped.get()) {
                            if( (mMusicID3List.size() % ID3_UPDATE_DENSITY == 1)) {
                                final int start = mMusicID3Start.get();
                                final int end = Math.min(start + ID3_UPDATE_DENSITY, mMusicID3List.size());
                                sWorker.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        synchronized (mImageList) {
                                            notifyMusicID3Listener(start, end);
                                        }
                                    }
                                });
                                mMusicID3Start.set(mMusicID3List.size());
                            }
                        }else{
                            sWorker.removeCallbacksAndMessages(null);
                            FlyLog.d("stop scan in getMusicID3Info");
                        }
                    }
                } catch (Exception e) {
                    FlyLog.e(e.toString());
                }
            }
        }
    }


    private JacState.ePowerState mEState = JacState.ePowerState.ePowerOn;

    public class JacSystemStates extends JacState {
        @Override
        public void OnPower(ePowerState eState) {
            super.OnPower(eState);
            mEState = eState;
            if (eState == ePowerState.ePowerOn) {
                FlyLog.d("ePowerState ePowerOn");
            } else if (eState == ePowerState.ePowerOff) {
                stopSelf();
                FlyLog.d("ePowerState ePowerOff");
            } else if (eState == ePowerState.ePowerStandby) {
                FlyLog.d("ePowerState ePowerStandby");
            } else if (eState == ePowerState.ePowerSleep) {
                FlyLog.d("ePowerState ePowerSleep");
            } else {
                FlyLog.e("ePowerState error status");
            }
        }
    }

}
