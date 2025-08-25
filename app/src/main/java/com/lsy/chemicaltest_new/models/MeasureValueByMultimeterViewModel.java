package com.lsy.chemicaltest_new.models;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lsy.chemicaltest_new.domain.TestValue;
import com.lsy.chemicaltest_new.utils.LiveDataUtils;

import java.util.ArrayList;
import java.util.List;

public class MeasureValueByMultimeterViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    MutableLiveData<List<TestValue>> mTestValueList = new MutableLiveData<>();//测量值 14次
    MutableLiveData<TestValue> mMaxValue = new MutableLiveData<>();//最大值
    MutableLiveData<Float> mDegree = new MutableLiveData<>();//4秒内值变化大不（变化不超过1度）的温度
    MutableLiveData<String> mLiveData_toast = new MutableLiveData<>();

    public MutableLiveData<List<TestValue>> getLiveData_TestValueList() {
        return mTestValueList;
    }
    public MutableLiveData<TestValue> getLiveData_MaxValue() {
        return mMaxValue;
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
     * 设置测量值中最大值
     * @param mMaxValue 最大值
     */
    public void setMaxValue(TestValue mMaxValue) {
        this.mMaxValue.postValue(mMaxValue);
    }
    public TestValue getMaxValue(){
        return mMaxValue.getValue();
    }

    public void setDegree(Float degree){
        mDegree.postValue(degree);
    }
    public Float getDegree() {
        return mDegree.getValue();
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


}