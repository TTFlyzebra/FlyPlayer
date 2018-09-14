package com.jancar.usbmedia.model;

import android.os.UserHandle;

public class StorageInfo {
    public String path;
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
        this.path = path;
    }

    public boolean isMounted() {
        return "mounted".equals(state);
    }

}
