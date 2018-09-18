package com.jancar.media.fragment;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jancar.media.R;
import com.jancar.media.activity.VideoActivity;
import com.jancar.media.adpater.VideoPlayListAdapater;
import com.jancar.media.base.BaseFragment;
import com.jancar.media.utils.FlyLog;

import java.util.List;

import tcking.github.com.giraffeplayer.GiraffePlayer;

public class VideoPlayListFragment extends BaseFragment implements
        VideoPlayListAdapater.OnItemClickListener,
        GiraffePlayer.OnPlayStatusChangeLiseter {
    private VideoActivity activity;
    private VideoPlayListAdapater adapter;
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
        return inflater.inflate(R.layout.fragment_video_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        recyclerView = (RecyclerView) view.findViewById(R.id.fm_photo_list_rv01);
        adapter = new VideoPlayListAdapater(getActivity(), activity.videoList, recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        activity.player.addStatusChangeLiseter(this);
        adapter.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        scrollToCureentPlayItem();
    }

    @Override
    public void onDestroy() {
        adapter.cancleAllTask();
        super.onDestroy();
    }

    @Override
    public void videoUrlList(List<String> videoUrlList) {
        FlyLog.d("get videos size=%d", videoUrlList == null ? 0 : videoUrlList.size());
        scrollToCureentPlayItem();
        adapter.update();
    }

    @Override
    public void onItemClick(View view, int pos) {
        activity.currenPos = pos;
        activity.player.play(activity.videoList.get(activity.currenPos));
        adapter.update();
    }

    @Override
    public void statusChange(int statu) {
        adapter.update();
    }

    private void scrollToCureentPlayItem() {
        try {
            recyclerView.getLayoutManager().scrollToPosition(activity.currenPos);
        }catch (Exception e){
            FlyLog.e(e.toString());
        }
    }
}
