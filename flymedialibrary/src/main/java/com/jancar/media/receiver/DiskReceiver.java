package com.jancar.media.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jancar.media.utils.FlyLog;

public class DiskReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        FlyLog.d(intent.toUri(0));
        if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
        }else if(intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
        }
    }
}