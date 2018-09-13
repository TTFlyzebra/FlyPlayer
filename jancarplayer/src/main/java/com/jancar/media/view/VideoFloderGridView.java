package com.jancar.media.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class VideoFloderGridView extends GridView {

    public VideoFloderGridView(Context context) {
        super(context);
    }

    public VideoFloderGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoFloderGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 重写该方法，达到使 GridView 适应 ExpandableListView 的效果
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}