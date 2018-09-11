package com.jancar.media.fragment;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jancar.media.R;
import com.jancar.media.activity.VideoActivity;
import com.jancar.media.adpater.PlayFileAdapater;
import com.jancar.media.base.BaseFragment;
import com.jancar.media.utils.FlyLog;

import java.util.List;

import tcking.github.com.giraffeplayer.GiraffePlayer;

public class VideoPlayListFragment extends BaseFragment implements PlayFileAdapater.OnItemClickListener,GiraffePlayer.OnPlayStatusChangeLiseter {
    private VideoActivity activity;
    private PlayFileAdapater adapter;
    private RecyclerView recyclerView;

    public static VideoPlayListFragment newInstance(Bundle args) {
        VideoPlayListFragment listPlayFileFragment = new VideoPlayListFragment();
        listPlayFileFragment.setArguments(args);
        return listPlayFileFragment;
    }

    public VideoPlayListFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (VideoActivity) getActivity();
        return inflater.inflate(R.layout.fragment_photo_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        recyclerView = (RecyclerView) view.findViewById(R.id.fm_photo_list_rv01);
        adapter = new PlayFileAdapater(getActivity(), activity.videoList, recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        activity.player.addStatusChangeLiseter(this);
        adapter.setOnItemClickListener(this);
    }

    @Override
    public void videoUrlList(List<String> videoUrlList) {
        FlyLog.d("get videos size=%d", videoUrlList == null ? 0 : videoUrlList.size());
        if (videoUrlList != null && getActivity() != null && activity != null) {
            activity.videoList.clear();
            activity.videoList.addAll(videoUrlList);
            adapter.update();
            if (videoUrlList.isEmpty()) {
                if (!activity.player.isPlaying()) {
                    activity.player.stop();
                }
            } else {
                if (!activity.player.isPlaying()) {
                    VideoActivity.currentPlayUrl = activity.videoList.get(0);
                    activity.player.play(VideoActivity.currentPlayUrl);
                    adapter.update();
                }
            }
        }
    }

    @Override
    public void onItemClick(View view, int pos) {
        activity.currenPos = pos;
        activity.currentPlayUrl = activity.videoList.get(activity.currenPos);
        activity.player.play(activity.currentPlayUrl);
        adapter.update();
    }

    @Override
    public void statusChange(int statu) {
        adapter.update();
    }
}
