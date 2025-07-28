package com.lsy.chemicaltest_new.models;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.room.Transaction;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.domain.CurveSetting;
import com.lsy.chemicaltest_new.utils.LiveDataUtils;

public class CurveSettingViewModel extends ViewModel {
    MutableLiveData<CurveSetting> liveData_curveSetting = new MutableLiveData<>();
    MutableLiveData<String> mLiveData_toast = new MutableLiveData<>();

    public MutableLiveData<CurveSetting> getLiveData_curveSetting() {
        return liveData_curveSetting;
    }

    public MutableLiveData<String> getmLiveData_toast() {
        return mLiveData_toast;
    }

    public void setToast(String toast) {
        LiveDataUtils.safeUpdate(mLiveData_toast,toast);
    }

    public void setCurveSetting(CurveSetting curveSetting) {
        liveData_curveSetting.setValue(curveSetting);
    }

    public CurveSetting getCurveSetting() {
        return liveData_curveSetting.getValue();
    }

    public interface SaveCurveSettingCallback{
        void onSaveSuccess();
        void onSaveFailed();
    }
    @Transaction
    public void save(CurveSetting curveSetting, SaveCurveSettingCallback callback) {
        try{
            MyApplication.DATABASE_INSTANCE.getCurveSettingDao().update(curveSetting);
        }
        catch (Exception e){
            if (callback != null)
                callback.onSaveFailed();
        }
        if (callback != null){
            callback.onSaveSuccess();
            liveData_curveSetting.postValue(curveSetting);
        }

    }
}
