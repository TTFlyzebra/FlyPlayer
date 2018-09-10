package com.jancar.media.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.jancar.media.R;
import com.jancar.media.activity.VideoActivity;
import com.jancar.media.adpater.FileListEListViewAdapter;
import com.jancar.media.listener.IUsbMediaListener;
import com.jancar.media.model.IUsbMediaScan;
import com.jancar.media.model.UsbMediaScan;
import com.jancar.media.utils.FlyLog;

import java.util.ArrayList;
import java.util.List;

public class FileListFragment extends Fragment implements IUsbMediaListener {
    private VideoActivity activity;
    private ExpandableListView expandableListView;
    private IUsbMediaScan usbMediaScan = UsbMediaScan.getInstance();

    /**
     * 每个分组的名字的集合
     */
    private List<String> groupList;

    /**
     * 每个分组下的每个子项的 GridView 数据集合
     */
    private List<String> itemGridList;

    /**
     * 所有分组的所有子项的 GridView 数据集合
     */
    private List<List<String>> itemList;

    public static FileListFragment newInstance(Bundle args) {
        FileListFragment listPlayFileFragment = new FileListFragment();
        listPlayFileFragment.setArguments(args);
        return listPlayFileFragment;
    }

    public FileListFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (VideoActivity) getActivity();
        return inflater.inflate(R.layout.fragment_file_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        expandableListView = (ExpandableListView) view.findViewById(R.id.fm_file_list_el01);
        // 分组
        groupList = new ArrayList<>();

        // 每个分组下的每个子项的 GridView 数据集合
        itemGridList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            itemGridList.add("电脑" + (i + 1));
        }

        // 所有分组的所有子项的 GridView 数据集合
        itemList = new ArrayList<>();
        itemList.add(itemGridList);
        itemList.add(itemGridList);
        // 创建适配器
        FileListEListViewAdapter adapter = new FileListEListViewAdapter(getActivity(), groupList, itemList);
        expandableListView.setAdapter(adapter);
        // 隐藏分组指示器
        expandableListView.setGroupIndicator(null);
        // 默认展开第一组
        expandableListView.expandGroup(0);
    }

    @Override
    public void onStart() {
        super.onStart();
        usbMediaScan.addListener(this);
    }

    @Override
    public void onStop() {
        usbMediaScan.removeListener(this);
        super.onStop();
    }

    @Override
    public void musicUrlList(List<String> musicUrlList) {

    }

    @Override
    public void videoUrlList(List<String> videoUrlList) {
        FlyLog.d("get videos size=%d",videoUrlList==null?0:videoUrlList.size());
        if (videoUrlList != null && getActivity() != null && activity != null) {
        }
    }

    @Override
    public void imageUrlList(List<String> imageUrlList) {

    }

    @Override
    public void usbRemove(String usbstore) {

    }
}
