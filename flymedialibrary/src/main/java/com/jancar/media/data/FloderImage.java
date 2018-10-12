package com.jancar.media.data;

public class FloderImage extends Image{
    public int type;
    public int sum;
    public int group;
    public boolean isSelect = false;

    public FloderImage(Image image) {
        url = image.url;
        sort = image.sort;
    }
}
