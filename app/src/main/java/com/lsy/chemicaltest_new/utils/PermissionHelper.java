package com.lsy.chemicaltest_new.utils;

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

public class PermissionHelper {
    private static final String TAG = "PermissionHelper";
    private static final SparseArray<WeakReference<PermissionCallback>> callbackMap = new SparseArray<>();
    private static int requestCodeCounter = 1000;

    public interface PermissionCallback {
        void onPermissionsGranted();
        void onPermissionsDenied();

        default void onShowRationale(List<String> permissions) {
            // 默认实现：不处理权限解释逻辑
        }
    }

    /**
     * 请求一组权限
     */
    public static void requestPermissions(@NonNull Activity activity,
                                          @NonNull String[] permissions,
                                          @NonNull PermissionCallback callback) {
        // 1. 参数校验
        if (activity.isFinishing() || activity.isDestroyed()) {
            Log.w(TAG, "Activity is finishing or destroyed");
            return;
        }

        if (permissions.length == 0) {
            Log.w(TAG, "Empty permissions array");
            return;
        }

        // 2. 检查已授权权限
        List<String> deniedPermissions = new ArrayList<>();
        List<String> rationalePermissions = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission);

                // 检查是否需要展示权限解释
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    rationalePermissions.add(permission);
                }
            }
        }

        // 3. 所有权限已授权
        if (deniedPermissions.isEmpty()) {
            callback.onPermissionsGranted();
            return;
        }

        // 4. 需要先解释权限用途
        if (!rationalePermissions.isEmpty()) {
            callback.onShowRationale(rationalePermissions);
            return;
        }

        // 5. 处理特殊权限
        int requestCode = generateRequestCode();
        callbackMap.put(requestCode, new WeakReference<>(callback));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                deniedPermissions.contains(Manifest.permission.MANAGE_EXTERNAL_STORAGE)) {
            handleManageExternalStorage(activity, requestCode);
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            handleNewMediaPermissions(activity, deniedPermissions, requestCode);
        }
        else {
            ActivityCompat.requestPermissions(
                    activity,
                    deniedPermissions.toArray(new String[0]),
                    requestCode
            );
        }
    }

    /**
     * 处理权限请求结果
     */
    public static void onRequestPermissionsResult(@NonNull Activity activity,
                                                  int requestCode,
                                                  @NonNull String[] permissions,
                                                  @NonNull int[] grantResults) {
        WeakReference<PermissionCallback> callbackRef = callbackMap.get(requestCode);
        if (callbackRef == null) {
            Log.d(TAG, "No callback found for requestCode: " + requestCode);
            return;
        }

        PermissionCallback callback = callbackRef.get();
        if (callback == null || activity.isFinishing()) {
            callbackMap.remove(requestCode);
            return;
        }

        // 检查是否全部授权
        boolean allGranted = true;
        List<String> permanentlyDenied = new ArrayList<>();

        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        activity, permissions[i])) {
                    permanentlyDenied.add(permissions[i]);
                }
            }
        }

        // 处理结果
        if (allGranted) {
            callback.onPermissionsGranted();
        } else {
            if (!permanentlyDenied.isEmpty()) {
                openAppSettings(activity);
            }
            callback.onPermissionsDenied();
        }

        callbackMap.remove(requestCode);
    }

    /**
     * 处理Activity返回结果（用于MANAGE_EXTERNAL_STORAGE）
     */
    public static void onActivityResult(@NonNull Activity activity,
                                        int requestCode,
                                        int resultCode,
                                        Intent data) {
        WeakReference<PermissionCallback> callbackRef = callbackMap.get(requestCode);
        if (callbackRef == null) return;

        PermissionCallback callback = callbackRef.get();
        if (callback == null || activity.isFinishing()) {
            callbackMap.remove(requestCode);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                callback.onPermissionsGranted();
            } else {
                callback.onPermissionsDenied();
            }
        }
        callbackMap.remove(requestCode);
    }

    // ==================== 私有方法 ====================

    private static int generateRequestCode() {
        // 确保requestCode不会溢出
        if (requestCodeCounter >= Integer.MAX_VALUE - 100) {
            requestCodeCounter = 1000;
        }
        return ++requestCodeCounter;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private static void handleManageExternalStorage(Activity activity, int requestCode) {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
            intent.setData(uri);
            activity.startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            Log.e(TAG, "Failed to open MANAGE_ALL_FILES_ACCESS settings", e);
            WeakReference<PermissionCallback> callbackRef = callbackMap.get(requestCode);
            if (callbackRef != null && callbackRef.get() != null) {
                callbackRef.get().onPermissionsDenied();
            }
            callbackMap.remove(requestCode);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private static void handleNewMediaPermissions(Activity activity,
                                                  List<String> deniedPermissions,
                                                  int requestCode) {
        List<String> mediaPermissions = new ArrayList<>();
        for (String permission : deniedPermissions) {
            if (isNewMediaPermission(permission)) {
                mediaPermissions.add(permission);
            }
        }

        if (!mediaPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    activity,
                    mediaPermissions.toArray(new String[0]),
                    requestCode
            );
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private static boolean isNewMediaPermission(String permission) {
        return permission.equals(Manifest.permission.READ_MEDIA_IMAGES) ||
                permission.equals(Manifest.permission.READ_MEDIA_VIDEO) ||
                permission.equals(Manifest.permission.READ_MEDIA_AUDIO);
    }

    private static void openAppSettings(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.fromParts("package", context.getPackageName(), null));
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to open app settings", e);
        }
    }
}