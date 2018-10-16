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
import com.jancar.media.data.Music;
import com.jancar.media.utils.FlyLog;
import com.jancar.player.photo.PhotoActivity;
import com.jancar.player.photo.R;
import com.jancar.player.photo.adpater.PhotoPlayListAdapater;

import java.util.List;

public class PhotoPlayListFragment extends BaseFragment implements
        PhotoPlayListAdapater.OnItemClickListener,
        ViewPager.OnPageChangeListener {
    private PhotoActivity activity;
    private PhotoPlayListAdapater adapter;
    private RecyclerView recyclerView;
    private TextView textView;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public static PhotoPlayListFragment newInstance(Bundle args) {
        PhotoPlayListFragment listPlayFileFragment = new PhotoPlayListFragment();
        listPlayFileFragment.setArguments(args);
        return listPlayFileFragment;
    }

    public PhotoPlayListFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (PhotoActivity) getActivity();
        activity.viewPager.addOnPageChangeListener(this);
        return inflater.inflate(R.layout.fragment_rv_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        recyclerView = (RecyclerView) view.findViewById(R.id.fm_rv01);
        textView = (TextView) view.findViewById(R.id.fm_tv01);
        textView.setText(String.format(getString(R.string.photo_scan2), activity.imageList.size()));
        adapter = new PhotoPlayListAdapater(getActivity(), activity.imageList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
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
    public void changePath(String path) {
        textView.setText(R.string.music_scan1);
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
    public void musicID3UrlList(List<Music> musicUrlList) {
        FlyLog.d("========================");
        try {
            textView.setText(String.format(getString(R.string.photo_scan2), activity.imageList.size()));
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
