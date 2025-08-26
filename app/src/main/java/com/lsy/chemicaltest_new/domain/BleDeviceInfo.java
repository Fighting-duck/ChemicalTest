package com.lsy.chemicaltest_new.domain;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "BleDeviceInfo")
public class BleDeviceInfo implements Parcelable{
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

    public static final Creator<BleDeviceInfo> CREATOR = new Creator<BleDeviceInfo>() {
        @Override
        public BleDeviceInfo createFromParcel(Parcel in) {
            return new BleDeviceInfo(in);
        }

        @Override
        public BleDeviceInfo[] newArray(int size) {
            return new BleDeviceInfo[size];
        }
    };
    protected BleDeviceInfo(Parcel in) {
        id = in.readInt();
        gear = in.readString();
        mileage = in.readString();
        unit = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(gear);
        parcel.writeString(mileage);
        parcel.writeString(unit);
    }
}
