package com.lsy.chemicaltest_new.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.activitys.smpleTest.CropActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ImageProcessor {
    private static final String TAG = "ImageProcessor";
    private final Context context;
    private final BitmapCache bitmapCache;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private File currentPhotoFile;
    private ImageProcessingCallback currentCallback;
    private FragmentActivity activity;
    private final String authority;
    private final ActivityResultLauncher<String[]> requestMultiplePermissionsLauncher;


    public interface ImageProcessingCallback {
        void onImageSelected(Bitmap bitmap);
        void onError(String message);
        default void onPermissionDenied() {
            onError("权限被拒绝");
        }
    }
    public ImageProcessor(@NonNull FragmentActivity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.bitmapCache = BitmapCache.getInstance();
        this.authority = activity.getString(R.string.file_provider_authority);
        initializeActivityLaunchers(activity);

        // 初始化多权限请求Launcher
        requestMultiplePermissionsLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = !result.containsValue(false);
                    if (allGranted) {
                        // 所有权限都授予，继续执行之前被中断的操作
                        if (currentCallback != null) {
                            if (checkCameraPermission() && checkStoragePermission()) {
                                // 拍照所需的权限
                                takePhotoInternal();
                            } else if (checkReadStoragePermission()) {
                                // 相册选择所需的权限
                                pickFromGalleryInternal();
                            }
                        }
                    } else if (currentCallback != null) {
                        currentCallback.onPermissionDenied();
                    }
                });
    }
    private void initializeActivityLaunchers(FragmentActivity activity) {
        // 初始化相册选择
        galleryLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> handleGalleryResult(result.getResultCode(), result.getData())
        );

        // 初始化相机拍摄
        cameraLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> handleCameraResult(success)
        );
    }
    // ============== 权限检查方法 ==============
    /**
     * 检查相机权限
     */
    private boolean checkCameraPermission() {
        return activity != null &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED;
    }
    /**
     * 检查存储权限
     */
    private boolean checkStoragePermission() {
        if (activity == null) return false;
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED;
    }
    /**
     * 检查读取外部存储权限
     */
    private boolean checkReadStoragePermission() {
        if (activity == null) return false;
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED;
    }
    /**
     * 请求拍照和存储权限
     */
    private void requestCameraAndStoragePermissions() {
        if (activity == null) return;
        List<String> permissionsNeeded = new ArrayList<>();
        if (!checkCameraPermission()) {
            permissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (!checkStoragePermission() && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionsNeeded.isEmpty()) {
            requestMultiplePermissionsLauncher.launch(
                    permissionsNeeded.toArray(new String[0])
            );
        } else {
            takePhotoInternal();
        }
    }
    /**
     * 请求读取外部存储权限
     */
    private void requestReadStoragePermission() {
        if (activity == null) return;
        if (!checkReadStoragePermission() && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            requestMultiplePermissionsLauncher.launch(
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
            );
        } else {
            pickFromGalleryInternal();
        }
    }
    // ============== 公开API ==============
    public void takePhoto(ImageProcessingCallback callback) {
        this.currentCallback = callback;
        requestCameraAndStoragePermissions();
    }
    public void pickFromGallery(ImageProcessingCallback callback) {
        this.currentCallback = callback;
        requestReadStoragePermission();
    }
    // ============== 内部实现方法 ==============
    private void takePhotoInternal() {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            notifyError("Activity不可用");
            return;
        }
        currentPhotoFile = PhotoUtil.createImageFile(activity);
        if (currentPhotoFile != null) {
            Uri photoUri = FileProvider.getUriForFile(context, authority, currentPhotoFile);
            cameraLauncher.launch(photoUri);
        } else {
            notifyError("无法创建临时文件");
        }
    }
    private void pickFromGalleryInternal() {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            notifyError("Activity不可用");
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        } catch (Exception e) {
            notifyError("无法打开相册: " + e.getMessage());
        }
    }
    public void startCrop(Bitmap bitmap, ActivityResultLauncher<Intent> bitmapResultLauncher) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            Log.d(TAG, "上下文已销毁");
            return;
        }
        try {
            int bitmapId = bitmapCache.cache(bitmap);
            Intent intent = new Intent(activity, CropActivity.class);
            intent.putExtra("BITMAP_ID", bitmapId);
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            bitmapResultLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "图片处理失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
public void startCrop(Bitmap bitmap,ActivityResultLauncher<Intent> bitmapResultLauncher, ImageProcessingCallback callback) {
    if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
        if (callback != null) {
            Log.d(TAG, "上下文已销毁");
        }
        return;
    }
    try {
        int bitmapId = bitmapCache.cache(bitmap);
        Intent intent = new Intent(activity, CropActivity.class); // 使用activity作为上下文
        intent.putExtra("BITMAP_ID", bitmapId);

        // 添加过渡动画
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        bitmapResultLauncher.launch(intent);
        this.currentCallback = callback;
    } catch (Exception e) {
        if (callback != null) {
            callback.onError(context.getString(R.string.toast_takePhoto_startCropFail));
        }
    }
}

    public void handleActivityResult(int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            notifyError(context.getString(R.string.toast_takePhoto_canceleOperation));
            return;
        }

        try {
            if (data != null && data.hasExtra("RETURN_BITMAP_ID")) {
                // 处理裁剪返回
                int id = data.getIntExtra("RETURN_BITMAP_ID", -1);
                Bitmap bitmap = bitmapCache.getAndRemove(id);
                if (bitmap != null && currentCallback != null) {
                    currentCallback.onImageSelected(bitmap);
                } else {
                    notifyError(context.getString(R.string.toast_takePhoto_pictureOutDate));
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "图片处理失败: " + e.getMessage());
        }
    }

    // ============== 私有方法 ==============

    private void handleCameraResult(boolean success) {
        if (success && currentPhotoFile != null) {
            new Thread(() -> {
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2; // 降采样
                    Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoFile.getAbsolutePath(), options);
                    safeProcessBitmap(bitmap);
                } catch (OutOfMemoryError | Exception e) {
                    notifyError(context.getString(R.string.toast_takePhoto_pictureOversize));
                }
            }).start();
        } else {
            notifyError(context.getString(R.string.toast_takePhoto_fail));
        }
    }

    private void handleGalleryResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            new Thread(() -> {
                try {
                    Uri uri = data.getData();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                    safeProcessBitmap(bitmap);
                } catch (Exception e) {
                    Log.e(TAG, "图片加载失败: " + e.getMessage());
                    notifyError(context.getString(R.string.toast_takePhoto_loadFail));
                }
            }).start();
        } else {
            notifyError(context.getString(R.string.toast_takePhoto_noSelectImage));
        }
    }

    private void safeProcessBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled() && currentCallback != null) {
            activity.runOnUiThread(() -> currentCallback.onImageSelected(bitmap));
        } else if (currentCallback != null) {
            notifyError(context.getString(R.string.toast_takePhoto_loadFail));
        }
    }

    private void notifyError(String message) {
        if (currentCallback != null) {
            activity.runOnUiThread(() -> currentCallback.onError(message));
        }
    }

    // ============== Bitmap缓存 ==============

    public static class BitmapCache {
        private static BitmapCache instance;
        private final SparseArray<Bitmap> cache = new SparseArray<>();
        private int nextId = 1;

        public static synchronized BitmapCache getInstance() {
            if (instance == null) {
                instance = new BitmapCache();
            }
            return instance;
        }

        public synchronized int cache(Bitmap bitmap) {
            int id = nextId++;
            cache.put(id, bitmap);
            return id;
        }

        public synchronized Bitmap getAndRemove(int id) {
            Bitmap bitmap = cache.get(id);
            cache.remove(id);
            return bitmap;
        }

        public synchronized void clear() {
            for (int i = 0; i < cache.size(); i++) {
                Bitmap bitmap = cache.valueAt(i);
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
            cache.clear();
        }
    }
    public void release() {
        bitmapCache.clear();
    }
}
