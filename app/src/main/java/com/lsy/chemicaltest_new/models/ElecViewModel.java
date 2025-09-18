package com.lsy.chemicaltest_new.models;

import static com.lsy.chemicaltest_new.utils.DynamicStringUtils.getString;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.room.Transaction;

import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.database.DataRepository;
import com.lsy.chemicaltest_new.domain.BleDeviceInfo;
import com.lsy.chemicaltest_new.domain.ElecTestResult;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.domain.TestValue;
import com.lsy.chemicaltest_new.utils.LiveDataUtils;
import com.lsy.chemicaltest_new.utils.NumberUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ElecViewModel extends ViewModel {

    private static final String TAG = "ElecViewModel";
    //电信号检测
    MutableLiveData<BleDeviceInfo> mBleDeviceInfo = new MutableLiveData<>();//连接蓝牙设备信息
    MutableLiveData<List<TestValue>> mTestValueList = new MutableLiveData<>();//测量值 14次
    MutableLiveData<TestValue> mMaxValue = new MutableLiveData<>();//最大值
    MutableLiveData<StandardCurve> mStandardCurve = new MutableLiveData<>();//使用的电信号标准曲线
    MutableLiveData<Float> mCOElec = new MutableLiveData<>();//电信号 浓度
    MutableLiveData<String> mDiseaseAnal = new MutableLiveData<>();//电信号 病害分析
    MediatorLiveData<ElecTestResult> mElecTestResult = new MediatorLiveData<>();//电信号检测结果综合
    //其它
    MutableLiveData<String> mLiveData_toast = new MutableLiveData<>();
    MutableLiveData<String> mLiveData_notice = new MutableLiveData<>();

    public MutableLiveData<BleDeviceInfo> getLiveData_BleDeviceInfo() {
        return mBleDeviceInfo;
    }
    public MutableLiveData<TestValue> getLiveData_MaxValue() {
        return mMaxValue;
    }
    public MutableLiveData<StandardCurve> getLiveData_StandardCurve() {
        return mStandardCurve;
    }
    public MutableLiveData<List<TestValue>> getLiveData_TestValueList() {
        return mTestValueList;
    }
    public MutableLiveData<Float> getLiveData_COElec() {
        return mCOElec;
    }
    public MutableLiveData<String> getLiveData_DiseaseAnalElec() {
        return mDiseaseAnal;
    }
    public MediatorLiveData<ElecTestResult> getLiveData_ElecTestResult() {
        return mElecTestResult;
    }
    public MutableLiveData<String> getLiveData_toast(){
        return mLiveData_toast;
    }
    public MutableLiveData<String> getLiveData_notice(){
        return mLiveData_notice;
    }


    public void setToast(String prompt){
        LiveDataUtils.safeUpdate(mLiveData_toast,prompt);
    }

    public ElecViewModel(){
        mElecTestResult.addSource(mStandardCurve,this::updateStandardCurve_Elec);
        mElecTestResult.addSource(mTestValueList, this::updateTestValueList);
        mElecTestResult.addSource(mMaxValue, this::updateMaxValue);
        mElecTestResult.addSource(mCOElec, this::updateCO);
        mElecTestResult.addSource(mDiseaseAnal, this::updateDiseaseAnal);
        mElecTestResult.addSource(mBleDeviceInfo, this::updateBleDeviceInfo);
    }

    private  void updateStandardCurve_Elec(StandardCurve curve) {
        ElecTestResult result = mElecTestResult.getValue();
        if (result == null) {
            result = new ElecTestResult();
        }
        if (curve!=null) {
            result.setStandard_curve_id(curve.getId());
            result.setStandardCurve(curve);
            Log.d(TAG, "ELec 当前使用曲线id："+curve.getId());
        }
        mElecTestResult.setValue(result);
    }

    private void updateTestValueList(List<TestValue> testValueList) {
        ElecTestResult result = mElecTestResult.getValue();
        if (result == null) {
            result = new ElecTestResult();
        }
        if (testValueList!=null) {
            result.setTestValueList(testValueList);
            result.setFourteen_measurements_list_and_fourteen_times_list(testValueList);
            Log.d(TAG, "当前14次测量结果："+testValueList);
        }
        mElecTestResult.setValue(result);
    }

    private void updateMaxValue(TestValue maxValue) {
        ElecTestResult result = mElecTestResult.getValue();
        if (result == null) {
            result = new ElecTestResult();
        }
        if (maxValue!=null) {
            result.setMaxValue_4(maxValue.getValue());
            Log.d(TAG, "当前14次测量结果最大值："+maxValue);
        }
        mElecTestResult.setValue(result);
    }

    private void updateCO(Float CO) {
        ElecTestResult result = mElecTestResult.getValue();
        if (result == null) {
            result = new ElecTestResult();
        }
        if (CO!=null) {
            result.setDetectionCo(CO);
            Log.d(TAG, "对应浓度值为："+CO);
        }
        mElecTestResult.setValue(result);
    }

    private void updateDiseaseAnal(String diseaseAnal) {
        ElecTestResult result = mElecTestResult.getValue();
        if (result == null) {
            result = new ElecTestResult();
        }
        if (diseaseAnal!=null) {
            result.setDiseaseAnal(diseaseAnal);
            Log.d(TAG, "病害分析结果："+diseaseAnal);
        }
        mElecTestResult.setValue(result);
    }

    private void updateBleDeviceInfo(BleDeviceInfo bleDeviceInfo) {
        ElecTestResult result = mElecTestResult.getValue();
        if (result == null) {
            result = new ElecTestResult();
        }
        if (bleDeviceInfo!=null) {
            result.setBleDeviceInfo(bleDeviceInfo);
            Log.d(TAG, "当前设备信息："+bleDeviceInfo.toString());
        }
        mElecTestResult.setValue(result);
    }

    public void setBleDeviceInfo_Elec(BleDeviceInfo bleDeviceInfo) {
        mBleDeviceInfo.setValue(bleDeviceInfo);
    }

    public void setStandardCurve_Elec(StandardCurve standardCurve) {
        mStandardCurve.setValue(standardCurve);
    }

    public void setElecCurveAndCalculateCO(StandardCurve standardCurve) {
        mStandardCurve.setValue(standardCurve);
        calculateCO(); // 计算出当前最大电流对应的浓度
        // 病害分析
        diseaseAnalElec();
    }

    /***
     * 设置测量值中最大值
     * @param mMaxValue 最大值
     */
    public void setMaxValue(TestValue mMaxValue) {
        this.mMaxValue.postValue(mMaxValue);
    }

    public void setDetectionCo(Float detectionCo) {
        mCOElec.setValue(detectionCo);
    }

    /***
     * 设置病害分析结果
     * @param diseaseAnal 病害分析结果
     */
    public void setDiseaseAnal_Elec(String diseaseAnal) {
        mDiseaseAnal.setValue(diseaseAnal);
    }

    public void setTestValueList(List<TestValue> mTestValueList) {
        this.mTestValueList.setValue(mTestValueList);
    }
    /***
     * 获取电信号测试曲线浓度X单位
     * @return 单位
     */
    public String getUnit_elec() {
        StandardCurve curve = mStandardCurve.getValue();
        if (curve != null) {
            return curve.getX_axis_unit();
        }
        return "";
    }

    /***
     * 使用电流计算浓度
     * @return 对应浓度
     */
    public void calculateCO(){
        StandardCurve curve = mStandardCurve.getValue();
        TestValue current = mMaxValue.getValue(); // 获取当前测试值 y
        if (curve != null && current!=null){
            Float co = curve.calculateX_toY(current.getValue());
            mCOElec.setValue(NumberUtils.roundCO(co));
            noticeCO(co,curve);
        }
    }
    /***
     * 通知CO值是否属于正常范围
     * @param CO 浓度值
     * @param curve 标准曲线
     */
    private void noticeCO(Float CO,StandardCurve curve){
        if (CO < curve.getMin_CO()){
            mLiveData_notice.setValue(getString(R.string.toast_abnormal_CoLessThanNormalValue));
        }
        else if (CO > curve.getMax_CO()){
            mLiveData_notice.setValue(getString(R.string.toast_abnormal_CoGreaterThanNormalValue));
        }
        else
            mLiveData_notice.setValue(getString(R.string.toast_normal));
    }
    /***
     * 病害分析
     * @return 病害分析结果
     */
    public String diseaseAnalElec(){
       Float CO = mCOElec.getValue();
       if (CO!=null){
           //根据CO进行病害分析
           //...
           String diseaseAnal = "病害分析,电信号检测->浓度值："+CO;
           mDiseaseAnal.setValue(diseaseAnal);
           return diseaseAnal;
       }
        return null;
    }

    @Transaction
    @SuppressLint("SimpleDateFormat")
    public void save(){
        //获取当前时间(保存时间)
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateString = formatter.format(currentTime);
        ElecTestResult result = mElecTestResult.getValue();
        if (result!=null){
            result.setDateTime(dateString);
            Log.d(TAG, "电信号保存时间："+dateString);
            mElecTestResult.setValue(result);
            DataRepository.getInstance().setElecTestResult(result);//保存到数据仓库
        }
        mLiveData_toast.setValue(getString(R.string.toast_tempSave_success));
    }

    public boolean clearAll() {
        // 移除所有数据源
        mElecTestResult.removeSource(mStandardCurve);
        mElecTestResult.removeSource(mBleDeviceInfo);
        mElecTestResult.removeSource(mTestValueList);
        mElecTestResult.removeSource(mMaxValue);
        mElecTestResult.removeSource(mCOElec);
        mElecTestResult.removeSource(mDiseaseAnal);


        // 清空每个数据源的值
        mBleDeviceInfo.setValue(null);
        mTestValueList.setValue(null);
        mMaxValue.setValue(null);
        mStandardCurve.setValue(null);
        mCOElec.setValue(null);
        mDiseaseAnal.setValue(null);

        mLiveData_notice.setValue(null);
        // 将 mElecTestResult 的值设置为 null 或默认值
        mElecTestResult.setValue(null);
        return true;
    }
}
