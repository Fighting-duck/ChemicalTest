package com.lsy.chemicaltest_new.models;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lsy.chemicaltest_new.domain.BleDeviceInfo;
import com.lsy.chemicaltest_new.domain.TestValue;
import com.lsy.chemicaltest_new.utils.LiveDataUtils;

import java.util.ArrayList;
import java.util.List;

public class MeasureValueByMultimeterViewModel extends ViewModel {
    MutableLiveData<BleDeviceInfo> mBleDeviceInfo = new MutableLiveData<>();//设备信息
    MutableLiveData<List<TestValue>> mTestValueList = new MutableLiveData<>();//测量值 14次
    MutableLiveData<TestValue> mResultValue = new MutableLiveData<>();//测量值（14次中最大电信号值  或4秒内值变化大不（变化不超过1度）的温度）
    MutableLiveData<Float> mDegree = new MutableLiveData<>();//4秒内值变化大不（变化不超过1度）的温度
    MutableLiveData<String> mLiveData_toast = new MutableLiveData<>();

    public MutableLiveData<BleDeviceInfo> getLiveData_BleDeviceInfo() {
        return mBleDeviceInfo;
    }
    public MutableLiveData<List<TestValue>> getLiveData_TestValueList() {
        return mTestValueList;
    }
    public MutableLiveData<TestValue> getLiveData_ResultValue() {
        return mResultValue;
    }
    public MutableLiveData<Float> getLiveData_Degree() {
        return mDegree;
    }
    public MutableLiveData<String> getLiveData_toast(){
        return mLiveData_toast;
    }
    public void setToast(String prompt){
        LiveDataUtils.safeUpdate(mLiveData_toast,prompt);
    }

    /***
     * 设置测量结果值
     * @param resultValue 电信号最大值/衡定温度值
     */
    public void setResultValue(TestValue resultValue) {
        this.mResultValue.postValue(resultValue);
    }
    public TestValue getResultValue(){
        return mResultValue.getValue();
    }

    /***
     * 设置温度变化值
     * @param degree 4秒内值变化大不（变化不超过1度）的温度
     */
    public void setDegree(Float degree){
        mDegree.postValue(degree);
    }
    public Float getDegree() {
        return mDegree.getValue();
    }

    public List<TestValue> getValueList(){
        return mTestValueList.getValue();
    }

    /***
     * 添加测量值到列表
     * @param value 测量值
     */
    public  void addValueToList(TestValue value){
        List<TestValue> values  = mTestValueList.getValue();
        if (values== null) {
            values = new ArrayList<>();
        }
        values.add(value);
        mTestValueList.setValue(values);
    }

    public void setBleDeviceInfo_Elec(BleDeviceInfo bleDeviceInfo) {
        mBleDeviceInfo.setValue(bleDeviceInfo);
    }
    public BleDeviceInfo getBleDeviceInfo_Elec() {
        return mBleDeviceInfo.getValue();
    }

}