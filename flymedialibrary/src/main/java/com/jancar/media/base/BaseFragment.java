package com.jancar.media.base;

import android.app.Fragment;

import com.jancar.media.data.Image;
import com.jancar.media.data.Music;
import com.jancar.media.data.Video;
import com.jancar.media.model.listener.IUsbMediaListener;

import java.util.List;

public class BaseFragment extends Fragment implements IUsbMediaListener {

    @Override
    public void onStart() {
        super.onStart();
        ((BaseActivity)getActivity()).addListener(this);
    }

    @Override
    public void onStop() {
        ((BaseActivity)getActivity()).removeListener(this);
        super.onStop();
    }

    @Override
    public void musicUrlList(List<Music> musicUrlList) {

    }

    @Override
    public void musicID3UrlList(List<Music> musicUrlList) {

    }

    @Override
    public void videoUrlList(List<Video> videoUrlList) {

    }

    @Override
    public void imageUrlList(List<Image> imageUrlList) {

    }

    @Override
    public void notifyPathChange(String path) {

    }

    @Override
    public void scanFinish(String path) {

    }

    @Override
    public void scanServiceConneted() {

    }
}
