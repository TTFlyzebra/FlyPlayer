package com.jancar.player.music;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.jancar.player.music.model.musicplayer.MusicPlayer;

public class MusicActivity_AP3 extends BaseMusicActivity1 {
    private ImageView ivImage;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.activity_music_ap3);
    }

    @Override
    protected void initView() {
        super.initView();
        ivImage = (ImageView) findViewById(R.id.ac_music_iv_image);
    }

    @Override
    public void playStatusChange(int statu) {
        super.playStatusChange(statu);
        switch (statu) {
            case MusicPlayer.STATUS_IDLE:
                ivImage.setImageBitmap(null);
                break;
        }
    }

    @Override
    protected void upCurrenPlayImage(Bitmap bitmap) {
        ivImage.setImageBitmap(bitmap);
    }
}
