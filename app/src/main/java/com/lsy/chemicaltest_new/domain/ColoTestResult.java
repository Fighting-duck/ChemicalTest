package com.lsy.chemicaltest_new.domain;

import android.graphics.Bitmap;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.lsy.chemicaltest_new.utils.PhotoUtil;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "sample_test_colo_table",
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
public class ColoTestResult  {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String dateTime;//做实验最后保存时间 YYYY-MM-DD HH:mm:ss
    private Integer standard_curve_id;//使用标准曲线的id
    private String originalImage_path;//原始图片路径
    private String cropImage_path;//框选区域图片路径
    private Integer correctedColor;//校正后颜色
    //private Integer color_B_value;//颜色中蓝色分量
    private String HSV_value;//HSV 值,  [H,S,V]逗号隔开
    private Float detectionCo;//检测物浓度
    private String diseaseAnal;//病害分析

    @Ignore
    private Bitmap originalImage;//原始图片
    @Ignore
    private Bitmap cropImage;//框选区域图片
    @Ignore
    private RGB RGB;//RGB值
    @Ignore
    private HSV hsv;//HSV值
    @Ignore
    private StandardCurve standardCurve;//标准曲线

    public ColoTestResult() {}

    public ColoTestResult(ColoTestResult other) {
        // 简单类型
        this.id = other.id;
        this.dateTime = other.dateTime;
        this.standard_curve_id = other.standard_curve_id;
        this.originalImage_path = other.originalImage_path;
        this.cropImage_path = other.cropImage_path;
        this.correctedColor = other.correctedColor;
        this.HSV_value = other.HSV_value;
        this.detectionCo = other.detectionCo;
        this.diseaseAnal = other.diseaseAnal;
        this.originalImage = PhotoUtil.deepCopy(other.originalImage);//深拷贝
        this.cropImage = PhotoUtil.deepCopy(other.cropImage);//深拷贝
        // 关联对象
        this.RGB = other.RGB !=null ? new RGB(other.RGB) : null;
        this.hsv = other.hsv != null ? new HSV(other.hsv) : null;
        this.standardCurve = other.standardCurve != null ? new StandardCurve(other.standardCurve) : null;
    }

    /***
     * 逗号隔开的格式字符串转化为字符串列表
     * @param str_list 逗号隔开的格式字符串
     * @return 字符串列表
     */
    public static List<String> getHSVList(String str_list) {
        //1. 去除首尾的方括号
        String trimmedInput = str_list.substring(1, str_list.length() - 1);
        //2. 按逗号分隔字符串
        String[] items = trimmedInput.split(",");
        //3..字符数组转化为字符列表
        List<String> arrayList = new ArrayList<>();
        for (String item : items) {
            // 去除每个元素周围的空白字符，并添加到结果列表中
            arrayList.add(item.trim());
        }
        //4. 返回pointList
        return arrayList;
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

    public String getOriginalImage_path() {
        return originalImage_path;
    }

    public void setOriginalImage_path(String originalImage_path) {
        this.originalImage_path = originalImage_path;
    }

    public String getCropImage_path() {
        return cropImage_path;
    }

    public void setCropImage_path(String cropImage_path) {
        this.cropImage_path = cropImage_path;
    }

    public Integer getCorrectedColor() {
        return correctedColor;
    }

    public void setCorrectedColor(Integer correctedColor) {
        this.correctedColor = correctedColor;
    }

    public String getHSV_value() {
        return HSV_value;
    }

    public void setHSV_value(String HSV_value) {
        this.HSV_value = HSV_value;
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

    public Bitmap getOriginalImage() {
        return originalImage;
    }

    public void setOriginalImage(Bitmap originalImage) {
        this.originalImage = originalImage;
    }

    public Bitmap getCropImage() {
        return cropImage;
    }

    public void setCropImage(Bitmap cropImage) {
        this.cropImage = cropImage;
    }

    public StandardCurve getStandardCurve() {
        return standardCurve;
    }

    public void setStandardCurve(StandardCurve standardCurve) {
        this.standardCurve = standardCurve;
    }

    public RGB getRGB() {
        return RGB;
    }

    public void setRGB(RGB rgb) {
        this.RGB = rgb;
    }

    public HSV getHsv() {
        return hsv;
    }

    public void setHsv(HSV hsv) {
        this.hsv = hsv;
    }

    @Override
    public String toString() {
        return "ColoTestResult{" +
                "id=" + id +
                ", dateTime='" + dateTime + '\'' +
                ", standard_curve_id=" + standard_curve_id +
                ", originalImage_path='" + originalImage_path + '\'' +
                ", cropImage_path='" + cropImage_path + '\'' +
                ", correctedColor=" + correctedColor +
                ", HSV_value='" + HSV_value + '\'' +
                ", detectionCo=" + detectionCo +
                ", diseaseAnal='" + diseaseAnal + '\'' +
                ", originalImage=" + originalImage +
                ", cropImage=" + cropImage +
                ", RGB=" + RGB +
                ", hsv=" + hsv +
                ", standardCurve=" + standardCurve +
                '}';
    }
}
