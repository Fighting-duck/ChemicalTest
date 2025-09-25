package com.lsy.chemicaltest_new.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.domain.Expression;
import com.lsy.chemicaltest_new.domain.Point;
import com.lsy.chemicaltest_new.domain.StandardCurve;

import org.apache.commons.math3.distribution.TDistribution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CombinedChartUtils {
    // 坐标轴预留空间比例
    private static final float X_AXIS_SPACE = 0.1f;
    private static final float Y_AXIS_SPACE = 0.15f;

    // 建议的Y轴刻度数量
    private static final int Y_AXIS_LABEL_COUNT = 6;
    /***
     * 设置图表特性
     */
    public static void setChart(CombinedChart combinedChart){
        //x轴
        XAxis xAxis = combinedChart.getXAxis();
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(10f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setLabelCount(7,false);//设置X轴的刻度数量,true表固定
        //y轴
        YAxis leftYAxis = combinedChart.getAxisLeft();
        leftYAxis.setDrawGridLines(true);
        leftYAxis.setDrawAxisLine(true);
        leftYAxis.setAxisMinimum(0f);
        leftYAxis.setAxisMaximum(10f);
        leftYAxis.setDrawAxisLine(true);
        leftYAxis.setCenterAxisLabels(true);// 将轴标记居中
        // 隐藏不希望显示部分，包括坐标轴标题、刻度等等，并禁用了双指放大等功能
        combinedChart.getAxisRight().setEnabled(false);
        combinedChart.getLegend().setEnabled(false);
        combinedChart.getDescription().setEnabled(false);
        combinedChart.setScaleEnabled(false);
        combinedChart.setTouchEnabled(false);
        //设置图例
        Legend legend = combinedChart.getLegend();
        legend.setEnabled(true);
        legend.setForm(Legend.LegendForm.LINE);//类型
        legend.setTextSize(12f);//文字大小
        legend.setTextColor(Color.BLACK);//文字颜色
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);//显示方向
        //显示点数
        combinedChart.setMaxVisibleValueCount(100);//限制显示的点数
    }

    /**
     * 根据Point数据集合计算并设置CombinedChart的坐标轴范围
     * @param chart 目标CombinedChart
     * @param pointList 数据点集合
     */
    public static void calculateAndSetAxisRange(CombinedChart chart, List<Point> pointList) {
        if (chart == null || pointList == null || pointList.isEmpty()) {
            return;
        }

        // 计算数据边界
        float minX = Float.MAX_VALUE, maxX = Float.MIN_VALUE;
        float minY = Float.MAX_VALUE, maxY = Float.MIN_VALUE;

        for (Point point : pointList) {
            if (point == null) continue;

            float x = point.getX_value();
            float y = point.getY_value();

            // 过滤无效数据
            if (Float.isNaN(x) || Float.isInfinite(x) ||
                    Float.isNaN(y) || Float.isInfinite(y)) {
                continue;
            }

            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }

        // 处理单一点或空数据的情况
        if (minX == maxX) {
            minX -= 0.5f;
            maxX += 0.5f;
        }
        if (minY == maxY) {
            minY -= 0.5f;
            maxY += 0.5f;
        }

        // 限制范围防止计算溢出
        float maxAllowedRange = 1e6f;
        float xRange = maxX - minX;
        float yRange = maxY - minY;

        if (xRange > maxAllowedRange) {
            minX = -maxAllowedRange / 2;
            maxX = maxAllowedRange / 2;
            xRange = maxAllowedRange;
        }
        if (yRange > maxAllowedRange) {
            minY = -maxAllowedRange / 2;
            maxY = maxAllowedRange / 2;
            yRange = maxAllowedRange;
        }

        // 添加预留空间
        minX -= xRange * X_AXIS_SPACE;
        maxX += xRange * X_AXIS_SPACE;
        minY -= yRange * Y_AXIS_SPACE;
        maxY += yRange * Y_AXIS_SPACE;

        // 设置X轴
        XAxis xAxis = chart.getXAxis();
        xAxis.setAxisMinimum(minX);
        xAxis.setAxisMaximum(maxX);
        xAxis.setGranularity(1f);

        // 设置Y轴（优化格式化器）
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(minY);
        leftAxis.setAxisMaximum(maxY);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return formatLargeNumber(value);
            }
        });

        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    /** 格式化大数字，避免字符串过长 */
    private static String formatLargeNumber(float value) {
        // 处理无效数值（NaN/Infinite）
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return "N/A";
        }

        // 处理接近于零的值（避免显示 -0.00）
        if (Math.abs(value) < 0.005f) {
            return "0.00";
        }

        // 负数统一处理
        boolean isNegative = value < 0;
        float absValue = Math.abs(value);

        // 按量级格式化
        String formattedValue;
        if (absValue >= 1e6) {
            formattedValue = String.format(Locale.US, "%.1fM", absValue / 1e6);
        } else if (absValue >= 1e3) {
            formattedValue = String.format(Locale.US, "%.1fK", absValue / 1e3);
        } else {
            formattedValue = String.format(Locale.US, "%.2f", absValue);
        }

        // 还原负号
        return isNegative ? "-" + formattedValue : formattedValue;
    }


