package com.jancar.player.music;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.jancar.media.data.Music;
import com.jancar.media.model.musicplayer.MusicPlayer;
import com.jancar.media.utils.FlyLog;
import com.jancar.player.music.adpater.GalleryAdapter;
import com.yarolegovich.discretescrollview.DSVOrientation;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.List;

public class MusicActivity extends MusicActivity_AP2 {
    private DiscreteScrollView discreteScrollView;
    private GalleryAdapter galleryAdapter;

    @Override
    protected void initView() {
        super.initView();
        discreteScrollView = findViewById(R.id.ac_music_viewpager);
        FlyLog.d("MusicActivity initView");
        discreteScrollView.setOrientation(DSVOrientation.HORIZONTAL);
        discreteScrollView.setItemTransformer(new ScaleTransformer.Builder()
                .setMaxScale(1.2f)
                .setMinScale(0.8f)
                .build());
        galleryAdapter = new GalleryAdapter(this, musicList, discreteScrollView);
        discreteScrollView.setAdapter(galleryAdapter);

        discreteScrollView.addOnItemChangedListener(new DiscreteScrollView.OnItemChangedListener<RecyclerView.ViewHolder>() {
            @Override
            public void onCurrentItemChanged(@Nullable RecyclerView.ViewHolder viewHolder, int currentPos) {
                if (isStop) return;
                int playPos = musicPlayer.getPlayPos();
                FlyLog.d("ScrollView onCurrentItemChanged Position=%d,playPos=%d,setPos=%d", currentPos, playPos,setPos);
                if (setPos == playPos) return;
                if (currentPos != playPos) {
                    if (playPos >= 0) {
                        musicPlayer.play(musicList.get(currentPos).url);
                    }
                }
            }
        });

    }


    @Override
    public void musicUrlList(List<Music> musicUrlList) {
        FlyLog.d("MusicActivity musicUrlList");
        super.musicUrlList(musicUrlList);
        galleryAdapter.notifyDataSetChanged();

    }


    int setPos = 0;

    @Override
    public void playStatusChange(int statu) {
        switch (statu) {
            case MusicPlayer.STATUS_PLAYING:
                setPos = musicPlayer.getPlayPos();
                if (setPos >= 0) {
                    FlyLog.d("ScrollView Scroll To Position=%d", setPos);
                    discreteScrollView.smoothScrollToPosition(setPos);
                }
                break;
            case MusicPlayer.STATUS_IDLE:
                break;
        }
        super.playStatusChange(statu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        galleryAdapter.cancleAllTask();
    }
}
