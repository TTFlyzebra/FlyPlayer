package com.jancar.media.Entry;

/**
 * @anthor Tzq
 * @time 2018/12/10 9:43
 * @describe TODO
 */
public class StorageEntry {
    private int type;
    private int storageNum;

    public StorageEntry(int type, int storageNum) {
        this.type = type;
        this.storageNum = storageNum;
    }

    public int getType() {
        return type;
    }

    public int getStorageNum() {
        return storageNum;
    }
}
