package com.jancar.player.music.fragment;

import android.os.Bundle;

import com.jancar.media.base.BaseFragment;
import com.jancar.media.data.Music;
import com.jancar.media.model.listener.IMusicPlayerListener;
import com.jancar.media.model.musicplayer.IMusicPlayer;
import com.jancar.media.model.musicplayer.MusicPlayer;
import com.jancar.player.music.BaseMusicActivity;

import java.util.List;

public class MusicFragment extends BaseFragment implements
        IMusicPlayerListener {
    protected List<Music> mMusicList;
    protected BaseMusicActivity activity;
    protected IMusicPlayer musicPlayer = MusicPlayer.getInstance();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (BaseMusicActivity) getActivity();
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
