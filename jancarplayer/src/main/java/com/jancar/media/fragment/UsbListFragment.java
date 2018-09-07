package com.jancar.media.fragment;

import android.app.Fragment;
import android.os.Bundle;

public class UsbListFragment extends Fragment{
    public static UsbListFragment newInstance(Bundle args){
        UsbListFragment usbListFragment = new UsbListFragment();
        usbListFragment.setArguments(args);
        return usbListFragment;
    }

    public UsbListFragment(){

    }
}
