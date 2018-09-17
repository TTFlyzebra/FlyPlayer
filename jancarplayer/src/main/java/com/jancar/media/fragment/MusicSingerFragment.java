package com.jancar.media.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.jancar.media.R;
import com.jancar.media.base.MusicFragment;
import com.jancar.media.data.Music;
import com.jancar.media.utils.FlyLog;

import java.util.List;


public class MusicSingerFragment extends MusicFragment{
    private ExpandableListView expandableListView;

    public static MusicSingerFragment newInstance(Bundle args) {
        MusicSingerFragment musicSingerFragment = new MusicSingerFragment();
        musicSingerFragment.setArguments(args);
        return musicSingerFragment;
    }

    public MusicSingerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ex_list, null);
    }

    @Override
    public void statusChange(int statu) {
    }


    @Override
    public void musicUrlList(List<String> musicUrlList) {
        FlyLog.d("get videos size=%d", musicUrlList == null ? 0 : musicUrlList.size());
    }

    @Override
    public void musicID3UrlList(List<Music> musicUrlList) {
        FlyLog.d("get videos size=%d", musicUrlList == null ? 0 : musicUrlList.size());
    }
}
