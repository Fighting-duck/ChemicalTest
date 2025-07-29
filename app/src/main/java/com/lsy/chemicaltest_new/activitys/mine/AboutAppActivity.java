package com.lsy.chemicaltest_new.activitys.mine;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.lsy.chemicaltest_new.BuildConfig;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.databinding.ActivityAboutAppBinding;
import com.lsy.chemicaltest_new.utils.ExportUtils;
import com.lsy.chemicaltest_new.utils.FileViewerUtils;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class AboutAppActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private ActivityAboutAppBinding mBinding;
    private static final String TAG = "AboutAppActivity";
    private Context mContext;
    // 需要请求的所有存储权限
    private static final String[] STORAGE_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 100;
    private static final int MY_PERMISSIONS_REQUEST_MANAGE_STORAGE = 10;
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
            if (requestPermissions())
                FileViewerUtils.openFileFromAssets(this, mAssetFileName_userGuide,"用户指南");
        }
        else if (id == mBinding.flCurveImportExample.getId()) {
            if (requestPermissions())
                FileViewerUtils.openFileFromAssets(this, mAssetFileName_curveImportExample,"标准曲线导入样例");
        }
    }

    //请求权限并导出
    private boolean requestPermissions() {
        // 检查是否已拥有权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 10 及以上版本，使用分区存储
            if (Environment.isExternalStorageManager()) {
               return true;
            } else {
                // 请求管理所有文件的权限
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, MY_PERMISSIONS_REQUEST_MANAGE_STORAGE);
            }
        } else {
            // Android 10 以下版本，使用传统存储权限
            String[] requiredPermissions = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ?
                    STORAGE_PERMISSIONS : new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
            if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
               return true;
            } else {
                // 请求权限
                EasyPermissions.requestPermissions(
                        this,
                        getString(R.string.toast_permission_write_storage),  // 权限被拒绝,  // 权限请求的解释说明
                        MY_PERMISSIONS_REQUEST_WRITE_STORAGE,
                        requiredPermissions
                );
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_PERMISSIONS_REQUEST_MANAGE_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(mContext, getString(R.string.toast_permission_grantPermissions), Toast.LENGTH_SHORT).show();
                } else {
                    // 权限被拒绝，提示用户
                    Toast.makeText(this,
                            getString(R.string.toast_permission_write_storage_deny),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 使用 EasyPermissions 处理权限请求的结果
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        // 权限被授予
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_STORAGE) {
            Toast.makeText(mContext, getString(R.string.toast_permission_grantPermissions), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        // 权限被拒绝
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_STORAGE) {
            Toast.makeText(this, getString(R.string.toast_permission_deny), Toast.LENGTH_SHORT).show();
            // 如果用户永久拒绝了权限，可以提示用户手动开启权限
            if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                new AppSettingsDialog.Builder(this)
                        .setTitle(getString(R.string.permission_dialog_title))
                        .setRationale(getString(R.string.permission_dialog_rational_storage))
                        .setPositiveButton(getString(R.string.permission_dialog_positive))
                        .setNegativeButton(getString(R.string.permission_dialog_negative))
                        .build()
                        .show();
            }
        }
    }
}