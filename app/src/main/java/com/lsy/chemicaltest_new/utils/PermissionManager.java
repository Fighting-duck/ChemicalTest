package com.lsy.chemicaltest_new.utils;

import static com.lsy.chemicaltest_new.utils.DynamicStringUtils.getString;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import android.Manifest;

import java.util.List;

import android.widget.Toast;

import com.lsy.chemicaltest_new.R;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public class PermissionManager implements EasyPermissions.PermissionCallbacks {
    // 存储权限相关常量
    private static final int REQUEST_MANAGE_STORAGE = 1001;
    private static final int REQUEST_WRITE_STORAGE = 1002;
    // 蓝牙权限相关常量
    private static final int RC_BLE_PERMISSIONS  = 2001;
    private final Activity activity;
    private final PermissionCallback callback;

    // 权限提示信息资源ID
    private final int deniedMessage = R.string.toast_permission_deny;
    private final int dialogTitle;//提示框标题
    private final int dialogRationale;//提示框内容

    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied();
    }

    public PermissionManager(Activity activity, PermissionCallback callback,
                             int dialogTitle, int dialogRationale) {
        this.activity = activity;
        this.callback = callback;
        this.dialogTitle = dialogTitle;
        this.dialogRationale = dialogRationale;
    }
    // =================== 存储权限 ===================
    public void checkAndRequestExportPermissions() {
        if (!StorageUtils.hasEnoughSpace(50)) {
            Toast.makeText(activity, getString(R.string.toast_insufficientSspace), Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                callback.onPermissionGranted();
            } else {
                requestManageExternalStorage();
            }
        } else {
            if (EasyPermissions.hasPermissions(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                callback.onPermissionGranted();
            } else {
                EasyPermissions.requestPermissions(
                        activity,
                        activity.getString(deniedMessage),
                        REQUEST_WRITE_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                );
            }
        }
    }
    // =================== 蓝牙权限 ===================
    public void checkBluetoothPermissions() {
        String[] perms = getRequiredBluetoothPermissions();
        if (EasyPermissions.hasPermissions(activity, perms)) {
            callback.onPermissionGranted();
        } else {
            requestBluetoothPermissions();
        }
    }
    @AfterPermissionGranted(RC_BLE_PERMISSIONS)
    private void requestBluetoothPermissions() {
        String[] perms = getRequiredBluetoothPermissions();
        String rationale = getBluetoothRationale();
        if (EasyPermissions.hasPermissions(activity, perms)) {
            callback.onPermissionGranted();
        } else {
            EasyPermissions.requestPermissions(
                    new PermissionRequest.Builder(activity, RC_BLE_PERMISSIONS, perms)
                            .setRationale(rationale)
                            .setPositiveButtonText(activity.getString(R.string.permission_dialog_positive_continue))
                            .setNegativeButtonText(activity.getString(R.string.permission_dialog_negative))
                            .build());
        }
    }
    private String[] getRequiredBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        } else {
            return new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        }
    }
    private String getBluetoothRationale() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? activity.getString(dialogRationale)
                : activity.getString(R.string.toast_save_fail_exception);//
    }
    @RequiresApi(api = Build.VERSION_CODES.R)
    private void requestManageExternalStorage() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
        activity.startActivityForResult(intent, REQUEST_MANAGE_STORAGE);
    }
    // =================== 统一处理回调 ===================
    public void handleActivityResult(int requestCode) {
        if (requestCode == REQUEST_MANAGE_STORAGE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                callback.onPermissionGranted();
            } else {
                callback.onPermissionDenied();
                Toast.makeText(activity, activity.getString(deniedMessage), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void handleRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == RC_BLE_PERMISSIONS) {
            callback.onPermissionGranted();
        } else if (requestCode == REQUEST_WRITE_STORAGE) {
            callback.onPermissionGranted();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == RC_BLE_PERMISSIONS) {
            //Toast.makeText(activity, activity.getString(blePermissionsDeniedMessage), Toast.LENGTH_SHORT).show();
        }else if (requestCode == REQUEST_WRITE_STORAGE) {
            Toast.makeText(activity, activity.getString(deniedMessage), Toast.LENGTH_SHORT).show();
        }
        callback.onPermissionDenied();
        if (EasyPermissions.somePermissionPermanentlyDenied(activity, perms)) {
            showPermissionDeniedDialog();
        }
    }

    private void showPermissionDeniedDialog() {
        new AppSettingsDialog.Builder(activity)
                .setTitle(activity.getString(dialogTitle))
                .setRationale(activity.getString(dialogRationale))
                .setPositiveButton(activity.getString(R.string.permission_dialog_positive))
                .setNegativeButton(activity.getString(R.string.permission_dialog_negative))
                .build()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Empty implementation - already handled by EasyPermissions
    }
}
