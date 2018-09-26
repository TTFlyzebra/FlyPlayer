package com.jancar.media.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat.Builder;
import android.widget.RemoteViews;

import com.jancar.media.R;
import com.jancar.media.activity.MusicActivity;
import com.jancar.media.model.listener.IMusicPlayerListener;
import com.jancar.media.model.musicplayer.IMusicPlayer;
import com.jancar.media.model.musicplayer.MusicPlayer;


public class MusicService extends Service implements IMusicPlayerListener {
    private final static String SERVICEACTION = "BROADCAST_SERVICE";
    private RemoteViews remoteviews = null;
    //	private NotificationManager notimanager = null;
    private final int NOTIFICATION_ID = 1;
    private Notification noti = null;

    private IMusicPlayer musicPlayer = MusicPlayer.getInstance();

    @SuppressLint("NewApi")
    @Override
    public void onCreate() {
        super.onCreate();
        remoteviews = new RemoteViews(getPackageName(), R.layout.notification);
        remoteviews.setImageViewResource(R.id.noti_butfore, R.drawable.butfore);
        remoteviews.setImageViewResource(R.id.noti_butplay, R.drawable.butstop);
        remoteviews.setImageViewResource(R.id.noti_butnext, R.drawable.butnext);
        remoteviews.setImageViewResource(R.id.noti_butexit, R.drawable.butexit_noti);
        Intent intent = new Intent();
        intent.setAction(SERVICEACTION);
        intent.putExtra("ACTION", "FORE");
        PendingIntent nextPI = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        intent.putExtra("ACTION", "PLAY");
        PendingIntent playPI = PendingIntent.getBroadcast(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        intent.putExtra("ACTION", "NEXT");
        PendingIntent forePI = PendingIntent.getBroadcast(this, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        intent.putExtra("ACTION", "EXIT");
        PendingIntent exitPI = PendingIntent.getBroadcast(this, 4, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteviews.setOnClickPendingIntent(R.id.noti_butfore, nextPI);
        remoteviews.setOnClickPendingIntent(R.id.noti_butplay, playPI);
        remoteviews.setOnClickPendingIntent(R.id.noti_butnext, forePI);
        remoteviews.setOnClickPendingIntent(R.id.noti_butexit, exitPI);
        setRemoteViews();
        Intent notiintent = new Intent(this, MusicActivity.class);
        notiintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent PdIntent = PendingIntent.getActivity(this, 0, notiintent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Bitmap icon =
        // BitmapFactory.decodeResource(getResources(),R.drawable.icon);
        noti = new Builder(MusicService.this)
                .setContent(remoteviews)
                .setContentIntent(PdIntent)
                .setOngoing(true)
                .build();
        noti.bigContentView = remoteviews;
        noti.icon = android.R.drawable.ic_media_play;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showNotification();
        return START_STICKY;
    }

    private void PlayForeMusic() {
        musicPlayer.playFore();
    }

    private void PlayNextMusic() {
        musicPlayer.playNext();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    private void showNotification() {
        setRemoteViews();
        startForeground(NOTIFICATION_ID, noti);
    }

    private void setRemoteViews() {
        remoteviews.setTextViewText(R.id.noti_tv01_musicname, musicPlayer.getPlayUrl());
        if (musicPlayer.isPlaying()) {
            remoteviews.setImageViewResource(R.id.noti_butplay,
                    R.drawable.butstop);
        } else {
            remoteviews.setImageViewResource(R.id.noti_butplay,
                    R.drawable.butplay);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void playStatusChange(int statu) {

    }

    @Override
    public void loopStatusChange(int staut) {

    }
}
