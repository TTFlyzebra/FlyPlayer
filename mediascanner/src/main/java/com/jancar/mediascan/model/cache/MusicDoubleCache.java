package com.jancar.mediascan.model.cache;

import android.content.Context;

import com.jancar.media.data.Music;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Author: FlyZebra
 * Time: 18-3-29 下午8:58.
 * Discription: This is MusicCache
 */

public class MusicDoubleCache implements ICache<Music> {
    private MusicMemoryCache musicMemoryCache;
    private MusicDiskCache musicDiskCache;
    private final static Executor executor = Executors.newFixedThreadPool(1);
    private static MusicDoubleCache doubleMusicCache = null;

    public static synchronized MusicDoubleCache getInstance(Context context) {
        if (doubleMusicCache == null) {
            doubleMusicCache = new MusicDoubleCache(context);
        }
        return doubleMusicCache;
    }

    private MusicDoubleCache(Context context) {
        musicDiskCache = new MusicDiskCache(context);
        musicMemoryCache = new MusicMemoryCache(context);
    }

    public Music get(String url) {
        Music music = musicMemoryCache.get(url);
        if (music == null) {
            music = musicDiskCache.get(url);
            if (music != null) {
                musicMemoryCache.put(url, music);
            }
        }
        return music;
    }

    @Override
    public void put(final String url, final Music music) {
        if (get(url) == null) {
            musicMemoryCache.put(url, music);
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                musicDiskCache.put(url, music);
            }
        });
    }

}
