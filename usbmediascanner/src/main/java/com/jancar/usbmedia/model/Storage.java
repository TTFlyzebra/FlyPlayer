package com.jancar.usbmedia.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.jancar.usbmedia.utils.StorageTools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Storage implements IStorage {
    private Context mContext;
    private List<StorageInfo> mStorageList = new ArrayList<>();
    private List<IStorageListener> listeners = new ArrayList<>();
    private static final Executor executor = Executors.newSingleThreadExecutor();
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private Storage() {
    }

    public static Storage getInstance() {
        return StorageManagerHolder.sInstance;
    }

    @Override
    public void addListener(IStorageListener iStorageListener) {
        listeners.add(iStorageListener);
        iStorageListener.storageList(mStorageList);
    }

    @Override
    public void removeListener(IStorageListener iStorageListener) {
        listeners.remove(iStorageListener);
    }

    private static class StorageManagerHolder {
        public static final Storage sInstance = new Storage();
    }


    public void init(Context context) {
        this.mContext = context;
    }

    @Override
    public void refresh() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final List<StorageInfo> list = StorageTools.getAvaliableStorage(StorageTools.listAllStorage(mContext));
                if (list != null && !list.isEmpty()) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mStorageList.clear();
                            mStorageList.addAll(list);
                            for (IStorageListener listener : listeners) {
                                listener.storageList(mStorageList);
                            }
                        }
                    });
                }
            }
        });
    }

    public void close() {
        listeners.clear();
    }

    

}
