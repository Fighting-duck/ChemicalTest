package com.lsy.chemicaltest_new.domain;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class TestValue implements Parcelable {
    private Long time_long;// YYYY HH:mm:ss值
    private String testTime;// HH:mm:ss
    private Float value;// 值
    private String unit;// 单位

    public TestValue(Long time_long,Float value, String unit) {
        this.time_long = time_long;
        this.value = value;
        this.unit = unit;
    }

    public TestValue(Float value, String unit) {
        this.value = value;
        this.unit = unit;
    }
    public String getTestTime() {
        return testTime;
    }

    public Long getTime_long() {
        return time_long;
    }

    public void setTime_long(Long time_long) {
        this.time_long = time_long;
    }

    public void setTestTime(String testTime) {
        this.testTime = testTime;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return value+" "+unit;
    }

    protected TestValue(Parcel in) {
        time_long = in.readLong();
        value = in.readFloat();
        unit = in.readString();
        testTime = in.readString();
    }

    public static final Creator<TestValue> CREATOR = new Creator<TestValue>() {
        @Override
        public TestValue createFromParcel(Parcel in) {
            return new TestValue(in);
        }

        @Override
        public TestValue[] newArray(int size) {
            return new TestValue[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeLong(time_long);
        parcel.writeFloat(value);
        parcel.writeString(unit);
        parcel.writeString(testTime);
    }
}
