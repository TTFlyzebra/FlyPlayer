package com.jancar.player.photo.entry;

/**
 * @anthor Tzq
 * @time 2018/12/7 14:29
 * @describe TODO
 */
public class PhotoNumber {
    private int type;
    private int num;

    public PhotoNumber(int type, int num) {
        this.type = type;
        this.num = num;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
