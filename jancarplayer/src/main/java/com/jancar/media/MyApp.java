package com.jancar.media;

import android.app.Application;
import android.view.ViewConfiguration;

import com.jancar.media.model.Storage;
import com.jancar.media.model.UsbMediaScan;
import com.jancar.media.utils.FlyLog;

import java.lang.reflect.Field;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        UsbMediaScan.getInstance().init(getApplicationContext());
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
}
