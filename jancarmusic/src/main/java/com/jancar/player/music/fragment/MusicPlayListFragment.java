package com.jancar.player.music.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jancar.media.data.Music;
import com.jancar.media.module.RecycleViewDivider;
import com.jancar.media.utils.FlyLog;
import com.jancar.player.music.MusicActivity;
import com.jancar.player.music.R;
import com.jancar.player.music.adpater.MusicPlayListAdapter;
import com.jancar.player.music.model.musicplayer.MusicPlayer;

import java.util.List;

public class MusicPlayListFragment extends MusicFragment implements
        MusicPlayListAdapter.OnItemClickListener {
    private RecyclerView recyclerView;
    private TextView textView;
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
        return inflater.inflate(R.layout.fragment_rv_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        recyclerView = (RecyclerView) view.findViewById(R.id.fm_rv01);
        textView = (TextView) view.findViewById(R.id.fm_tv01);
        adapter = new MusicPlayListAdapter(activity, mMusicList, recyclerView);
        adapter.setOnItemClickListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new RecycleViewDivider(getActivity(),
                LinearLayoutManager.HORIZONTAL, 1, getActivity().getResources().getColor(R.color.divider)));
        recyclerView.setAdapter(adapter);
        musicUrlList(mMusicList);
    }


    @Override
    public void notifyPathChange(String path) {
        FlyLog.d("notifyPathChange path=%s",path);
        mMusicList.clear();
        textView.setText(R.string.music_scan1);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void musicUrlList(List<Music> musicUrlList) {
        FlyLog.d("get player.music size=%d", musicUrlList == null ? 0 : musicUrlList.size());
//        mMusicList.addAll(musicUrlList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void scanFinish(String path) {
        FlyLog.d("finish scan path=%s",path);
        textView.setText(String.format(getString(R.string.music_scan2), mMusicList.size()));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(View view, int pos) {
        musicPlayer.play(mMusicList.get(pos).url);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(MusicActivity.isScan){
            textView.setText(R.string.music_scan1);
        }else{
            textView.setText(String.format(getString(R.string.music_scan2), mMusicList.size()));
        }
        scrollToCureentPlayItem();
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
