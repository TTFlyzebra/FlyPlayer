package com.jancar.player.video.fragment;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jancar.media.base.BaseFragment;
import com.jancar.media.data.FloderVideo;
import com.jancar.media.data.Music;
import com.jancar.media.data.Video;
import com.jancar.media.utils.FlyLog;
import com.jancar.player.video.R;
import com.jancar.player.video.VideoActivity;
import com.jancar.player.video.adpater.VideoFloderAdapater;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tcking.github.com.giraffeplayer.GiraffePlayer;


public class VideoFloderFragment extends BaseFragment implements
        VideoFloderAdapater.OnItemClickListener,
        GiraffePlayer.OnPlayStatusChangeLiseter{
    private VideoActivity activity;
    private RecyclerView recyclerView;
    private List<FloderVideo> mAllList = new ArrayList<>();
    private List<FloderVideo> mAdapterList = new ArrayList<>();
    private Set<String> mHashSet = new HashSet<>();
    private VideoFloderAdapater adapter;
    private boolean isClick = false;

    public static VideoFloderFragment newInstance(Bundle args) {
        VideoFloderFragment musicAlbumFragment = new VideoFloderFragment();
        musicAlbumFragment.setArguments(args);
        return musicAlbumFragment;
    }

    public VideoFloderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (VideoActivity) getActivity();
        return inflater.inflate(R.layout.fragment_rv_list2, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        recyclerView = (RecyclerView) view.findViewById(R.id.fm_rv01);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new VideoFloderAdapater(getActivity(), mAdapterList, 3,recyclerView);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
        videoUrlList(activity.videoList);
    }


    @Override
    public void onResume() {
        super.onResume();
        scrollCurrentPos();
    }

    @Override
    public void onStart() {
        super.onStart();
        activity.player.addStatusChangeLiseter(this);
    }

    @Override
    public void onStop() {
        activity.player.removeStatusChangeLiseter(this);
        super.onStop();
    }

    private void scrollCurrentPos() {
        int sort = -1;
        for(Video video:mAllList){
            if(activity.player.getPlayUrl().endsWith(video.url)){
                sort = video.sort;
                break;
            }
        }
        int pos = -1;
        for(int i=0;i<mAdapterList.size();i++){
            if(mAdapterList.get(i).type==2&&mAdapterList.get(i).sort==sort){
                pos = i;
                break;
            }
        }
        adapter.notifyDataSetChanged();
        ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(pos, 60);
    }

    @Override
    public void notifyPathChange(String path) {
        mAllList.clear();
        mHashSet.clear();
        adapter.notifyDataSetChanged();
    }

    private FloderVideo floderParant;

    @Override
    public void videoUrlList(List<Video> musicUrlList) {
        FlyLog.d("get musics size=%d", musicUrlList == null ? 0 : musicUrlList.size());
        try {
            if (musicUrlList != null && getActivity() != null && activity != null) {
                for (int i = 0; i < musicUrlList.size(); i++) {
                    String url = musicUrlList.get(i).url;
                    int last = url.lastIndexOf(File.separator);
                    String path = url.substring(0, last).intern();
                    if (!mHashSet.contains(path)) {
                        mHashSet.add(path);
                        floderParant = new FloderVideo(musicUrlList.get(i));
                        floderParant.group = floderParant.sort;
                        floderParant.url = path;
                        floderParant.sum = 1;
                        floderParant.type = 1;
                        mAllList.add(floderParant);
                    } else {
                        floderParant.sum++;
                    }
                    FloderVideo floder = new FloderVideo(musicUrlList.get(i));
                    floder.group = floderParant.sort;
                    floder.type = 2;
                    mAllList.add(floder);
                }
                updateView();
            }
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }

    private void updateView() {
        mAdapterList.clear();
        int group = -1;
        int sort = -1;
        for(Video video:mAllList){
            if(activity.player.getPlayUrl().endsWith(video.url)){
                sort = video.sort;
                break;
            }
        }
        for (int i = 0; i < mAllList.size(); i++) {
            if (mAllList.get(i).type == 1) {
                mAdapterList.add(mAllList.get(i));
                int start = mAllList.get(i).group;
                int end = mAllList.get(i).sum + start;
                if ((sort >= start && sort < end)) {
                    group = mAllList.get(i).group;
                    mAllList.get(i).isSelect = true;
                    FlyLog.d("sort = %d,group = %d", sort, group);
                }else {
                    mAllList.get(i).isSelect = false;
                }
                continue;
            }
            if (mAllList.get(i).group == group) {
                mAdapterList.add(mAllList.get(i));
            }
        }

        /**
         * 首次定位到当前播放位置
         */
        scrollCurrentPos();
    }

    @Override
    public void musicID3UrlList(List<Music> musicUrlList) {
        FlyLog.d("get id3musics size=%d", musicUrlList == null ? 0 : musicUrlList.size());
    }


    @Override
    public void onItemClick(View v, FloderVideo floderVideo) {
        isClick = true;
        if (floderVideo.type == 1) {
            mAdapterList.clear();
            for (int i = 0; i < mAllList.size(); i++) {
                if (mAllList.get(i).type == 1) {
                    mAdapterList.add(mAllList.get(i));
                    if (mAllList.get(i).group != floderVideo.group) {
                        mAllList.get(i).isSelect = false;
                    }
                } else if ((mAllList.get(i).group == floderVideo.group) && (!floderVideo.isSelect)) {
                    mAdapterList.add(mAllList.get(i));
                }
            }
        } else {
            activity.player.play(floderVideo.url);
        }
        floderVideo.isSelect = !floderVideo.isSelect;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void statusChange(int statu) {
        if (!isClick) {
            updateView();
        }
        isClick = false;
    }

    @Override
    public void onDestroy() {
        adapter.cancleAllTask();
        adapter.setOnItemClickListener(null);
        super.onDestroy();
    }
}
