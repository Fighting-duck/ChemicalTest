package com.lsy.chemicaltest_new.activitys;

import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.domain.language.ContextWrapper;
import com.lsy.chemicaltest_new.database.SharePreferencesManager;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        SharePreferencesManager sharePreferencesManager = new SharePreferencesManager(newBase);
        String languageCode = sharePreferencesManager.getLanguage();
        super.attachBaseContext(ContextWrapper.wrap(newBase, languageCode));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        // 在BaseActivity的onCreate中添加：
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);// 清除半透明状态栏标志
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);// 允许绘制状态栏背景
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));// 设置状态栏颜色（需要 API 21+）
    }
}

