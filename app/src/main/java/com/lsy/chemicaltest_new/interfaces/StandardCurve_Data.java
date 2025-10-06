package com.lsy.chemicaltest_new.interfaces;

import com.lsy.chemicaltest_new.domain.Point;
import com.lsy.chemicaltest_new.domain.StandardCurve;

import java.util.List;

public interface StandardCurve_Data {
    /***
     * 在数据库中保存曲线的point
     * @param pointList 点数据
     * @return 所有point的id
     */
    List<Long> savePoints(List<Point> pointList);
    /***
     * 更新点数据
     * @param oldList 旧点数据
     * @param newList 新的点数据
     * @return 插入的点ID
     */
    List<Long> alterPoints(List<Point> oldList, List<Point> newList);

    /**
     * 为标准曲线加载关联数据(并行加载)
     * @param curve 要加载数据的曲线（不可为null）
     */
    void completeCurve(StandardCurve curve);

    /***
     * 获取点数据
     * @return 点数据
     */
    List<Point> getPointList(String point_list);
    /***
     * 批量删除曲线
     * @param curves 所有曲线
     * @param deleteCurves 要删除的曲线
     * @return 删除后的曲线列表 curves-deleteCurves
     */
    List<StandardCurve> deleteCurves(List<StandardCurve> curves,List<StandardCurve> deleteCurves);
}
