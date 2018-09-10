package com.jancar.media.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

@SuppressLint("AppCompatCustomView")
public class FlyTabTextView extends TextView{
    public FlyTabTextView(Context context) {
        super(context);
    }

    public FlyTabTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FlyTabTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
