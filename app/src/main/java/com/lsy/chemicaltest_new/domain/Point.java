package com.lsy.chemicaltest_new.domain;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(tableName = "point_table")
public class Point implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Integer id;
    @ColumnInfo(name = "x_value")
    private Float x_value;
    @ColumnInfo(name = "y_value")
    private Float y_value;
    @ColumnInfo(name = "add_time")
    private String add_time;

    public Point(){}

    @Ignore
    public Point(Integer id, Float x_value, Float y_value, String add_time) {
        this.id = id;
        this.x_value = x_value;
        this.y_value = y_value;
        this.add_time = add_time;
    }

    @Ignore
    public Point(Float x_value, Float y_value, String add_time) {
        this.x_value = x_value;
        this.y_value = y_value;
        this.add_time = add_time;
    }

    @Ignore
    public Point(Point point) {
        this.id = point.id;
        this.x_value = point.x_value;
        this.y_value = point.y_value;
        this.add_time = point.add_time;
    }


    // getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Float getX_value() {
        return x_value;
    }

    public void setX_value(Float x_value) {
        this.x_value = x_value;
    }

    public Float getY_value() {
        return y_value;
    }

    public void setY_value(Float y_value) {
        this.y_value = y_value;
    }

    public String getAdd_time() {
        return add_time;
    }

    public void setAdd_time(String add_time) {
        this.add_time = add_time;
    }

    @Override
    public String toString() {
        return "Point{" +
                "id=" + id +
                ", x_value=" + x_value +
                ", y_value=" + y_value +
                ", add_time=" + add_time +
                '}';
    }

    /***
     * pointList转换为entryList
     * @param pointList pointList
     * @return entryList
     */
    public static List<Entry> pointList_to_entryList(List<Point> pointList) {
        List<Entry> entryList = new ArrayList<>();
        for (Point point : pointList) {
            if (point != null)
                entryList.add(new Entry(point.getX_value(), point.getY_value()));
        }
        return entryList;
    }

    //数据保存与恢复
    // Parcelable 构造函数
    protected Point(Parcel in) {
        // 使用 readValue 保持与 CREATOR 一致
        id = (Integer) in.readValue(Integer.class.getClassLoader());
        x_value = in.readFloat();
        y_value = in.readFloat();
        add_time = in.readString();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeValue(id);
        parcel.writeFloat(x_value);
        parcel.writeFloat(y_value);
        parcel.writeString(add_time);
    }

    public static final Parcelable.Creator<Point> CREATOR = new Parcelable.Creator<Point>() {
        @Override
        public Point createFromParcel(Parcel in) {
            return new Point(in);
        }

        @Override
        public Point[] newArray(int size) {
            return new Point[size];
        }
    };

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) return false;

        if (obj.getClass() != this.getClass()) return false;
        else {
            Point point = (Point) obj;
            return Objects.equals(this.id, point.getId()) &&
                    Objects.equals(this.x_value, point.getX_value()) &&
                    Objects.equals(this.y_value, point.getY_value());
        }
    }
}
