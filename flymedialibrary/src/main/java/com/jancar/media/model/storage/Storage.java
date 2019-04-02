package com.jancar.media.model.storage;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.jancar.media.R;
import com.jancar.media.data.StorageInfo;
import com.jancar.media.model.listener.IStorageListener;
import com.jancar.media.utils.StorageTools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Storage implements IStorage {
    public static final String ALL_STORAGE = "/storage";
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
                            if(mStorageList.size()>1) {
                                StorageInfo storageInfo = new StorageInfo(ALL_STORAGE);
                                storageInfo.isRemoveable = false;
                                storageInfo.mDescription = mContext.getString(R.string.allstorage);
                                mStorageList.add(storageInfo);
                            }
                            for (IStorageListener listener : listeners) {
                                listener.storageList(mStorageList);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public int getStorageSum() {
        refreshStorage();
        return mStorageList.size();
    }

    @Override
    public String getStorageByUrl(String url) {
        refreshStorage();
        for(StorageInfo storageInfo:mStorageList){
            if(url.contains(storageInfo.mPath)){
                return storageInfo.mPath;
            }
        }
        Pattern pattern = Pattern.compile("/");
        Matcher findMatcher = pattern.matcher(url);
        int number = 0;
        while(findMatcher.find()) {
            number++;
            if(number == 3){
                break;
            }
        }
        int start = findMatcher.start();
        return url.substring(0,start);
    }

    private void refreshStorage(){
        if (mStorageList.isEmpty()) {
            final List<StorageInfo> list = StorageTools.getAvaliableStorage(StorageTools.listAllStorage(mContext));
            if (list != null && !list.isEmpty()) {
                mStorageList.clear();
                mStorageList.addAll(list);
                if(mStorageList.size()>1) {
                    StorageInfo storageInfo = new StorageInfo(ALL_STORAGE);
                    storageInfo.isRemoveable = false;
                    storageInfo.mDescription = mContext.getString(R.string.allstorage);
                    mStorageList.add(storageInfo);
                }
            }
        }
    }

    public void close() {
        listeners.clear();
    }


}
