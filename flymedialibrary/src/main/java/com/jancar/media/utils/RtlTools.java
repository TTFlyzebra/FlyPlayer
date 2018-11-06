package com.jancar.media.utils;

import android.text.TextUtils;
import android.view.View;

import java.util.Locale;

public class RtlTools {

    public static boolean isRtl() {
        return TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL;
    }

    public static boolean isLayoutRtl(View view) {
        return View.LAYOUT_DIRECTION_RTL == view.getLayoutDirection();
    }
}
