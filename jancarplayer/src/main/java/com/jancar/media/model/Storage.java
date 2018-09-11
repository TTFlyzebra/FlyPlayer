package com.jancar.media.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.storage.StorageManager;

import com.jancar.media.listener.IStorageListener;
import com.jancar.media.utils.FlyLog;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Storage implements IStorage {
    private Context mContext;
    private List<com.jancar.media.data.Storage> mStorageList = new ArrayList<>();
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
        refresh();
    }

    public void refresh() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final List<com.jancar.media.data.Storage> list = getAvaliableStorage(listAllStorage(mContext));
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
    }

    public static List<com.jancar.media.data.Storage> listAllStorage(Context context) {
        ArrayList<com.jancar.media.data.Storage> storages = new ArrayList<>();
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            Class<?>[] paramClasses = {};
            Method getVolumeList = StorageManager.class.getMethod("getVolumeList", paramClasses);
            Object[] params = {};
            Object[] invokes = (Object[]) getVolumeList.invoke(storageManager, params);

            if (invokes != null) {
                com.jancar.media.data.Storage info = null;
                for (int i = 0; i < invokes.length; i++) {
                    Object obj = invokes[i];
                    Method getPath = obj.getClass().getMethod("getPath");
                    String path = (String) getPath.invoke(obj, new Object[0]);
                    info = new com.jancar.media.data.Storage(path);

                    Method getVolumeState = StorageManager.class.getMethod("getVolumeState", String.class);
                    info.state = (String) getVolumeState.invoke(storageManager, info.path);

                    Method isRemovable = obj.getClass().getMethod("isRemovable");
                    info.isRemoveable = (Boolean) isRemovable.invoke(obj, new Object[0]);

                    Field mDescription = obj.getClass().getDeclaredField("mDescription");
                    mDescription.setAccessible(true);
                    info.mDescription = (String) mDescription.get(obj);

                    Field mMtpReserveSize = obj.getClass().getDeclaredField("mMtpReserveSize");
                    mMtpReserveSize.setAccessible(true);
                    info.mMtpReserveSize = (long) mMtpReserveSize.get(obj);

                    Field mId = obj.getClass().getDeclaredField("mId");
                    mId.setAccessible(true);
                    info.mId = (String) mId.get(obj);

                    Field mStorageId = obj.getClass().getDeclaredField("mStorageId");
                    mStorageId.setAccessible(true);
                    info.mStorageId = (int) mStorageId.get(obj);

                    Field mMaxFileSize = obj.getClass().getDeclaredField("mMaxFileSize");
                    mMaxFileSize.setAccessible(true);
                    info.mMaxFileSize = (int) mStorageId.get(obj);

                    storages.add(info);
                }
            }
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
//        storages.trimToSize();
        return storages;
    }

    public static List<com.jancar.media.data.Storage> getAvaliableStorage(List<com.jancar.media.data.Storage> infos) {
        if (infos == null && infos.isEmpty()) return null;
        List<com.jancar.media.data.Storage> storages = new ArrayList<com.jancar.media.data.Storage>();
        for (com.jancar.media.data.Storage info : infos) {
            File file = new File(info.path);
            if ((file.exists()) && (file.isDirectory()) && info.isMounted()) {
                storages.add(info);
            }
        }
        return storages;
    }

}
