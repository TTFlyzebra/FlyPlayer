package com.jancar.media.base;

import android.os.Bundle;

import com.jancar.media.activity.MusicActivity;
import com.jancar.media.model.IMusicPlayer;
import com.jancar.media.model.MusicPlayer;

public class MusicFragment extends BaseFragment{
    private MusicActivity activity;
    private IMusicPlayer musicPlayer = MusicPlayer.getInstance();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MusicActivity) getActivity();
    }
}
