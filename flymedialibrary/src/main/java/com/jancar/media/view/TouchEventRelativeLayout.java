package com.jancar.media.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by FlyZebra on 2014/8/15.
 */
public class TouchEventRelativeLayout extends RelativeLayout{
    public TouchEventRelativeLayout(Context context) {
        super(context);
    }

    public TouchEventRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchEventRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(onTouchEventListener!=null){
            onTouchEventListener.onFlyTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    public interface OnTouchEventListener{
        void onFlyTouchEvent(MotionEvent ev);
    }

    private OnTouchEventListener onTouchEventListener;

    public void setOnTouchEventListener(OnTouchEventListener onTouchEventListener) {
        this.onTouchEventListener = onTouchEventListener;
    }
}
