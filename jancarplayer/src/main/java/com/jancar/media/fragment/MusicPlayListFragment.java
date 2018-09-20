package com.jancar.media.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jancar.media.R;
import com.jancar.media.adpater.MusicPlayListAdapter;
import com.jancar.media.base.MusicFragment;
import com.jancar.media.data.Music;
import com.jancar.media.model.musicplayer.MusicPlayer;
import com.jancar.media.module.RecycleViewDivider;
import com.jancar.media.utils.FlyLog;

import java.util.ArrayList;
import java.util.List;

public class MusicPlayListFragment extends MusicFragment implements
        MusicPlayListAdapter.OnItemClickListener {
    private RecyclerView recyclerView;
    private TextView textView;
    private MusicPlayListAdapter adapter;
    private List<Music> mMusicList = new ArrayList<>();

    public static MusicPlayListFragment newInstance(Bundle args) {
        MusicPlayListFragment musicPlayListFragment = new MusicPlayListFragment();
        musicPlayListFragment.setArguments(args);
        return musicPlayListFragment;
    }

    public MusicPlayListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rv_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        recyclerView = (RecyclerView) view.findViewById(R.id.fm_rv01);
        textView = (TextView) view.findViewById(R.id.fm_tv01);
        textView.setText(R.string.music_scan1);
        adapter = new MusicPlayListAdapter(activity, mMusicList, recyclerView);
        adapter.setOnItemClickListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new RecycleViewDivider(getActivity(),
                LinearLayoutManager.HORIZONTAL, 1, getActivity().getResources().getColor(R.color.divider)));
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void musicUrlList(List<String> musicUrlList) {
        FlyLog.d("get music size=%d", musicUrlList == null ? 0 : musicUrlList.size());
        textView.setText(R.string.music_scan1);
        mMusicList.clear();
        for (int i = 0; i < musicUrlList.size(); i++) {
            Music music = new Music();
            music.url = musicUrlList.get(i);
            music.artist = "-";
            mMusicList.add(music);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void musicID3UrlList(List<Music> musicUrlList) {
        FlyLog.d("get musicid3 size=%d", musicUrlList == null ? 0 : musicUrlList.size());
        textView.setText(String.format(getString(R.string.music_scan2), mMusicList.size()));
        if (musicUrlList == null || musicUrlList.isEmpty()) {
            return;
        }
        try {
            for (int i = 0; i < musicUrlList.size(); i++) {
                mMusicList.get(i).artist = musicUrlList.get(i).artist;
            }
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
        adapter.notifyDataSetChanged();
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
    public void playStatusChange(int statu) {

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
        try {
            recyclerView.getLayoutManager().scrollToPosition(musicPlayer.getPlayPos());
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }

    }


}
