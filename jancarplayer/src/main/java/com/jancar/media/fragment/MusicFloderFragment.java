package com.jancar.media.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.jancar.media.R;
import com.jancar.media.adpater.MusicFloderAdapter;
import com.jancar.media.base.MusicFragment;
import com.jancar.media.data.Music;
import com.jancar.media.model.musicplayer.MusicPlayer;
import com.jancar.media.utils.FlyLog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MusicFloderFragment extends MusicFragment implements
        MusicFloderAdapter.OnItemClickListener {
    private ExpandableListView expandableListView;
    private List<String> groupList = new ArrayList<>();
    private List<List<String>> itemList = new ArrayList<>();
    private Map<String, List<String>> mHashMap = new HashMap<>();
    private MusicFloderAdapter adapter;
    private boolean isClick = false;

    public static MusicFloderFragment newInstance(Bundle args) {
        MusicFloderFragment musicAlbumFragment = new MusicFloderFragment();
        musicAlbumFragment.setArguments(args);
        return musicAlbumFragment;
    }

    public MusicFloderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ex_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        expandableListView = (ExpandableListView) view.findViewById(R.id.fm_file_list_el01);
        expandableListView.setItemsCanFocus(true);
        adapter = new MusicFloderAdapter(getActivity(), groupList, itemList);
        expandableListView.setAdapter(adapter);
        expandableListView.setGroupIndicator(null);
        adapter.setOnItemClickListener(this);
        musicUrlList(mMusicList);
    }


    @Override
    public void playStatusChange(int statu) {
        switch (statu) {
            case MusicPlayer.STATUS_PLAYING:
                if (!isClick) {
                    scrollCurrentPos();
                }
                isClick = false;
                adapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        scrollCurrentPos();
    }

    private void scrollCurrentPos() {
        int findPos1 = -1;
        int findPos2 = -1;
        for (int i = 0; i < itemList.size(); i++) {
            if (findPos1 == -1) {
                for (int j = 0; j < itemList.get(i).size(); j++) {
                    if (itemList.get(i).get(j).equals(musicPlayer.getPlayUrl())) {
                        expandableListView.expandGroup(i, false);
                        findPos1 = i;
                        findPos2 = j;
                        break;
                    }
                }
            }
            if (i != findPos1) {
                expandableListView.collapseGroup(i);
            }
        }
        expandableListView.smoothScrollToPositionFromTop(findPos1, 0, 0);
    }

    @Override
    public void changePath(String path) {
        mHashMap.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void musicUrlList(List<Music> musicUrlList) {
        FlyLog.d("get musics size=%d", musicUrlList == null ? 0 : musicUrlList.size());
        try {
            if (musicUrlList != null && getActivity() != null && activity != null) {
                groupList.clear();
                itemList.clear();
                for (int i = 0; i < musicUrlList.size(); i++) {
                    String url = musicUrlList.get(i).url;
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

                /**
                 * 首次定位到当前播放位置
                 */
                scrollCurrentPos();
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }

    @Override
    public void musicID3UrlList(List<Music> musicUrlList) {
        FlyLog.d("get id3musics size=%d", musicUrlList == null ? 0 : musicUrlList.size());
    }

    @Override
    public void onItemClick(View view, String string) {
        isClick = true;
        musicPlayer.play(string);
    }
}
