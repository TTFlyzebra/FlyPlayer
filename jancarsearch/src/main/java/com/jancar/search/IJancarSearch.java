package com.jancar.search;

import android.content.Context;

import com.jancar.search.JancarSearch.Result;

import java.util.List;

/**
 * Author FlyZebra
 * 2018/12/21 9:53
 * Describ:
 **/
public interface IJancarSearch {

    /**
     * 建议传入ApplictionContext,防止执有Activity对像引起内存泄漏
     *
     * @param context
     * @return
     */
    void register(Context context);


    /**
     * 释放资源
     */
    void unregister();

    /**
     * 按文件名搜索媒体文件（支持模糊搜索）
     *
     * @param fileName 文件名
     * @return
     */
    List<String> searchFileByName(String fileName);

    /**
     * 按歌手名称搜索
     *
     * @param singerName 歌手名称
     * @return
     */
    List<String> searchMusicBySinger(String singerName);

    /**
     * 按歌曲名称搜索
     *
     * @param title 歌曲名称
     * @return
     */
    List<String> searchMusicByTitle(String title);

    /**
     * 按专辑名称搜索
     *
     * @param album 专辑名称
     * @return
     */
    List<String> searchMusicByAlbum(String album);

    /**
     * 按歌手名称和按歌曲名称搜索
     *
     * @param singerName 歌手名称
     * @param title      歌曲名称
     * @return
     */
    List<String> searchMusic(String singerName, String title);


    /**
     * 按歌手名称,歌曲名称和专辑名称搜索
     *
     * @param singerName 歌手名称
     * @param title      歌曲名称
     * @param album      专辑名称
     * @return
     */
    List<String> searchMusic(String singerName, String title, String album);

    /**
     * 按文件名搜索媒体文件（支持模糊搜索）
     *
     * @param fileName 文件名
     * @return
     */
    void searchFileByName(String fileName, Result result);

    /**
     * 按歌手名称搜索
     *
     * @param singerName 歌手名称
     * @return
     */
    void searchMusicBySinger(String singerName, Result result);

    /**
     * 按歌曲名称搜索
     *
     * @param title 歌曲名称
     * @return
     */
    void searchMusicByTitle(String title, Result result);

    /**
     * 按专辑名称搜索
     *
     * @param album 专辑名称
     * @return
     */
    void searchMusicByAlbum(String album, Result result);

    /**
     * 按歌手名称和按歌曲名称搜索
     *
     * @param singerName 歌手名称
     * @param title      歌曲名称
     * @return
     */
    void searchMusic(String singerName, String title, Result result);


    /**
     * 按歌手名称,歌曲名称和专辑名称搜索
     *
     * @param singerName 歌手名称
     * @param title      歌曲名称
     * @return
     */
    void searchMusic(String singerName, String title, String album, Result result);



}
