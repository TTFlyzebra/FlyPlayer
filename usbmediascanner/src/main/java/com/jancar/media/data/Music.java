package com.jancar.media.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Music implements Parcelable{
    /**
     * 地址
     */
    public String url;
    /**
     * 歌名
     */
    public String name;
    /**
     * 艺术家
     */
    public String artist;

    /**
     * 专辑
     */
    public String album;

    public Music(){

    }

    protected Music(Parcel in) {
        url = in.readString();
        name = in.readString();
        artist = in.readString();
        album = in.readString();
    }

    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel in) {
            return new Music(in);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(name);
        dest.writeString(artist);
        dest.writeString(album);
    }
}
