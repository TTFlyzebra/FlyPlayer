package com.jancar.media.data;

public class FloderVideo extends Video{
    public int type;
    public int sum;
    public int group;
    public boolean isSelect = false;

    public FloderVideo(Video video) {
        url = video.url;
        sort = video.sort;
    }
}
