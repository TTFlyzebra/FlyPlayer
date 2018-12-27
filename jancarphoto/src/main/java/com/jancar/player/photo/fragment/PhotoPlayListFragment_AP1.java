package com.jancar.player.photo.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jancar.media.base.BaseFragment;
import com.jancar.media.data.Image;
import com.jancar.media.utils.DisplayUtils;
import com.jancar.media.utils.FlyLog;
import com.jancar.player.photo.PhotoActivity_AP1;
import com.jancar.player.photo.R;
import com.jancar.player.photo.adpater.PhotoPlayListAdapater;

import java.util.List;

public class PhotoPlayListFragment_AP1 extends BaseFragment implements
        PhotoPlayListAdapater.OnItemClickListener,
        ViewPager.OnPageChangeListener {
    protected PhotoActivity_AP1 activity;
    protected PhotoPlayListAdapater adapter;
    protected RecyclerView recyclerView;
    protected TextView scanMsgTv;
    protected Handler mHandler = new Handler(Looper.getMainLooper());

    protected int spanCount = 3;

    public static PhotoPlayListFragment_AP1 newInstance(Bundle args) {
        PhotoPlayListFragment_AP1 listPlayFileFragment = new PhotoPlayListFragment_AP1();
        listPlayFileFragment.setArguments(args);
        return listPlayFileFragment;
    }

    public PhotoPlayListFragment_AP1() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (PhotoActivity_AP1) getActivity();
        activity.viewPager.addOnPageChangeListener(this);
        return inflater.inflate(R.layout.fragment_rv_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        recyclerView = (RecyclerView) view.findViewById(R.id.fm_rv01);
        scanMsgTv = (TextView) view.findViewById(R.id.fm_tv01);
        scanMsgTv.setText(String.format(getString(R.string.photo_scan2), activity.imageList.size()));
        adapter = new PhotoPlayListAdapater(getActivity(), activity.imageList);
        int width = DisplayUtils.getMetrices(getActivity()).widthPixels;
        if (width >= 1280) {
            spanCount = 4;
        } else {
            spanCount = 3;
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), spanCount);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (PhotoActivity_AP1.isScan) {
            scanMsgTv.setText(R.string.music_scan1);
        } else {
            scanMsgTv.setText(String.format(getString(R.string.photo_scan2), activity.imageList.size()));
        }
        ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(activity.viewPager.getCurrentItem(), 0);
    }

    @Override
    public void onItemClick(View view, int pos) {
        activity.setSelectItem(pos);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        try {
            adapter.notifyDataSetChanged();
            recyclerView.getLayoutManager().scrollToPosition(position);
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void notifyPathChange(String path) {
        scanMsgTv.setText(R.string.music_scan1);
        activity.imageList.clear();
        adapter.notifyDataSetChanged();
    }


    @Override
    public void imageUrlList(List<Image> imageUrlList) {
        try {
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }

    }

    @Override
    public void scanFinish(String path) {
        try {
            scanMsgTv.setText(String.format(getString(R.string.photo_scan2), activity.imageList.size()));
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }

    @Override
    public void onDestroy() {
        adapter.setOnItemClickListener(null);
        activity.viewPager.removeOnPageChangeListener(this);
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
