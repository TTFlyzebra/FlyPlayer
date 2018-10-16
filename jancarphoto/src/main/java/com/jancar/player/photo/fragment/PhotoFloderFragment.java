package com.jancar.player.photo.fragment;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jancar.media.base.BaseFragment;
import com.jancar.media.data.FloderImage;
import com.jancar.media.data.Image;
import com.jancar.media.data.Music;
import com.jancar.media.utils.FlyLog;
import com.jancar.player.photo.PhotoActivity;
import com.jancar.player.photo.R;
import com.jancar.player.photo.adpater.PhotoFloderAdapater;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class PhotoFloderFragment extends BaseFragment implements
        PhotoFloderAdapater.OnItemClickListener,
        ViewPager.OnPageChangeListener {
    private PhotoActivity activity;
    private RecyclerView recyclerView;
    private List<FloderImage> mAllList = new ArrayList<>();
    private List<FloderImage> mAdapterList = new ArrayList<>();
    private Set<String> mHashSet = new HashSet<>();
    private PhotoFloderAdapater adapter;
    private boolean isClick = false;

    public static PhotoFloderFragment newInstance(Bundle args) {
        PhotoFloderFragment musicAlbumFragment = new PhotoFloderFragment();
        musicAlbumFragment.setArguments(args);
        return musicAlbumFragment;
    }

    public PhotoFloderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (PhotoActivity) getActivity();
        activity.viewPager.addOnPageChangeListener(this);
        return inflater.inflate(R.layout.fragment_rv_list2, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        recyclerView = (RecyclerView) view.findViewById(R.id.fm_rv01);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new PhotoFloderAdapater(getActivity(), mAdapterList, 3);
        recyclerView.setAdapter(adapter);
//        recyclerView.addItemDecoration(new RecycleViewDivider(getActivity(),
//                LinearLayoutManager.HORIZONTAL, 1, getActivity().getResources().getColor(R.color.divider)));
        adapter.setOnItemClickListener(this);
        imageUrlList(activity.imageList);
    }


    @Override
    public void onResume() {
        super.onResume();
        scrollCurrentPos();
    }

    private void scrollCurrentPos() {
        int sort = activity.CURRENT_IMAGE == null ? -1 : activity.CURRENT_IMAGE.sort;
        int pos = -1;
        for(int i=0;i<mAdapterList.size();i++){
            if(mAdapterList.get(i).type==2&&mAdapterList.get(i).sort==sort){
                pos = i;
                break;
            }
        }
        adapter.notifyDataSetChanged();
        ((LinearLayoutManager)recyclerView.getLayoutManager()).scrollToPositionWithOffset(pos,60);
    }

    @Override
    public void changePath(String path) {
        mAllList.clear();
        mHashSet.clear();
        adapter.notifyDataSetChanged();
    }

    private FloderImage floderParant;

    @Override
    public void imageUrlList(List<Image> musicUrlList) {
        FlyLog.d("get musics size=%d", musicUrlList == null ? 0 : musicUrlList.size());
        try {
            if (musicUrlList != null && getActivity() != null && activity != null) {
                for (int i = 0; i < musicUrlList.size(); i++) {
                    String url = musicUrlList.get(i).url;
                    int last = url.lastIndexOf(File.separator);
                    String path = url.substring(0, last).intern();
                    if (!mHashSet.contains(path)) {
                        mHashSet.add(path);
                        floderParant = new FloderImage(musicUrlList.get(i));
                        floderParant.group = floderParant.sort;
                        floderParant.url = path;
                        floderParant.sum = 1;
                        floderParant.type = 1;
                        mAllList.add(floderParant);
                    } else {
                        floderParant.sum++;
                    }
                    FloderImage floder = new FloderImage(musicUrlList.get(i));
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
        if (activity.CURRENT_IMAGE != null) {
            sort = activity.CURRENT_IMAGE.sort;
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
    public void onItemClick(View v, FloderImage floderImage) {
        isClick = true;
        if (floderImage.type == 1) {
            mAdapterList.clear();
            for (int i = 0; i < mAllList.size(); i++) {
                if (mAllList.get(i).type == 1) {
                    mAdapterList.add(mAllList.get(i));
                    if (mAllList.get(i).group != floderImage.group) {
                        mAllList.get(i).isSelect = false;
                    }
                } else if ((mAllList.get(i).group == floderImage.group) && (!floderImage.isSelect)) {
                    mAdapterList.add(mAllList.get(i));
                }
            }
        } else {
            activity.setSelectItem(floderImage.sort);
        }
        floderImage.isSelect = !floderImage.isSelect;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (!isClick) {
            updateView();
        }
        isClick = false;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
