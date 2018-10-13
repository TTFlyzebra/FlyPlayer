package com.jancar.music.model.listener;

import com.jancar.media.data.Image;
import com.jancar.media.data.Music;
import com.jancar.media.data.Video;

import java.util.List;

public interface IUsbMediaListener {
    /**
     * 通知更新扫描U盘所得到的music列表
     * @param musicUrlList 返回数据
     */
    void musicUrlList(List<Music> musicUrlList);

    /**
     * 通知更新扫描U盘所得到的music列表
     * @param musicUrlList 返回数据
     */
    void musicID3UrlList(List<Music> musicUrlList);

    /**
     * 通知更新扫描U盘所得到的video列表
     * @param videoUrlList 返回数据
 */
    void videoUrlList(List<Video> videoUrlList);


    /**
     * 通知更新扫描U盘所得到的Image列表
     * @param imageUrlList 返回数据
     */
    void imageUrlList(List<Image> imageUrlList);

    /**
     * 存储器改变
     * @param path
     */
    void changePath(String path);

}
