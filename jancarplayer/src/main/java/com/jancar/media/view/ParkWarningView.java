package com.jancar.media.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.jancar.JancarManager;
import com.jancar.state.JacState;


public class ParkWarningView extends LinearLayout {

	private static final String TAG = "ParkWarningView";
	private boolean mbParkingEnable; 	// 行车禁止视频
	private boolean mbUnderParking;		// 驻车状态
    private JancarManager jancarManager;
    private JacState jacState = new JacState() {
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
	}

	public void updateParkProperty() {
        mbParkingEnable = SystemProperties.getBoolean("persist.jancar.brakewarn", false);
        Log.e(TAG, "parking_enable:" + mbParkingEnable + " mbUnderParking:" + mbUnderParking);
		if (mbParkingEnable && !mbUnderParking) {
			setVisibility(View.VISIBLE);
		} else {
			setVisibility(View.GONE);
		}
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
}
