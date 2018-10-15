package com.jancar.player.music.model.listener;

import com.jancar.media.data.StorageInfo;

import java.util.List;

public interface IStorageListener {

    void storageList(List<StorageInfo> storageList);
}
