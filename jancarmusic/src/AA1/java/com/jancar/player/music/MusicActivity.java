package com.jancar.player.music;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.jancar.media.data.Music;
import com.jancar.media.utils.FlyLog;
import com.jancar.player.music.adpater.ShopAdapter;
import com.jancar.player.music.model.musicplayer.MusicPlayer;
import com.yarolegovich.discretescrollview.DSVOrientation;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.List;

public class MusicActivity extends MusicActivity_AA1 {
    private DiscreteScrollView discreteScrollView;
    private ShopAdapter shopAdapter;

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
        shopAdapter = new ShopAdapter(this,musicList,discreteScrollView);
        discreteScrollView.setAdapter(shopAdapter);

        discreteScrollView.addOnItemChangedListener(new DiscreteScrollView.OnItemChangedListener<RecyclerView.ViewHolder>() {
            @Override
            public void onCurrentItemChanged(@Nullable RecyclerView.ViewHolder viewHolder, int adapterPosition) {
                FlyLog.d("onCurrentItemChanged adapterPosition=%d",adapterPosition);
                if(!musicList.get(adapterPosition).equals(musicPlayer.getPlayUrl())){
                   musicPlayer.play(musicList.get(adapterPosition).url);
                }
            }
        });
    }


    @Override
    public void musicUrlList(List<Music> musicUrlList) {
        FlyLog.d("MusicActivity musicUrlList");
        super.musicUrlList(musicUrlList);
        shopAdapter.notifyDataSetChanged();

    }

    @Override
    public void playStatusChange(int statu) {
        super.playStatusChange(statu);
        switch (statu) {
            case MusicPlayer.STATUS_PLAYING:
                if(discreteScrollView.getCurrentItem()!=musicPlayer.getPlayPos()) {
                    discreteScrollView.smoothScrollToPosition(musicPlayer.getPlayPos());
                }
                break;
            case MusicPlayer.STATUS_IDLE:
                break;
        }
    }

}
