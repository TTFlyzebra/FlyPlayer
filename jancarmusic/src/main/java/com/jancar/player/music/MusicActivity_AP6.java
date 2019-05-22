package com.jancar.player.music;

import android.graphics.Bitmap;

import com.jancar.media.model.musicplayer.MusicPlayer;
import com.jancar.media.view.CircleImageView;
import com.jancar.media.view.HexagonImageView;

public class MusicActivity_AP6 extends BaseMusicActivity1{
    private HexagonImageView ivImage;

    @Override
    protected void initView() {
        super.initView();
        ivImage = (HexagonImageView) findViewById(R.id.ac_music_iv_image);
    }

    @Override
    public void playStatusChange(int statu) {
        super.playStatusChange(statu);
        switch (statu) {
            case MusicPlayer.STATUS_IDLE:
                ivImage.setImageResource(R.drawable.media_music_iv02);
                break;
        }
    }

    @Override
    protected void upCurrenPlayImage(Bitmap bitmap) {
        ivImage.setImageBitmap(bitmap == null ? defaultBitmap : bitmap);
    }
}
