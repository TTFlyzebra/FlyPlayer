package com.jancar.player.music;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.jancar.media.utils.BlurUtil;

public class MusicActivity_AP2 extends BaseMusicActivity1 {
    private ImageView ac_music_bkimg;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.activity_music_ap2);
    }


    @Override
    protected void initView() {
        super.initView();
        ac_music_bkimg = (ImageView) findViewById(R.id.ac_music_bkimg);
    }

    @Override
    protected void upCurrenPlayImage(final Bitmap bitmap) {
        super.upCurrenPlayImage(bitmap);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final Bitmap blurBitmap = bitmap==null? null :BlurUtil.blur(MusicActivity_AP2.this, bitmap);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ac_music_bkimg.setImageBitmap(blurBitmap);
                    }
                });
            }
        });
    }
}
