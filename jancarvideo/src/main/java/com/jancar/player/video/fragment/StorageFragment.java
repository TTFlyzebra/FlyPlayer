package com.jancar.player.video.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jancar.media.base.BaseActivity;
import com.jancar.media.base.BaseFragment;
import com.jancar.media.data.StorageInfo;
import com.jancar.media.model.listener.IStorageListener;
import com.jancar.media.model.mediascan.MediaScan;
import com.jancar.media.model.storage.IStorage;
import com.jancar.media.model.storage.Storage;
import com.jancar.media.receiver.DiskReceiver;
import com.jancar.media.utils.FlyLog;
import com.jancar.player.video.R;
import com.jancar.player.video.adpater.StorageAdapater;

import java.util.ArrayList;
import java.util.List;

public class StorageFragment extends BaseFragment implements
        IStorageListener,
        StorageAdapater.OnItemClickListener {
    private RecyclerView recyclerView;
    private StorageAdapater adapater;
    private List<StorageInfo> mList = new ArrayList<>();
    private IStorage storage = Storage.getInstance();
    private MyReceiver receiver ;

    public StorageFragment() {
    }

    public static StorageFragment newInstance(Bundle args) {
        StorageFragment usbListFragment = new StorageFragment();
        usbListFragment.setArguments(args);
        return usbListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storage.refresh();
        receiver = new MyReceiver();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        mFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        getActivity().registerReceiver(receiver,mFilter);
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_store_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        recyclerView = (RecyclerView) view.findViewById(R.id.fm_storage_rv01);
        adapater = new StorageAdapater(getActivity(), mList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapater);
        adapater.setOnItemClickListener(this);
        adapater.setCurrentPath(((BaseActivity)getActivity()).currenPath);
    }

    @Override
    public void storageList(List<StorageInfo> storageList) {
        if (storageList == null) return;
        mList.clear();
        mList.addAll(storageList);
        adapater.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        storage.addListener(this);
    }


    @Override
    public void onStop() {
        storage.removeListener(this);
        super.onStop();
    }

    @Override
    public void changePath(String path) {
        storage.refresh();
        adapater.setCurrentPath(path);
        adapater.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(View view, int pos) {
        FlyLog.d("openStorager storage mPath=%s", mList.get(pos));
        MediaScan.getInstance().openStorager(mList.get(pos));
    }

    public class MyReceiver extends DiskReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            FlyLog.d(intent.toUri(0));
            if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)
                    || intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                FlyLog.d("onReceive");
                if(storage!=null){
                    storage.refresh();
                }
            }
        }
    }
}
