package com.jancar.media.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.jancar.media.utils.FlyLog;

public class AnimationImageView extends ImageView {
    private AnimationDrawable animationDrawable;

    public AnimationImageView(Context context) {
        this(context, null);
    }

    public AnimationImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimationImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        try {
            animationDrawable = (AnimationDrawable) getDrawable();
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        FlyLog.d("onVisibilityChanged visibility=%d",visibility);
        try {
            if (animationDrawable == null) return;
            if (visibility == 0) {
                animationDrawable.start();
            } else {
                animationDrawable.stop();
            }
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }

}
