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

    /**
     * 时间
     */
    public long time;

    /**
     * 顺序
     */
    public int sort;


    public Music(){

    }

    protected Music(Parcel in) {
        url = in.readString();
        name = in.readString();
        artist = in.readString();
        album = in.readString();
        time = in.readLong();
        sort = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(name);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeLong(time);
        dest.writeInt(sort);
    }

    @Override
    public int describeContents() {
        return 0;
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
    public String toString() {
        return "Music{" +
                "url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", time=" + time +
                ", sort=" + sort +
                '}';
    }
}
