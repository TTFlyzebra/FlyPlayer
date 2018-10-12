package com.jancar.media.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class MarqueeTextView2 extends TextView {
    public final static String TAG = MarqueeTextView2.class.getSimpleName();

    public MarqueeTextView2(Context context) {
        this(context, null);
    }

    public MarqueeTextView2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MarqueeTextView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean isFocused() {
        return true;
    }
}
