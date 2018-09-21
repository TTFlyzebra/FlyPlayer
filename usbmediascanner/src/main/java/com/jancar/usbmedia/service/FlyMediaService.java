package com.jancar.usbmedia.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;

import com.jancar.media.FlyMedia;
import com.jancar.media.Notify;
import com.jancar.media.data.Image;
import com.jancar.media.data.Music;
import com.jancar.media.data.Video;
import com.jancar.usbmedia.R;
import com.jancar.usbmedia.data.Const;
import com.jancar.usbmedia.model.cache.ListFileDiskCache;
import com.jancar.usbmedia.model.cache.MusicDoubleCache;
import com.jancar.usbmedia.model.storage.IStorage;
import com.jancar.usbmedia.model.storage.IStorageListener;
import com.jancar.usbmedia.model.storage.Storage;
import com.jancar.usbmedia.model.storage.StorageInfo;
import com.jancar.usbmedia.utils.FlyLog;
import com.jancar.usbmedia.utils.GsonUtils;
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
import java.util.concurrent.atomic.AtomicInteger;

public class FlyMediaService extends Service implements IStorageListener {
    private static final Executor executor = Executors.newSingleThreadExecutor();
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private AtomicBoolean isStoped = new AtomicBoolean(false);
    private AtomicBoolean isNotifyVideo = new AtomicBoolean(false);
    private AtomicBoolean isNotifyMusic = new AtomicBoolean(false);
    private AtomicBoolean isNotifyImage = new AtomicBoolean(false);
    private List<Music> mMusicList = Collections.synchronizedList(new ArrayList<Music>());
    private List<Music> mMusicID3List = Collections.synchronizedList(new ArrayList<Music>());
    private List<Video> mVideoList = Collections.synchronizedList(new ArrayList<Video>());
    private List<Image> mImageList = Collections.synchronizedList(new ArrayList<Image>());
    private AtomicInteger mMusicStart = new AtomicInteger(0);
    private AtomicInteger mMusicID3Start = new AtomicInteger(0);
    private AtomicInteger mVideoStart = new AtomicInteger(0);
    private AtomicInteger mImageStart = new AtomicInteger(0);

    private AtomicInteger mMusicEnd = new AtomicInteger(0);
    private AtomicInteger mMusicID3End = new AtomicInteger(0);
    private AtomicInteger mVideoEnd = new AtomicInteger(0);
    private AtomicInteger mImageEnd = new AtomicInteger(0);

    private RemoteCallbackList<Notify> mNotifys = new RemoteCallbackList<>();
    private MusicDoubleCache mDoubleMusicCache;
    private ListFileDiskCache mListDiskCache;
    private String localPaths = "T";
    private boolean isFirst = false;
    private static final String NORMAL = "NORMAL";
    private String currentPath = NORMAL;
    private static final int UPDATE_DENSITY = 100;
    private static final int ID3_UPDATE_DENSITY = 20;
    private static final int THREAD_WAIT_TIME = 20;
    private IStorage iStorage = Storage.getInstance();
    private IBinder mBinder = new FlyMedia.Stub() {
        @Override
        public void scanDisk(final String disk) throws RemoteException {
            isFirst = false;
            FlyLog.d("start scan disk!");
            scanPath(disk);
        }

        @Override
        public void notify(final Notify notify) throws RemoteException {
            FlyLog.d("notify client=%s", notify.toString());
            try {
                if(!isRunning.get()) {
                    notify.notifyPath(currentPath);
                    synchronized (mMusicList) {
                        notifyMusic(mMusicList, notify);
                    }
                    synchronized (mVideoList) {
                        notifyVideo(mVideoList, notify);
                    }
                    synchronized (mImageList) {
                        notifyImage(mImageList, notify);
                    }
                    synchronized (mMusicID3List) {
                        notifyID3Music(mMusicID3List, notify);
                    }
                }
            } catch (RemoteException e) {
                FlyLog.e(e.toString());
            }
        }

        @Override
        public void registerNotify(final Notify notify) throws RemoteException {
            mNotifys.register(notify);
            FlyLog.d("registerNotify client=%s", notify.toString());
            try {
                if(!isRunning.get()) {
                    notify.notifyPath(currentPath);
                    synchronized (mMusicList) {
                        notifyMusic(mMusicList, notify);
                    }
                    synchronized (mVideoList) {
                        notifyVideo(mVideoList, notify);
                    }
                    synchronized (mImageList) {
                        notifyImage(mImageList, notify);
                    }
                    synchronized (mMusicID3List) {
                        notifyID3Music(mMusicID3List, notify);
                    }
                }
            } catch (RemoteException e) {
                FlyLog.e(e.toString());
            }
        }

        @Override
        public void unregisterNotify(Notify notify) throws RemoteException {
            mNotifys.unregister(notify);
        }

    };

