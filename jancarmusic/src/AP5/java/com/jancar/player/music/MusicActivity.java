package com.jancar.player.music;


import android.graphics.Bitmap;
import android.widget.ImageView;

import com.jancar.media.utils.BlurUtil;

public class MusicActivity extends MusicActivity_AP3{
    private ImageView ac_music_bkimg;

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
                final Bitmap blurBitmap = bitmap==null? null :BlurUtil.blur(MusicActivity.this, bitmap);
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
