package com.jancar.media.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

import com.jancar.JancarManager;
import com.jancar.media.utils.SystemPropertiesProxy;
import com.jancar.state.JacState;

import java.lang.ref.WeakReference;


public class ParkWarningView extends LinearLayout {

	private static final String TAG = "ParkWarningView";
	private static boolean mbParkingEnable; 	// 行车禁止视频
	private static boolean mbUnderParking;		// 驻车状态
    private JancarManager jancarManager;
    private static JacState jacState = new JacState() {
        @Override
        public void OnBrake(boolean bState) {
            super.OnBrake(bState);
            Log.e(TAG, "OnBrake: " + bState );
            mbUnderParking = bState;
            updateParkProperty();
        }
    };

	public ParkWarningView(Context context) {
		this(context, null);
	}

	public ParkWarningView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@SuppressLint("WrongConstant")
    public ParkWarningView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        jancarManager = (JancarManager)context.getSystemService(JancarManager.JAC_SERVICE);
		mJancarHandler = new MyHandler(new WeakReference<ParkWarningView>(this));
	}

	public static void updateParkProperty() {
        mbParkingEnable = SystemProperties.getBoolean(SystemPropertiesProxy.Property.PERSIST_KEY_BRAKEWARN, false);
        Log.e(TAG, "parking_enable:" + mbParkingEnable + " mbUnderParking:" + mbUnderParking);
        boolean flag = mbParkingEnable && !mbUnderParking;
        mJancarHandler.obtainMessage(1,flag).sendToTarget();
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasWindowFocus);
		if (hasWindowFocus) {
			updateParkProperty();
		}
	}

	@Override
	protected void onAttachedToWindow() {
		// TODO Auto-generated method stub
		super.onAttachedToWindow();
        Log.e(TAG, "registerReceiver:mParkingListener");
		jancarManager.registerJacStateListener(jacState.asBinder());
	}

	@Override
	protected void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
		jancarManager.unregisterJacStateListener(jacState.asBinder());
	}

	public void onDestory(){
		mJancarHandler.stop();
	}

	@SuppressLint("HandlerLeak")
	public class MyHandler extends Handler {
		WeakReference<ParkWarningView> softReference;

		public MyHandler(WeakReference<ParkWarningView> softReference) {
			this.softReference = softReference;
		}

		public void stop(){
			removeCallbacksAndMessages(null);
			softReference.clear();
			softReference = null;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 1:
					boolean flag = (boolean) msg.obj;
					if(softReference!=null) {
						ParkWarningView view = softReference.get();
						view.setVisibility(flag?VISIBLE:GONE);
					}
					break;
			}
		}
	}

	protected static MyHandler mJancarHandler;
}
