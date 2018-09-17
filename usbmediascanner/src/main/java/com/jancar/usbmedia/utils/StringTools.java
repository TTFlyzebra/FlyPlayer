package com.jancar.usbmedia.utils;

import java.io.File;

public class StringTools {
    public static String getNameByPath(String url){
        try {
            int start = url.lastIndexOf(File.separator) + 1;
            int end = url.lastIndexOf('.');
            start = Math.max(0, start);
            end = Math.max(0, end);
            start = Math.min(start, url.length() - 1);
            end = Math.min(end, url.length() - 1);
            return url.substring(start, end);
        }catch (Exception e){
            FlyLog.e(e.toString());
            return "";
        }
    }
}
