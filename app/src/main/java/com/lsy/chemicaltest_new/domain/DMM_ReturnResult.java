package com.lsy.chemicaltest_new.domain;

/***
 * 万用表返回数据
 */
public class DMM_ReturnResult {
    private BleDeviceInfo bleDeviceInfo;//挡位+里程+单位
    private TestValue testValue;//测量值
    public DMM_ReturnResult(){}

    public DMM_ReturnResult(BleDeviceInfo bleDeviceInfo, TestValue testValue) {
        this.bleDeviceInfo = bleDeviceInfo;
        this.testValue = testValue;
    }

    public BleDeviceInfo getBleDeviceInfo() {
        return bleDeviceInfo;
    }

    public DMM_ReturnResult setBleDeviceInfo(BleDeviceInfo bleDeviceInfo) {
        this.bleDeviceInfo = bleDeviceInfo;
        return this;
    }

    public TestValue getTestValue() {
        return testValue;
    }

    public DMM_ReturnResult setTestValue(TestValue testValue) {
        this.testValue = testValue;
        return this;
    }

    @Override
    public String toString() {
        return "DMM_ReturnResult{" +
                "bleDeviceInfo=" + bleDeviceInfo +
                ", testValue=" + testValue +
                '}';
    }
}