    private void notifyMusic(List<Music> mMusicList, Notify notify) {
        int start = 0;
        int sum = mMusicList.size();
        while (start < sum) {
            int end = Math.min(sum,start+UPDATE_DENSITY);
            try {
                notify.notifyMusic(mMusicList.subList(start,end));
            } catch (RemoteException e) {
                FlyLog.e();
                break;
            }
            start = start+UPDATE_DENSITY;
        }
    }

    private void notifyVideo(List<Video> mVideoList, Notify notify) {
        int start = 0;
        int sum = mVideoList.size();
        while (start < sum) {
            int end = Math.min(sum,start+UPDATE_DENSITY);
            try {
                notify.notifyVideo(mVideoList.subList(start,end));
            } catch (RemoteException e) {
                FlyLog.e();
                break;
            }
            start = start+UPDATE_DENSITY;
        }
    }

    private void notifyImage(List<Image> mImageList, Notify notify) {
        int start = 0;
        int sum = mImageList.size();
        while (start < sum) {
            int end = Math.min(sum,start+UPDATE_DENSITY);
            try {
                notify.notifyImage(mImageList.subList(start,end));
            } catch (RemoteException e) {
                FlyLog.e();
                break;
            }
            start = start+UPDATE_DENSITY;
        }
    }

    private void notifyID3Music(List<Music> mMusicID3List, Notify notify) {
        int start = 0;
        int sum = mMusicID3List.size();
        while (start < sum) {
            int end = Math.min(sum,start+UPDATE_DENSITY);
            try {
                notify.notifyID3Music(mMusicID3List.subList(start,end));
            } catch (RemoteException e) {
                FlyLog.e();
                break;
            }
            start = start+UPDATE_DENSITY;
        }
    }

