package com.jancar.media.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.jancar.media.R;
import com.jancar.media.activity.VideoActivity;
import com.jancar.media.adpater.VideoFloderAdapter;
import com.jancar.media.base.BaseFragment;
import com.jancar.media.utils.FlyLog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tcking.github.com.giraffeplayer.GiraffePlayer;

public class VideoFloderFragment extends BaseFragment implements VideoFloderAdapter.OnItemClickListener,GiraffePlayer.OnPlayStatusChangeLiseter{
    private VideoActivity activity;
    private ExpandableListView expandableListView;
    private List<String> groupList = new ArrayList<>();
    private List<List<String>> itemList = new ArrayList<>();
    private VideoFloderAdapter adapter;
    private Map<String, List<String>> mHashMap = new HashMap<>();

    public static VideoFloderFragment newInstance(Bundle args) {
        VideoFloderFragment listPlayFileFragment = new VideoFloderFragment();
        listPlayFileFragment.setArguments(args);
        return listPlayFileFragment;
    }

    public VideoFloderFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (VideoActivity) getActivity();
        return inflater.inflate(R.layout.fragment_floder_grid_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        expandableListView = (ExpandableListView) view.findViewById(R.id.fm_file_list_el01);
        adapter = new VideoFloderAdapter(getActivity(), groupList, itemList);
        expandableListView.setAdapter(adapter);
        expandableListView.setGroupIndicator(null);
        adapter.setOnItemClickListener(this);
        activity.player.addStatusChangeLiseter(this);
    }

    @Override
    public void videoUrlList(List<String> videoUrlList) {
        FlyLog.d("get videos size=%d", videoUrlList == null ? 0 : videoUrlList.size());
        try {
            if (videoUrlList != null && getActivity() != null && activity != null) {
                mHashMap.clear();
                groupList.clear();
                itemList.clear();
                for (int i = 0; i < videoUrlList.size(); i++) {
                    String url = videoUrlList.get(i);
                    int last = url.lastIndexOf(File.separator);
                    String path = url.substring(0, last);
                    if (mHashMap.get(path) == null) {
                        mHashMap.put(path, new ArrayList<String>());
                    }
                    mHashMap.get(path).add(url);
                }
                groupList.addAll(mHashMap.keySet());

                Collections.sort(groupList, new Comparator<String>() {
                    public int compare(String p1, String p2) {
                        return p1.compareToIgnoreCase(p2);
                    }
                });

                for (String key : groupList) {
                    itemList.add(mHashMap.get(key));
                }

                adapter.notifyDataSetChanged();
            }
        }catch (Exception e){
            FlyLog.e(e.toString());
        }
    }

    @Override
    public void onItemClick(View view, String url) {
        //TODO:标记播放位置
        activity.player.play(url);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void statusChange(int statu) {
        adapter.notifyDataSetChanged();
    }
}