/*    //传入一条直线，根据其类型选择线条颜色，若直线公式为空，则最小二乘法拟合该曲线，不为空，直接使用公式，最后在联合图中显示直线图和散点图
    public static List<Float> buildChart(Context context, StandardCurve standardCurve, CombinedChart combinedChart) {
        List<Point> pointList = standardCurve.getPointList();
        if (pointList == null) return null;
        CombinedData combinedData = new CombinedData();//联合图数据
        ScatterData scatterData = new ScatterData();//散点图数据
        LineData lineData = new LineData();//直线图数据
        // 将 ScatterData 和 LineData 添加到 CombinedData
        combinedData.setData(scatterData);
        combinedData.setData(lineData);
        // 设置联合图表数据到 CombinedChart
        combinedChart.setData(combinedData);
        //将颜色资源 ID 转换为实际的颜色值
        int color = ContextCompat.getColor(context, standardCurve.getCurveColor());
        String type = standardCurve.getCurveType();
        List<Float> k_b_corr = null;
        //pointList转EntryList
        List<Entry> entryList = Point.pointList_to_entryList(pointList);
        if (!entryList.isEmpty()) {
            update_SC_Chart(scatterData, entryList, color);//创建散点图

            if (standardCurve.getFormula()==null){//最小二乘法拟合该曲线
                float minX = entryList.get(0).getX();
                float maxX = entryList.get(entryList.size() - 1).getX();
                k_b_corr= build_FitLine(entryList);
                if (k_b_corr.isEmpty()) return null;
                standardCurve.setFormula(new Expression(k_b_corr.get(0),k_b_corr.get(1)));
                standardCurve.setCORR(k_b_corr.get(3));
                List<Entry> entryList_two = build_EntryList(k_b_corr.get(0),k_b_corr.get(1),minX,maxX);
                update_LC_Chart(lineData, entryList_two, color);
            }
            else {//直接使用曲线公式创建曲线
                LineDataSet thermal_dataSet = buildLineCurve(standardCurve, color, type);
                lineData.addDataSet(thermal_dataSet);
            }
        }
        //更新y轴  x轴
        if (k_b_corr != null && !k_b_corr.isEmpty()){
            float finalYMax = k_b_corr.get(0)>0 ? entryList.get(entryList.size()-1).getY() : entryList.get(0).getY();
            float finalYMax1 = finalYMax +  0.1F*finalYMax;
            YAxis yAxis = combinedChart.getAxisLeft();
            yAxis.setAxisMaximum(finalYMax1);
            XAxis xAxis = combinedChart.getXAxis();
            xAxis.setAxisMinimum(entryList.get(0).getX()- 0.1f*entryList.get(0).getX());
            xAxis.setAxisMaximum(entryList.get(entryList.size() - 1).getX()+0.1f*entryList.get(entryList.size() - 1).getX());
            // 刷新图表
            combinedChart.notifyDataSetChanged(); // 通知数据变化
            combinedChart.invalidate();
        }
        //添加描述
        Description description = combinedChart.getDescription();
        description.setEnabled(true);//是否可用
        description.setText(standardCurve.getX_axis_unit());
        description.setTextColor(Color.BLACK);//字体颜色
        description.setTextSize(12f);//字体大小
        // 刷新图表
        combinedChart.invalidate();
        return k_b_corr;
    }*/
    //传入一条直线，根据其类型选择线条颜色，若直线公式为空，则最小二乘法拟合该曲线，不为空，直接使用公式，最后在联合图中显示直线图和散点图
