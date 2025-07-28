package com.lsy.chemicaltest_new.domain;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "experimenter_table")
public class Experimenter {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    private Integer id;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "phone")
    private String phone;
    @ColumnInfo(name = "image_path")
    private String imagePath;

    @Ignore
    private Bitmap image;

    public Experimenter() {
    }

    public Experimenter(Experimenter other){
        this.id = other.id;
        this.name = other.name;
        this.phone = other.phone;
        this.imagePath = other.imagePath;
        this.image = other.image;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String username) {
        this.name = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Experimenter{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Experimenter experimenter = (Experimenter) obj;
        return Objects.equals(id, experimenter.id) &&
                Objects.equals(name, experimenter.name) &&
                Objects.equals(imagePath, experimenter.imagePath);
    }
}
