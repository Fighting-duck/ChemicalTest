package com.lsy.chemicaltest_new.database;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Locale;

public class SharePreferencesManager {
    public static final String LANGUAGE_CODE_EN = "en";
    public static final String LANGUAGE_CODE_CH = "zh";
    //  使用  SharedPreferences 存储语言选择
    private final SharedPreferences sharedPreferences;

    public SharePreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
    }

    // 保存选中的语言
    public void setLanguage(String languageCode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("language", languageCode);
        editor.apply();
    }
    // 获取当前语言（默认返回系统语言）
    public String getLanguage() {
        return sharedPreferences.getString("language", Locale.getDefault().getLanguage());
    }
    // 保存实验员名
    public void setUserName(String userName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userName", userName);
        editor.apply();
    }
    // 获取实验员名
    public String getUserName() {
        return sharedPreferences.getString("userName", null);
    }
    // 保存头像路径
    public void setAvatarPath(String avatarPath) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("avatarPath", avatarPath);
        editor.apply();
    }
    // 获取头像路径
    public String getAvatarPath() {
        return sharedPreferences.getString("avatarPath", null);
    }
}

