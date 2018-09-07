package com.jancar.usbmedia.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.jancar.usbmedia.service.FlyMediaService;
import com.jancar.usbmedia.utils.FlyLog;


public class USBDiskReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        FlyLog.d(intent.toUri(0));
        if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
            final Uri uri = intent.getData();
            if (uri == null) return;
            if (!uri.getScheme().equals("file")) return;
            String path = uri.getPath();
            if (path == null) return;
            Intent scanService = new Intent(context, FlyMediaService.class);
            intent.putExtra("SCANPATH", path);
            context.startService(scanService);
        }
    }
}
