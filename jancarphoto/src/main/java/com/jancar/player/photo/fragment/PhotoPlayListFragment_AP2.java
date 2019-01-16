package com.jancar.player.photo.fragment;

import android.os.Bundle;
import android.view.View;

/**
 * 不显示底部扫描信息
 */
public class PhotoPlayListFragment_AP2 extends PhotoPlayListFragment_AP1 {

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scanMsgTv.setVisibility(View.GONE);
    }
}
