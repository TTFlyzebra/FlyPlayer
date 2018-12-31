package com.jancar.media.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

public class BlurUtil {
    private static final float BITMAP_SCALE = 0.5f;
    private static final int BLUR_RADIUS = 5;

    public static Bitmap blur(Context context, Bitmap bitmap) {
        return blur(context, bitmap, BITMAP_SCALE, BLUR_RADIUS);
    }

    public static Bitmap blur(Context context, Bitmap bitmap, float bitmap_scale) {
        return blur(context, bitmap, bitmap_scale, BLUR_RADIUS);
    }

    public static Bitmap blur(Context context, Bitmap bitmap, int blur_radius) {
        return blur(context, bitmap, BITMAP_SCALE, blur_radius);
    }

    public static Bitmap blur(Context context, Bitmap bitmap, float bitmap_scale, int blur_radius) {
        Bitmap inputBitmap = Bitmap.createScaledBitmap(bitmap, Math.round(bitmap.getWidth() * bitmap_scale),
                Math.round(bitmap.getHeight() * bitmap_scale), false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(blur_radius);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);
        rs.destroy();
        return outputBitmap;
    }
}
