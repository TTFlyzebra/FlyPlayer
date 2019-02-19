// Notify.aidl
package com.jancar.media;

import com.jancar.media.data.Music;
import com.jancar.media.data.Image;
import com.jancar.media.data.Video;

// Declare any non-default types here with import statements

interface Notify {
    /**
     * 通知扫描服务返回的音乐列表
     */
    void notifyMusic(inout List<Music> list);

    /**
     * 通知扫描服务返回的视频列表
     */
    void notifyVideo(inout List<Video> list);

    /**
     * 通知扫描服务返回的图片列表
     */
    void notifyImage(inout List<Image> list);

    /**
     * 通知扫描服务返回的已有Id3信息的音乐列表
     */
    void notifyID3Music(inout List<Music> list);

    /**
     * 通知扫描服务将对指定路径开始扫描
     */
    void notifyPath(String path);

    /**
     * 通知扫描服务已完成对指定路径的扫描
     */
    void notifyFinish(String path);
}
