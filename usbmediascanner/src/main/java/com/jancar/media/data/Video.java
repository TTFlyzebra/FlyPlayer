package com.jancar.media.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Video implements Parcelable {
    /**
     * 地址
     */
    public String url;

    /**
     * 顺序
     */
    public int sort;


    public Video() {

    }

    public Video(String url,int sort) {
        this.url = url;
        this.sort = sort;
    }

    protected Video(Parcel in) {
        url = in.readString();
        sort = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeInt(sort);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    @Override
    public String toString() {
        return "Video{" +
                "url='" + url + '\'' +
                ", sort=" + sort +
                '}';
    }
}
