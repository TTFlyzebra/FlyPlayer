package com.jancar.media.model;

import android.content.Context;
import android.os.storage.StorageManager;

import com.jancar.media.data.StorageInfo;
import com.jancar.media.listener.IStorageListener;
import com.jancar.media.utils.FlyLog;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class StorageHandler implements IStorageHandler {
    private Context mContext;
    private List<StorageInfo> mStorageList = new ArrayList<>();
    private List<IStorageListener> listeners = new ArrayList<>();

    private StorageHandler() {
    }

    public static StorageHandler getInstance() {
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
        public static final StorageHandler sInstance = new StorageHandler();
    }


    public void init(Context context) {
        List<StorageInfo> list = getAvaliableStorage(listAllStorage(context));
        if (list != null && !list.isEmpty()) {
            mStorageList.clear();
            mStorageList.addAll(list);
            for (IStorageListener listener : listeners) {
                listener.storageList(mStorageList);
            }
        }
    }

    public void close() {
    }

    public static List<StorageInfo> listAllStorage(Context context) {
        ArrayList<StorageInfo> storages = new ArrayList<>();
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            Class<?>[] paramClasses = {};
            Method getVolumeList = StorageManager.class.getMethod("getVolumeList", paramClasses);
            Object[] params = {};
            Object[] invokes = (Object[]) getVolumeList.invoke(storageManager, params);

            if (invokes != null) {
                StorageInfo info = null;
                for (int i = 0; i < invokes.length; i++) {
                    Object obj = invokes[i];
                    Method getPath = obj.getClass().getMethod("getPath");
                    String path = (String) getPath.invoke(obj, new Object[0]);
                    info = new StorageInfo(path);

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

    public static List<StorageInfo> getAvaliableStorage(List<StorageInfo> infos) {
        if (infos == null && infos.isEmpty()) return null;
        List<StorageInfo> storages = new ArrayList<StorageInfo>();
        for (StorageInfo info : infos) {
            File file = new File(info.path);
            if ((file.exists()) && (file.isDirectory()) && info.isMounted()) {
                storages.add(info);
            }
        }
        return storages;
    }

}
