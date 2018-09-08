package com.jancar.media.model;

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
import com.jancar.media.listener.IUsbMediaListener;
import com.jancar.media.utils.FlyLog;

import java.util.ArrayList;
import java.util.List;

public class UsbMediaScan implements IUsbMediaScan {
    private FlyMedia mFlyMedia;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            FlyLog.d("usbscan service connected");
            try {
                mFlyMedia = FlyMedia.Stub.asInterface(service);
                if (mFlyMedia == null) return;
                mFlyMedia.registerNotify(notify);
                for (IUsbMediaListener listener : listeners) {
                    listener.musicUrlList(mFlyMedia.getMusics());
                    listener.imageUrlList(mFlyMedia.getImages());
                    listener.videoUrlList(mFlyMedia.getVideos());
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            FlyLog.d("usbscan service disconnected");
        }
    };

    private Notify notify = new Notify.Stub() {
        @Override
        public void notifyMusic(final List<String> list) throws RemoteException {
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
        public void notifyVideo(final List<String> list) throws RemoteException {
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
        public void notifyImage(final List<String> list) throws RemoteException {
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
    };

    private Context mContext;
    private List<IUsbMediaListener> listeners = new ArrayList<>();


    private UsbMediaScan() {
    }

    public static UsbMediaScan getInstance() {
        return UsbMediaScanHolder.sInstance;
    }

    private static class UsbMediaScanHolder {
        public static final UsbMediaScan sInstance = new UsbMediaScan();
    }

    private void bindService() {
        FlyLog.d("bindService");
        try {
            Intent intent = new Intent();
            intent.setPackage("com.jancar.usbmedia");
            intent.setAction("com.jancar.usbmedia.FlyMediaService");
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
    public void init(Context context) {
        mContext = context;
        bindService();
    }

    @Override
    public void close() {
        unBindService();
    }

    @Override
    public void addListener(IUsbMediaListener iUsbMediaListener) {
        listeners.add(iUsbMediaListener);
        if (mFlyMedia != null) {
            try {
                iUsbMediaListener.musicUrlList(mFlyMedia.getMusics());
                iUsbMediaListener.imageUrlList(mFlyMedia.getImages());
                iUsbMediaListener.videoUrlList(mFlyMedia.getVideos());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeListener(IUsbMediaListener iUsbMediaListener) {
        listeners.remove(iUsbMediaListener);
    }

}
