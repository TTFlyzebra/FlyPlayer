// IMedia.aidl
package com.jancar.media;

import com.jancar.media.Notify;

interface FlyMedia {

    /**
     * 通知扫描服务扫描指定路径。
     */
    void scanDisk(String disk);

    /**
     * 注册接收扫描获取的信息通知
     */
    void registerNotify(Notify notify);

    /**
     * 注销接收扫描获取的信息通知
     */
    void unregisterNotify(Notify notify);
}
