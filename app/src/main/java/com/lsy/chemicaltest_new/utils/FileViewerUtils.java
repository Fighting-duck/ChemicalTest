package com.lsy.chemicaltest_new.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.lsy.chemicaltest_new.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileViewerUtils {
    private static final String[] SUPPORTED_TYPES = {
            "pdf", "xls", "xlsx", "doc", "docx", "ppt", "pptx", "txt",
            "jpg", "jpeg", "png", "gif", "mp4", "mp3"
    };
    public static void openFileFromAssets(Context context, String assetPath,String storeName) {
        try {
            // 1. 获取文件扩展名
            String fileExt = getFileExtension(assetPath);
            String mimeType = getMimeType(fileExt);
            // 检查是否支持的文件类型
            if (!isSupportedType(fileExt)) {
                Toast.makeText(context, "不支持打开此文件类型", Toast.LENGTH_SHORT).show();
                return;
            }
            // 2. 复制到缓存目录
            File file = new File(StorageUtils.getSaveTextPath(), storeName + "." + fileExt);
            copyFile(context.getAssets().open(assetPath), file);
            // 3. 获取URI
            Uri uri = FileProvider.getUriForFile(context,
                    context.getString(R.string.file_provider_authority),
                    file);
            // 4. 调用查看器
            Intent intent = new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, mimeType)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "未找到可以打开此文件的应用程序", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "打开文件失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("FileViewerUtils", "Error: " + e.getMessage());
        }
    }
    private static boolean isSupportedType(String fileExt) {
        if (fileExt == null) return false;
        for (String type : SUPPORTED_TYPES) {
            if (type.equalsIgnoreCase(fileExt)) {
                return true;
            }
        }
        return false;
    }
    private static String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
    private static String getMimeType(String fileExt) {
        switch (fileExt) {
            case "pdf": return "application/pdf";
            case "xls": return "application/vnd.ms-excel";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "ppt": return "application/vnd.ms-powerpoint";
            case "pptx": return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt": return "text/plain";
            case "jpg":
            case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "mp4": return "video/mp4";
            case "mp3": return "audio/mpeg";
            default: return "*/*";
        }
    }
    private static void copyFile(InputStream in, File out) throws IOException {
        try (OutputStream fos = new FileOutputStream(out)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
        }
    }
}
