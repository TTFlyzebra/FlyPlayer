package com.jancar.player.video.fragment;

import android.os.Bundle;
import android.view.View;

public class VideoPlayListFragment_AP2 extends VideoPlayListFragment_AP1{
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scanMsgTv.setVisibility(View.GONE);
    }
}
