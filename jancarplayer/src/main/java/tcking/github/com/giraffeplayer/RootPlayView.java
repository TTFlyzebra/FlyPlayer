package tcking.github.com.giraffeplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class RootPlayView extends RelativeLayout{
    public static long TOUCHTIME = System.currentTimeMillis();
    public RootPlayView(Context context) {
        super(context);
    }

    public RootPlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RootPlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                TOUCHTIME = System.currentTimeMillis();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
}
