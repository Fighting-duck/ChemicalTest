package com.lsy.chemicaltest_new.domain;

public class TestValue {
    private Long time_long;//HH:mm:ss值
    private String testTime;//HH:mm:ss
    private Float value;
    private String unit;

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
}
