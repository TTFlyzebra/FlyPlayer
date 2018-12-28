package com.jancar.player.music.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.jancar.media.data.Music;
import com.jancar.media.utils.FlyLog;
import com.jancar.player.music.R;
import com.jancar.player.music.adpater.MusicAlbumAdapter;
import com.jancar.player.music.model.musicplayer.MusicPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MusicAlbumFragment extends MusicFragment implements
        MusicAlbumAdapter.OnItemClickListener {
    private ExpandableListView expandableListView;
    private List<String> albumGroupList = new ArrayList<>();
    private List<List<String>> albumItemList = new ArrayList<>();
    private Map<String, List<String>> ablumHashMap = new HashMap<>();
    private MusicAlbumAdapter adapter;
    private boolean isClick = false;

    public static MusicAlbumFragment newInstance(Bundle args) {
        MusicAlbumFragment musicAlbumFragment = new MusicAlbumFragment();
        musicAlbumFragment.setArguments(args);
        return musicAlbumFragment;
    }

    public MusicAlbumFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ex_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        expandableListView = (ExpandableListView) view.findViewById(R.id.fm_file_list_el01);
        expandableListView.setItemsCanFocus(true);
        adapter = new MusicAlbumAdapter(getActivity(), albumGroupList, albumItemList);
        expandableListView.setAdapter(adapter);
        expandableListView.setGroupIndicator(null);
        adapter.setOnItemClickListener(this);
        ablumHashMap.clear();
        musicID3UrlList(mMusicList);
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
        for (int i = 0; i < albumItemList.size(); i++) {
            if (findPos1 == -1) {
                for (int j = 0; j < albumItemList.get(i).size(); j++) {
                    if (albumItemList.get(i).get(j).equals(musicPlayer.getPlayUrl())) {
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
    public void musicUrlList(List<Music> musicUrlList) {
        FlyLog.d("get musics size=%d", musicUrlList == null ? 0 : musicUrlList.size());
    }

    @Override
    public void notifyPathChange(String path) {
        ablumHashMap.clear();
        adapter.notifyDataSetChanged();
    }

    private boolean isFistGet = true;

    @Override
    public void musicID3UrlList(List<Music> musicUrlList) {
        FlyLog.d("get id3musics size=%d", musicUrlList == null ? 0 : musicUrlList.size());
        try {
            if (musicUrlList != null && getActivity() != null && activity != null) {
                albumGroupList.clear();
                albumItemList.clear();
                for (int i = 0; i < musicUrlList.size(); i++) {
                    String album = musicUrlList.get(i).album;
                    if (ablumHashMap.get(album) == null) {
                        ablumHashMap.put(album, new ArrayList<String>());
                    }
                    ablumHashMap.get(album).add(musicUrlList.get(i).url);
                }
                albumGroupList.addAll(ablumHashMap.keySet());

//                Collections.sort(groupList, new Comparator<String>() {
//                    public int compare(String p1, String p2) {
//                        if (p1.startsWith(getString(R.string.no_album_start))) {
//                            return 1;
//                        } else if (p2.startsWith(getString(R.string.no_album_start))) {
//                            return -1;
//                        } else {
//                            return p1.compareToIgnoreCase(p2);
//                        }
//                    }
//                });

                for (String key : albumGroupList) {
                    albumItemList.add(ablumHashMap.get(key));
                }
                if (isFistGet) {
                    isFistGet = false;
                    scrollCurrentPos();
                }
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }

    @Override
    public void onItemClick(View view, String url) {
        isClick = true;
        musicPlayer.play(url);
    }

    @Override
    public void onDestroy() {
        adapter.setOnItemClickListener(null);
        super.onDestroy();
    }
}
