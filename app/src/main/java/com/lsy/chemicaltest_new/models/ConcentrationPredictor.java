package com.lsy.chemicaltest_new.models;

import com.lsy.chemicaltest_new.domain.SensorData;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import java.util.List;

/**
 * 专门处理多元线性回归的类
 *
 * y = β0 + β1 * x1 + β2 * x2 + β3 * x3
 * y :浓度
 * β0: 截距
 * β1: x1（电流） 的系数
 * β2: x2（温度） 的系数
 * β3: x3（b值） 的系数
 *
 */
public class ConcentrationPredictor {
    private double[] coefficients;  // 回归系数 [β0, β1, β2, β3]

    // 训练模型
    public void trainModel(List<SensorData> trainingData) {
        int sampleSize = trainingData.size();
        double[] y = new double[sampleSize];  // 浓度对数值
        double[][] x = new double[sampleSize][3];  // [电流,温度,b值]

        // 准备数据
        for (int i = 0; i < sampleSize; i++) {
            SensorData data = trainingData.get(i);
            y[i] = data.getLogConcentration();
            x[i][0] = data.getCurrent();
            x[i][1] = data.getTemperature();
            x[i][2] = data.getBValue();
        }

        // 训练回归模型
        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.newSampleData(y, x);
        coefficients = regression.estimateRegressionParameters();
    }

    // 预测浓度
    public double predictConcentration(double current, double temperature, double bValue) {
        if (coefficients == null || coefficients.length != 4) {
            throw new IllegalStateException("模型尚未训练或训练失败");
        }

        // 计算对数浓度
        double logConcentration = coefficients[0]
                + coefficients[1] * current
                + coefficients[2] * temperature
                + coefficients[3] * bValue;

        // 将对数浓度转换为实际浓度
        return Math.pow(10, logConcentration);
    }

    // 获取模型评估指标
    public double getRSquared() {
        // 需要保存回归对象才能计算R²
        // 或者可以在trainModel方法中计算并保存
        return 0; // 简化示例
    }

    // 获取回归系数
    public double[] getCoefficients() {
        return coefficients;
    }
}
