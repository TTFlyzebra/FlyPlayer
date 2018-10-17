package com.jancar.media.model.listener;

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
     * 存储器改变，每次扫描都有两次通知，一次为读取缓存的通知，一次为扫描完成的通知
     * @param path
     */
    void notifyPathChange(String path);


    /**
     * 存储器扫描完成
     * @param path
     */
    void scanFinish(String path);


    /**
     * 连接服务成功
    */
    void scanServiceConneted();

}
