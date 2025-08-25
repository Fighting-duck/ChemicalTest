package com.lsy.chemicaltest_new.models;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.clj.fastble.data.BleDevice;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.domain.BleDeviceInfo;
import com.lsy.chemicaltest_new.domain.TestValue;
import com.lsy.chemicaltest_new.utils.LineChartUtil;
import com.lsy.chemicaltest_new.utils.LiveDataUtils;

import java.util.ArrayList;
import java.util.List;

public class ConnectMultimeterViewModel extends ViewModel {
    MutableLiveData<Boolean> mIsConnectDevice = new MutableLiveData<>(Boolean.FALSE);//是否连接蓝牙设备
    MutableLiveData<TestValue> mRealTimeValue = new MutableLiveData<>();//实时测量值
    MutableLiveData<List<BleDevice>> mBleDeviceList = new MutableLiveData<>();//设备列表
    MutableLiveData<BleDevice> mConnectBleDevice = new MutableLiveData<>();//连接蓝牙设备
    MutableLiveData<BleDeviceInfo> mBleDeviceInfo = new MutableLiveData<>();//连接蓝牙设备信息
    MutableLiveData<LineData> mLines = new MutableLiveData<>();//图表数据
    MutableLiveData<String> mLiveData_toast = new MutableLiveData<>();
    MutableLiveData<List<TestValue>> mTestValueList = new MutableLiveData<>();//测量值 14次
    MutableLiveData<TestValue> mMaxValue = new MutableLiveData<>();//最大值
    MutableLiveData<Float> mDegree = new MutableLiveData<>();//4秒内值变化大不（变化不超过1度）的温度
    Context mContext; // 上下文信息

    // get LiveData
    public MutableLiveData<Boolean> getLiveData_isConnectDevice(){ return mIsConnectDevice;}
    public MutableLiveData<TestValue> getLiveData_RealTimeValue() {
        return mRealTimeValue;
    }
    public MutableLiveData<List<BleDevice>> getLiveData_BleDeviceList() {
        return mBleDeviceList;
    }
    public MutableLiveData<BleDevice> getLiveData_ConnectBleDevice() {
        return mConnectBleDevice;
    }
    public MutableLiveData<BleDeviceInfo> getLiveData_BleDeviceInfo() {
        return mBleDeviceInfo;
    }
    public MutableLiveData<LineData> getLiveData_Lines() {
        return mLines;
    }
    public MutableLiveData<String> getLiveData_toast(){
        return mLiveData_toast;
    }
    public MutableLiveData<List<TestValue>> getLiveData_TestValueList() {
        return mTestValueList;
    }
    public MutableLiveData<TestValue> getLiveData_MaxValue() {
        return mMaxValue;
    }
    public MutableLiveData<Float> getLiveData_Degree() {
        return mDegree;
    }

    public void setToast(String prompt){
        LiveDataUtils.safeUpdate(mLiveData_toast,prompt);
    }
    public void setContext(Context context){
        mContext = context;
    }

    /***
     * 设置蓝牙设备是否连接
     * @param isConnected 连接状态
     */
    public void setIsConnected(boolean isConnected) {
        mIsConnectDevice.setValue(isConnected);
    }
    /***
     * 设置连接的设备
     * @param device
     */
    public void setConnectDevice(BleDevice device) {
        mConnectBleDevice.setValue(device);
    }
    /***
     * 设置挡位和里程
     * @param bleDeviceInfo 打包数据
     */
    public void setGearAndMileage(BleDeviceInfo bleDeviceInfo) {
        mBleDeviceInfo.setValue(bleDeviceInfo);
    }
    /***
     * 设置当前测试值
     * @param testValue 测量值
     */
    public void setCurrentTestValue(TestValue testValue) {
        mRealTimeValue.setValue(testValue);
    }
    public void setDegree(Float degree){
        mDegree.postValue(degree);
    }
    /***
     * 清空蓝牙设备列表
     */
    public void clearDevices() {
        mBleDeviceList.setValue(null);
    }

    /***
     * 添加设备到蓝牙设备列表
     * @param device 蓝牙设备
     */
    public void addDevice(BleDevice device) {
        List<BleDevice> devices = mBleDeviceList.getValue();
        if (devices == null) {
            devices = new ArrayList<>();
        }
        devices.add(device);
        mBleDeviceList.setValue(devices);
    }

    /***
     * 向实时电流表中添加一个点
     * @param xValue x值
     * @param yValue y值
     */
    public void addEntryInLast(float xValue,float yValue) {
        LineData lineData = mLines.getValue();
        if (lineData == null)  {
            lineData = new LineData();
            LineDataSet lineDataSet = new LineDataSet(null, mContext.getString(R.string.chart_electric));
            lineData = LineChartUtil.addLine(lineData, lineDataSet, R.color.line_electric);
        }
        //获取点数
        int count = lineData.getDataSetByIndex(0).getEntryCount();
        //保证最多只有50个点
        if (count > 50) {
            lineData.getDataSetByIndex(0).removeEntry(0);
        }
        //添加点
        lineData = LineChartUtil.addEntryInLast(lineData,xValue, yValue,0);
        mLines.setValue(lineData);
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

    public List<BleDevice> getBleDeviceList() {
        return mBleDeviceList.getValue();
    }
    public BleDevice getConnectBleDevice() {
        return mConnectBleDevice.getValue();
    }
}
