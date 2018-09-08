package com.jancar.media.listener;

import java.util.List;

public interface IUsbMediaListener {
    /**
     * 通知更新扫描U盘所得到的music列表
     * @param musicUrlList 返回数据
     */
    void musicUrlList(List<String> musicUrlList);

    /**
     * 通知更新扫描U盘所得到的video列表
     * @param videoUrlList 返回数据
     */
    void videoUrlList(List<String> videoUrlList);


    /**
     * 通知更新扫描U盘所得到的Image列表
     * @param imageUrlList 返回数据
     */
    void imageUrlList(List<String> imageUrlList);

    /**
     *
     * @param usbstore U盘路径
     */
    void usbRemove(String usbstore);

}
