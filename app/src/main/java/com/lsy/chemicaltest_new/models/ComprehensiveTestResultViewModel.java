package com.lsy.chemicaltest_new.models;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.domain.History_multiple;
import com.lsy.chemicaltest_new.utils.LiveDataUtils;

public class ComprehensiveTestResultViewModel extends ViewModel {
    MutableLiveData<History_multiple> mLiveData_history = new MutableLiveData<>();
    MutableLiveData<String> mLiveData_toast = new MutableLiveData<>();
    MutableLiveData<Boolean> mLiveData_elec_isNormal = new MutableLiveData<>();
    MutableLiveData<Boolean> mLiveData_colo_isNormal = new MutableLiveData<>();
    MutableLiveData<Boolean> mLiveData_thermal_isNormal = new MutableLiveData<>();
    Context mContext;
    public void setContext(Context context){
        mContext = context;
    }
    public LiveData<History_multiple> getLiveData_history() {
        return mLiveData_history;
    }
    public MutableLiveData<String> getLiveData_toast(){
        return mLiveData_toast;
    }
    public LiveData<Boolean> getLiveData_elec_isNormal(){
        return mLiveData_elec_isNormal;
    }
    public LiveData<Boolean> getLiveData_colo_isNormal(){
        return mLiveData_colo_isNormal;
    }
    public LiveData<Boolean> getLiveData_thermal_isNormal(){
        return mLiveData_thermal_isNormal;
    }

    public void setHistory(History_multiple history) {
        mLiveData_history.setValue(history);
    }
    public void setToast(String prompt){
        LiveDataUtils.safeUpdate(mLiveData_toast,prompt);
    }

    public void clearAll() {
        mLiveData_history.setValue(null);
    }

    public void isNormal(Float co, Float co_min, Float co_max,Integer type){
        // 参数验证
        if (co==null || co_min==null || co_max==null || type==null){
            return;
        }
        // 判断是否是正常
        boolean isNormal = co >= co_min && co <= co_max;
        switch (type){
            case 1:
                mLiveData_elec_isNormal.setValue(isNormal);
                break;
            case 2:
                mLiveData_colo_isNormal.setValue(isNormal);
                break;
            case 3:
                mLiveData_thermal_isNormal.setValue(isNormal);
                break;
        }
    }

    public void toastIsNormal(int i) {
        Boolean isNormal = null;
        switch (i){
            case 1:
                isNormal = mLiveData_elec_isNormal.getValue();
                break;
            case 2:
                isNormal = mLiveData_colo_isNormal.getValue();
                break;
            case 3:
                isNormal = mLiveData_thermal_isNormal.getValue();
        }
        if (isNormal!=null){
            if (isNormal)
                setToast(mContext.getString(R.string.toast_normal));
            else
                setToast(mContext.getString(R.string.toast_abnormal));
        }
    }
}
