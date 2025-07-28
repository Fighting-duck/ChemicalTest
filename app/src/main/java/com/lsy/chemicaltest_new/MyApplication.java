package com.lsy.chemicaltest_new;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.blankj.utilcode.util.Utils;
import com.lsy.chemicaltest_new.database.AppDatabase;
import com.lsy.chemicaltest_new.domain.CurveSetting;
import com.lsy.chemicaltest_new.domain.Experimenter;
import com.lsy.chemicaltest_new.domain.language.ContextWrapper;
import com.lsy.chemicaltest_new.database.SharePreferencesManager;
import com.lsy.chemicaltest_new.utils.DynamicStringUtils;
import com.lsy.chemicaltest_new.utils.SavedDisplay;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    public static MyApplication INSTANCE;//得到Application唯一实例
    // 使用静态线程池避免频繁创建,避免 OOM 风险
    public static ExecutorService DB_EXECUTOR = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() // 根据CPU核心数动态设置
    );
    private List<SavedDisplay> displays = new LinkedList<>();
    public static AppDatabase DATABASE_INSTANCE;//数据库
    private boolean isSaveColoOriginalImage = false;
    private boolean isUpdateHistory = true;//用户是否更新了历史记录（删除更新）
    private boolean isUpdateSample = true;//用户是否更新了样本（添加或删除）


    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        DATABASE_INSTANCE = AppDatabase.getInstance(this);
        setDefaultProperties();

        // Application 中初始化
        DynamicStringUtils.init(Utils.getApp());

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // 关闭线程池
        DB_EXECUTOR.shutdown(); // 停止接受新任务

        try {
            // 等待现有任务完成（最大30秒）
            if (!DB_EXECUTOR.awaitTermination(30, TimeUnit.SECONDS)) {
                DB_EXECUTOR.shutdownNow(); // 强制终止
            }
        } catch (InterruptedException e) {
            // 如果等待过程中被中断，也强制关闭
            DB_EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        SharePreferencesManager sharePreferencesManager = new SharePreferencesManager(base);
        String languageCode = sharePreferencesManager.getLanguage();
        String userName = sharePreferencesManager.getUserName();
        // 如果语言设置为空，默认使用中文
        if (languageCode == null || languageCode.isEmpty())
            languageCode = SharePreferencesManager.LANGUAGE_CODE_CH;
        // 如果用户名未设置，初始化为 "Experimenter"
        if (userName == null || userName.isEmpty())
            sharePreferencesManager.setUserName("Experimenter");
        Log.d(TAG, "用户名: " + sharePreferencesManager.getUserName());
        super.attachBaseContext(ContextWrapper.wrap(base, languageCode));
    }

    public void setDefaultProperties(){
        // 添加曲线设置
       Integer count = DATABASE_INSTANCE.getCurveSettingDao().getCount();
       if (count == 0){
           CurveSetting curveSetting = new CurveSetting();
           curveSetting.setX_axis_unit(getString(R.string.unit_mol));
           curveSetting.setMin_CO(0.0f);
           curveSetting.setMax_CO(10.0f);
           curveSetting.setMinCorr(90.0f);
           DATABASE_INSTANCE.getCurveSettingDao().insert(curveSetting);
       }
       else
           Log.d(TAG, "setCurveDefaultProperties: " + count);
    }

    public boolean isUpdateHistory() {
        return isUpdateHistory;
    }
    public void setUpdateHistory(boolean updateHistory) {
        isUpdateHistory = updateHistory;
    }

    public boolean isUpdateSample() {
        return isUpdateSample;
    }
    public void setUpdateSample(boolean updateSample) {
        isUpdateSample = updateSample;
    }

    public boolean getIsSaveColoOriginalImage() {
        return isSaveColoOriginalImage;
    }
    public void setIsSaveColoOriginalImage(boolean isSaveColoOriginalImage) {
        this.isSaveColoOriginalImage = isSaveColoOriginalImage;
    }
}


