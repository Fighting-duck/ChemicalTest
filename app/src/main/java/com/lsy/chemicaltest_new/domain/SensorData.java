package com.lsy.chemicaltest_new.domain;

public class SensorData {
    float current; // y₁ (电流)
    float temperature; // y₂ (温度)
    float bValue; // y₃ (b值)
    float logConcentration; // x (取对数后的浓度)

    public float getCurrent() {
        return current;
    }

    public void setCurrent(float current) {
        this.current = current;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getBValue() {
        return bValue;
    }

    public void setBValue(float bValue) {
        this.bValue = bValue;
    }

    public float getLogConcentration() {
        return logConcentration;
    }

    public void setLogConcentration(float logConcentration) {
        this.logConcentration = logConcentration;
    }
}
