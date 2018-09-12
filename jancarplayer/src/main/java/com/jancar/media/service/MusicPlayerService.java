package com.jancar.media.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MusicPlayerService extends Service{
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
