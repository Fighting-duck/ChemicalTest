package com.lsy.chemicaltest_new.domain;

import java.util.ArrayList;
import java.util.List;

public class HSV {
    private float hue;//Hue（色调）：角度值，范围是 0 到 360。
    private float saturation;//Saturation（饱和度）：比例值，范围是 0 到 1。
    private float value;//Value（亮度）：比例值，范围是 0 到 1。
    public HSV() {}
    public HSV(float hue, float saturation, float value) {
        this.hue = hue;
        this.saturation = saturation;
        this.value = value;
    }
    public HSV(HSV other){
        this.hue = other.hue;
        this.saturation = other.saturation;
        this.value = other.value;
    }

    // Getter 和 Setter 方法
    public float getHue() { return hue; }
    public float getSaturation() { return saturation; }
    public float getValue() { return value; }
    //字符串转化为List
    public static List<Float> getHSVList(String hsv_value) {
        //1. 去除首尾的方括号
        String trimmedInput = hsv_value.substring(1, hsv_value.length() - 1);
        // 按逗号分隔字符串
        String[] items = trimmedInput.split(",");
        //2. 将每个分隔后的字符串转换为浮点数，并收集到列表中
        List<Float> hsvList = new ArrayList<>();
        for (String item : items) {
            // 去除每个元素周围的空白字符
            String trimmedItem = item.trim();
            // 将字符串转换为整数
            Float number = Float.parseFloat(trimmedItem);
            // 添加到结果列表中
            hsvList.add(number);
        }
        //3.返回pointList
        return hsvList;
    }

    @Override
    public String toString() {
        return "[" + hue + "," + saturation + "," + value + ']';
    }
}