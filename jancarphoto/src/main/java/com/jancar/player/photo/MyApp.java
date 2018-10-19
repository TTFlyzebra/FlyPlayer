package com.jancar.player.photo;

import android.app.Application;
import android.content.Context;
import android.view.ViewConfiguration;

import com.jancar.media.model.mediascan.MediaScan;
import com.jancar.media.model.storage.Storage;
import com.jancar.media.utils.FlyLog;

import java.lang.reflect.Field;

public class MyApp extends  Application{

    @Override
    public void onCreate() {
        super.onCreate();
        MediaScan.getInstance().init(getApplicationContext());
        Storage.getInstance().init(getApplicationContext());
        /**
         * 设置Marquee不显示省略号
         */
        try {
            ViewConfiguration configuration = ViewConfiguration.get(getApplicationContext());
            Class claz = configuration.getClass();
            Field field = claz.getDeclaredField("mFadingMarqueeEnabled");
            field.setAccessible(true);
            field.set(configuration, true);
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
