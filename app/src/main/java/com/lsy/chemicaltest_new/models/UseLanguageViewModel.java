package com.lsy.chemicaltest_new.models;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lsy.chemicaltest_new.utils.LiveDataUtils;

public class UseLanguageViewModel extends ViewModel {
    // 数据状态枚举
    public enum LanguageState {
        CH,    // 中文
        EN,    // 英文
    }
    MutableLiveData<LanguageState> liveData_languageState = new MutableLiveData<>();
    MutableLiveData<String> liveData_toast = new MutableLiveData<>();

    public MutableLiveData<LanguageState> getLiveData_languageState() {
        return liveData_languageState;
    }
    public MutableLiveData<String> getLiveData_toast() {
        return liveData_toast;
    }

    public void setLanguageState(LanguageState languageState) {
        liveData_languageState.setValue(languageState);
    }
    public void setToast(String prompt) {
        LiveDataUtils.safeUpdate(liveData_toast, prompt);
    }
}
