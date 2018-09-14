package com.jancar.media.fragment;

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

import com.jancar.media.R;
import com.jancar.media.activity.PhotoActivity;
import com.jancar.media.adpater.PhotoPlayListAdapater;
import com.jancar.media.base.BaseFragment;
import com.jancar.media.utils.FlyLog;

import java.util.List;

public class PhotoPlayListFragment extends BaseFragment implements
        PhotoPlayListAdapater.OnItemClickListener,
        ViewPager.OnPageChangeListener {
    private PhotoActivity activity;
    private PhotoPlayListAdapater adapter;
    private RecyclerView recyclerView;
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
        return inflater.inflate(R.layout.fragment_photo_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        recyclerView = (RecyclerView) view.findViewById(R.id.fm_photo_list_rv01);
        adapter = new PhotoPlayListAdapater(getActivity(), activity.photoList);
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
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        try {
            adapter.notifyDataSetChanged();
            ((LinearLayoutManager)recyclerView.getLayoutManager()).scrollToPosition(position);
        }catch (Exception e){
            FlyLog.e(e.toString());
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void imageUrlList(List<String> imageUrlList) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        },200);

    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
