package com.jancar.music.fragment;

import android.os.Bundle;

import com.jancar.music.MainActivity;
import com.jancar.media.base.BaseFragment;
import com.jancar.media.data.Music;
import com.jancar.music.model.listener.IMusicPlayerListener;
import com.jancar.music.model.musicplayer.IMusicPlayer;
import com.jancar.music.model.musicplayer.MusicPlayer;

import java.util.List;

public class MusicFragment extends BaseFragment implements
        IMusicPlayerListener {
    protected List<Music> mMusicList;
    protected MainActivity activity;
    protected IMusicPlayer musicPlayer = MusicPlayer.getInstance();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        mMusicList = activity.musicList;
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
    public void playStatusChange(int statu) {

    }

    @Override
    public void loopStatusChange(int staut) {

    }
}
