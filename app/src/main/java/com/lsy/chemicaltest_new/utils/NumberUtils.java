package com.lsy.chemicaltest_new.utils;

/***
 * 数学工具类
 * 处理数字精度、四舍五入、格式化、转换等
 */
public class NumberUtils {
    // 标准曲线相关数据精度:k,b,r
    private static final int accuracyCurve_k_b_r = 4;
    // 标准曲线相关数据精度:lgX,avgY
    private static final int accuracyCurve_lgX_avgY = 6;
    // 浓度数据精度
    private static final int accuracyCO = 3;

    /**
     * 保留指定位数的有效数字
     */
    private static float roundToSignificantFigures(float value, int figures) {
        if (Float.isNaN(value) || value == 0f || Float.isInfinite(value)) {
            return value;
        }
        float magnitude = (float) Math.pow(10, figures - 1 - (int) Math.log10(Math.abs(value)));
        return Math.round(value * magnitude) / magnitude;
    }
    public static float roundCurve_k_b_r(float value) {
        return roundToSignificantFigures(value, accuracyCurve_k_b_r);
    }
    public static float roundCurve_lgX_avgY(float value) {
        return roundToSignificantFigures(value, accuracyCurve_lgX_avgY);
    }
    public static float roundCO(float value) {
        return roundToSignificantFigures(value, accuracyCO);
    }
}
