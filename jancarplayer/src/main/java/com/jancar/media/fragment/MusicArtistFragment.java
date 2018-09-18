package com.jancar.media.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.jancar.media.R;
import com.jancar.media.adpater.MusicArtistAdapter;
import com.jancar.media.base.MusicFragment;
import com.jancar.media.data.Music;
import com.jancar.media.utils.FlyLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MusicArtistFragment extends MusicFragment implements
        MusicArtistAdapter.OnItemClickListener {
    private ExpandableListView expandableListView;
    private List<String> groupList = new ArrayList<>();
    private List<List<Music>> itemList = new ArrayList<>();
    private Map<String, List<Music>> mHashMap = new HashMap<>();
    private MusicArtistAdapter adapter;
    private boolean isClick = false;

    public static MusicArtistFragment newInstance(Bundle args) {
        MusicArtistFragment musicSingerFragment = new MusicArtistFragment();
        musicSingerFragment.setArguments(args);
        return musicSingerFragment;
    }

    public MusicArtistFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ex_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        expandableListView = (ExpandableListView) view.findViewById(R.id.fm_file_list_el01);
        expandableListView.setItemsCanFocus(true);
        adapter = new MusicArtistAdapter(getActivity(), groupList, itemList);
        expandableListView.setAdapter(adapter);
        expandableListView.setGroupIndicator(null);
        adapter.setOnItemClickListener(this);
    }


    @Override
    public void statusChange(int statu) {
        if (!isClick) {
            scrollCurrentPos();
        }
        isClick = false;
        adapter.notifyDataSetChanged();

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
                    if (itemList.get(i).get(j).url.equals(musicPlayer.getPlayUrl())) {
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
                    String artist = musicUrlList.get(i).artist;
                    if (mHashMap.get(artist) == null) {
                        mHashMap.put(artist, new ArrayList<Music>());
                    }
                    mHashMap.get(artist).add(musicUrlList.get(i));
                }
                groupList.addAll(mHashMap.keySet());

                Collections.sort(groupList, new Comparator<String>() {
                    public int compare(String p1, String p2) {
                        if (p1.startsWith(getString(R.string.no_album_start))) {
                            return 1;
                        }else if (p2.startsWith(getString(R.string.no_album_start))) {
                            return -1;
                        } else {
                            return p1.compareToIgnoreCase(p2);
                        }
                    }
                });

                for (String key : groupList) {
                    itemList.add(mHashMap.get(key));
                }
                scrollCurrentPos();
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }

    @Override
    public void onItemClick(View view, Music music) {
        isClick = true;
        musicPlayer.play(music.url);
    }
}