/*    public static List<Float> buildChart(Context context, CombinedChart combinedChart, List<Point> pointList, Integer type, String x_unit) {
        if (pointList == null || type>3 || type<0) return null;
        CombinedData combinedData = new CombinedData();//联合图数据
        ScatterData scatterData = new ScatterData();//散点图数据
        LineData lineData = new LineData();//直线图数据
        // 将 ScatterData 和 LineData 添加到 CombinedData
        combinedData.setData(scatterData);
        combinedData.setData(lineData);
        // 设置联合图表数据到 CombinedChart
        combinedChart.setData(combinedData);
        List<Float> k_b_corr = null;
        int sc_color = ContextCompat.getColor(context, StandardCurve.getCurveColor(type));
        int lc_color = ContextCompat.getColor(context, R.color.purple_200);
        //pointList转EntryList
        List<Entry> entryList = Point.pointList_to_entryList(pointList);
        if (!entryList.isEmpty()) {
            update_SC_Chart(scatterData, entryList, sc_color);//创建散点图
            float minX = entryList.get(0).getX();
            float maxX = entryList.get(entryList.size() - 1).getX();
            k_b_corr= build_FitLine(entryList);
            if (k_b_corr.isEmpty()) return null;
            List<Entry> entryList_two = build_EntryList(k_b_corr.get(0),k_b_corr.get(1),minX,maxX);
            update_LC_Chart(lineData, entryList_two, lc_color);
        }

        //添加描述
        Description description = combinedChart.getDescription();
        description.setEnabled(true);//是否可用
        description.setText(x_unit);
        description.setTextColor(Color.BLACK);//字体颜色
        description.setTextSize(12f);//字体大小
        // 刷新图表
        calculateAndSetAxisRange(combinedChart, pointList);
        combinedChart.invalidate();
        return k_b_corr;
    }*/
