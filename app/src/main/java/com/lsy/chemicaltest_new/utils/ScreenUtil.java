package com.lsy.chemicaltest_new.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class ScreenUtil {

    /**
     * 获取屏幕宽度
     *
     * @param context 上下文对象
     * @return 屏幕宽度（像素）
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @param context 上下文对象
     * @return 屏幕高度（像素）
     */
    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }
}
