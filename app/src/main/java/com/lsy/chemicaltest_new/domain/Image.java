package com.lsy.chemicaltest_new.domain;

import android.graphics.Bitmap;

/** 图像信息类 */
public class Image {
    private Bitmap image;
    private String location;

    public Image(Bitmap image,String location) {
        this.image = image;
        this.location = location;
    }
    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
