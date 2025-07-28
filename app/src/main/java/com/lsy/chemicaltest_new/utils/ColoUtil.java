package com.lsy.chemicaltest_new.utils;

import android.graphics.Color;


public class ColoUtil {
    public static int[] gainRGB(int color) {
        int red = Color.red(color);//提取红色分量
        int green = Color.green(color);//提取绿色分量
        int blue = Color.blue(color);//提取蓝色分量
        return new int[]{red, green, blue};
    }

    public static String gainColorStr(int color) {
        int red = Color.red(color);//提取红色分量
        int green = Color.green(color);//提取绿色分量
        int blue = Color.blue(color);//提取蓝色分量
        return ("#" + red + green + blue).toUpperCase();
    }

    public static String gainColorStr(int red, int green, int blue) {
        return ("#" + red + green + blue).toUpperCase();
    }

}
