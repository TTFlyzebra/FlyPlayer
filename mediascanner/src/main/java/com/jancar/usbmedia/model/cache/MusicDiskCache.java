package com.jancar.usbmedia.model.cache;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.jakewharton.disklrucache.DiskLruCache;
import com.jancar.media.data.Music;
import com.jancar.usbmedia.utils.EncodeHelper;
import com.jancar.usbmedia.utils.FlyLog;
import com.jancar.usbmedia.utils.GsonUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Author: FlyZebra
 * Time: 18-3-29 下午9:07.
 * Discription: This is BitmapMemoryCache
 */

public class MusicDiskCache implements ICache<Music> {
    private final int max_size = 10 * 1024 * 1024;
    private static byte[] bytes = new byte[4096];
    private DiskLruCache mDiskLruCache;
    private Context mContext;

    public MusicDiskCache(Context context) {
        mContext = context;
        init();
    }

    public boolean init() {
        try {
            mDiskLruCache = DiskLruCache.open(new File(getSavePath(mContext)), getAppVersion(mContext), 1, max_size);
            return mDiskLruCache != null;
        } catch (IOException e) {
            FlyLog.e(e.toString());
            return false;
        }
    }

    @Override
    public Music get(String key) {
        Music music = null;
        DiskLruCache.Snapshot snapShot = null;
        InputStream in = null;
        try {
            snapShot = mDiskLruCache.get(EncodeHelper.md5(key));
            if (snapShot != null) {
                in = snapShot.getInputStream(0);
                int len = in.read(bytes);
                String json = new String(bytes, 0, len);
                music = GsonUtils.json2Object(json, Music.class);
            }
        } catch (IOException e) {
            FlyLog.e(e.toString());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (snapShot != null) {
                    snapShot.close();
                }
            } catch (IOException e) {
                FlyLog.e(e.toString());
            }
        }
        return music;
    }

    @Override
    public void put(String key, Music music) {
        String str = GsonUtils.obj2Json(music);
        if (TextUtils.isEmpty(str)) return;
        OutputStream outputStream = null;
        try {
            DiskLruCache.Editor editor = mDiskLruCache.edit(EncodeHelper.md5(key));
            if (editor == null) return;
            outputStream = editor.newOutputStream(0);
            byte[] bytes = str.getBytes();
            outputStream.write(bytes);
            outputStream.flush();
            editor.commit();
            mDiskLruCache.flush();
        } catch (IOException e) {
            FlyLog.e(e.toString());
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                FlyLog.e(e.toString());
            }
        }
    }

    private int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            FlyLog.e(e.toString());
        }
        return 1;
    }

    private String getSavePath(Context context) {
        File str = context.getCacheDir();
        String savePath = str.getAbsolutePath() + File.separator + "jancar" + File.separator + "musicid3";
        return savePath;
    }
}
