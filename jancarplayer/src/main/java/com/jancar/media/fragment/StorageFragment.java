package com.jancar.media.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jancar.media.R;
import com.jancar.media.adpater.StorageAdapater;
import com.jancar.media.data.StorageInfo;
import com.jancar.media.listener.IStorageListener;
import com.jancar.media.model.StorageHandler;

import java.util.ArrayList;
import java.util.List;

public class StorageFragment extends Fragment implements IStorageListener {
    private RecyclerView recyclerView;
    private StorageAdapater adapater;
    private List<StorageInfo> mList = new ArrayList<>();
    private StorageHandler storageHandler = StorageHandler.getInstance();

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
        storageHandler.init(getActivity());
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
        storageHandler.addListener(this);
    }


    @Override
    public void onStop() {
        storageHandler.removeListener(this);
        super.onStop();
    }
}
