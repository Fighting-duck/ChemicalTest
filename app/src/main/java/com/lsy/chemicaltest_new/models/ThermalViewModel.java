package com.lsy.chemicaltest_new.models;

import static com.lsy.chemicaltest_new.utils.DynamicStringUtils.getString;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Transaction;

import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.database.DataRepository;
import com.lsy.chemicaltest_new.domain.BleDeviceInfo;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.domain.Temperature_Elec;
import com.lsy.chemicaltest_new.domain.ThermalTestResult;
import com.lsy.chemicaltest_new.utils.LiveDataUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ThermalViewModel extends AndroidViewModel {
    private static final String TAG = "ThermalViewModel";
    MediatorLiveData<Temperature_Elec> mLiveData_Temperature_Elec = new MediatorLiveData<>();//万用表测温度检查结果综合
    private MediatorLiveData<ThermalTestResult> mLiveData_ThermalTestResult = new MediatorLiveData<>();// 红外摄像仪测量结果
    private MutableLiveData<StandardCurve> mLiveData_curve = new MutableLiveData<>();// 标准曲线
    private MutableLiveData<Bitmap> mLiveData_thermalBitmap = new MutableLiveData<>();// 红外摄像仪测量结果图片
    MutableLiveData<BleDeviceInfo> mLiveData_bleDeviceInfo = new MutableLiveData<>();//连接蓝牙设备信息
    private MutableLiveData<Float> mLiveData_centralTemperature = new MutableLiveData<>();// 中心温度
    private MutableLiveData<Float> mLiveData_CO = new MutableLiveData<>();// 浓度
    private MutableLiveData<String> mLiveData_diseaseAnal = new MutableLiveData<>();// 病害分析
    private MutableLiveData<String> mLiveData_toast = new MutableLiveData<>();

    public MutableLiveData<String> getLiveData_toast(){
        return mLiveData_toast;
    }
    public void setToast(String prompt){
        LiveDataUtils.safeUpdate(mLiveData_toast,prompt);
    }

    public MediatorLiveData<Temperature_Elec> getLiveData_Temperature_Elec() {
        return mLiveData_Temperature_Elec;
    }
    public MediatorLiveData<ThermalTestResult> getLiveData_ThermalTestResult() {
        return mLiveData_ThermalTestResult;
    }
    public MutableLiveData<Bitmap> getLiveData_ThermalBitmap() {
        return mLiveData_thermalBitmap;
    }
    public MutableLiveData<StandardCurve> getLiveData_Curve() {
        return mLiveData_curve;
    }
    public MutableLiveData<Float> getLiveData_CentralTemperature() {
        return mLiveData_centralTemperature;
    }
    public MutableLiveData<BleDeviceInfo> getLiveData_BleDeviceInfo() {
        return mLiveData_bleDeviceInfo;
    }
    public MutableLiveData<Float> getLiveData_CO() {
        return mLiveData_CO;
    }
    public MutableLiveData<String> getLiveData_diseaseAnal() {
        return mLiveData_diseaseAnal;
    }

    public ThermalViewModel(Application application) {
        super(application);
        mLiveData_ThermalTestResult.addSource(mLiveData_thermalBitmap,this::updateThermalBitmap);
        mLiveData_ThermalTestResult.addSource(mLiveData_curve, this::updateCurve);
        mLiveData_ThermalTestResult.addSource(mLiveData_centralTemperature, this::updateCentralTemperature);
        mLiveData_ThermalTestResult.addSource(mLiveData_CO, this::updateCO);
        mLiveData_ThermalTestResult.addSource(mLiveData_diseaseAnal, this::updateDiseaseAnal);

        mLiveData_Temperature_Elec.addSource(mLiveData_bleDeviceInfo,this::updateBleDeviceInfo);
        mLiveData_Temperature_Elec.addSource(mLiveData_curve,this::updateStandardCurveDegree);
        mLiveData_Temperature_Elec.addSource(mLiveData_centralTemperature, this::updateDegree);
        mLiveData_Temperature_Elec.addSource(mLiveData_CO, this::updateCOTemperature);
        mLiveData_Temperature_Elec.addSource(mLiveData_diseaseAnal, this::updateDiseaseAnalTemperature);
    }

    private void updateDiseaseAnalTemperature(String s) {
        Temperature_Elec temperature_elec = mLiveData_Temperature_Elec.getValue();
        if (temperature_elec == null) {
            temperature_elec = new Temperature_Elec();
        }
        if (s!=null){
            temperature_elec.setDiseaseAnal(s);
            Log.d(TAG, "当前病害分析："+s);
        }
        mLiveData_Temperature_Elec.setValue(temperature_elec);
    }

    private void updateCOTemperature(Float aFloat) {
        Temperature_Elec temperature_elec = mLiveData_Temperature_Elec.getValue();
        if (temperature_elec == null) {
            temperature_elec = new Temperature_Elec();
        }
        if (aFloat!=null){
            temperature_elec.setDetectionCo(aFloat);
            Log.d(TAG, "当前浓度："+aFloat);
        }
        mLiveData_Temperature_Elec.setValue(temperature_elec);
    }

    private void updateStandardCurveDegree(StandardCurve curve) {
        Temperature_Elec temperature_elec = mLiveData_Temperature_Elec.getValue();
        if (temperature_elec == null) {
            temperature_elec = new Temperature_Elec();
        }
        if (curve!=null) {
            temperature_elec.setStandard_curve_id(curve.getId());
            temperature_elec.setStandardCurve(curve);
            Log.d(TAG, "degree 当前使用曲线id："+curve.getId());
        }
        mLiveData_Temperature_Elec.setValue(temperature_elec);
    }

    private void updateDegree(Float aFloat) {
        Temperature_Elec temperature_elec = mLiveData_Temperature_Elec.getValue();
        if (temperature_elec == null) {
            temperature_elec = new Temperature_Elec();
        }
        if (aFloat!=null){
            temperature_elec.setTemperature(aFloat);
            Log.d(TAG, "当前温度："+aFloat);
        }
        mLiveData_Temperature_Elec.setValue(temperature_elec);
    }
    public void updateBleDeviceInfo(BleDeviceInfo bleDeviceInfo) {
        Temperature_Elec result = mLiveData_Temperature_Elec.getValue();
        if (result == null) {
            result = new Temperature_Elec();
        }
        if (bleDeviceInfo!=null) {
            result.setBleDeviceInfo(bleDeviceInfo);
            Log.d(TAG, "当前设备信息："+bleDeviceInfo.toString());
        }
        mLiveData_Temperature_Elec.setValue(result);
    }
    private  void updateThermalBitmap(Bitmap bitmap) {
        ThermalTestResult thermalTestResult = mLiveData_ThermalTestResult.getValue();
        if (thermalTestResult == null) {
            thermalTestResult = new ThermalTestResult();
        }
        thermalTestResult.setThermalBitmap(bitmap);
        mLiveData_ThermalTestResult.setValue(thermalTestResult);
    }
    private  void updateCurve(StandardCurve curve) {
        ThermalTestResult thermalTestResult = mLiveData_ThermalTestResult.getValue();
        if (thermalTestResult == null) {
            thermalTestResult = new ThermalTestResult();
        }
        thermalTestResult.setStandard_curve_id(curve.getId());
        thermalTestResult.setStandardCurve(curve);
        mLiveData_ThermalTestResult.setValue(thermalTestResult);
    }
    private  void updateCentralTemperature(Float centralTemperature) {
        ThermalTestResult thermalTestResult = mLiveData_ThermalTestResult.getValue();
        if (thermalTestResult == null) {
            thermalTestResult = new ThermalTestResult();
        }
        thermalTestResult.setCentralTemperature(centralTemperature);
        mLiveData_ThermalTestResult.setValue(thermalTestResult);
    }
    private  void updateCO(Float CO) {
        ThermalTestResult thermalTestResult = mLiveData_ThermalTestResult.getValue();
        if (thermalTestResult == null) {
            thermalTestResult = new ThermalTestResult();
        }
        thermalTestResult.setDetectionCo(CO);
        mLiveData_ThermalTestResult.setValue(thermalTestResult);
    }
    private  void updateDiseaseAnal(String diseaseAnal) {
        ThermalTestResult thermalTestResult = mLiveData_ThermalTestResult.getValue();
        if (thermalTestResult == null) {
            thermalTestResult = new ThermalTestResult();
        }
        thermalTestResult.setDiseaseAnal(diseaseAnal);
        mLiveData_ThermalTestResult.setValue(thermalTestResult);
    }

    public void setThermalTestResult(ThermalTestResult thermalTestResult) {
        mLiveData_ThermalTestResult.setValue(thermalTestResult);
    }
    public void setThermalBitmap(Bitmap bitmap) {
        mLiveData_thermalBitmap.setValue(bitmap);
    }
    //计算中心温度
    public Float calculateCentralTemperature(Bitmap bitmap) {
        // 算法实现
        //...
        Float temperature = 0.0f;
        mLiveData_centralTemperature.setValue(temperature);
        calculateCO(temperature);
        return temperature;
    }
    //通过直线计算浓度
    public void calculateCO(Float temperature) {
        StandardCurve curve = mLiveData_curve.getValue();
        if (curve != null && temperature!=null){
            Float CO = curve.calculateX_toY(temperature);
            mLiveData_CO.setValue(CO);
            //noticeCO(CO,curve,0);
        }
    }
    //病害分析
    public void startDiseaseAnal(){
        Float CO = mLiveData_CO.getValue();
        if (CO!=null){
            String result = "病害分析+"+CO;
            // 算法实现
            //...
            mLiveData_diseaseAnal.setValue(result);
        }
    }

    public Boolean isFinishTest() {
        if (mLiveData_diseaseAnal.getValue() == null)
            return false;
        else
            return true;
    }
    
    public void setTemperature(Float temperature){
        mLiveData_centralTemperature.setValue(temperature);
        calculateCO(temperature);
    }
   @Transaction
   @SuppressLint("SimpleDateFormat")
   public boolean save(){
       //ThermalTestResult thermalTestResult = mLiveData_ThermalTestResult.getValue();
       Temperature_Elec temperature_elec = mLiveData_Temperature_Elec.getValue();
       if ( temperature_elec != null){
           // 获取当前时间(保存时间)
           Date currentTime = new Date();
           SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
           String dateString = formatter.format(currentTime);
          /* thermalTestResult.setDateTime(dateString);
           mLiveData_ThermalTestResult.setValue(thermalTestResult);
           DataRepository.getInstance().setThermalTestResult(thermalTestResult);//保存到数据仓库*/

           temperature_elec.setDateTime(dateString);
           Log.d(TAG, "温度保存时间："+dateString);
           mLiveData_Temperature_Elec.setValue(temperature_elec);
           DataRepository.getInstance().setTemperature_Elec(temperature_elec);//保存到数据仓库
           setToast(getString(R.string.toast_tempSave_success));
           mLiveData_toast.setValue("暂存成功！");
           return true;
       }
       setToast(getString(R.string.toast_tempSave_fail));
       return false;
   }

    public void clearAll() {
        // 移除所有数据源
        mLiveData_ThermalTestResult.removeSource(mLiveData_thermalBitmap);
        mLiveData_ThermalTestResult.removeSource(mLiveData_curve);
        mLiveData_ThermalTestResult.removeSource(mLiveData_centralTemperature);
        mLiveData_ThermalTestResult.removeSource(mLiveData_CO);
        mLiveData_ThermalTestResult.removeSource(mLiveData_diseaseAnal);
        mLiveData_ThermalTestResult.removeSource(mLiveData_toast);

        mLiveData_Temperature_Elec.removeSource(mLiveData_curve);
        mLiveData_Temperature_Elec.removeSource(mLiveData_centralTemperature);
        mLiveData_Temperature_Elec.removeSource(mLiveData_bleDeviceInfo);
        mLiveData_Temperature_Elec.removeSource(mLiveData_CO);
        mLiveData_Temperature_Elec.removeSource(mLiveData_diseaseAnal);
        // 清空每个数据源的值
        mLiveData_curve.setValue(null);
        mLiveData_centralTemperature.setValue(null);
        mLiveData_bleDeviceInfo.setValue(null);
        mLiveData_CO.setValue(null);
        mLiveData_diseaseAnal.setValue(null);
        mLiveData_thermalBitmap.setValue(null);
        mLiveData_toast.setValue(null);
        // 将 mLiveData_ThermalTestResult 的值设置为 null 或默认值
        mLiveData_Temperature_Elec.setValue(null);
        mLiveData_ThermalTestResult.setValue(null);
    }

    public void setBleDeviceInfo_Elec(BleDeviceInfo bleDeviceInfo) {
        mLiveData_bleDeviceInfo.setValue(bleDeviceInfo);
    }
    public void setStandardCurve(StandardCurve standardCurve) {
        mLiveData_curve.setValue(standardCurve);
        calculate_DegreeCO(); // 计算出当前温度对应的浓度
    }
    /***
     * 使用温度算浓度
     * @return 对应浓度
     */
    public void calculate_DegreeCO(){
        StandardCurve curve = mLiveData_curve.getValue();
        Float temperature = mLiveData_centralTemperature.getValue(); // 获取当前测试值
        if (curve != null && temperature!=null){
            Float CO = curve.calculateX_toY(temperature);
            mLiveData_CO.setValue(CO);
            // 病害分析
            startDiseaseAnal();
            //noticeCO(CO,curve,1);
        }
    }

    /***
     * 获取温度测试曲线浓度X单位
     * @return 单位
     */
    public String getUnit() {
        StandardCurve curve = mLiveData_curve.getValue();
        if (curve != null) {
            return curve.getX_axis_unit();
        }
        return "";
    }

    public void setDiseaseAnal(String diseaseAnal) {
        mLiveData_diseaseAnal.setValue(diseaseAnal);
    }

    public String getDiseaseAnal() {
        return mLiveData_diseaseAnal.getValue();
    }

    public void setCO(Float detectionCo) {
        mLiveData_CO.setValue(detectionCo);
    }
}
