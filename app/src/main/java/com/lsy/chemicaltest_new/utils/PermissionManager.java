package com.lsy.chemicaltest_new.utils;

import static com.lsy.chemicaltest_new.utils.DynamicStringUtils.getString;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.lsy.chemicaltest_new.R;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import java.util.List;

public class PermissionManager implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_MANAGE_STORAGE = 1001;
    private static final int REQUEST_WRITE_STORAGE = 1002;

    private final Activity activity;
    private final PermissionCallback callback;
    private final int manageStorageDeniedMessage;
    private final int writeStorageDeniedMessage;
    private final int dialogTitle;
    private final int dialogRationale;

    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied();
    }

    public PermissionManager(Activity activity, PermissionCallback callback,
                             int manageStorageDeniedMessage, int writeStorageDeniedMessage,
                             int dialogTitle, int dialogRationale) {
        this.activity = activity;
        this.callback = callback;
        this.manageStorageDeniedMessage = manageStorageDeniedMessage;
        this.writeStorageDeniedMessage = writeStorageDeniedMessage;
        this.dialogTitle = dialogTitle;
        this.dialogRationale = dialogRationale;
    }

    public void checkAndRequestExportPermissions(Context mContext) {
        if (StorageUtils.hasEnoughSpace(50)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    callback.onPermissionGranted();
                } else {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    activity.startActivityForResult(intent, REQUEST_MANAGE_STORAGE);
                }
            } else {
                if (EasyPermissions.hasPermissions(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    callback.onPermissionGranted();
                } else {
                    EasyPermissions.requestPermissions(
                            activity,
                            activity.getString(writeStorageDeniedMessage),
                            REQUEST_WRITE_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    );
                }
            }
        }
        else
            Toast.makeText(mContext, getString(R.string.toast_insufficientSspace), Toast.LENGTH_SHORT).show();

    }

    public void handleActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_MANAGE_STORAGE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                callback.onPermissionGranted();
            } else {
                callback.onPermissionDenied();
                Toast.makeText(activity, activity.getString(manageStorageDeniedMessage), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void handleRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_WRITE_STORAGE) {
            callback.onPermissionGranted();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_WRITE_STORAGE) {
            callback.onPermissionDenied();
            Toast.makeText(activity, activity.getString(writeStorageDeniedMessage), Toast.LENGTH_SHORT).show();

            if (EasyPermissions.somePermissionPermanentlyDenied(activity, perms)) {
                showPermissionDeniedDialog();
            }
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
