package com.jancar.media.fragment;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jancar.media.FlyMedia;
import com.jancar.media.Notify;
import com.jancar.media.R;
import com.jancar.media.activity.VideoActivity;
import com.jancar.media.adpater.FileAdapater2;
import com.jancar.media.utils.FlyLog;

import java.util.List;

public class PhotoListFragment extends Fragment{
    private FileAdapater2 fileAdapater;
    private RecyclerView recyclerView;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private FlyMedia mFlyMedia;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mFlyMedia = FlyMedia.Stub.asInterface(service);
            try {
                mFlyMedia.registerNotify(notify);
                FlyLog.d("getVideos");
                List<String> list = mFlyMedia.getVideos();
                if (list != null && !list.isEmpty()) {
                    VideoActivity.videoList.clear();
                    VideoActivity.videoList.addAll(list);
                    fileAdapater.update();
                }
            } catch (RemoteException e) {
                FlyLog.e(e.toString());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private Notify notify = new Notify.Stub() {
        @Override
        public void notifyMusic(List<String> list) throws RemoteException {

        }

        @Override
        public void notifyVideo(final List<String> list) throws RemoteException {
            if(list!=null&&!list.isEmpty()){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        VideoActivity.videoList.clear();
                        VideoActivity.videoList.addAll(list);
                        fileAdapater.update();
                    }
                });
            }
        }

        @Override
        public void notifyImage(List<String> list) throws RemoteException {

        }
    };


    public static PhotoListFragment newInstance(Bundle args){
        PhotoListFragment listPlayFileFragment = new PhotoListFragment();
        listPlayFileFragment.setArguments(args);
        return listPlayFileFragment;
    }

    public PhotoListFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo_list,null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        recyclerView = (RecyclerView) view.findViewById(R.id.fm_photo_list_rv01);
        fileAdapater = new FileAdapater2(getActivity(), VideoActivity.videoList,recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),3);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(fileAdapater);
    }

    @Override
    public void onStart() {
        super.onStart();
        bindService();
    }

    @Override
    public void onStop() {
        unBindService();
        super.onStop();
    }

    private void bindService(){
        try {
            Intent intent = new Intent();
            intent.setPackage("com.jancar.media");
            intent.setAction("com.jancar.media.FlyMediaService");
            getActivity().bindService(intent, conn, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }

    private void unBindService() {
        try {
            if (mFlyMedia != null) {
                mFlyMedia.unregisterNotify(notify);
            }
            getActivity().unbindService(conn);
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }
}
