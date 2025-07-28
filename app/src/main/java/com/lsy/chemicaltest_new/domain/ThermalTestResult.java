package com.lsy.chemicaltest_new.domain;

import android.graphics.Bitmap;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.lsy.chemicaltest_new.utils.PhotoUtil;

@Entity(tableName = "sample_test_thermal_table",
        foreignKeys = {
                @ForeignKey(
                        entity = StandardCurve.class,
                        parentColumns = "id",
                        childColumns = "standard_curve_id",
                        onDelete = ForeignKey.NO_ACTION,
                        onUpdate = ForeignKey.NO_ACTION
                )
        },
        indices = {
                @Index(value = "standard_curve_id")
        })
public class ThermalTestResult {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String dateTime;//做实验最后保存时间 YYYY-MM-DD HH:mm:ss
    private Integer standard_curve_id;//使用标准曲线的id
    private String thermalBitmap_path;//热成像图像存储路径
    private Float centralTemperature;//中心温度
    private Float detectionCo;//检测物浓度
    private String diseaseAnal;//病害分析结果

    @Ignore
    private Bitmap thermalBitmap;//热成像图像

    @Ignore
    private StandardCurve standardCurve;

    public ThermalTestResult() {}

    public ThermalTestResult(ThermalTestResult other){
        //简单类型
        this.id = other.id;
        this.dateTime = other.dateTime;
        this.standard_curve_id = other.standard_curve_id;
        this.thermalBitmap_path = other.thermalBitmap_path;
        this.centralTemperature = other.centralTemperature;
        this.detectionCo = other.detectionCo;
        this.diseaseAnal = other.diseaseAnal;
        this.thermalBitmap = PhotoUtil.deepCopy(other.thermalBitmap);//深拷贝
        //关联对象
        this.standardCurve = other.standardCurve != null ? new StandardCurve(other.standardCurve) : null;
    }

    // Getter 和 Setter 方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public Integer getStandard_curve_id() {
        return standard_curve_id;
    }

    public void setStandard_curve_id(Integer standard_curve_id) {
        this.standard_curve_id = standard_curve_id;
    }

    public String getThermalBitmap_path() {
        return thermalBitmap_path;
    }

    public void setThermalBitmap_path(String thermalBitmap_path) {
        this.thermalBitmap_path = thermalBitmap_path;
    }

    public Float getCentralTemperature() {
        return centralTemperature;
    }

    public void setCentralTemperature(Float centralTemperature) {
        this.centralTemperature = centralTemperature;
    }

    public Float getDetectionCo() {
        return detectionCo;
    }

    public void setDetectionCo(Float detectionCo) {
        this.detectionCo = detectionCo;
    }

    public String getDiseaseAnal() {
        return diseaseAnal;
    }

    public void setDiseaseAnal(String diseaseAnal) {
        this.diseaseAnal = diseaseAnal;
    }

    public StandardCurve getStandardCurve() {
        return standardCurve;
    }

    public void setStandardCurve(StandardCurve standardCurve) {
        this.standardCurve = standardCurve;
    }

    public Bitmap getThermalBitmap() {
        return thermalBitmap;
    }

    public void setThermalBitmap(Bitmap thermalBitmap) {
        this.thermalBitmap = thermalBitmap;

    }

    @Override
    public String toString() {
        return "ThermalTestResult{" +
                "id=" + id +
                ", dateTime='" + dateTime + '\'' +
                ", standard_curve_id=" + standard_curve_id +
                ", thermalBitmap_path='" + thermalBitmap_path + '\'' +
                ", centralTemperature=" + centralTemperature +
                ", detectionCo=" + detectionCo +
                ", diseaseAnal='" + diseaseAnal + '\'' +
                ", thermalBitmap=" + thermalBitmap +
                ", standardCurve=" + standardCurve +
                '}';
    }
}
