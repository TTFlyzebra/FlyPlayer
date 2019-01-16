package com.jancar.player.music;


import android.os.Bundle;

public class MusicActivity extends MusicActivity_AP3{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setBackgroundResource(R.drawable.ic_music_bak);
    }
}
