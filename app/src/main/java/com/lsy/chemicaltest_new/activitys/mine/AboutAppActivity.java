package com.lsy.chemicaltest_new.activitys.mine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.lsy.chemicaltest_new.BuildConfig;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.databinding.ActivityAboutAppBinding;
import com.lsy.chemicaltest_new.utils.FileViewerUtils;

public class AboutAppActivity extends AppCompatActivity {
    private ActivityAboutAppBinding mBinding;
    private static final String TAG = "AboutAppActivity";
    private Context mContext;
    private static final String mAssetFileName_userGuide = "userGuide_2025_7_20.pdf";
    private static final String mAssetFileName_curveImportExample = "standCurve_import_example.xlsx";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityAboutAppBinding.inflate(getLayoutInflater());
        mContext = this;
        setContentView(mBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        uiInit();
    }

    @SuppressLint("SetTextI18n")
    private void uiInit() {
        mBinding.ivBack.setOnClickListener(v -> finish());
        mBinding.flUserGuide.setOnClickListener(this::onClick);
        mBinding.flCurveImportExample.setOnClickListener(this::onClick);

        //设置版本号
        String versionName = BuildConfig.VERSION_NAME; // 直接获取
        // String versionName = getVersion(); // 间接获取
        mBinding.tvVersion.setText("V " + versionName);
    }

    private String getVersion(){
        try {
            Context context = getApplicationContext();
            PackageManager packageManager = context.getPackageManager();
            String packageName = context.getPackageName();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void onClick(View view) {
        int id = view.getId();
        if (id == mBinding.flUserGuide.getId()) {
            FileViewerUtils.openFileFromAssets(this, mAssetFileName_userGuide,"用户指南");
        }
        else if (id == mBinding.flCurveImportExample.getId()) {
            FileViewerUtils.openFileFromAssets(this, mAssetFileName_curveImportExample,"标准曲线导入样例");
        }
    }
}