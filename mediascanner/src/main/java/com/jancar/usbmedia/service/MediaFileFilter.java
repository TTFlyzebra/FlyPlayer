package com.jancar.usbmedia.service;

import java.io.File;
import java.io.FilenameFilter;

public class MediaFileFilter implements FilenameFilter {
    private String filter = ".mp4";

    public MediaFileFilter(String filter) {
        this.filter = filter;
    }


    @Override
    public boolean accept(File dir, String filename) {
        if (dir.isDirectory()) return true;
        int ret = filename.lastIndexOf('.');
        if(ret<0) return false;
        String strSuffix = filename.substring(ret,filename.length()).toLowerCase();
        return filter.indexOf(strSuffix)>=0;
//        return filename.endsWith(filter);
    }
}
