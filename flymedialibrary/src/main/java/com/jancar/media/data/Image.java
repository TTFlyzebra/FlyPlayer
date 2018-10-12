package com.jancar.media.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Image implements Parcelable{
    /**
     * 地址
     */
    public String url;

    /**
     * 顺序
     */
    public int sort;


    public Image(){

    }

    protected Image(Parcel in) {
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

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    @Override
    public String toString() {
        return "Music{" +
                "url='" + url + '\'' +
                ", sort=" + sort +
                '}';
    }
}
