package com.jancar.jancarplayers;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import tcking.github.com.giraffeplayer.GiraffePlayer;
import tcking.github.com.giraffeplayer.IjkVideoView;

public class VideoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        GiraffePlayer player = new GiraffePlayer(this);
        player.play("file:///storage/udisk2/test.mp4");
    }
}
