package com.jancar.jancarplayers;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import tcking.github.com.giraffeplayer.GiraffePlayer;

public class VideoActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageView play_fore,play_next,play_list;
    private RelativeLayout ac_video_palylist;
    private boolean isShowVideoPlayList = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        play_fore = (ImageView) findViewById(R.id.menu_play_fore);
        play_next = (ImageView) findViewById(R.id.menu_play_next);
        play_list = (ImageView) findViewById(R.id.menu_play_list);
        ac_video_palylist = (RelativeLayout) findViewById(R.id.ac_video_palylist);

        play_fore.setOnClickListener(this);
        play_next.setOnClickListener(this);
        play_list.setOnClickListener(this);

        GiraffePlayer player = new GiraffePlayer(this);
        player.play("file:///storage/udisk2/test.mp4");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.menu_play_list:
                isShowVideoPlayList=!isShowVideoPlayList;
                showVideoPlayListLayout(isShowVideoPlayList);
                break;
        }
    }

    private void showVideoPlayListLayout(boolean isShow) {
        ac_video_palylist.animate().translationX(isShow?-341:0).setDuration(300).start();
    }
}
