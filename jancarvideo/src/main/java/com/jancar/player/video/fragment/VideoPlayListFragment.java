package com.jancar.player.video.fragment;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jancar.media.base.BaseFragment;
import com.jancar.media.data.Video;
import com.jancar.media.utils.DisplayUtils;
import com.jancar.media.utils.FlyLog;
import com.jancar.player.video.R;
import com.jancar.player.video.VideoActivity;
import com.jancar.player.video.adpater.VideoPlayListAdapater;

import java.util.List;

import tcking.github.com.giraffeplayer.GiraffePlayer;

public class VideoPlayListFragment extends BaseFragment implements
        VideoPlayListAdapater.OnItemClickListener,
        GiraffePlayer.OnPlayStatusChangeLiseter {
    private VideoActivity activity;
    private VideoPlayListAdapater adapter;
    private RecyclerView recyclerView;
    private TextView textView;
    private int spanCount = 3;

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
        return inflater.inflate(R.layout.fragment_rv_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        recyclerView = (RecyclerView) view.findViewById(R.id.fm_rv01);
        textView = (TextView) view.findViewById(R.id.fm_tv01);
        adapter = new VideoPlayListAdapater(getActivity(), activity.videoList, recyclerView);

        int width = DisplayUtils.getMetrices(getActivity()).widthPixels;
        if(width>=1280){
            spanCount = 4;
        }else{
            spanCount = 3;
        }


        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), spanCount);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        activity.player.addStatusChangeLiseter(this);
        adapter.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(VideoActivity.isScan){
            textView.setText(R.string.music_scan1);
        }else{
            textView.setText(String.format(getString(R.string.video_scan2), activity.videoList.size()));
        }
        scrollToCureentPlayItem();
    }

    @Override
    public void onDestroy() {
        activity.player.removeStatusChangeLiseter(this);
        adapter.cancleAllTask();
        adapter.setOnItemClickListener(null);
        super.onDestroy();
    }

    @Override
    public void notifyPathChange(String path) {
        textView.setText(R.string.music_scan1);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void videoUrlList(List<Video> videoUrlList) {
        FlyLog.d("get videos size=%d", videoUrlList == null ? 0 : videoUrlList.size());
//        scrollToCureentPlayItem();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void scanFinish(String path) {
        try {
            textView.setText(String.format(getString(R.string.video_scan2), activity.videoList.size()));
        }catch (Exception e){
            FlyLog.e(e.toString());
        }
    }

    @Override
    public void onItemClick(View view, int pos) {
        activity.currenPos = pos;
        activity.player.play(activity.videoList.get(activity.currenPos).url);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void statusChange(int statu) {
        scrollToCureentPlayItem();
        adapter.notifyDataSetChanged();
    }

    private void scrollToCureentPlayItem() {
        try {
            recyclerView.getLayoutManager().scrollToPosition(activity.currenPos);
        }catch (Exception e){
            FlyLog.e(e.toString());
        }
    }
}
