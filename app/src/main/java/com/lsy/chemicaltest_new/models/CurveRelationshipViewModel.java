package com.lsy.chemicaltest_new.models;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.domain.Expression;
import com.lsy.chemicaltest_new.domain.Point;
import com.lsy.chemicaltest_new.domain.SensorData;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.implement.StandardCurveDataImpl;
import com.lsy.chemicaltest_new.utils.ConcentrationPredictor;

import java.util.ArrayList;
import java.util.List;

public class CurveRelationshipViewModel extends ViewModel {
    MutableLiveData<Integer> mLiveData_sampleID = new MutableLiveData<>();
    MutableLiveData<List<SensorData>> mLiveData_sensorData = new MutableLiveData<>();
    MutableLiveData<ConcentrationPredictor> mLiveData_predictor = new MutableLiveData<>();

    public MutableLiveData<Integer> getLiveData_sampleID() {
        return mLiveData_sampleID;
    }
    public MutableLiveData<List<SensorData>> getLiveData_sensorData() {
        return mLiveData_sensorData;
    }
    public MutableLiveData<ConcentrationPredictor> getLiveData_predictor() {
        return mLiveData_predictor;
    }

    public void setSampleID(int sampleId) {
        mLiveData_sampleID.setValue(sampleId);
        List<SensorData> sensorData = extractDataPoints(sampleId);
        setSensorData(sensorData);
    }
    public void setSensorData(List<SensorData> sensorData) {
        mLiveData_sensorData.setValue(sensorData);
        trainModel(sensorData);
    }
    public void setPredictor(ConcentrationPredictor predictor) {
        mLiveData_predictor.setValue(predictor);
    }

    /***
     * 在数据库中用样品id获取三个曲线数据，并提取各自提取点集
     * @param sampleId 样品id
     *
     */
    public List<SensorData> extractDataPoints(int sampleId) {
        List<StandardCurve> curves = MyApplication.DATABASE_INSTANCE.getStandardCurveDao().findBySampleId(sampleId);
        List<Point> pointList_elec = new ArrayList<>();
        List<Point> pointList_colo = new ArrayList<>();
        List<Point> pointList_thermal = new ArrayList<>();
        Expression expression_elec = null;
        Expression expression_colo = null;
        Expression expression_thermal = null;
        //1. 填充点集和表达式
        for (StandardCurve curve : curves){
            List<Point> points = StandardCurveDataImpl.getInstance().getPointList(curve.getPoint_set());//已按x轴升序排列
            Expression expression = Expression.buildExpression(curve.getExpression());
            switch (curve.getType()){
                case 1:
                    pointList_elec.addAll(points);
                    expression_elec = expression;
                    break;
                case 2:
                    pointList_colo.addAll(points);
                    expression_colo = expression;
                    break;
                case 3:
                    pointList_thermal.addAll(points);
                    expression_thermal = expression;
                    break;
            }
        }
        // 2. 表达式为空直接返回
        if (expression_elec == null || expression_colo == null || expression_thermal == null) return null;
        // 3.汇集这三个点集中所有不同x数据点，若存在无x值的点则通过公式补全
        List<SensorData> sensorDataList = new ArrayList<>();// 创建数据对象
        int i=0,j=0,k=0;
        while (i<pointList_elec.size() || j<pointList_colo.size() || k<pointList_thermal.size()){
            SensorData sensorData = new SensorData();
            float lgx_elec = (i < pointList_elec.size()) ? pointList_elec.get(i).getX_value() : Float.MAX_VALUE;
            float lgx_colo = (j < pointList_colo.size()) ? pointList_colo.get(j).getX_value() : Float.MAX_VALUE;
            float lgx_thermal = (k < pointList_thermal.size()) ? pointList_thermal.get(k).getX_value() : Float.MAX_VALUE;
            //取最小的x值
            Float lgx_min = Math.min(lgx_elec,
                    Math.min(lgx_colo,lgx_thermal));
            if (lgx_min == Float.MAX_VALUE) break;
            sensorData.setLogConcentration(lgx_min);//设置lgX
            if (lgx_min.equals(lgx_elec))
                sensorData.setCurrent(pointList_elec.get(i++).getY_value());
            else
                sensorData.setCurrent(expression_elec.calculateX_toY(lgx_min));
            if (lgx_min.equals(lgx_colo))
                sensorData.setBValue(pointList_colo.get(j++).getY_value());
            else
                sensorData.setBValue(expression_colo.calculateX_toY(lgx_min));
            if (lgx_min.equals(lgx_thermal))
                sensorData.setTemperature(pointList_thermal.get(k++).getY_value());
            else
                sensorData.setTemperature(expression_thermal.calculateX_toY(lgx_min));
            sensorDataList.add(sensorData);
        }
        return sensorDataList;
    }
    /***
     * 训练模型
     * @param sensorDataList 训练集
     * @return 曲线数据
     */
    public void trainModel(List<SensorData> sensorDataList) {
        ConcentrationPredictor predictor = mLiveData_predictor.getValue();
        if (predictor == null){
            predictor = new ConcentrationPredictor();
        }
        predictor.trainModel(sensorDataList);
        mLiveData_predictor.setValue(predictor);
    }

    /***
     * 预测浓度
     * @param current 电流
     * @param temperature 温度
     * @param bValue B值
     * @return 浓度
     */
    public double predictConcentration(double current, double temperature, double bValue) {
        ConcentrationPredictor predictor = mLiveData_predictor.getValue();
        if (predictor == null) return -1;
        return predictor.predictConcentration(current, temperature, bValue);
    }
}
