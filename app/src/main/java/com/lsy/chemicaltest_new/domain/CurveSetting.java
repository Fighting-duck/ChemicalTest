package com.lsy.chemicaltest_new.domain;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "curve_setting_table")
public class CurveSetting {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "x_axis_unit")
    private String x_axis_unit; // 浓度单位---x轴单位
    @ColumnInfo(name = "min_CO")
    private Float min_CO; // 浓度最小值
    @ColumnInfo(name = "max_CO")
    private Float max_CO; // 浓度最大值
    @ColumnInfo(name = "minCorr")
    private Float minCorr;//最小相关系数，小于此数提示异常，需要校准
    public CurveSetting() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getX_axis_unit() {
        return x_axis_unit;
    }

    public void setX_axis_unit(String x_axis_unit) {
        this.x_axis_unit = x_axis_unit;
    }

    public Float getMin_CO() {
        return min_CO;
    }

    public void setMin_CO(Float min_CO) {
        this.min_CO = min_CO;
    }

    public Float getMax_CO() {
        return max_CO;
    }

    public void setMax_CO(Float max_CO) {
        this.max_CO = max_CO;
    }

    public Float getMinCorr() {
        return minCorr;
    }

    public void setMinCorr(Float minCorr) {
        this.minCorr = minCorr;
    }

    @Override
    public String toString() {
        return "ElecSetting{" +
                "x_axis_unit='" + x_axis_unit + '\'' +
                ", min_CO=" + min_CO +
                ", max_CO=" + max_CO +
                ", minCorr=" + minCorr +
                '}';
    }
}
