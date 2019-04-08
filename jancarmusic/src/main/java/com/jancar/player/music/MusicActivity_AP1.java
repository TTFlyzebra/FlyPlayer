package com.jancar.player.music;

import android.graphics.Bitmap;

import com.jancar.media.model.musicplayer.MusicPlayer;
import com.jancar.media.view.CircleImageView;

public class MusicActivity_AP1 extends BaseMusicActivity1{
    private CircleImageView ivImage;

    @Override
    protected void initView() {
        super.initView();
        ivImage = (CircleImageView) findViewById(R.id.ac_music_iv_image);
    }

    @Override
    public void playStatusChange(int statu) {
        super.playStatusChange(statu);
        switch (statu) {
            case MusicPlayer.STATUS_IDLE:
                ivImage.setImageResource(R.drawable.media_music_iv02);
                break;
        }
        ivImage.setAnimatePlaying(musicPlayer.isPlaying());
    }

    @Override
    protected void upCurrenPlayImage(Bitmap bitmap) {
        ivImage.setImageBitmap(bitmap == null ? defaultBitmap : bitmap);
    }
}
