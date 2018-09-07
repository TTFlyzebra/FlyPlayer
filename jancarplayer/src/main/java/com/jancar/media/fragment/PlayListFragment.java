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
import com.jancar.media.adpater.FileAdapater;
import com.jancar.media.utils.FlyLog;

import java.util.List;

public class PlayListFragment extends Fragment{
    private VideoActivity activity;
    private FileAdapater fileAdapater;
    private RecyclerView recyclerView;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private FlyMedia mFlyMedia;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            FlyLog.d("usbscan service connected");
            mFlyMedia = FlyMedia.Stub.asInterface(service);
            try {
                mFlyMedia.registerNotify(notify);
                List<String> list = mFlyMedia.getVideos();
                FlyLog.d("get videos size=%d",list==null?0:list.size());
                if (list != null && !list.isEmpty()&&getActivity()!=null&&activity!=null) {
                    activity.videoList.clear();
                    activity.videoList.addAll(list);
                    fileAdapater.update();
                    if(!activity.player.isPlaying()){
                        activity.player.play(activity.videoList.get(0));
                    }
                }
            } catch (RemoteException e) {
                FlyLog.e(e.toString());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            FlyLog.d("usbscan service disconnected");
        }
    };

    private Notify notify = new Notify.Stub() {
        @Override
        public void notifyMusic(List<String> list) throws RemoteException {
            FlyLog.d("get music list size=%d",list==null?0:list.size());
        }

        @Override
        public void notifyVideo(final List<String> list) throws RemoteException {
            FlyLog.d("get video list size=%d",list==null?0:list.size());
            if(list!=null&&!list.isEmpty()){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(getActivity()!=null&&activity!=null) {
                            activity.videoList.clear();
                            activity.videoList.addAll(list);
                            fileAdapater.update();
                        }
                    }
                });
            }
        }

        @Override
        public void notifyImage(List<String> list) throws RemoteException {
            FlyLog.d("get image list size=%d",list==null?0:list.size());
        }
    };


    public static PlayListFragment newInstance(Bundle args){
        PlayListFragment listPlayFileFragment = new PlayListFragment();
        listPlayFileFragment.setArguments(args);
        return listPlayFileFragment;
    }

    public PlayListFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (VideoActivity) getActivity();
        return inflater.inflate(R.layout.fragment_photo_list,null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        recyclerView = (RecyclerView) view.findViewById(R.id.fm_photo_list_rv01);
        fileAdapater = new FileAdapater(getActivity(), activity.videoList,recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),3);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(fileAdapater);

        fileAdapater.setOnItemClickListener(new FileAdapater.OnItemClickListener() {
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
        bindService();
    }

    @Override
    public void onStop() {
        unBindService();
        super.onStop();
    }

    private void bindService(){
        FlyLog.d("bindService");
        try {
            Intent intent = new Intent();
            intent.setPackage("com.jancar.usbmedia");
            intent.setAction("com.jancar.usbmedia.FlyMediaService");
            getActivity().bindService(intent, conn, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }

    private void unBindService() {
        FlyLog.d("unBindService");
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
