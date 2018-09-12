package com.jancar.media.base;

import android.os.Bundle;

import com.jancar.media.activity.MusicActivity;
import com.jancar.media.model.IMusicPlayer;
import com.jancar.media.model.MusicPlayer;

import java.util.List;

public class MusicFragment extends BaseFragment{
    protected List<String> musicList;
    protected MusicActivity activity;
    protected IMusicPlayer musicPlayer = MusicPlayer.getInstance();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MusicActivity) getActivity();
        musicList = activity.musicList;
    }

}
