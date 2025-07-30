package com.lsy.chemicaltest_new.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.palette.graphics.Palette;

import com.lsy.chemicaltest_new.R;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PhotoUtil {
    private static final String TAG = "PhotoUtil";

    /***
     * 通过文件路径加载 Bitmap 图片
     * @param filePath 路径
     * @return Bitmap 图片
     */
    public static Bitmap loadBitmapFromPath(String filePath) {
        // 创建一个文件对象
        File imageFile = new File(filePath);

        // 检查文件是否存在
        if (imageFile.exists()) {
            // 从文件路径加载 Bitmap
            return BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        } else {
            // 文件不存在，返回 null 或者处理错误
            return null;
        }
    }
    /**
     * 为了防止横屏照片时，返回产生的旋转，所以这里做的是将照片始终正常的竖屏显示
     * @param bitmap 待旋转的图片
     * @return
     */
    public static Bitmap rotateIfRequired(Bitmap bitmap,File outputImage) {
        if (bitmap == null || outputImage==null) return null;
        try {
            ExifInterface exifInterface = new ExifInterface(outputImage.getPath());
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) return rotateBitmap(bitmap, 90);
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) return rotateBitmap(bitmap, 180);
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) return rotateBitmap(bitmap, 270);
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static Bitmap rotateBitmap(Bitmap bitmap, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate((float)degree);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        //  将不再需要的Bitmap对象回收
        bitmap.recycle();
        return rotatedBitmap;
    }

    // 临时创建图片文件
    public static File createImageFile(Context context) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "JPEG_" + timeStamp + ".jpg";
        File storageDir = context.getExternalCacheDir(); // 使用缓存目录，避免申请存储权限
        File photoFile = new File(storageDir, fileName);

        try {
            if (photoFile.createNewFile()) {
                return photoFile;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    //根据路径读取图片
    public static Bitmap getBitmapFromPath(String path){
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        return bitmap;
    }
    //根据路径删除图片
    public static Boolean deleteImage(String path) {
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("文件不存在: " + path);
            return false;
        }
        if (!file.isFile()) {
            System.out.println("路径指向的不是一个文件: " + path);
            return false;
        }
        if (!file.canWrite()) {
            System.out.println("文件不可写，可能是权限问题或文件被占用: " + path);
            return false;
        }
        if (file.delete()) {
            System.out.println("文件删除成功: " + path);
            return true;
        } else {
            System.out.println("文件删除失败，可能是文件被占用或存储设备问题: " + path);
            return false;
        }
    }

    /***
     * 查看图片大图
     * @param context 上下文
     * @param drawable 视图的 drawable
      */
    public static void viewLargeImage(Context context, BitmapDrawable drawable) {
        if (drawable == null) {
            Toast.makeText(context, context.getString(R.string.toast_no_image), Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap bitmap = drawable.getBitmap();//获取视图中图片文件
        viewLargeImage(context, bitmap);
    }
    /***
     * 查看图片大图
     * @param context 上下文
     * @param bitmap 图片
     */
    public static void viewLargeImage(Context context, Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(context, context.getString(R.string.toast_no_image), Toast.LENGTH_SHORT).show();
            return;
        }
        // 创建AlertDialog.Builder对象
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
        // 获取bitmap宽度和高度
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        // 获取屏幕的宽度和高度
        Display display = dialog.getWindow().getWindowManager().getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();

        // 计算图片的缩放比例
        float scaleWidth = (float) screenWidth / (float) bitmapWidth;
        float scaleHeight = (float) screenHeight / (float) bitmapHeight;

        float scale = Math.min(scaleWidth, scaleHeight); // 取较小的缩放比例以适应屏幕

        // 调整图片大小
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);

        // 添加一个ImageView控件存放放缩后的图像
        final ImageView zoomImageView = new ImageView(context);
        zoomImageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        zoomImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE); // 设置为CENTER_INSIDE以保持图片比例
        zoomImageView.setImageBitmap(scaledBitmap);
        dialog.setContentView(zoomImageView);

        // 添加点击事件，将放大后的图像控件设置为Null,释放内存
        zoomImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                zoomImageView.setImageBitmap(null);
            }
        });
    }

    /**
     * 保存单张图片
     * @param context 上下文（用于获取目录路径）
     * @param bitmap 图片
     * @param prefix 图片前缀
     * @return 图片保存路径，失败返回null
     */
    public static String saveBitmapToFile(Context context, Bitmap bitmap, String prefix) {
        String fileName = prefix + System.currentTimeMillis() + ".png"; // 默认PNG格式
        File file = new File(StorageUtils.getSaveImagePath(), fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            boolean success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            if (success) {
                // 通知媒体库更新
                MediaScannerConnection.scanFile(context,
                        new String[]{file.getAbsolutePath()},
                        new String[]{"image/png"},
                        null);
                return file.getAbsolutePath();
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    // 获取Bitmap指定位置的颜色
    public static int getColorAt(Bitmap bitmap, int x, int y) {
        if (bitmap != null && x >= 0 && x < bitmap.getWidth() && y >= 0 && y < bitmap.getHeight()) {
            return bitmap.getPixel(x, y);
        }
        return Color.TRANSPARENT;
    }

    /***
     * 获取以图片中心为中心，边长为sideLength像素的正方形平均RGB
     * @param bitmap 图片
     * @param sideLength 正方形边长
     * @return 平均RGB，如果提取失败返回 Color.TRANSPARENT
     */
    public static Integer getAverageRGB(Bitmap bitmap,int sideLength) {
        int[] rgbSum = calculateColorSum(bitmap,sideLength);
        if (rgbSum == null) {
            return Color.TRANSPARENT;
        }
        int redSum = rgbSum[0];
        int greenSum = rgbSum[1];
        int blueSum = rgbSum[2];
        int totalPixels = rgbSum[3];

        if (totalPixels == 0) {
            return Color.TRANSPARENT;
        }

        int averageRed = redSum / totalPixels;
        int averageGreen = greenSum / totalPixels;
        int averageBlue = blueSum / totalPixels;

        return Color.rgb(averageRed, averageGreen, averageBlue);
    }
    /***
     * 提取以矩形图片中心点为中心、边长为 sideLength 像素正方形区域的平均 HSV 值(保留两位小数)
     * @param bitmap 图片
     * @param sideLength 正方形边长
     * @return HSV数组，如果提取失败返回 null
     */
    public static List<Float> getAverageHSV(Bitmap bitmap, int sideLength) {
        int[] rgbSum = calculateColorSum(bitmap,sideLength);
        if (rgbSum == null) {
            return null;
        }
        int redSum = rgbSum[0];
        int greenSum = rgbSum[1];
        int blueSum = rgbSum[2];
        int totalPixels = rgbSum[3];

        if (totalPixels == 0) {
            return null;
        }

        int averageRed = redSum / totalPixels;
        int averageGreen = greenSum / totalPixels;
        int averageBlue = blueSum / totalPixels;

        float[] hsv = new float[3];
        Color.RGBToHSV(averageRed, averageGreen, averageBlue, hsv);

        List<Float> hsvList = new ArrayList<>();
        for (float value : hsv) {
            // 先乘 100 取整，再除以 100.0f 得到两位小数
            float roundedValue = (float) Math.round (value * 100) / 100.0f;
            hsvList.add (roundedValue);
        }

        return hsvList;
    }

    /***
     * 计算中心区域的红、绿、蓝分量的总和以及像素总数
     * @param bitmap 图片
     * @param sideLength 正方形边长
     * @return 包含红、绿、蓝总和以及像素总数的整数数组
     */
    private static int[] calculateColorSum(Bitmap bitmap,int sideLength) {
        if (bitmap == null) {
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // 计算正方形区域的左上角坐标
        int centerX = width / 2;
        int centerY = height / 2;
        int left = Math.max(0, centerX - sideLength / 2);
        int top = Math.max(0, centerY - sideLength / 2);
        int right = Math.min(width, left + sideLength);
        int bottom = Math.min(height, top + sideLength);

        int totalPixels = (right - left) * (bottom - top);
        int redSum = 0, greenSum = 0, blueSum = 0;

        // 遍历指定区域的每个像素
        for (int y = top; y < bottom; y++) {
            for (int x = left; x < right; x++) {
                int color = bitmap.getPixel(x, y);
                redSum += Color.red(color);
                greenSum += Color.green(color);
                blueSum += Color.blue(color);
            }
        }

        return new int[]{redSum, greenSum, blueSum, totalPixels};
    }
    /***
     * 深度复制Bitmap
     * @param source 源Bitmap
     * @return 深度复制后的Bitmap，如果源Bitmap为空则返回null
     */
    public static Bitmap deepCopy(Bitmap source) {
        if (source == null) return null;
        try {
            // 首先尝试快速复制
            if (source.isMutable()) {//  判断是否可变
                return source.copy(source.getConfig(), true);
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        // 降级方案
        Bitmap result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());
        new Canvas(result).drawBitmap(source, 0, 0, null);
        return result;
    }

    // 获取Palette内置的6种推荐颜色
    public static int[] getDefaultColors(Bitmap bitmap) {
        Palette palette = Palette.from(bitmap).generate();

        return new int[]{
                palette.getDominantColor(Color.TRANSPARENT),
                palette.getVibrantColor(Color.TRANSPARENT),
                palette.getLightVibrantColor(Color.TRANSPARENT),
                palette.getDarkVibrantColor(Color.TRANSPARENT),
                palette.getMutedColor(Color.TRANSPARENT),
                palette.getDarkMutedColor(Color.TRANSPARENT)
        };
    }

    /**
     * 提取图片中占比最高的几种非相似颜色
     * @param bitmap 输入图片
     * @param maxColors 最大返回颜色数量
     * @param hueTolerance 色相差值容差（0~360），越小越严格
     * @param minSaturation 最小饱和度（0~1）
     * @param minValue 最小亮度（0~1）
     * @return 去重后的主要颜色列表（按占比降序）
     */
    public static List<Integer> getDistinctColors(
            Bitmap bitmap,
            int maxColors,
            float hueTolerance,
            float minSaturation,
            float minValue
    ) {
        // 1. 生成Palette
        Palette palette = Palette.from(bitmap)
                .resizeBitmapArea(250000)  // 限制计算面积以优化性能
                .maximumColorCount(12)     // 初始提取较多颜色，避免遗漏
                .generate();

        // 2. 获取所有Swatch并按占比（Population）降序排序
        List<Palette.Swatch> swatches = new ArrayList<>(palette.getSwatches());
        swatches.sort((a, b) -> Integer.compare(b.getPopulation(), a.getPopulation()));

        // 3. 去重：在相似色相区间内，只保留占比最高的颜色
        List<Integer> distinctColors = new ArrayList<>();
        float[][] existingHues = new float[maxColors][3]; // 存储已选颜色的HSV值

        for (Palette.Swatch swatch : swatches) {
            if (distinctColors.size() >= maxColors) break;

            int rgb = swatch.getRgb();
            float[] hsv = new float[3];
            Color.colorToHSV(rgb, hsv);

            // 检查饱和度、亮度是否满足条件
            if (hsv[1] >= minSaturation && hsv[2] >= minValue) {
                boolean isSimilarToExisting = false;

                // 遍历已选颜色，检查色相是否相似
                for (int i = 0; i < distinctColors.size(); i++) {
                    float hueDiff = Math.abs(existingHues[i][0] - hsv[0]);
                    // 考虑色相的环形特性（如359°和1°相差2°，但绝对差是358°）
                    hueDiff = Math.min(hueDiff, 360 - hueDiff);

                    if (hueDiff < hueTolerance) {
                        isSimilarToExisting = true;
                        break; // 如果相似，跳过
                    }
                }

                // 不相似则加入结果
                if (!isSimilarToExisting) {
                    distinctColors.add(rgb);
                    System.arraycopy(hsv, 0, existingHues[distinctColors.size() - 1], 0, 3);
                }
            }
        }

        return distinctColors;
    }

}
