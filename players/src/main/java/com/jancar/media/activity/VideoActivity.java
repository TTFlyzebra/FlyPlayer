package com.jancar.media.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.jancar.media.R;
import com.jancar.media.base.BaseActivity;
import com.jancar.media.fragment.PhotoListFragment;
import com.jancar.media.utils.FlyLog;

import java.util.ArrayList;
import java.util.List;

import tcking.github.com.giraffeplayer.GiraffePlayer;

import static tcking.github.com.giraffeplayer.GiraffePlayer.isShowVideoPlayList;

public class VideoActivity extends BaseActivity implements View.OnClickListener {
    private ImageView play_fore, play_next, play_list;
    private LinearLayout play_ll01_playlist;
    private String currentPlayUrl;

    public static List<String> videoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        play_fore = (ImageView) findViewById(R.id.menu_play_fore);
        play_next = (ImageView) findViewById(R.id.menu_play_next);
        play_list = (ImageView) findViewById(R.id.menu_play_list);
        play_ll01_playlist = (LinearLayout) findViewById(R.id.play_ll01_playlist);

        play_fore.setOnClickListener(this);
        play_next.setOnClickListener(this);
        play_list.setOnClickListener(this);


        final GiraffePlayer player = new GiraffePlayer(this);


        Intent intent = getIntent();
        String playUrl = intent.getStringExtra("playurl");
        FlyLog.d("playurl=%s",playUrl);
        if(!TextUtils.isEmpty(playUrl)) {
            currentPlayUrl = playUrl;
            player.play(playUrl);
        }
        getFragmentManager().beginTransaction().replace(R.id.ac_video_fragment,new PhotoListFragment()).commit();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_play_list:
                showOrHideVideoPlayListLayout();
                break;
        }
    }

    private void showOrHideVideoPlayListLayout() {
        isShowVideoPlayList = !isShowVideoPlayList;
        play_ll01_playlist.animate().translationX(isShowVideoPlayList ? -341 : 0).setDuration(300).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
