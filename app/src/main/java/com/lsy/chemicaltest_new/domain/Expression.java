package com.lsy.chemicaltest_new.domain;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Expression implements Parcelable {
    private Float k;
    private Float b;

    public Expression() {
    }
    public Expression(Float k, Float b) {
        this.k = k;
        this.b = b;
    }
    public Expression(Expression other){
        this.k = other.k;
        this.b = other.b;
    }

    public static Expression buildExpression(String str_expression) {
        String[] parts = str_expression.split(",");
        if (parts.length!=2) return null;
        Float k = Float.parseFloat(parts[0]);
        Float b = Float.parseFloat(parts[1]);
        return new Expression(k, b);
    }

    public Float calculate(Float x) {
        return k * x + b;
    }
    public Float getK() {
        return k;
    }
    public Float getB() {
        return b;
    }
    public void setK(Float k) {
        this.k = k;
    }
    public void setB(Float b) {
        this.b = b;
    }
    @Override
    public String toString() {
        return "y = " + k + " x + "+ b + " b";
    }

    // 数据保存与恢复
    // Parcelable 构造函数
    protected Expression(Parcel in) {
        k = in.readFloat();
        b = in.readFloat();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeFloat(k);
        parcel.writeFloat(b);
    }
    public static final Parcelable.Creator<Expression> CREATOR = new Parcelable.Creator<Expression>() {
        @Override
        public Expression createFromParcel(Parcel in) {
            return new Expression(in);
        }

        @Override
        public Expression[] newArray(int size) {
            return new Expression[size];
        }
    };
}
