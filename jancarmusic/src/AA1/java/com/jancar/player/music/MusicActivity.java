package com.jancar.player.music;

import com.jancar.media.data.Music;
import com.jancar.media.utils.FlyLog;
import com.jancar.player.music.adpater.GalleryAdapter;
import com.jancar.player.music.model.musicplayer.MusicPlayer;
import com.yarolegovich.discretescrollview.DSVOrientation;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.List;

public class MusicActivity extends MusicActivity_AA1 {
    private DiscreteScrollView recyclerView;
    private GalleryAdapter galleryAdapter;

    @Override
    protected void initView() {
        super.initView();
        recyclerView = findViewById(R.id.ac_music_viewpager);
        FlyLog.d("MusicActivity initView");
        recyclerView.setOrientation(DSVOrientation.HORIZONTAL);
        recyclerView.setItemTransformer(new ScaleTransformer.Builder()
                .setMinScale(0.8f)
                .build());
        galleryAdapter = new GalleryAdapter(musicList);
        recyclerView.setAdapter(galleryAdapter);
    }


    @Override
    public void musicUrlList(List<Music> musicUrlList) {
        FlyLog.d("MusicActivity musicUrlList");
        super.musicUrlList(musicUrlList);
        galleryAdapter.notifyDataSetChanged();

    }

    @Override
    public void playStatusChange(int statu) {
        super.playStatusChange(statu);
        switch (statu) {
            case MusicPlayer.STATUS_PLAYING:
                recyclerView.smoothScrollToPosition(musicPlayer.getPlayPos());
                break;
            case MusicPlayer.STATUS_IDLE:
                break;
        }
    }

}
