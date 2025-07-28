package com.lsy.chemicaltest_new.interfaces;

import com.lsy.chemicaltest_new.domain.Point;

public interface OperateCurve {
    /***
     * 通过多点计算x平均值显示
     * @param x
     */

    void set_point_x(Double x);

    /***
     * 通过多点计算y平均值显示
     * @param y
     */
    void set_point_y(Double y);
    /***
     * 设置曲线最小值
     * @param xMin
     * @return 成功返回当前值，不成功返回旧值
     */
    Float set_curve_xMin(Float xMin);
    /***
     * 设置曲线最大值
     * @param xMax
     * @return 成功返回当前值，不成功返回旧值
     */
    Float set_curve_xMax(Float xMax);
    /***
     * 设置曲线的相关系数
     * @param CORR 相关系数
     */
    void set_curve_CORR(Float CORR);
    /**
     * 向图表中添加一个点。如果该点已存在，则更新该点。
     */
    void addPoint();
    /***
     * 根据索引删除图表中一个点
     * @param position 索引
     * @return 返回被删除的点
     */
    Point removePoint(int position);
}
