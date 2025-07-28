package com.lsy.chemicaltest_new.domain;

public class TestValue {
    private String testTime;
    private Float value;
    private String unit;

    public TestValue(Float value, String unit) {
        this.value = value;
        this.unit = unit;
    }
    public String getTestTime() {
        return testTime;
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
