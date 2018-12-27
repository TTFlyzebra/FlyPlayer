package com.jancar.media.model.mediascan;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;

import com.jancar.media.FlyMedia;
import com.jancar.media.Notify;
import com.jancar.media.data.Image;
import com.jancar.media.data.Music;
import com.jancar.media.data.StorageInfo;
import com.jancar.media.data.Video;
import com.jancar.media.model.listener.IUsbMediaListener;
import com.jancar.media.utils.FlyLog;

import java.util.ArrayList;
import java.util.List;

public class MediaScan implements IMediaScan {
    private FlyMedia mFlyMedia;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Context mContext;
    private List<IUsbMediaListener> listeners = new ArrayList<>();
    private boolean isReConnect = false;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            FlyLog.d("mediascan service connected");
            try {
                mFlyMedia = FlyMedia.Stub.asInterface(service);
                if (mFlyMedia == null) return;
                mFlyMedia.registerNotify(notify);
                if (!isReConnect) {
                    for (IUsbMediaListener listener : listeners) {
                        listener.scanServiceConneted();
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            FlyLog.e("usbscan service disconnected");
            isReConnect = true;
            bindService();
        }
    };

    private Notify notify = new Notify.Stub() {
        @Override
        public void notifyMusic(final List<Music> list) throws RemoteException {
            FlyLog.d("get music list size=%d", list == null ? 0 : list.size());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (IUsbMediaListener listener : listeners) {
                        listener.musicUrlList(list);
                    }
                }
            });
        }

        @Override
        public void notifyVideo(final List<Video> list) throws RemoteException {
            FlyLog.d("get video list size=%d", list == null ? 0 : list.size());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (IUsbMediaListener listener : listeners) {
                        listener.videoUrlList(list);
                    }
                }
            });
        }

        @Override
        public void notifyImage(final List<Image> list) throws RemoteException {
            FlyLog.d("get image list size=%d", list == null ? 0 : list.size());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (IUsbMediaListener listener : listeners) {
                        listener.imageUrlList(list);
                    }
                }
            });
        }

        @Override
        public void notifyPath(final String path) throws RemoteException {
            FlyLog.d("get mPath=%s", path);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (IUsbMediaListener listener : listeners) {
                        listener.notifyPathChange(path);
                    }
                }
            });
        }

        @Override
        public void notifyFinish(final String path) throws RemoteException {
            FlyLog.d("scanFinish mPath=%s", path);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (IUsbMediaListener listener : listeners) {
                        listener.scanFinish(path);
                    }
                }
            });
        }

        @Override
        public void notifyID3Music(final List<Music> list) throws RemoteException {
            FlyLog.d("get musicid3 list size=%d", list == null ? 0 : list.size());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (IUsbMediaListener listener : listeners) {
                        listener.musicID3UrlList(list);
                    }
                }
            });
        }
    };


    private MediaScan() {
    }

    public static MediaScan getInstance() {
        return UsbMediaScanHolder.sInstance;
    }

    public void init(Context applicationContext) {
        this.mContext = applicationContext;
    }

    private static class UsbMediaScanHolder {
        public static final MediaScan sInstance = new MediaScan();
    }

    private void bindService() {
        FlyLog.d("bindService");
        try {
            Intent intent = new Intent();
            intent.setPackage("com.jancar.mediascan");
            intent.setAction("com.jancar.mediascan.FlyMediaService");
            mContext.bindService(intent, conn, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }

    private void unBindService() {
        FlyLog.d("unBindService");
        try {
            if (mFlyMedia != null) {
                mFlyMedia.unregisterNotify(notify);
            }
            mContext.unbindService(conn);
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }

    @Override
    public void open() {
        isReConnect = false;
        bindService();
    }

    @Override
    public void close() {
        unBindService();
    }

    @Override
    public void openStorager(StorageInfo storageInfo) {
        try {
            FlyLog.d("openStorager path=%s", storageInfo.mPath);
            if(mFlyMedia!=null){
                mFlyMedia.scanDisk(storageInfo.mPath);
            }
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }

    @Override
    public void addListener(IUsbMediaListener iUsbMediaListener) {
        listeners.add(iUsbMediaListener);
//        if (mFlyMedia != null) {
//            try {
//                mFlyMedia.notify(notify);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public void removeListener(IUsbMediaListener iUsbMediaListener) {
        listeners.remove(iUsbMediaListener);
    }

}
