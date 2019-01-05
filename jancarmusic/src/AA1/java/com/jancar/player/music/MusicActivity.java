package com.jancar.player.music;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.jancar.media.data.Music;
import com.jancar.media.utils.FlyLog;
import com.jancar.player.music.adpater.GalleryAdapter;
import com.jancar.player.music.model.musicplayer.MusicPlayer;
import com.yarolegovich.discretescrollview.DSVOrientation;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.List;

public class MusicActivity extends MusicActivity_AA1 {
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
            public void onCurrentItemChanged(@Nullable RecyclerView.ViewHolder viewHolder, int adapterPosition) {
                FlyLog.d("onCurrentItemChanged adapterPosition=%d", adapterPosition);
                if(adapterPosition==0){
                    if(musicPlayer.isPlaying()){
                        try{
                            discreteScrollView.smoothScrollToPosition(musicPlayer.getPlayPos());
                        }catch (Exception e){
                            FlyLog.e(e.toString());
                            e.printStackTrace();
                        }
                    }
                } else if (adapterPosition >= 0 && musicPlayer.getPlayPos() > 0) {
                    try {
                        String url1 = musicList.get(adapterPosition).url;
                        String url2 = musicPlayer.getPlayUrl();
                        if (!url1.equals(url2)) {
                            musicPlayer.play(musicList.get(adapterPosition).url);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
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


    @Override
    public void playStatusChange(int statu) {
        super.playStatusChange(statu);
        switch (statu) {
            case MusicPlayer.STATUS_PLAYING:
                if (musicPlayer.getPlayPos() >= 0 && discreteScrollView.getCurrentItem() != musicPlayer.getPlayPos()) {
                    try{
                        discreteScrollView.smoothScrollToPosition(musicPlayer.getPlayPos());
                    }catch (Exception e){
                        FlyLog.e(e.toString());
                        e.printStackTrace();
                    }
                }
                break;
            case MusicPlayer.STATUS_IDLE:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        galleryAdapter.cancleAllTask();
    }
}
