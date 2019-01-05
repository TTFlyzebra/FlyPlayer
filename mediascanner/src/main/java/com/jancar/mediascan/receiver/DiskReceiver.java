package com.jancar.mediascan.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.jancar.mediascan.data.Const;
import com.jancar.mediascan.service.FlyMediaService;
import com.jancar.mediascan.utils.FlyLog;


public class DiskReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        FlyLog.d("DiskReceiver intent="+intent.toUri(0));
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        switch (action) {
            case Intent.ACTION_MEDIA_MOUNTED: {
                final Uri uri = intent.getData();
                if (uri == null) return;
                if (!uri.getScheme().equals("file")) return;
                String path = uri.getPath();
                if (path == null) return;
                FlyLog.d("MEDIA_MOUNTED path=%s", path);
                Intent scanService = new Intent(context, FlyMediaService.class);
                scanService.putExtra(Const.SCAN_PATH_KEY, path);
                context.startService(scanService);
                break;
            }
            case Intent.ACTION_MEDIA_EJECT:
            case Intent.ACTION_MEDIA_REMOVED:
            case Intent.ACTION_MEDIA_BAD_REMOVAL:
            case Intent.ACTION_MEDIA_UNMOUNTED: {
                final Uri uri = intent.getData();
                if (uri == null) return;
                if (!uri.getScheme().equals("file")) return;
                String path = uri.getPath();
                if (path == null) return;
                FlyLog.d("MEDIA_UNMOUNTED path=%s", path);
                Intent removeService = new Intent(context, FlyMediaService.class);
                removeService.putExtra(Const.UMOUNT_STORE, path);
                context.startService(removeService);
                break;
            }
            default:
                break;
        }

    }
}
