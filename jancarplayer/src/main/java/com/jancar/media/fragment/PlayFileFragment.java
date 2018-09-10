package com.jancar.media.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jancar.media.R;
import com.jancar.media.activity.VideoActivity;
import com.jancar.media.adpater.PlayFileAdapater;
import com.jancar.media.listener.IUsbMediaListener;
import com.jancar.media.model.IUsbMediaScan;
import com.jancar.media.model.UsbMediaScan;
import com.jancar.media.utils.FlyLog;

import java.util.List;

public class PlayFileFragment extends Fragment implements IUsbMediaListener {
    private VideoActivity activity;
    private PlayFileAdapater fileAdapater;
    private RecyclerView recyclerView;
    private IUsbMediaScan usbMediaScan = UsbMediaScan.getInstance();

    public static PlayFileFragment newInstance(Bundle args) {
        PlayFileFragment listPlayFileFragment = new PlayFileFragment();
        listPlayFileFragment.setArguments(args);
        return listPlayFileFragment;
    }

    public PlayFileFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (VideoActivity) getActivity();
        return inflater.inflate(R.layout.fragment_photo_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        recyclerView = (RecyclerView) view.findViewById(R.id.fm_photo_list_rv01);
        fileAdapater = new PlayFileAdapater(getActivity(), activity.videoList, recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(fileAdapater);

        fileAdapater.setOnItemClickListener(new PlayFileAdapater.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int pos) {
                activity.currenPos = pos;
                activity.currentPlayUrl = activity.videoList.get(activity.currenPos);
                activity.player.play(activity.currentPlayUrl);
            }
        });
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
            activity.videoList.clear();
            activity.videoList.addAll(videoUrlList);
            fileAdapater.update();
            if (videoUrlList.isEmpty()) {
                if (!activity.player.isPlaying()) {
                    activity.player.stop();
                }
            } else {
                if (!activity.player.isPlaying()) {
                    activity.player.play(activity.videoList.get(0));
                }
            }
        }
    }

    @Override
    public void imageUrlList(List<String> imageUrlList) {

    }

    @Override
    public void usbRemove(String usbstore) {

    }
}
