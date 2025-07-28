package com.lsy.chemicaltest_new.domain;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

@Entity(tableName = "sample_table")
public class Sample implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "imagePath")
    private String imagePath;
    @ColumnInfo(name = "description")
    private String description;
    @ColumnInfo(name = "validity")
    private int validity = 1;
    @Ignore
    private Bitmap image;
    public Sample() {
    }

    @Ignore
    public Sample(String name) {
        this.name = name;
    }
    @Ignore
    public Sample(int id, String name,int validity) {
        this.id = id;
        this.name = name;
        this.validity = validity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public int getValidity() {
        return validity;
    }
    public void setValidity(int validity) {
        this.validity = validity;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Sample{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", description='" + description + '\'' +
                ", validity=" + validity +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Sample sample = (Sample) obj;
        return id == sample.id &&
                Objects.equals(name, sample.name) &&
                Objects.equals(imagePath, sample.imagePath) &&
                Objects.equals(description, sample.description) &&
                Objects.equals(validity, sample.validity);
    }

    // Parcelable 相关方法
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(imagePath);
        parcel.writeString(description);
        parcel.writeInt(validity);
    }

    protected Sample(Parcel in) {
        id = in.readInt();
        name = in.readString();
        imagePath = in.readString();
        description = in.readString();
        validity = in.readInt();
    }

    public static final Creator<Sample> CREATOR = new Creator<Sample>() {
        @Override
        public Sample createFromParcel(Parcel in) {
            return new Sample(in);
        }

        @Override
        public Sample[] newArray(int size) {
            return new Sample[size];
        }
    };
}
