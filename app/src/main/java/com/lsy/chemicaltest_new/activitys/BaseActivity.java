package com.lsy.chemicaltest_new.activitys;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import com.lsy.chemicaltest_new.domain.language.ContextWrapper;
import com.lsy.chemicaltest_new.database.SharePreferencesManager;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        SharePreferencesManager sharePreferencesManager = new SharePreferencesManager(newBase);
        String languageCode = sharePreferencesManager.getLanguage();
        super.attachBaseContext(ContextWrapper.wrap(newBase, languageCode));
    }
}

