package com.lsy.chemicaltest_new.utils;

import static com.blankj.utilcode.util.ViewUtils.runOnUiThread;

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
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.domain.Expression;
import com.lsy.chemicaltest_new.domain.Point;
import com.lsy.chemicaltest_new.domain.StandardCurve;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        float minX = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = Float.MIN_VALUE;

        for (Point point : pointList) {
            if (point == null) continue;

            float x = point.getX_value();
            float y = point.getY_value();

            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }

        // 处理空数据或单一点的情况
        if (minX == maxX) {
            minX -= 1;
            maxX += 1;
        }

        if (minY == maxY) {
            minY -= 1;
            maxY += 1;
        }

        // 添加预留空间
        float xRange = maxX - minX;
        float yRange = maxY - minY;

        minX -= xRange * X_AXIS_SPACE;
        maxX += xRange * X_AXIS_SPACE;
        minY -= yRange * Y_AXIS_SPACE;
        maxY += yRange * Y_AXIS_SPACE;

        // 设置X轴
        XAxis xAxis = chart.getXAxis();
        xAxis.setAxisMinimum(minX);
        xAxis.setAxisMaximum(maxX);
        xAxis.setGranularity(1f); // 设置X轴最小间隔
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // 设置Y轴
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(minY);
        leftAxis.setAxisMaximum(maxY);
        leftAxis.setLabelCount(Y_AXIS_LABEL_COUNT, false); // 建议的刻度数量
        leftAxis.setSpaceTop(Y_AXIS_SPACE * 100); // 顶部预留空间百分比

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false); // 禁用右侧Y轴

        // 刷新图表
        chart.notifyDataSetChanged();
        chart.invalidate();
    }
    //传入一条直线，根据其类型选择线条颜色，若直线公式为空，则最小二乘法拟合该曲线，不为空，直接使用公式，最后在联合图中显示直线图和散点图
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
    }
    //传入一条直线，根据其类型选择线条颜色，若直线公式为空，则最小二乘法拟合该曲线，不为空，直接使用公式，最后在联合图中显示直线图和散点图
    public static List<Float> buildChart(Context context, CombinedChart combinedChart, List<Point> pointList, Integer type, String x_unit) {
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
     * 使用最小二乘法来拟合直线 直线方程Y=kX + b
     * N:数据点数量
     * 斜率 k 的计算公式为：k = (N * Σ(xy) - Σx * Σy) / (N * Σ(x^2) - (Σx)^2)
     * 截距 b 的计算公式为：b = (Σy - k * Σx) / N
     * @param  entries 散点数据集
     * @return (k,b,corr)
     */
    @SuppressLint({"SetTextI18n","DefaultLocale"})
    public static List<Float> build_FitLine(List<Entry> entries){
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
        float k = (N * sumXY - sumX * sumY) / (N * sumX2 - sumX * sumX);
        float b = (sumY - k * sumX) / N;
        // 计算相关系数 r
        float r = (N * sumXY - sumX * sumY) / (float) Math.sqrt((N * sumX2 - sumX * sumX) * (N * sumY2 - sumY * sumY));
        List<Float> result = new ArrayList<>();
        result.add(k);
        result.add(b);
        result.add(r);
        return result;
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
