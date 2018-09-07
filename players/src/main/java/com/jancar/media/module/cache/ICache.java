package com.jancar.media.module.cache;

import android.graphics.Bitmap;

/**
 * Author: FlyZebra
 * Time: 18-3-29 下午9:07.
 * Discription: This is ICache
 */

public interface ICache<T extends Object> {
    Bitmap get(String key);
    void put(String key, T bitmap);
}
