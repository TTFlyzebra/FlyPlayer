package com.jancar.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Author FlyZebra
 * 2018/12/21 9:53
 * Describ:
 **/
public class JancarSearch implements IJancarSearch {
    private Context mContext;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private static final Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private JancarSearch() {
    }

    public interface Result {
        /**
         * 显示搜索返回结果
         * @param list
         */
        void notifySearch(List<String> list);
    }

    private static class JancarSearchHolder {
        @SuppressLint("StaticFieldLeak")
        static final JancarSearch sInstance = new JancarSearch();
    }

    public static JancarSearch getInstance() {
        return JancarSearchHolder.sInstance;
    }

    @Override
    public void register(Context context) {
        this.mContext = context;
    }

    @Override
    public void unregister() {
        mHandler.removeCallbacksAndMessages(null);
        this.mContext = null;
    }

    @Override
    public List<String> searchFileByName(String fileName) {
        return null;
    }

    @Override
    public List<String> searchMusicBySinger(String singerName) {
        SearchLog.d("search music singerName=%s", singerName);
        List<String> list = new ArrayList<>();
        try {
            if (mContext != null) {
                Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        new String[]{
                                MediaStore.Audio.Media.TITLE,
                                MediaStore.Audio.Media.ARTIST,
                                MediaStore.Audio.Media.ALBUM,
                                MediaStore.Audio.Media.DATA},
                        MediaStore.Audio.Media.ARTIST + " LIKE" + " '%" + singerName + "%'",
                        null, MediaStore.Audio.Media.DATE_ADDED + " DESC");
                while (cursor != null && cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    list.add(path);
                }
            }
        } catch (Exception e) {
            SearchLog.e(e.toString());
        }
        return list;
    }

    @Override
    public List<String> searchMusicByTitle(String title) {
        SearchLog.d("search music title=%s", title);
        List<String> list = new ArrayList<>();
        try {
            if (mContext != null) {
                Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        new String[]{
                                MediaStore.Audio.Media.TITLE,
                                MediaStore.Audio.Media.ARTIST,
                                MediaStore.Audio.Media.ALBUM,
                                MediaStore.Audio.Media.DATA},
                        MediaStore.Audio.Media.TITLE + " LIKE" + " '%" + title + "%'",
                        null, MediaStore.Audio.Media.DATE_ADDED + " DESC");
                while (cursor != null && cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    list.add(path);
                }
            }
        } catch (Exception e) {
            SearchLog.e(e.toString());
        }
        return list;
    }

    @Override
    public List<String> searchMusicByAlbum(String album) {
        SearchLog.d("search music album=%s", album);
        List<String> list = new ArrayList<>();
        try {
            if (mContext != null) {
                Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        new String[]{
                                MediaStore.Audio.Media.TITLE,
                                MediaStore.Audio.Media.ARTIST,
                                MediaStore.Audio.Media.ALBUM,
                                MediaStore.Audio.Media.DATA},
                        MediaStore.Audio.Media.ALBUM + " LIKE" + " '%" + album + "%'",
                        null, MediaStore.Audio.Media.DATE_ADDED + " DESC");
                while (cursor != null && cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    list.add(path);
                }
            }
        } catch (Exception e) {
            SearchLog.e(e.toString());
        }
        return list;
    }

    @Override
    public List<String> searchMusic(String singerName, String title) {
        SearchLog.d("search music singerName=%s,title=%s", singerName, title);
        List<String> list = new ArrayList<>();
        try {
            if (mContext != null) {
                Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        new String[]{
                                MediaStore.Audio.Media.TITLE,
                                MediaStore.Audio.Media.ARTIST,
                                MediaStore.Audio.Media.ALBUM,
                                MediaStore.Audio.Media.DATA},
                        MediaStore.Audio.Media.TITLE + " LIKE" + " '%" + title + "%'" +
                                " AND " + MediaStore.Audio.Media.ARTIST + " LIKE" + " '%" + singerName + "%'",
                        null, MediaStore.Audio.Media.DATE_ADDED + " DESC");
                while (cursor != null && cursor.moveToNext()) {
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    list.add(path);
                }
            }
        } catch (Exception e) {
            SearchLog.e(e.toString());
        }
        return list;
    }

    @Override
    public void searchFileByName(String fileName, Result result) {
        executor.execute(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void searchMusicBySinger(final String singerName, final Result result) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                result.notifySearch(searchMusicBySinger(singerName));
            }
        });
    }

    @Override
    public void searchMusicByTitle(final String title, final Result result) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                result.notifySearch(searchMusicByTitle(title));
            }
        });
    }

    @Override
    public void searchMusicByAlbum(final String album, final Result result) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                result.notifySearch(searchMusicByAlbum(album));
            }
        });
    }

    @Override
    public void searchMusic(final String singerName, final String title, final Result result) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                result.notifySearch(searchMusic(singerName,title));
            }
        });
    }

}
