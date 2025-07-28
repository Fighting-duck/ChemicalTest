package com.lsy.chemicaltest_new.models;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.room.Transaction;

import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.database.DataRepository;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.domain.ThermalTestResult;
import com.lsy.chemicaltest_new.utils.LiveDataUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ThermalViewModel extends AndroidViewModel {
    private MediatorLiveData<ThermalTestResult> mLiveData_ThermalTestResult = new MediatorLiveData<>();
    private MutableLiveData<StandardCurve> mLiveData_curve = new MutableLiveData<>();
    private MutableLiveData<Bitmap> mLiveData_thermalBitmap = new MutableLiveData<>();
    private MutableLiveData<Float> mLiveData_centralTemperature = new MutableLiveData<>();
    private MutableLiveData<Float> mLiveData_CO = new MutableLiveData<>();
    private MutableLiveData<String> mLiveData_diseaseAnal = new MutableLiveData<>();

    private MutableLiveData<String> mLiveData_toast = new MutableLiveData<>();
    Context mContext;
    public void setContext(Context context){
        mContext = context;
    }
    public MutableLiveData<String> getLiveData_toast(){
        return mLiveData_toast;
    }
    public void setToast(String prompt){
        LiveDataUtils.safeUpdate(mLiveData_toast,prompt);
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

    public void setStandardCurve(StandardCurve curve) {
        mLiveData_curve.setValue(curve);
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
    public Float calculateCO(Float x) {
        // 算法实现
        //...
        Float CO = 0.0f;
        mLiveData_CO.setValue(CO);
        return CO;
    }
    //病害分析
    public void diseaseAnal(){
        Float CO = mLiveData_CO.getValue();
        String result = "病害分析+"+CO;
        // 算法实现
        //...
        mLiveData_diseaseAnal.setValue(result);
    }

    public Boolean isFinishTest() {
        if (mLiveData_diseaseAnal.getValue() == null)
            return false;
        else
            return true;
    }

   /* @Transaction
    @SuppressLint("SimpleDateFormat")
    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean save(Context context){
        ThermalTestResult result = mLiveData_ThermalTestResult.getValue();
        assert result != null;
        //1.保存光热图像
        Bitmap thermalBitmap = mLiveData_thermalBitmap.getValue();
        String path = PhotoUtil.saveBitmapToFile(context,thermalBitmap,"thermal_");
        if (path!=null) {
            result.setThermalBitmap_path(path);
        }
        else return false;
        //2. 获取当前时间(保存时间)
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateString = formatter.format(currentTime);
        result.setDateTime(dateString);
        //3. 保存结果
        Long id = MyApplication.DATABASE_INSTANCE.getThermalTestResultDao().add(result);
        result.setId(Math.toIntExact(id));
        mLiveData_ThermalTestResult.setValue(result);
        return true;
    }*/
   @Transaction
   @SuppressLint("SimpleDateFormat")
   public boolean save(){
       ThermalTestResult result = mLiveData_ThermalTestResult.getValue();
       if ( result != null){
           // 获取当前时间(保存时间)
           Date currentTime = new Date();
           SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
           String dateString = formatter.format(currentTime);
           result.setDateTime(dateString);
           mLiveData_ThermalTestResult.setValue(result);
           DataRepository.getInstance().setThermalTestResult(result);//保存到数据仓库
           setToast(mContext.getString(R.string.toast_tempSave_success));
           mLiveData_toast.setValue("暂存成功！");
           return true;
       }
       setToast(mContext.getString(R.string.toast_tempSave_fail));
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
        // 清空每个数据源的值
        mLiveData_curve.setValue(null);
        mLiveData_centralTemperature.setValue(null);
        mLiveData_CO.setValue(null);
        mLiveData_diseaseAnal.setValue(null);
        mLiveData_thermalBitmap.setValue(null);
        mLiveData_toast.setValue(null);
        // 将 mLiveData_ThermalTestResult 的值设置为 null 或默认值
        mLiveData_ThermalTestResult.setValue(null);
    }
}
