package com.lsy.chemicaltest_new.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.palette.graphics.Palette;

/**
 * 头像带模糊背景效果处理类（优化版）
 * 修复问题：
 * 1. 大 Bitmap 导致 Canvas 崩溃（限制最大尺寸）
 * 2. RenderScript 资源泄漏问题（try-finally 保护）
 * 3. Android 12+ RenderScript 兼容性问题（备用方案）
 */
public class AvatarWithBlurBackground {
    private static final int MAX_BITMAP_SIZE = 1024; // 最大允许的图片边长（px）
    private static final float BLUR_SCALE_FACTOR = 0.4f; // 模糊前缩放比例

    private final Context context;
    private final AppCompatImageView backgroundView;

    public AvatarWithBlurBackground(Context context, AppCompatImageView backgroundView) {
        this.context = context;
        this.backgroundView = backgroundView;
    }

    /**
     * 安全加载头像（自动处理大图）
     */
    public void loadAvatar(Bitmap originalBitmap) {
        if (originalBitmap == null || originalBitmap.isRecycled()) {
            backgroundView.setImageBitmap(null);
            return;
        }

        try {
            // 1. 检查并缩放原始图片（防止OOM）
            Bitmap safeBitmap = getSafeBitmap(originalBitmap);

            // 2. 生成模糊背景
            Bitmap blurredBitmap = blurBitmap(safeBitmap, 25f);
            backgroundView.setImageBitmap(blurredBitmap);

            // 3. 提取主色调（异步）
            Palette.from(safeBitmap).generate(palette -> {
                int dominantColor = palette.getDominantColor(Color.GRAY);
                backgroundView.post(() -> { // 确保在主线程更新UI
                    backgroundView.setColorFilter(
                            dominantColor & 0x80FFFFFF,
                            android.graphics.PorterDuff.Mode.MULTIPLY
                    );
                });
            });

        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            backgroundView.setImageBitmap(null);
        }
    }

    /**
     * 获取安全尺寸的Bitmap（不超过MAX_BITMAP_SIZE）
     */
    private Bitmap getSafeBitmap(Bitmap original) {
        int width = original.getWidth();
        int height = original.getHeight();

        if (width <= MAX_BITMAP_SIZE && height <= MAX_BITMAP_SIZE) {
            return original.copy(original.getConfig(), false);
        }

        float scale = Math.min(
                (float) MAX_BITMAP_SIZE / width,
                (float) MAX_BITMAP_SIZE / height
        );

        return Bitmap.createScaledBitmap(
                original,
                (int) (width * scale),
                (int) (height * scale),
                false
        );
    }

    /**
     * 高斯模糊处理（自动选择最佳实现）
     */
    private Bitmap blurBitmap(Bitmap image, float blurRadius) {
        // 1. 先缩小图片（优化性能）
        int scaledWidth = (int) (image.getWidth() * BLUR_SCALE_FACTOR);
        int scaledHeight = (int) (image.getHeight() * BLUR_SCALE_FACTOR);
        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, scaledWidth, scaledHeight, false);

        // 2. 模糊处理
        Bitmap outputBitmap;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            outputBitmap = renderEffectBlur(inputBitmap, blurRadius); // Android 12+ 推荐方案
        } else {
            outputBitmap = renderScriptBlur(inputBitmap, blurRadius); // 传统RenderScript方案
        }

        // 3. 注意：不再放大回原尺寸！
        return outputBitmap;
    }

    /**
     * RenderScript模糊方案（Android 12以下）
     */
    private Bitmap renderScriptBlur(Bitmap image, float blurRadius) {
        RenderScript rs = null;
        try {
            rs = RenderScript.create(context);
            Bitmap output = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);

            Allocation tmpIn = Allocation.createFromBitmap(rs, image);
            Allocation tmpOut = Allocation.createFromBitmap(rs, output);
            ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

            blurScript.setRadius(Math.min(blurRadius, 25f)); // 限制最大25px
            blurScript.setInput(tmpIn);
            blurScript.forEach(tmpOut);
            tmpOut.copyTo(output);

            return output;
        } finally {
            if (rs != null) {
                rs.destroy(); // 确保释放资源
            }
        }
    }

    /**
     * Android 12+ 推荐模糊方案（避开废弃的RenderScript）
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    private Bitmap renderEffectBlur(Bitmap image, float blurRadius) {
        // 使用RenderEffect（需要API 31+）
        try {
            android.graphics.RenderEffect renderEffect = android.graphics.RenderEffect
                    .createBlurEffect(
                            blurRadius, blurRadius,
                            android.graphics.Shader.TileMode.CLAMP
                    );

            Bitmap output = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
            android.graphics.Canvas canvas = new android.graphics.Canvas(output);
            canvas.drawBitmap(image, 0, 0, null);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                backgroundView.setRenderEffect(renderEffect);
            }

            return output;
        } catch (Exception e) {
            // 回退到RenderScript（如果设备不支持RenderEffect）
            return renderScriptBlur(image, blurRadius);
        }
    }
}