    private void notifyVideoListener(int start) {
        synchronized (mVideoList) {
            try {
                FlyLog.d("notify video list start=%d", start);
                final int N = mNotifys.beginBroadcast();
                FlyLog.d("start notify client, client sum=%d", N);
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

    private void notifyMusicListener(int start) {
        synchronized (mMusicList) {
            try {
                FlyLog.d("notify music list start=%d", start);
                final int N = mNotifys.beginBroadcast();
                FlyLog.d("start notify client, client sum=%d", N);
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

    private void notifyImageListener(int start) {
        synchronized (mImageList) {
            try {
                FlyLog.d("notify music list start=%d", start);
                final int N = mNotifys.beginBroadcast();
                FlyLog.d("start notify client, client sum=%d", N);
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

    private void notifyMusicID3Listener(int start) {
        synchronized (mMusicID3List) {
            try {
                FlyLog.d("notify music list start=%d", start);
                final int N = mNotifys.beginBroadcast();
                FlyLog.d("start notify client, client sum=%d", N);
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


    public synchronized void notifyVideoListener(List<Video> list) {
        try {
            final int N = mNotifys.beginBroadcast();
            for (int i = 0; i < N; i++) {
                Notify l = mNotifys.getBroadcastItem(i);
                if (l != null) {
                    notifyVideo(list,l);
                }
            }
            mNotifys.finishBroadcast();
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }

    public synchronized void notifyMusicListener(List<Music> mMusicList) {
        try {
            FlyLog.d("notify music list size=%d", mMusicList.size());
            final int N = mNotifys.beginBroadcast();
            FlyLog.d("start notify client, client sum=%d", N);
            for (int i = 0; i < N; i++) {
                Notify l = mNotifys.getBroadcastItem(i);
                if (l != null) {
                    notifyMusic(mMusicList,l);
                }
            }
            mNotifys.finishBroadcast();
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }

    public synchronized void notifyImageListener(List<Image> mImageList) {
        try {
            FlyLog.d("notify image list size=%d", mImageList == null ? 0 : mImageList.size());
            final int N = mNotifys.beginBroadcast();
            FlyLog.d("start notify client, client sum=%d", N);
            for (int i = 0; i < N; i++) {
                Notify l = mNotifys.getBroadcastItem(i);
                if (l != null) {
                    notifyImage(mImageList,l);
                }
            }
            mNotifys.finishBroadcast();
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }


    private synchronized void notifyPathListener() {
        try {
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
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }

    private void scanPath(final String path) {
        FlyLog.d("scan mPath=%s", path);
        isStoped.set(true);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                FlyLog.d("start scan mPath=%s", path);
                isNotifyMusic.set(false);
                List<Music> musics = mListDiskCache.get(path + "music", Music.class);
                if (musics != null) {
                    isNotifyMusic.set(true);
                    notifyMusicListener(musics);
                    FlyLog.d("loading save musics size=%d", musics.size());
                }

                isNotifyVideo.set(false);
                List<Video> videos = mListDiskCache.get(path + "video", Video.class);
                if (videos != null) {
                    isNotifyVideo.set(true);
                    notifyVideoListener(videos);
                    FlyLog.d("loading save videos size=%d", videos.size());
                }

                isNotifyImage.set(false);
                List<Image> images = mListDiskCache.get(path + "image", Image.class);
                if (images != null) {
                    isNotifyImage.set(true);
                    notifyImageListener(images);
                    FlyLog.d("loading save images size=%d", images.size());
                }

                while (isRunning.get()) {
                    FlyLog.d("wait another scan finish=%s", path);
                    try {
                        Thread.sleep(THREAD_WAIT_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                currentPath = path;
                notifyPathListener();
                isRunning.set(true);
                isStoped.set(false);
                initListCount();
                try {
                    getVideoFromPath(new File(path));
                } catch (Exception e) {
                    FlyLog.e(e.toString());
                }

                if(isNotifyMusic.get()){
                    synchronized (mMusicList) {
                        notifyMusicListener(mMusicList);
                    }
                }else{
                    notifyMusicListener(mMusicStart.get());
                }
                if(isNotifyVideo.get()){
                    synchronized (mVideoList) {
                        notifyVideoListener(mVideoList);
                    }
                }else{
                    notifyVideoListener(mVideoStart.get());
                }
                if(isNotifyImage.get()){
                    synchronized (mImageList) {
                        notifyImageListener(mImageList);
                    }
                }else{
                    notifyImageListener(mImageStart.get());
                }

                if (localPaths.contains(path)) {
                    if (!mVideoList.isEmpty())
                        mListDiskCache.put(path + "video", GsonUtils.obj2Json(mVideoList));
                    if (!mMusicList.isEmpty())
                        mListDiskCache.put(path + "music", GsonUtils.obj2Json(mMusicList));
                    if (!mImageList.isEmpty())
                        mListDiskCache.put(path + "image", GsonUtils.obj2Json(mImageList));
                    FlyLog.d("finish save path=%s", path);
                }

                getMusicID3Info();
                if (!isStoped.get()) {
                    notifyMusicID3Listener(mMusicID3Start.get());
                }
                isRunning.set(false);
                FlyLog.d("finish scan mPath=%s", path);
            }
        });
    }

    private void initListCount() {
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
            initListCount();
            currentPath = NORMAL;
            notifyMusicListener(0);
            notifyVideoListener(0);
            notifyImageListener(0);

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
                    if (isFirst) {
                        isFirst = false;
                        //OPEN FILE
                        Utils.startActivity(this, "com.jancar.media", "com.jancar.media.activity.MusicActivity");
                    }
                    synchronized (mMusicList) {
                        mMusicList.add(new Music(url, mMusicEnd.get()));
                        mMusicEnd.getAndIncrement();
                        if (!isNotifyMusic.get() && (mMusicList.size() % UPDATE_DENSITY) == 1) {
                            notifyMusicListener(mMusicStart.get());
                            mMusicStart.set(mMusicList.size());
                        }
                    }
//                    FlyLog.d("add a music=%s", url);
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
                                    music.artist = TextUtils.isEmpty(id3v2Tag.getArtist()) ? "" : id3v2Tag.getArtist();
                                    music.album = TextUtils.isEmpty(id3v2Tag.getAlbum()) ? "" : id3v2Tag.getAlbum();
                                    music.name = TextUtils.isEmpty(id3v2Tag.getTitle()) ? "" : id3v2Tag.getTitle();
                                } else if (mp3file.hasId3v1Tag()) {
                                    ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                                    music.artist = TextUtils.isEmpty(id3v1Tag.getArtist()) ? "" : id3v1Tag.getArtist();
                                    music.album = TextUtils.isEmpty(id3v1Tag.getAlbum()) ? "" : id3v1Tag.getAlbum();
                                    music.name = TextUtils.isEmpty(id3v1Tag.getTitle()) ? "" : id3v1Tag.getTitle();
                                }
                            }catch (Exception e){
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
                        music.artist = id3music.artist;
                        music.album = id3music.album;
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

    @Override
    public void storageList(List<StorageInfo> storageList) {
        if (storageList != null && !storageList.isEmpty()) {
            localPaths = "T";
            for (StorageInfo storageInfo : storageList) {
                if (!storageInfo.isRemoveable) {
                    localPaths = localPaths + "#T#S#Y#" + storageInfo.mPath;
                }
            }
            FlyLog.d("localPaths=%s", localPaths);
            scanPath(storageList.get(0).mPath);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDoubleMusicCache = MusicDoubleCache.getInstance(getApplicationContext());
        mListDiskCache = new ListFileDiskCache(this);
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
            FlyLog.e(e.toString());
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        iStorage.removeListener(this);
        super.onDestroy();
    }
}
