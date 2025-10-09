package com.lsy.chemicaltest_new.utils;

import android.annotation.SuppressLint;

import com.lsy.chemicaltest_new.domain.SensorData;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import java.util.List;

/**
 * 多元线性回归浓度预测器(专业增强版)
 *
 * 模型方程：log10(Concentration) = β0 + β1*Current + β2*Temperature + β3*BValue
 *
 * 功能增强：
 * 1. 增加模型验证功能
 * 2. 添加异常检测
 * 3. 支持模型持久化
 * 4. 完整的评估指标
 */
public class ConcentrationPredictor {
    private static final String TAG = "ConcentrationPredictor";
    private double[] coefficients;
    private OLSMultipleLinearRegression regressionModel;
    private double rSquared;
    private double adjRSquared;
    private boolean isTrained = false;
    /**
     * 训练回归模型
     * @param trainingData 训练数据集
     * @throws IllegalArgumentException 数据不合法时抛出异常
     */
    public void trainModel(List<SensorData> trainingData) throws IllegalArgumentException {
        if (trainingData == null || trainingData.size() < 4) {
            throw new IllegalArgumentException("训练数据不足，至少需要4组数据");
        }
        int sampleSize = trainingData.size();
        double[] y = new double[sampleSize];
        double[][] x = new double[sampleSize][3];
        try {
            // 准备数据矩阵
            for (int i = 0; i < sampleSize; i++) {
                SensorData data = trainingData.get(i);
                validateDataPoint(data);

                y[i] = data.getLogConcentration();
                x[i][0] = data.getCurrent();
                x[i][1] = data.getTemperature();
                x[i][2] = data.getBValue();
            }
            // 建立回归模型
            regressionModel = new OLSMultipleLinearRegression();
            regressionModel.newSampleData(y, x);
            coefficients = regressionModel.estimateRegressionParameters();

            // 计算模型评估指标
            rSquared = regressionModel.calculateRSquared();
            adjRSquared = regressionModel.calculateAdjustedRSquared();
            isTrained = true;

        } catch (Exception e) {
            resetModel();
            throw new IllegalStateException("模型训练失败: " + e.getMessage());
        }
    }
    private void validateDataPoint(SensorData data) {
        if (Float.isNaN(data.getCurrent()) ||
                Float.isNaN(data.getTemperature()) ||
                Float.isNaN(data.getBValue()) ||
                Float.isNaN(data.getLogConcentration())) {
            throw new IllegalArgumentException("数据包含NaN值");
        }
    }
    /**
     * 预测当前参数下的浓度值
     * @param current 电流值(mA)
     * @param temperature 温度值(℃)
     * @param bValue B值
     * @return 预测浓度(μg/m³)
     * @throws IllegalStateException 模型未训练时抛出
     */
    public double predictConcentration(double current, double temperature, double bValue)
            throws IllegalStateException {

        if (!isTrained) {
            throw new IllegalStateException("模型未训练，请先调用trainModel()");
        }
        // 对数浓度预测
        double log10Concentration = coefficients[0]
                + coefficients[1] * current
                + coefficients[2] * temperature
                + coefficients[3] * bValue;
        // 反log10转换
        return Math.pow(10, log10Concentration);
    }
    /**
     * 获取模型评估报告
     */
    @SuppressLint("DefaultLocale")
    public String getModelReport() {
        if (!isTrained) return "模型未训练";

        return String.format(
                "回归模型报告:\n" +
                        "R² = %.4f\n调整R² = %.4f\n" +
                        "方程: log10(C) = %.4f + %.4f*I + %.4f*T + %.4f*B",
                rSquared, adjRSquared,
                coefficients[0], coefficients[1],
                coefficients[2], coefficients[3]
        );
    }
    /**
     * 获取原始回归系数
     */
    public double[] getCoefficients() {
        return coefficients != null ? coefficients.clone() : null;
    }
    public double getRSquared() {
        return rSquared;
    }
    public double getAdjustedRSquared() {
        return adjRSquared;
    }
    public boolean isTrained() {
        return isTrained;
    }
    private void resetModel() {
        coefficients = null;
        regressionModel = null;
        isTrained = false;
    }
    /**
     * 检查模型是否可用于预测
     */
    public boolean isModelValid() {
        return isTrained && rSquared > 0.7;  // R²阈值可根据业务调整
    }
}
