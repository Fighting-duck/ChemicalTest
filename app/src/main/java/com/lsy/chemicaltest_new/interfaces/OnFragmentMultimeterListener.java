package com.lsy.chemicaltest_new.interfaces;

import com.lsy.chemicaltest_new.domain.BleDeviceInfo;
import com.lsy.chemicaltest_new.domain.TestValue;

// 定义接口
public interface OnFragmentMultimeterListener {
    void onMeasureValue(TestValue testValue);//获取实时测量值
    void onDeviceInfo(BleDeviceInfo bleDeviceInfo);//获取当前设备信息
}