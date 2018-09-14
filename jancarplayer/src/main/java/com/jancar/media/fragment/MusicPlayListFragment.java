package com.jancar.media.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jancar.media.R;
import com.jancar.media.adpater.MusicPlayListAdapter;
import com.jancar.media.base.MusicFragment;
import com.jancar.media.model.MusicPlayer;
import com.jancar.media.module.RecycleViewDivider;
import com.jancar.media.utils.FlyLog;

import java.util.List;

public class MusicPlayListFragment extends MusicFragment implements
        MusicPlayListAdapter.OnItemClickListener {
    private RecyclerView recyclerView;
    private MusicPlayListAdapter adapter;

    public static MusicPlayListFragment newInstance(Bundle args) {
        MusicPlayListFragment musicPlayListFragment = new MusicPlayListFragment();
        musicPlayListFragment.setArguments(args);
        return musicPlayListFragment;
    }

    public MusicPlayListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        recyclerView = (RecyclerView) view.findViewById(R.id.fm_music_list_rv01);
        adapter = new MusicPlayListAdapter(activity, musicList, recyclerView);
        adapter.setOnItemClickListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new RecycleViewDivider(getActivity(), LinearLayoutManager.HORIZONTAL, 2, 0x1FFFFFFF));
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void musicUrlList(List<String> musicUrlList) {
        FlyLog.d("get videos size=%d", musicUrlList == null ? 0 : musicUrlList.size());
        if (musicUrlList != null && getActivity() != null && activity != null) {
            musicList.clear();
            musicList.addAll(musicUrlList);
            adapter.notifyDataSetChanged();
            if (musicList.isEmpty()) {
                if (!musicPlayer.isPlaying()) {
                    musicPlayer.stop();
                }
            } else {
                if (!musicPlayer.isPlaying()) {
                    musicPlayer.play(musicList.get(0));
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onItemClick(View view, int pos) {
        musicPlayer.play(musicList.get(pos));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        scrollToCureentPlayItem();
        super.onResume();
    }

    @Override
    public void statusChange(int statu) {

        switch (statu) {
            case MusicPlayer.STATUS_COMPLETED:
                break;
            case MusicPlayer.STATUS_PLAYING:
                scrollToCureentPlayItem();
                break;
            case MusicPlayer.STATUS_ERROR:
            case MusicPlayer.STATUS_PAUSE:
            case MusicPlayer.STATUS_LOADING:
                break;
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void scrollToCureentPlayItem() {
        if (musicList == null || musicList.isEmpty() || musicPlayer == null) return;
        for (int i = 0; i < musicList.size(); i++) {
            if (musicPlayer.getPlayUrl().equals(musicList.get(i))) {
                recyclerView.getLayoutManager().scrollToPosition(i);
//                recyclerView.smoothScrollToPosition(i);
                break;
            }
        }

    }
}
