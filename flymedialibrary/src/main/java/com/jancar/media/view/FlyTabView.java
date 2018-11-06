package com.jancar.media.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.jancar.media.R;
import com.jancar.media.utils.FlyLog;

import java.util.List;

public class FlyTabView extends FrameLayout implements View.OnClickListener {
    private String titles[] = null;
    private FlyTabTextView textViews[] = null;
    private View focusView = null;
    private int focusPos = 0;
    private Context context;
    private int animDuration = 300;
    private int width = 0;
    private int height = 0;
    private int childWidth = 0;
    private int[][] states = new int[][]{{android.R.attr.state_enabled}, {}};
    private int[] colors = new int[]{0xFFFFFFFF, 0xFF0370E5};
    private ColorStateList colorStateList = new ColorStateList(states, colors);
    private OnItemClickListener onItemClickListener;

    public FlyTabView(Context context) {
        this(context, null);
    }

    public FlyTabView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlyTabView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
    }

    public void setTitles(String[] strs) {
        this.titles = strs;
        if (titles == null || titles.length == 0) return;
        textViews = new FlyTabTextView[titles.length];
        post(new Runnable() {
            @Override
            public void run() {
                width = getMeasuredWidth();
                height = getMeasuredHeight();
                childWidth = width / textViews.length;

                for (int i = 0; i < textViews.length; i++) {
                    LayoutParams lp = new LayoutParams(childWidth, height);
                    lp.setMarginStart(i * childWidth);
                    textViews[i] = new FlyTabTextView(context);
                    textViews[i].setGravity(Gravity.CENTER);
                    textViews[i].setText(titles[i]);
                    textViews[i].setTextColor(colorStateList);
                    textViews[i].setTag(i);
                    textViews[i].setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            context.getResources().getDimensionPixelSize(R.dimen.lrc_divider_height));
                    textViews[i].setOnClickListener(FlyTabView.this);
                    addView(textViews[i], lp);
                }
                focusView = new View(context);
                LayoutParams lpbak = new LayoutParams(childWidth, height - 5);
                addView(focusView, lpbak);
                focusView.setBackgroundResource(R.drawable.bottom_line_blue);
                setSelectItem(0);
            }
        });
    }

    public void setTitles(List<String> titleList) {
        if (titleList != null && !titleList.isEmpty()) {
            setTitles((String[]) titleList.toArray());
        }
    }

    @Override
    public void onClick(View v) {
        FlyLog.d("onclick %d", v.getTag());
        focusPos = (int) v.getTag();
        setSelectItem(animDuration);
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(v, (Integer) v.getTag());
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        FlyLog.d("Tab width =%d", getMeasuredWidth());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        FlyLog.d("Tab width =%d", width);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void setSelectItem(int duration) {
        try {
            if(focusView!=null&&textViews!=null) {
                focusView.animate().translationX(childWidth * focusPos).setDuration(duration).start();
                for (int i = 0; i < textViews.length; i++) {
                    textViews[i].setEnabled(i != focusPos);
                }
            }
        }catch (Exception e){
            FlyLog.e(e.toString());
        }
    }

    public void setFocusPos(int pos) {
        FlyLog.d("setFocusPos=%d", pos);
        focusPos = pos;
        setSelectItem(0);
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int pos);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
