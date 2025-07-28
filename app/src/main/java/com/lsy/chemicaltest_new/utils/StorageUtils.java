package com.lsy.chemicaltest_new.utils;

import android.os.Environment;
import java.io.File;
import android.os.StatFs;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class StorageUtils {
    // 内存缓存路径，避免重复创建File对象
    private static final Map<String, File> pathCache = new HashMap<>();

    // 主文件夹名称常量
    private static final String MAIN_DIR_NAME = "样品检测";
    private static final String TEXT_DIR_NAME = "文档";
    private static final String IMAGE_DIR_NAME = "图片";

    /**
            * 获取或创建主存储目录
     * @return 主目录File对象，若存储不可用返回null
     */
    @Nullable
    public static File getSaveMainPath() {
        return getOrCreateDir(MAIN_DIR_NAME);
    }

    /**
     * 获取文档存储目录
     * @return 文档目录File对象，若主目录创建失败返回null
     */
    @Nullable
    public static File getSaveTextPath() {
        File mainDir = getSaveMainPath();
        return mainDir != null ? getOrCreateDir(TEXT_DIR_NAME, mainDir) : null;
    }

    /**
     * 获取图片存储目录
     * @return 图片目录File对象，若主目录创建失败返回null
     */
    @Nullable
    public static File getSaveImagePath() {
        File mainDir = getSaveMainPath();
        return mainDir != null ? getOrCreateDir(IMAGE_DIR_NAME, mainDir) : null;
    }

    // 核心方法：创建或获取目录（带缓存）
    @Nullable
    private static File getOrCreateDir(@NonNull String dirName) {
        // 检查外部存储是否可用
        if (!isExternalStorageWritable()) {
            return null;
        }

        // 优先从缓存读取
        if (pathCache.containsKey(dirName)) {
            return pathCache.get(dirName);
        }

        // 创建主目录
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), dirName);
        return handleDirCreation(dir, dirName);
    }

    @Nullable
    private static File getOrCreateDir(@NonNull String dirName, @NonNull File parentDir) {
        String cacheKey = parentDir.getName() + File.separator + dirName;
        if (pathCache.containsKey(cacheKey)) {
            return pathCache.get(cacheKey);
        }

        File dir = new File(parentDir, dirName);
        return handleDirCreation(dir, cacheKey);
    }

    @Nullable
    private static File handleDirCreation(@NonNull File dir, @NonNull String cacheKey) {
        try {
            if (!dir.exists() && !dir.mkdirs()) {
                return null; // 创建失败
            }
            pathCache.put(cacheKey, dir);
            return dir;
        } catch (SecurityException e) {
            // 处理无权限情况
            return null;
        }
    }

    /**
     * 检查外部存储是否可写
     */
    private static boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /***
     * 检查存储空间是否足够（单位：MB）
     * @param requiredSpaceMB 所需空间大小（MB）
     */
    public static boolean hasEnoughSpace(long requiredSpaceMB) {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long availableBytes = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
        return availableBytes >= requiredSpaceMB * 1024 * 1024;
    }
}
