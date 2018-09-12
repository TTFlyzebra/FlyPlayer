package com.jancar.media.base;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jancar.media.R;
import com.jancar.media.model.IUsbMediaScan;
import com.jancar.media.model.UsbMedia;
import com.jancar.media.utils.FlyLog;

import java.lang.reflect.Constructor;

public class BaseActivity extends AppCompatActivity {
    protected IUsbMediaScan usbMediaScan = UsbMedia.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usbMediaScan.init(this);
    }

    @Override
    protected void onDestroy() {
        usbMediaScan.close();
        super.onDestroy();
    }

    public void replaceFragment(String fName) {
        FlyLog.d("replaceFragment com.jancar.media.fragment.%s", fName);
        try {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Class<?> cls = Class.forName("com.jancar.media.fragment." + fName);
            Constructor<?> cons = cls.getConstructor();
            Fragment fragment = (Fragment) cons.newInstance();
            ft.replace(R.id.ac_replace_fragment, fragment).commit();
        } catch (Exception e) {
            FlyLog.e(e.toString());
        }
    }
}
