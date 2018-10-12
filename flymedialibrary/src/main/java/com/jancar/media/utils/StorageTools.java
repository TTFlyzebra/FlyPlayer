package com.jancar.media.utils;

import android.content.Context;
import android.os.storage.StorageManager;

import com.jancar.media.data.StorageInfo;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class StorageTools {
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
                    info.state = (String) getVolumeState.invoke(storageManager, info.mPath);

                    Method isRemovable = obj.getClass().getMethod("isRemovable");
                    info.isRemoveable = (Boolean) isRemovable.invoke(obj, new Object[0]);

                    try {
                        Field mDescription = obj.getClass().getDeclaredField("mDescription");
                        mDescription.setAccessible(true);
                        info.mDescription = (String) mDescription.get(obj);
                    }catch (Exception e){
                        info.mDescription = path;
                        FlyLog.i(e.toString());
                    }

                    try {
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
                    }catch (Exception e){
                        FlyLog.i(e.toString());
                    }

                    storages.add(info);
                }
            }
        } catch (Exception e) {
            FlyLog.i(e.toString());
        }
        return storages;
    }

    public static List<StorageInfo> getAvaliableStorage(List<StorageInfo> infos) {
        if (infos == null || infos.isEmpty()) return null;
        List<StorageInfo> storages = new ArrayList<StorageInfo>();
        for (StorageInfo info : infos) {
            File file = new File(info.mPath);
            if ((file.exists()) && (file.isDirectory()) && info.isMounted()) {
                storages.add(info);
            }
        }
        return storages;
    }

    /**
     * 判断是否是本地存储设备
     *
     * @param context
     * @param path
     * @return
     */
    public static boolean isRemoved(Context context, String path) {
        boolean flag = false;
        List<StorageInfo> list = getAvaliableStorage(listAllStorage(context));
        if (list == null || list.size() == 0) {
            flag = false;
        } else {
            for (StorageInfo info : list) {
                if (path.equals(info.mPath)) {
                    flag = info.isRemoveable;
                    break;
                }
            }
        }
        FlyLog.d("%s is removeable = %b", path, flag);
        return flag;
    }
}
