package com.jancar.usbmedia.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class Utils {

    public static void startActivity(Context context, String pkg, String cls) {
        try {
            Intent intent = new Intent();
            ComponentName cn = new ComponentName(pkg, cls);
            intent.setComponent(cn);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }catch (Exception e){
            FlyLog.e(e.toString());
        }
    }
}
