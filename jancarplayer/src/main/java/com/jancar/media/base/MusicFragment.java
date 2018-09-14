package com.jancar.media.base;

import android.os.Bundle;

import com.jancar.media.activity.MusicActivity;
import com.jancar.media.listener.IMusicPlayerListener;
import com.jancar.media.model.IMusicPlayer;
import com.jancar.media.model.MusicPlayer;

import java.util.List;

public class MusicFragment extends BaseFragment implements
        IMusicPlayerListener {
    protected List<String> musicList;
    protected MusicActivity activity;
    protected IMusicPlayer musicPlayer = MusicPlayer.getInstance();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MusicActivity) getActivity();
        musicList = activity.musicList;
    }

    @Override
    public void onStart() {
        super.onStart();
        musicPlayer.addListener(this);
    }

    @Override
    public void onStop() {
        musicPlayer.removeListener(this);
        super.onStop();
    }

    @Override
    public void statusChange(int statu) {

    }
}
