package com.lsy.chemicaltest_new.domain;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "sample_test_elec_table",
        foreignKeys = {
                @ForeignKey(
                        entity = StandardCurve.class,
                        parentColumns = "id",
                        childColumns = "standard_curve_id",
                        onDelete = ForeignKey.NO_ACTION,
                        onUpdate = ForeignKey.NO_ACTION
                ),
                @ForeignKey(
                        entity = BleDeviceInfo.class,
                        parentColumns = "id",
                        childColumns = "bleDeviceInfo_id",
                        onDelete = ForeignKey.NO_ACTION,
                        onUpdate = ForeignKey.NO_ACTION
                ),
        },
        indices = {
                @Index(value = "standard_curve_id"),
                @Index(value = "bleDeviceInfo_id")
        })
public class ElecTestResult{
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String dateTime;//做实验最后保存时间 YYYY-MM-DD HH:mm:ss
    private Integer standard_curve_id;//使用标准曲线的id
    private Integer bleDeviceInfo_id;//蓝牙设备信息的id
    private String fourteen_measurements_list;//14 次测量值列表,逗号隔开
    private String fourteen_times_list;//14 次测量值时间列表,逗号隔开
    private Float maxValue_4;//4秒内测量最大值
    private Float detectionCo;//检测物浓度
    private String diseaseAnal;//病害分析结果

    @Ignore
    private List<Float> valueList = new ArrayList<>();//所有测量值 14次
    @Ignore
    private List<TestValue> testValueList = new ArrayList<>();//所有测量值+时间+单位 14次
    @Ignore
    private StandardCurve standardCurve;//标准曲线
    @Ignore
    private BleDeviceInfo bleDeviceInfo;//蓝牙设备信息

    /***
     * fourteen_measurements_list+fourteen_times_list+unit -> testValueList
     * @param fourteen_measurements_list  14 次测量值列表,逗号隔开
     * @param fourteen_times_list 14 次测量值时间列表,逗号隔开
     * @param unit 单位
     */
    public void setTestValueList(String fourteen_measurements_list, String fourteen_times_list, String unit) {
        List<String> measurements = getPointList(fourteen_measurements_list);
        List<String> times = getPointList(fourteen_times_list);
        for (int i = 0; i < measurements.size(); i++) {
            TestValue testValue = new TestValue(Float.parseFloat(measurements.get(i)), unit);
            testValue.setTestTime(times.get(i));
            testValueList.add(testValue);
        }
    }
    /***
     * testValueList -> fourteen_measurements_list+fourteen_times_list
     * @param testValueList 所有测量值+时间+单位 14次
     */
    public void setFourteen_measurements_list_and_fourteen_times_list(List<TestValue> testValueList) {
        if (testValueList != null &&testValueList.size() == 14){
            // 1. 获取所有测量值和时间
            List<String> timeList = new ArrayList<String>();
            List<Float> values = new ArrayList<Float>();
            for (TestValue value : testValueList) {
                timeList.add(value.getTestTime());
                values.add(value.getValue());
            }
            fourteen_measurements_list = values.toString();
            fourteen_times_list = timeList.toString();
        }
    }

    /***
     * 逗号隔开的格式字符串转化为字符串列表
     * @param str_list 逗号隔开的格式字符串
     * @return 字符串列表
     */
    public static List<String> getPointList(String str_list) {
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

    public ElecTestResult(){}

    public ElecTestResult(ElecTestResult other){
        // 简单类型
        this.id = other.id;
        this.dateTime = other.dateTime;
        this.standard_curve_id = other.standard_curve_id;
        this.bleDeviceInfo_id = other.bleDeviceInfo_id;
        this.fourteen_measurements_list = other.fourteen_measurements_list;
        this.fourteen_times_list = other.fourteen_times_list;
        this.maxValue_4 = other.maxValue_4;
        this.detectionCo = other.detectionCo;
        this.diseaseAnal = other.diseaseAnal;
        // 列表
        this.valueList = new ArrayList<>(other.valueList);
        this.testValueList = new ArrayList<>(other.testValueList);
        // 关联对象
        this.standardCurve = other.standardCurve !=  null ? new StandardCurve(other.standardCurve) : null;
        this.bleDeviceInfo = other.bleDeviceInfo != null ? new BleDeviceInfo(other.bleDeviceInfo) : null;
    }

    public void addValueInLast(float value) {
        valueList.add(value);
        if (valueList.size() > 11) {
            valueList.remove(0);
        }
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

    public Integer getBleDeviceInfo_id() {
        return bleDeviceInfo_id;
    }

    public void setBleDeviceInfo_id(Integer bleDeviceInfo_id) {
        this.bleDeviceInfo_id = bleDeviceInfo_id;
    }

    public String getFourteen_measurements_list() {
        return fourteen_measurements_list;
    }

    public void setFourteen_measurements_list(String fourteen_measurements_list) {
        this.fourteen_measurements_list = fourteen_measurements_list;
    }

    public String getFourteen_times_list() {
        return fourteen_times_list;
    }

    public void setFourteen_times_list(String fourteen_times_list) {
        this.fourteen_times_list = fourteen_times_list;
    }

    public Float getMaxValue_4() {
        return maxValue_4;
    }

    public void setMaxValue_4(Float maxValue_4) {
        this.maxValue_4 = maxValue_4;
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

    public List<Float> getValueList() {
        return valueList;
    }

    public void setValueList(List<Float> valueList) {
        this.valueList = valueList;
    }

    public List<TestValue> getTestValueList() {
        return testValueList;
    }

    public void setTestValueList(List<TestValue> testValueList) {
        this.testValueList = testValueList;
    }

    public StandardCurve getStandardCurve() {
        return standardCurve;
    }

    public void setStandardCurve(StandardCurve standardCurve) {
        this.standardCurve = standardCurve;
    }

    public BleDeviceInfo getBleDeviceInfo() {
        return bleDeviceInfo;
    }

    public void setBleDeviceInfo(BleDeviceInfo bleDeviceInfo) {
        this.bleDeviceInfo = bleDeviceInfo;
    }

    @Override
    public String toString() {
        return "ElecTestResult{" +
                "id=" + id +
                ", dateTime='" + dateTime + '\'' +
                ", standard_curve_id=" + standard_curve_id +
                ", bleDeviceInfo_id=" + bleDeviceInfo_id +
                ", fourteen_measurements_list='" + fourteen_measurements_list + '\'' +
                ", fourteen_times_list='" + fourteen_times_list + '\'' +
                ", maxValue_4=" + maxValue_4 +
                ", detectionCo=" + detectionCo +
                ", diseaseAnal='" + diseaseAnal + '\'' +
                ", valueList=" + valueList +
                ", testValueList=" + testValueList +
                ", standardCurve=" + standardCurve +
                ", bleDeviceInfo=" + bleDeviceInfo +
                '}';
    }
}
