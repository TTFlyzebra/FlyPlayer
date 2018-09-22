package com.jancar.mediascan.model.cache;

import android.content.Context;
import android.util.LruCache;

import com.jancar.media.data.Music;

/**
 * Author: FlyZebra
 * Time: 18-3-29 下午9:07.
 * Discription: This is BitmapMemoryCache
 */

public class MusicMemoryCache implements ICache<Music> {
    LruCache<String, Music> lruCache;

    public MusicMemoryCache(Context context) {
        lruCache = new LruCache<String, Music>(65535) {
            @Override
            protected int sizeOf(String key, Music value) {
                return 1;
            }
        };
    }

    @Override
    public Music get(String key) {
        return lruCache.get(key);
    }

    @Override
    public void put(String key, Music music) {
        lruCache.put(key, music);
    }
}
