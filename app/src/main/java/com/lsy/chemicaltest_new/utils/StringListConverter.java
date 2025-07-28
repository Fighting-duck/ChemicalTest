package com.lsy.chemicaltest_new.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringListConverter {
    /***
     * 将一个由逗号分隔的字符串转换成一个浮点数列表
     * @param str 输入的字符串，其中的数值由逗号分隔
     * @return 返回一个包含浮点数值的List<Float>类型的列表
     */
    public static List<Float> toList(String str){
        List<String> str_list = Arrays.asList(str.trim().split(","));
        List<Float> float_list = new ArrayList<>();
        for (String s : str_list) {
            float_list.add(Float.parseFloat(s));
        }
        return float_list;
    }

    /***
     * 将一个浮点数列表转换成一个由逗号分隔的字符串
     * @param float_list 输入的浮点数列表
     * @return 一个由逗号分隔的浮点数值字符串
     */
    public static String toString(List<Float> float_list) {
        StringBuilder sb = new StringBuilder();
        for (Float f : float_list) {
            sb.append(f).append(",");
        }
        return sb.toString();
    }
}
