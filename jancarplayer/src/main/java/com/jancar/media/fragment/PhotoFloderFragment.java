package com.jancar.media.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.jancar.media.R;
import com.jancar.media.activity.PhotoActivity;
import com.jancar.media.adpater.PhotoFloderAdapter;
import com.jancar.media.base.BaseFragment;
import com.jancar.media.utils.FlyLog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhotoFloderFragment extends BaseFragment implements
        PhotoFloderAdapter.OnItemClickListener,
        ViewPager.OnPageChangeListener{
    private PhotoActivity activity;
    private ExpandableListView expandableListView;
    private List<String> groupList = new ArrayList<>();
    private List<List<String>> itemList = new ArrayList<>();
    private PhotoFloderAdapter adapter;
    private Map<String, List<String>> mHashMap = new HashMap<>();
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private List<String> photoList;

    public static PhotoFloderFragment newInstance(Bundle args) {
        PhotoFloderFragment listPlayFileFragment = new PhotoFloderFragment();
        listPlayFileFragment.setArguments(args);
        return listPlayFileFragment;
    }

    public PhotoFloderFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (PhotoActivity) getActivity();
        activity.viewPager.addOnPageChangeListener(this);
        photoList = activity.photoList;
        return inflater.inflate(R.layout.fragment_ex_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        expandableListView = (ExpandableListView) view.findViewById(R.id.fm_file_list_el01);
        adapter = new PhotoFloderAdapter(getActivity(), groupList, itemList);
        expandableListView.setAdapter(adapter);
        expandableListView.setGroupIndicator(null);
        adapter.setOnItemClickListener(this);
    }

    @Override
    public void imageUrlList(List<String> imageUrlList) {
        FlyLog.d("get videos size=%d", imageUrlList == null ? 0 : imageUrlList.size());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (photoList != null && getActivity() != null && activity != null) {
                        mHashMap.clear();
                        groupList.clear();
                        itemList.clear();
                        for (int i = 0; i < photoList.size(); i++) {
                            String url = photoList.get(i);
                            int last = url.lastIndexOf(File.separator);
                            String path = url.substring(0, last);
                            if (mHashMap.get(path) == null) {
                                mHashMap.put(path, new ArrayList<String>());
                            }
                            mHashMap.get(path).add(url);
                        }
                        groupList.addAll(mHashMap.keySet());

                        Collections.sort(groupList, new Comparator<String>() {
                            public int compare(String p1, String p2) {
                                return p1.compareToIgnoreCase(p2);
                            }
                        });

                        for (String key : groupList) {
                            itemList.add(mHashMap.get(key));
                        }
                        scrollCurrentPos();
                        adapter.notifyDataSetChanged();
                    }
                }catch (Exception e){
                    FlyLog.e(e.toString());
                }
            }
        },200);
    }

    @Override
    public void onItemClick(View view, String url) {
        isClick = true;
        activity.setSelectItem(url);
        adapter.notifyDataSetChanged();
    }

    private boolean isClick = false;
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
                    if (itemList.get(i).get(j).equals(activity.CRET_URL)) {
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
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (!isClick) {
            scrollCurrentPos();
        }
        isClick = false;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
