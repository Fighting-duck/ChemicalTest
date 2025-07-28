package com.lsy.chemicaltest_new.domain;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/***
 * 电信号检测温度
 */
@Entity(tableName = "temperature_elec_table")
public class Temperature_Elec {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String dateTime;//做实验最后保存时间 YYYY-MM-DD HH:mm:ss
    private Integer standard_curve_id;//使用标准曲线的id
    private Integer bleDeviceInfo_id;//蓝牙设备信息的id
    private Float temperature;//温度
    private Float detectionCo;//检测物浓度
    private String diseaseAnal;//病害分析结果

    @Ignore
    private BleDeviceInfo bleDeviceInfo;//蓝牙设备信息
    @Ignore
    private StandardCurve standardCurve;

    public Temperature_Elec() {
    }
    public Temperature_Elec(Temperature_Elec other) {
        //简单类型
        this.id = other.id;
        this.dateTime = other.dateTime;
        this.bleDeviceInfo_id = other.bleDeviceInfo_id;
        this.temperature = other.temperature;
        this.detectionCo = other.detectionCo;
        this.diseaseAnal = other.diseaseAnal;
        this.standard_curve_id = other.standard_curve_id;
        //关联对象
        this.standardCurve = other.standardCurve != null ? new StandardCurve(other.standardCurve) : null;
        this.bleDeviceInfo = other.bleDeviceInfo != null ? new BleDeviceInfo(other.bleDeviceInfo) : null;
    }

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

    public Integer getBleDeviceInfo_id() {
        return bleDeviceInfo_id;
    }

    public void setBleDeviceInfo_id(Integer bleDeviceInfo_id) {
        this.bleDeviceInfo_id = bleDeviceInfo_id;
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
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

    public BleDeviceInfo getBleDeviceInfo() {
        return bleDeviceInfo;
    }

    public void setBleDeviceInfo(BleDeviceInfo bleDeviceInfo) {
        this.bleDeviceInfo = bleDeviceInfo;
    }

    public Integer getStandard_curve_id() {
        return standard_curve_id;
    }

    public void setStandard_curve_id(Integer standard_curve_id) {
        this.standard_curve_id = standard_curve_id;
    }

    public StandardCurve getStandardCurve() {
        return standardCurve;
    }

    public void setStandardCurve(StandardCurve standardCurve) {
        this.standardCurve = standardCurve;
    }

    @Override
    public String toString() {
        return "Temperature_Elec{" +
                "id=" + id +
                ", dateTime='" + dateTime + '\'' +
                ", standard_curve_id=" + standard_curve_id +
                ", bleDeviceInfo_id=" + bleDeviceInfo_id +
                ", temperature=" + temperature +
                ", detectionCo=" + detectionCo +
                ", diseaseAnal='" + diseaseAnal + '\'' +
                ", bleDeviceInfo=" + bleDeviceInfo +
                ", standardCurve=" + standardCurve +
                '}';
    }
}
