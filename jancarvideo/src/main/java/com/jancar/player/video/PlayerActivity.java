package com.jancar.player.video;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import tcking.github.com.giraffeplayer.IjkVideoView;
import tv.danmaku.ijk.media.player.IMediaPlayer;

public class PlayerActivity extends Activity {
    private IjkVideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_player);
        videoView = (IjkVideoView) findViewById(R.id.ac_player_video_view);
        videoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                finish();
                return false;
            }
        });
        play(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(videoView!=null){
            videoView.stopPlayback();
        }
        play(intent);
    }

    void play(Intent intent){
        Uri uri = intent.getData();
        if(uri!=null) {
            videoView.setVideoURI(uri);
            videoView.start();
        }else{
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        videoView.stopPlayback();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