public static LinearRegressionResult buildChart(Context context, CombinedChart combinedChart, List<Point> pointList, Integer type, String x_unit) {
    if (pointList == null || type>3 || type<0) return null;
    CombinedData combinedData = new CombinedData();//联合图数据
    ScatterData scatterData = new ScatterData();//散点图数据
    LineData lineData = new LineData();//直线图数据
    // 将 ScatterData 和 LineData 添加到 CombinedData
    combinedData.setData(scatterData);
    combinedData.setData(lineData);
    // 设置联合图表数据到 CombinedChart
    combinedChart.setData(combinedData);
    LinearRegressionResult regressionResult = null;
    int sc_color = ContextCompat.getColor(context, StandardCurve.getCurveColor(type));
    int lc_color = ContextCompat.getColor(context, R.color.purple_200);
    //pointList转EntryList
    List<Entry> entryList = Point.pointList_to_entryList(pointList);
    if (!entryList.isEmpty()) {
        update_SC_Chart(scatterData, entryList, sc_color);//创建散点图
        float minX = entryList.get(0).getX();
        float maxX = entryList.get(entryList.size() - 1).getX();
        regressionResult = build_FitLine(entryList);
        List<Entry> entryList_two = build_EntryList(regressionResult.getK(), regressionResult.getB(), minX, maxX);
        update_LC_Chart(lineData, entryList_two, lc_color);

    }

    //添加描述
    Description description = combinedChart.getDescription();
    description.setEnabled(true);//是否可用
    description.setText(x_unit);
    description.setTextColor(Color.BLACK);//字体颜色
    description.setTextSize(12f);//字体大小
    // 刷新图表
    calculateAndSetAxisRange(combinedChart, pointList);
    combinedChart.invalidate();
    return regressionResult;
}

    /***
     * 利用标准曲线创建散点图
     * @param scatterData
     * @param entries
     * @param color
     * @return
     */
    private static ScatterData update_SC_Chart(ScatterData scatterData, List<Entry> entries, int color){
        // 创建一个散点图的数据集
        ScatterDataSet scatterDataSet = new ScatterDataSet(entries,"散点图");
        // 设置散点图的数据集的一些属性
        scatterDataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE); // 设置点的形状
        scatterDataSet.setColor(color); // 设置点的颜色
        scatterDataSet.setDrawValues(false);//是否绘制值
        scatterData.clearValues();
        scatterData.addDataSet(scatterDataSet);
        scatterData.notifyDataChanged();
        return scatterData;
    }

    /***
     * 利用标准曲线创建直线图
     * @param lines
     * @param entries
     * @param lineColor
     * @return
     */
    private static LineData update_LC_Chart(LineData lines, List<Entry> entries, int lineColor){
        // 创建LineDataSet
        LineDataSet lineDataSet = new LineDataSet(entries, "直线");
        lineDataSet.setColor(lineColor);
        lineDataSet.setLineWidth(2f);//线宽
        lineDataSet.setDrawCircles(false);//是否绘制点
        lineDataSet.setDrawValues(false);//是否绘制值
        // 设置为false以不绘制水平线和垂直线
        lineDataSet.setDrawHorizontalHighlightIndicator(false);
        lineDataSet.setDrawVerticalHighlightIndicator(false);
        lineDataSet.setCircleColor(lineColor);
        lineDataSet.setCircleRadius(3f);
        // 创建一个直线图的数据
        lines.clearValues();
        lines.addDataSet(lineDataSet);
        lines.notifyDataChanged();
        return lines;
    }

    /***
     * 线性回归返回结果
     */
    public static class LinearRegressionResult {
        private float k, b, mse, xMean, sxx;//斜率，截距，均方误差，x均值，x方差
        private int n;//数据点数量

        public LinearRegressionResult(float k, float b, float mse, float xMean, float sxx, int n) {
            this.k = k;
            this.b = b;
            this.mse = mse;
            this.xMean = xMean;
            this.sxx = sxx;
            this.n = n;
        }

        /**
         * 计算某 x 处的预测值 95% 置信区间
         */
        public float[] predictInterval(float x) {
            float yPred = k * x + b;
            float sep = (float) Math.sqrt(mse * (1 + 1.0 / n + Math.pow(x - xMean, 2) / sxx));
            TDistribution tDist = new TDistribution(n - 2);
            float tCritical = (float) tDist.inverseCumulativeProbability(0.975); // 95%置信区间
            float margin = tCritical * sep;
            return new float[]{yPred - margin, yPred + margin};
        }
        /**
         * 特殊场景：根据y值反推x的可能范围（95%置信区间）
         */
        public float[] inversePredictInterval(float y) {
            // 从y反推x的估计值
            float xEst = (y - b) / k;

            // 计算反推的标准误差（基于回归模型的误差传递）
            float seInverse = (float) Math.sqrt(
                    (mse / (k * k)) * (1 + 1.0 / n + Math.pow(xEst - xMean, 2) / sxx)
            );

            TDistribution tDist = new TDistribution(n - 2);
            float tCritical = (float) tDist.inverseCumulativeProbability(0.975); // 95%置信区间
            float margin = tCritical * seInverse;
            return new float[]{xEst - margin, xEst + margin};
        }
        /**
         * 计算 Pearson 相关系数 r
         */
        public float correlationCoefficient() {
            float syy = mse * (n - 2) + k * k * sxx;
            return (float) ((k * Math.sqrt(sxx)) / Math.sqrt(syy));
        }

        public float getK() {
            return k;
        }

        public void setK(float k) {
            this.k = k;
        }

        public float getB() {
            return b;
        }

        public void setB(float b) {
            this.b = b;
        }

        public float getMse() {
            return mse;
        }

        public void setMse(float mse) {
            this.mse = mse;
        }

        public float getxMean() {
            return xMean;
        }

        public void setxMean(float xMean) {
            this.xMean = xMean;
        }

        public float getSxx() {
            return sxx;
        }

        public void setSxx(float sxx) {
            this.sxx = sxx;
        }

        public int getN() {
            return n;
        }

        public void setN(int n) {
            this.n = n;
        }
    }


    /***
     * 使用最小二乘法来拟合直线 直线方程Y=kX + b
     * N:数据点数量
     * 斜率 k 的计算公式为：k = (N * Σ(xy) - Σx * Σy) / (N * Σ(x^2) - (Σx)^2)
     * 截距 b 的计算公式为：b = (Σy - k * Σx) / N
     * @param  entries 散点数据集
     * @return (k,b,corr)
     */
