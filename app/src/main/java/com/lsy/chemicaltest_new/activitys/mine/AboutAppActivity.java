package com.lsy.chemicaltest_new.activitys.mine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.lsy.chemicaltest_new.BuildConfig;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.databinding.ActivityAboutAppBinding;
import com.lsy.chemicaltest_new.utils.FileViewerUtils;
import com.lsy.chemicaltest_new.utils.PermissionManager;

public class AboutAppActivity extends AppCompatActivity{
    private ActivityAboutAppBinding mBinding;
    private static final String TAG = "AboutAppActivity";
    private Context mContext;
    private PermissionManager permissionManager;
    private String mAssetFileName_userGuide;
    private String mAssetFileName_curveImportExample;
    private enum Action {
        OPEN_USER_GUIDE,
        OPEN_CURVE_EXAMPLE
    }
    private Action currentAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityAboutAppBinding.inflate(getLayoutInflater());
        mContext = this;
        setContentView(mBinding.getRoot());
        mAssetFileName_userGuide = mContext.getString(R.string.file_FileName_userGuide);
        mAssetFileName_curveImportExample = mContext.getString(R.string.file_FileName_curveImportExample);
        permissionManager = new PermissionManager(this, new PermissionManager.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                if (currentAction == Action.OPEN_USER_GUIDE) {
                    FileViewerUtils.openFileFromAssets(AboutAppActivity.this,
                            mAssetFileName_userGuide, "用户指南");
                } else if (currentAction == Action.OPEN_CURVE_EXAMPLE) {
                    FileViewerUtils.openFileFromAssets(AboutAppActivity.this,
                            mAssetFileName_curveImportExample, "标准曲线导入样例");
                }
            }

            @Override
            public void onPermissionDenied() {

            }
        },
                R.string.permission_dialog_title,
                R.string.permission_dialog_rational_storage);
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
            currentAction = Action.OPEN_USER_GUIDE;
            permissionManager.checkAndRequestExportPermissions(); // 50MB 所需空间
        }
        else if (id == mBinding.flCurveImportExample.getId()) {
            currentAction = Action.OPEN_CURVE_EXAMPLE;
            permissionManager.checkAndRequestExportPermissions(); // 50MB 所需空间
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        permissionManager.handleActivityResult(requestCode);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManager.handleRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}