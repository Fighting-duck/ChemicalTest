package com.lsy.chemicaltest_new.domain;

import androidx.room.Entity;

@Entity(tableName = "setting")
public class Setting {
    private Integer isSaveColoOriginalImage;//是否保存原始图片---比色图像分析

    public Integer getIsSaveColoOriginalImage() {
        return isSaveColoOriginalImage;
    }

    public void setIsSaveColoOriginalImage(Integer isSaveColoOriginalImage) {
        this.isSaveColoOriginalImage = isSaveColoOriginalImage;
    }
}