/*    @SuppressLint({"SetTextI18n","DefaultLocale"})
    public static List<Float> build_FitLine(List<Entry> entries){
        final int N = entries.size();
        float sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0, sumY2 =0;
        for (Entry entry : entries) {
            float x = entry.getX();
            float y = entry.getY();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
            sumY2 += y * y;
        }
        final float denominator = (N * sumX2) - (sumX * sumX);
        // 处理垂直线或数值不稳定情况
        if (Math.abs(denominator) < 1e-6f) {
            return Arrays.asList(Float.NaN, Float.NaN, 0f); // 返回无效标记
        }
        float k = (N * sumXY - sumX * sumY) / denominator;
        float b = (sumY - k * sumX) / N;
        // 计算相关系数 r
        //float r = (N * sumXY - sumX * sumY) / (float) Math.sqrt((N * sumX2 - sumX * sumX) * (N * sumY2 - sumY * sumY));
        float r = computeR(N, sumX, sumY, sumXY, sumX2, sumY2);
        List<Float> result = new ArrayList<>();
        result.add(NumberUtils.roundCurve_k_b_r( k));
        result.add(NumberUtils.roundCurve_k_b_r( b));
        result.add(NumberUtils.roundCurve_k_b_r( r));
        return result;
    }*/

    @SuppressLint({"SetTextI18n","DefaultLocale"})
    public static LinearRegressionResult build_FitLine(List<Entry> entries){
        final int N = entries.size();
        float sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (Entry entry : entries) {
            float x = entry.getX();
            float y = entry.getY();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        float xMean = sumX / N;
        float yMean = sumY / N;
        // 计算斜率和截距
        final float denominator = (N * sumX2) - (sumX * sumX);
        float k = (N * sumXY - sumX * sumY) / denominator;
        float b = yMean - k * xMean;
        //计算残差平方和SSE和均方误差MSE
        float sse = 0;
        for (Entry entry : entries) {
            float x = entry.getX();
            float y = entry.getY();
            float yPred = k * x + b;//预测值
            sse += (float) Math.pow(y - yPred, 2);
        }
        float mse = sse / (N - 2);

        //计算Sxx
        float sxx = 0;
        for (Entry entry : entries) {
            float x = entry.getX();
            sxx += (float) Math.pow(x - xMean, 2);
        }
        return new LinearRegressionResult(k, b, mse, xMean, sxx, N);
    }

    /**
     * 安全计算Pearson相关系数（强制限制在[0,1]范围内）
     */
    private static float computeR(
            int N, float sumX, float sumY,
            float sumXY, float sumX2, float sumY2) {

        float cov = (N * sumXY) - (sumX * sumY);
        float varX = (N * sumX2) - (sumX * sumX);
        float varY = (N * sumY2) - (sumY * sumY);
        // 处理分母接近零的情况
        if (varX <= 1e-6f || varY <= 1e-6f) {
            return 0f;
        }
        // 计算结果并钳制到[0,1]范围
        float r = cov / (float) Math.sqrt(varX * varY);
        r = Math.min(1f, Math.max(-1f, r)); // 先限制到 [-1, 1]
        r = Math.abs(r);                     // 转成 [0,1]
        return r;
    }

    /***
     * 通过直线方程Y=Kx+b构建一条直线
     * @param k 斜率
     * @param b 截距
     * @param minX 最小值
     * @param maxX 最大值
     * @return 直线点数据集
     */
    private static List<Entry> build_EntryList(float k, float b,Float minX, Float maxX){
        // 创建拟合线的点
        List<Entry> lineEntries = new ArrayList<>();
        lineEntries.add(new Entry(minX, k * minX + b)); // 最小值点
        lineEntries.add(new Entry(maxX, k * maxX + b)); // 最大值点
        return lineEntries;
    }
    /***
     * 计算相关系数corr
     * @param
     */
    @SuppressLint("DefaultLocale")
    private static float gain_CORR(List<Entry> entries){
        int N = entries.size();
        float sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0, sumY2 =0;
        for (Entry entry : entries) {
            float x = entry.getX();
            float y = entry.getY();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
            sumY2 += y * y;
        }
        // 计算相关系数 r
        float r = (N * sumXY - sumX * sumY) / (float) Math.sqrt((N * sumX2 - sumX * sumX) * (N * sumY2 - sumY * sumY));
        return r;
    }
    /***
     * 利用标准曲线公式创建直线图
     * @param standardCurve
     * @param lineColor
     * @return
     */
    private static LineDataSet buildLineCurve(StandardCurve standardCurve, int lineColor, String name){
        List<Point> pointList = standardCurve.getPointList();
        // pointList转EntryList
        List<Entry> entryList = Point.pointList_to_entryList(pointList);
        // 根据标准曲线创建点集
        Expression expression = standardCurve.getFormula();
        List<Entry> fitLine = new ArrayList<>();
        float k = expression.getK();
        float b = expression.getB();
        float minX = entryList.get(0).getX();
        float maxX = entryList.get(entryList.size() - 1).getX();
        fitLine.add(new Entry(minX, k * minX + b)); // 最小值点
        fitLine.add(new Entry(maxX, k * maxX + b)); // 最大值点
        // 创建LineDataSet
        LineDataSet lineDataSet = new LineDataSet(fitLine, name);
        lineDataSet.setColor(lineColor);
        lineDataSet.setLineWidth(2f);//线宽
        lineDataSet.setDrawCircles(false);//是否绘制点
        lineDataSet.setDrawValues(false);//是否绘制值
        // 设置为false以不绘制水平线和垂直线
        lineDataSet.setDrawHorizontalHighlightIndicator(false);
        lineDataSet.setDrawVerticalHighlightIndicator(false);
        lineDataSet.setCircleColor(lineColor);
        lineDataSet.setCircleRadius(3f);
        return lineDataSet;
    }


}
