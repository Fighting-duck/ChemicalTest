package com.lsy.chemicaltest_new.domain;

import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "BleDeviceInfo")
public class BleDeviceInfo {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String gear;//当前挡位
    private String mileage;//里程
    private String unit;//单位

    public BleDeviceInfo() {}
    @Ignore
    public BleDeviceInfo(String gear, String mileage, String unit) {
        this.gear = gear;
        this.mileage = mileage;
        this.unit = unit;
    }

    public BleDeviceInfo(BleDeviceInfo other){
        this.id = other.id;
        this.gear = other.gear;
        this.mileage = other.mileage;
        this.unit = other.unit;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGear() {
        return gear;
    }

    public void setGear(String gear) {
        this.gear = gear;
    }

    public String getMileage() {
        return mileage;
    }

    public void setMileage(String mileage) {
        this.mileage = mileage;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "BleDeviceInfo{" +
                "gear='" + gear + '\'' +
                ", mileage='" + mileage + '\'' +
                ", unit='" + unit + '\'' +
                '}';
    }
}
