package com.jancar.usbmedia.model.storage;

import android.os.UserHandle;

public class StorageInfo {
    public String mPath;
    public boolean isRemoveable;
    public String state;
    public String mDescription;
    public long mMtpReserveSize;
    public String mId;
    public int mStorageId;
    public long mMaxFileSize;
    public boolean mPrimary;
    public boolean mEmulated;
    public boolean mAllowMassStorage;
    public UserHandle mOwner;
    public String mFsUuid;

    public StorageInfo(String path) {
        this.mPath = path;
    }

    public boolean isMounted() {
        return "mounted".equals(state);
    }

}
