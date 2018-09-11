package com.jancar.media.activity;

import android.os.Bundle;
import android.view.View;

import com.jancar.media.R;
import com.jancar.media.base.BaseActivity;
import com.jancar.media.view.FlyTabTextView;
import com.jancar.media.view.FlyTabView;

import java.util.List;

public class MusicActivity extends BaseActivity implements FlyTabView.OnItemClickListener {
    private FlyTabView tabView;
    private String titles[] = new String[]{"存储器", "单曲", "歌手", "专辑", "文件夹"};
    private String fmName[] = new String[]{"StorageFragment", "StorageFragment", "StorageFragment", "StorageFragment", "StorageFragment"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        replaceFragment(fmName[0]);

        titles = new String[]{getString(R.string.storage), getString(R.string.single), getString(R.string.singer), getString(R.string.album), getString(R.string.folder)};

        tabView = (FlyTabView) findViewById(R.id.ac_music_tabview);
        tabView.setTitles(titles);
        tabView.setOnItemClickListener(this);
    }

    @Override
    public void musicUrlList(List<String> musicUrlList) {

    }


    @Override
    public void onItemClick(View v, int pos) {
        if (v instanceof FlyTabTextView) {
            replaceFragment(fmName[pos]);
        }
    }


}
