package com.jancar.media.model.musicplayer;

public interface IPlayStatus {
    /**
     * 播放出错
     */
    int STATUS_ERROR = -1;
    /**
     * 未初始化
     */
    int STATUS_IDLE = 0;
    /**
     * 加载缓存准备播放
     */
    int STATUS_LOADING = 1;
    /**
     * 正在播放
     */
    int STATUS_PLAYING = 2;
    /**
     * 暂停播放
     */
    int STATUS_PAUSE = 3;
    /**
     * 播放完成
     */
    int STATUS_COMPLETED = 4;
}
