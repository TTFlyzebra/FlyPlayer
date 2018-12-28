package com.jancar.player.music.fragment;

import android.os.Bundle;
import android.view.View;

/**
 * 不显示底部扫描信息
 */
public class MusicPlayListFragment_AA1 extends MusicPlayListFragment_AP1 {

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scanMsgTv.setVisibility(View.GONE);
    }
}
