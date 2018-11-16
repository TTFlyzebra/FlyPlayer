package com.jancar.player.photo.data;

import android.content.Context;
import android.support.v4.util.Pools;

import com.github.chrisbanes.photoview.PhotoView;

public class PhotoViewPool {
    private Pools.SimplePool<PhotoView> pool;
    private static PhotoViewPool photoViewPool;

    private PhotoViewPool() {
        pool = new Pools.SimplePool<>(10);
    }

    public static synchronized PhotoViewPool getInstance() {
        if (photoViewPool == null) {
            photoViewPool = new PhotoViewPool();
        }
        return photoViewPool;
    }

    public Pools.SimplePool<PhotoView> getPool() {
        return pool;
    }

    //对象池中获取对象
    public static PhotoView obtain(Context context) {
        try {
            PhotoView photoView = getInstance().getPool().acquire();
            return photoView == null ? new PhotoView(context) : photoView;
        } catch (Exception e) {
            return new PhotoView(context);
        }
    }

    //返回对象
    public static void recycle(PhotoView uiGeter) {
        try {
            getInstance().getPool().release(uiGeter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
