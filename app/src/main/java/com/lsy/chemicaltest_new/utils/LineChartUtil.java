package com.lsy.chemicaltest_new.utils;

import static com.blankj.utilcode.util.StringUtils.getString;

import android.graphics.Color;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.lsy.chemicaltest_new.R;

/***
 * 图表中曲线管理
 */
public class LineChartUtil {
    private static final String TAG = "LineChartUtil";

    /***
     * 设置图表基本属性
     * @param chart 图表视图
     * @return 是否设置成功
     */
    public static Boolean setLineChart(LineChart chart) {
        /***图表设置***/
        chart.setDragEnabled(true);//设置是否可以拖动
        chart.setScaleEnabled(true);//设置是否可以缩放
        chart.setScaleEnabled(true);//允许滚动
        chart.setDrawBorders(true);//是否显示边界
        chart.setTouchEnabled(false);//设置是否可以点击
        chart.setPinchZoom(false);//设置是否可以缩放
        chart.setDoubleTapToZoomEnabled(false);//设置是否可以双击缩放
        chart.setDrawGridBackground(false);//是否展示网格线
        chart.setTouchEnabled(false); //是否有触摸事件
        chart.setHighlightPerDragEnabled(false);//允许高亮线-指示器
        //chart.setMaxVisibleValueCount(7);// 当前统计图表中最多在x轴坐标线上显示的总量
        //chart.setVisibleYRangeMaximum(7, YAxis.AxisDependency.LEFT);// 当前统计图表中最多在Y轴坐标线上显示的总量
        //设置XY轴动画效果
        chart.animateY(2500);
        chart.animateX(1500);
        /***设置X轴***/
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//设置X轴的位置
        xAxis.setLabelCount(7,false);//设置X轴的刻度数量,true表固定
        //xAxis.setAxisMinimum(0f);//设置X轴的值（最小值、最大值、然后会根据设置的刻度数量自动分配刻度显示
        //xAxis.setAxisMaximum(7f);
        xAxis.setDrawGridLines(true);//网格线是否隐藏
        xAxis.setLabelRotationAngle(-20);//将X轴上的文字旋转一定度数

        /***左侧Y轴***/
        YAxis leftYAxis = chart.getAxisLeft();
        leftYAxis.setLabelCount(7,true);
        leftYAxis.setAxisMinimum(0f);
        leftYAxis.setAxisMaximum(600f);
        leftYAxis.setDrawGridLines(true);//网格线是否隐藏
        leftYAxis.setCenterAxisLabels(false);// 将轴标记居中
        /***右边Y轴***/
        YAxis rightYAxis = chart.getAxisRight();
        rightYAxis.setEnabled(false);
        /***图例***/
        Legend legend = chart.getLegend();
        legend.setEnabled(false);
        legend.setForm(Legend.LegendForm.LINE);//类型
        legend.setTextSize(12f);//文字大小
        legend.setTextColor(Color.CYAN);//文字颜色
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);//显示方向
        /**描述**/
        Description description = chart.getDescription();
        description.setEnabled(true);//是否可用
        description.setText(getString(R.string.chart_xTabel_time));//x轴描述
        description.setTextColor(Color.BLUE);//颜色
        //设置是否可以点击数据点
        chart.setTouchEnabled(false);

        //设置数据
        LineData lineData = new LineData();
        chart.setData(lineData);
        Log.d(TAG,"设置LineChart");
        return true;
    }
    /***
     * 添加一条线
     * @param lineColor 线条颜色
     * */
    public static LineData addLine(LineData lines,LineDataSet lineDataSet,int lineColor) {
        /***点集合，即一条折线***/
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);//依赖轴
        lineDataSet.setColor(lineColor);//线条颜色
        lineDataSet.setValueTextColor(Color.RED);//值颜色
        lineDataSet.setLineWidth(2f);//线宽
        lineDataSet.setDrawCircles(false);//是否绘制点
        lineDataSet.setDrawValues(false);//是否绘制值
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);//设置线条为三次贝塞尔曲线
        //开启高亮线下部分填充
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillColor(Color.RED);//填充色
        lineDataSet.setFillAlpha(5);//透明度
        //线集合，所有折线以数组的形式存到此集合中
        lines.addDataSet(lineDataSet);
        lines.notifyDataChanged();
        Log.d(TAG,"添加一条线");
        return lines;
    }

    /**
     * 给lineData中index条线在尾部动态添加数据yValues
     * * 在一个LineChart中存放的折线，其实是以索引从0开始编号的
     * @param lineData 线
     * @param xValue x值
     * @param yValue y值
     * @param index linData中那一条线
     */
    public static LineData addEntryInLast(LineData lineData,Float xValue,float yValue,int index) {
        // 通过索引index得到一条折线，之后得到折线上当前点的数量
        LineDataSet dataSet = (LineDataSet) lineData.getDataSetByIndex(index);
        if (dataSet==null) return null;
//        if (dataSet.getEntryCount()>6){//保证屏幕上仅有7个点
//            dataSet.removeEntry(0);
//        }
        Entry entry = new Entry(xValue, yValue); // 创建一个点
        lineData.addEntry(entry,index); // 将entry添加到尾部
        //通知数据已经改变
        lineData.notifyDataChanged();
        Log.d(TAG,"添加信息："+yValue);
        return lineData;
    }
}
