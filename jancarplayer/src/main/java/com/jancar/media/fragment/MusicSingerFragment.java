package com.jancar.media.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.jancar.media.R;
import com.jancar.media.adpater.MusicSingerAdapter;
import com.jancar.media.base.MusicFragment;
import com.jancar.media.data.Music;
import com.jancar.media.utils.FlyLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MusicSingerFragment extends MusicFragment{
    private ExpandableListView expandableListView;
    private List<String> groupList = new ArrayList<>();
    private List<List<Music>> itemList = new ArrayList<>();
    private Map<String, List<Music>> mHashMap = new HashMap<>();
    private MusicSingerAdapter adapter;

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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        expandableListView = (ExpandableListView) view.findViewById(R.id.fm_file_list_el01);
        adapter = new MusicSingerAdapter(getActivity(), groupList, itemList);
        expandableListView.setAdapter(adapter);
        expandableListView.setGroupIndicator(null);
    }


    @Override
    public void statusChange(int statu) {
    }


    @Override
    public void musicUrlList(List<String> musicUrlList) {
        FlyLog.d("get musics size=%d", musicUrlList == null ? 0 : musicUrlList.size());
    }

    @Override
    public void musicID3UrlList(List<Music> musicUrlList) {
        FlyLog.d("get id3musics size=%d", musicUrlList == null ? 0 : musicUrlList.size());
        try {
            if (musicUrlList != null && getActivity() != null && activity != null) {
                mHashMap.clear();
                groupList.clear();
                itemList.clear();
                for (int i = 0; i < musicUrlList.size(); i++) {
                    String singer = musicUrlList.get(i).artist;
                    if (mHashMap.get(singer) == null) {
                        mHashMap.put(singer, new ArrayList<Music>());
                    }
                    mHashMap.get(singer).add(musicUrlList.get(i));
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
}
