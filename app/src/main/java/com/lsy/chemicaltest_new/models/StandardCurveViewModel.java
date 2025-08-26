package com.lsy.chemicaltest_new.models;

import static com.lsy.chemicaltest_new.utils.DynamicStringUtils.getString;

import android.util.Log;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.domain.Expression;
import com.lsy.chemicaltest_new.domain.Point;
import com.lsy.chemicaltest_new.domain.Sample;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.interfaces.OperateCurve;
import com.lsy.chemicaltest_new.utils.LiveDataUtils;
import com.lsy.chemicaltest_new.utils.TimeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StandardCurveViewModel extends ViewModel implements OperateCurve {
    private static final String TAG = "StandardCurveViewModel";
    private static final float tolerance = 1e-6f;//误差范围
    private static final int MAX_POINT_COUNT = 50;//最多点数

    MutableLiveData<List<Sample>> mLiveData_sampleList = new MutableLiveData<>();
    MutableLiveData<Integer> mLiveData_samplePosition = new MutableLiveData<>();//在sampleList中的位置
    MutableLiveData<Integer> mLiveData_curveType = new MutableLiveData<>();//曲线类型 1:elec 2:color 3:thermal
    MutableLiveData<String> mLiveData_curveName = new MutableLiveData<>();
    MutableLiveData<String> mLiveData_xUnit = new MutableLiveData<>();
    MutableLiveData<String> mLiveData_yUnit = new MutableLiveData<>();
    MutableLiveData<Float> mLiveData_x_min = new MutableLiveData<>();
    MutableLiveData<Float> mLiveData_x_max = new MutableLiveData<>();
    MutableLiveData<Float> mLiveData_minCORR = new MutableLiveData<>();
    MutableLiveData<Point> mLiveData_Point = new MutableLiveData<>();
    MutableLiveData<List<Point>> mLiveData_PointList = new MutableLiveData<>();
    MutableLiveData<Expression> mLiveData_Expression = new MutableLiveData<>();
    MutableLiveData<Float> mLiveData_CORR = new MutableLiveData<>();
    MutableLiveData<String> mLiveData_description = new MutableLiveData<>();
    MediatorLiveData<StandardCurve> mLiveData_Curve = new MediatorLiveData<>();
    MutableLiveData<String> mLiveData_toast = new MutableLiveData<>();
    MutableLiveData<String> mLiveData_CO_noticeCorr = new MutableLiveData<>();


    public MutableLiveData<List<Sample>> getLiveData_sampleList(){
        return mLiveData_sampleList;
    }
    public MutableLiveData<Integer> getLiveData_sample(){
        return mLiveData_samplePosition;
    }
    public MutableLiveData<Integer> getLiveData_curveType(){
        return mLiveData_curveType;
    }
    public MutableLiveData<String> getLiveData_curveName(){
        return mLiveData_curveName;
    }
    public MutableLiveData<Point> getLiveData_entry() {
        return mLiveData_Point;
    }
    public MutableLiveData<String> getLiveData_xUnit(){
        return mLiveData_xUnit;
    }
    public MutableLiveData<String> getLiveData_yUnit(){
        return mLiveData_yUnit;
    }
    public MutableLiveData<Float> getLiveData_minCORR(){
        return mLiveData_minCORR;
    }
    public MutableLiveData<List<Point>> getLiveData_pointList(){
        return mLiveData_PointList;
    }
    public MutableLiveData<Float> getLiveData_xMin(){
        return mLiveData_x_min;
    }
    public MutableLiveData<Float> getLiveData_xMax(){
        return mLiveData_x_max;
    }
    public MutableLiveData<Expression> getLiveData_Expression(){
        return mLiveData_Expression;
    }
    public MutableLiveData<Float> getLiveData_CORR(){
        return mLiveData_CORR;
    }
    public MediatorLiveData<StandardCurve> getMediatorLiveData_Curve(){
        return mLiveData_Curve;
    }
    public MutableLiveData<String> getLiveData_description(){
        return mLiveData_description;
    }
    public MutableLiveData<String> getLiveData_toast(){
        return mLiveData_toast;
    }
    public MutableLiveData<String> getLiveData_CO_noticeCorr(){
        return mLiveData_CO_noticeCorr;
    }
    public void setToast(String prompt){
        LiveDataUtils.safeUpdate(mLiveData_toast,prompt);
    }

    public StandardCurveViewModel() {
        mLiveData_Curve.addSource(mLiveData_curveName, this::updateCurveName);
        mLiveData_Curve.addSource(mLiveData_samplePosition, this::updateSample);
        mLiveData_Curve.addSource(mLiveData_curveType,this::updateCurveType);
        mLiveData_Curve.addSource(mLiveData_xUnit, this::updateXUnit);
        mLiveData_Curve.addSource(mLiveData_yUnit, this::updateYUnit);
        mLiveData_Curve.addSource(mLiveData_x_min, this::updateMinCO);
        mLiveData_Curve.addSource(mLiveData_x_max, this::updateMaxCO);
        mLiveData_Curve.addSource(mLiveData_minCORR, this::updateMinCORR);
        mLiveData_Curve.addSource(mLiveData_Expression, this::updateExpression);
        mLiveData_Curve.addSource(mLiveData_CORR, this::updateCORR);
        mLiveData_Curve.addSource(mLiveData_PointList, this::updatePointList);
        mLiveData_Curve.addSource(mLiveData_description, this::updateDescription);
    }

    // 更新曲线名称
    private void updateCurveName(String curveName) {
        StandardCurve curve = mLiveData_Curve.getValue();
        if (curve == null) {
            curve = new StandardCurve();
        }
        if (curveName != null) {
            curve.setName(curveName);
        }
        mLiveData_Curve.setValue(curve);
        Log.d(TAG, "updateCurveName: " + curve.toString());
    }
    // 更新曲线样本
    private void updateSample(Integer samplePosition) {
        StandardCurve curve = mLiveData_Curve.getValue();
        if (curve == null) {
            curve = new StandardCurve();
        }
        List<Sample> sampleList = mLiveData_sampleList.getValue();
        if (samplePosition != null && sampleList != null) {
            curve.setSample_id(sampleList.get(samplePosition).getId());
            curve.setSample(sampleList.get(samplePosition));
        }
        mLiveData_Curve.setValue(curve);
        Log.d(TAG, "updateSampleId: " + curve.toString());
    }
    // 更新曲线类型
    private void updateCurveType(Integer curveType) {
        StandardCurve curve = mLiveData_Curve.getValue();
        if (curve == null) {
            curve = new StandardCurve();
        }
        if (curveType!= null) {
            curve.setType(curveType);
        }
        mLiveData_Curve.setValue(curve);
        Log.d(TAG, "updateCurveType: " + curve.toString());
    }

    // 更新X轴单位
    private void updateXUnit(String xUnit) {
        StandardCurve curve = mLiveData_Curve.getValue();
        if (curve == null) {
            curve = new StandardCurve();
        }
        if (xUnit != null) {
            curve.setX_axis_unit(xUnit);
        }
        mLiveData_Curve.setValue(curve);
        Log.d(TAG, "updateXUnit: " + curve.toString());
    }

    // 更新Y轴单位
    private void updateYUnit(String yUnit) {
        StandardCurve curve = mLiveData_Curve.getValue();
        if (curve == null) {
            curve = new StandardCurve();
        }
        if (yUnit != null) {
            curve.setY_axis_unit(yUnit);
        }
        mLiveData_Curve.setValue(curve);
        Log.d(TAG, "updateYUnit: " + curve.toString());
    }

    // 更新最小CO值
    private void updateMinCO(Float min_CO) {
        StandardCurve curve = mLiveData_Curve.getValue();
        if (curve == null) {
            curve = new StandardCurve();
        }
        if (min_CO != null) {
            curve.setMin_CO(min_CO);
        }
        mLiveData_Curve.setValue(curve);
        Log.d(TAG, "updateMinCO: " + curve.toString());
    }

    // 更新最大CO值
    private void updateMaxCO(Float max_CO) {
        StandardCurve curve = mLiveData_Curve.getValue();
        if (curve == null) {
            curve = new StandardCurve();
        }
        if (max_CO != null) {
            curve.setMax_CO(max_CO);
        }
        mLiveData_Curve.setValue(curve);
        Log.d(TAG, "updateMaxCO: " + curve.toString());
    }

    // 更新最小相关性值
    private void updateMinCORR(Float minCORR) {
        StandardCurve curve = mLiveData_Curve.getValue();
        if (curve == null) {
            curve = new StandardCurve();
        }
        if (minCORR != null) {
            curve.setMinCorr(minCORR);
        }
        mLiveData_Curve.setValue(curve);
        Log.d(TAG, "updateMinCORR: " + curve.toString());
    }

    // 更新表达式
    private void updateExpression(Expression expression) {
        StandardCurve curve = mLiveData_Curve.getValue();
        if (curve == null) {
            curve = new StandardCurve();
        }
        if (expression != null) {
            curve.setExpression(expression.getK() + "," + expression.getB());
        }
        mLiveData_Curve.setValue(curve);
        Log.d(TAG, "updateExpression: " + curve.toString());
    }

    // 更新相关性值
    private void updateCORR(Float CORR) {
        StandardCurve curve = mLiveData_Curve.getValue();
        if (curve == null) {
            curve = new StandardCurve();
        }
        if (CORR != null) {
            curve.setCORR(CORR);
        }
        mLiveData_Curve.setValue(curve);
        Log.d(TAG, "updateCORR: " + curve.toString());
    }

    // 更新点列表
    private void updatePointList(List<Point> pointList) {
        StandardCurve curve = mLiveData_Curve.getValue();
        if (curve == null) {
            curve = new StandardCurve();
        }
        if (pointList != null) {
            curve.setPointList(pointList);
        }
        mLiveData_Curve.setValue(curve);
        Log.d(TAG, "updatePointList: " + curve.toString());
    }

    // 更新描述
    private void updateDescription(String description) {
        StandardCurve curve = mLiveData_Curve.getValue();
        if (curve == null) {
            curve = new StandardCurve();
        }
        if (description != null) {
            curve.setDescription(description);
        }
        mLiveData_Curve.setValue(curve);
        Log.d(TAG, "updateDescription: " + curve.toString());
    }

    /***
     * 初始化曲线参数
     * @param curve 曲线
     */
    public void setCurve(StandardCurve curve) {
        mLiveData_Curve.setValue(curve);
        mLiveData_curveName.setValue(curve.getName());
        mLiveData_curveType.setValue(curve.getType());
        Integer samplePosition = getSamplePositionById(curve.getSample_id());
        if (samplePosition != null)
            mLiveData_samplePosition.setValue(samplePosition);
        mLiveData_xUnit.setValue(curve.getX_axis_unit());
        mLiveData_yUnit.setValue(curve.getY_axis_unit());
        mLiveData_x_min.setValue(curve.getMin_CO());
        mLiveData_x_max.setValue(curve.getMax_CO());
        mLiveData_minCORR.setValue(curve.getMinCorr());
        if (curve.getExpression()!=null)
            mLiveData_Expression.setValue(curve.getFormula());
        if (curve.getCORR()!=null){
            mLiveData_CORR.setValue(curve.getCORR());
        }
        if (curve.getDescription()!=null)
            mLiveData_description.setValue(curve.getDescription());
        if (curve.getPointList()!=null && !curve.getPointList().isEmpty())
            mLiveData_PointList.setValue(curve.getPointList());
    }

    /***
     * 根据id获取样品在列表中的位置
     * @param id  id
     * @return 位置
     */
    private Integer getSamplePositionById(Integer id){
        List<Sample> sampleList = mLiveData_sampleList.getValue();
        if (sampleList == null) return null;
        for (int i = 0; i < sampleList.size(); i++){
            if (sampleList.get(i).getId() == id){
                return i;
            }
        }
        return null;
    }

    /***
     * 重置曲线名称
     * @param samplePosition 样品在列表中的位置
     * @param type 直线类型
     */
    public void set_curve_name(Integer samplePosition,int type) {
        List<Sample> sampleList = mLiveData_sampleList.getValue();
        if (sampleList != null && samplePosition != null && samplePosition < sampleList.size()) {
            String curveName = sampleList.get(samplePosition).getName() +
                    "-" + StandardCurve.getCurveType(type)+
                    "-"+getString(R.string.title_standardCurve);
            mLiveData_curveName.setValue(curveName);
        }
    }
    public void set_curve_name(String curveName){
        mLiveData_curveName.setValue(curveName);
    }

    /***
     * 设置曲线x轴单位，即浓度
     * @param xUnit x轴单位
     */
    public void set_curve_xUnit(String xUnit) {
        mLiveData_xUnit.setValue(xUnit);
    }
    /***
     * 设置曲线y轴单位，即电流、色度、
     * @param yUnit y轴单位
     */
    public void set_curve_yUnit(String yUnit) {
        mLiveData_yUnit.setValue(yUnit);
    }
    /***
     * 设置曲线最小值
     * @param xMin 新的最小值
     * @return 成功返回当前值，不成功返回旧值
     */
    @Override
    public Float set_curve_xMin(Float xMin) {
        if (xMin == null) {
            mLiveData_x_min.setValue(null);
            return null;
        }
       else {
            Float xMax = mLiveData_x_max.getValue();
            Float xMin_old = mLiveData_x_min.getValue();
            if (xMax != null && xMin>xMax)
                return xMin_old;
            else
                mLiveData_x_min.setValue(xMin);
            return xMin;
        }
    }

    /***
     * 设置曲线最大值
     * @param xMax 新的最大值
     * @return 成功返回当前值，不成功返回旧值
     */
    @Override
    public Float set_curve_xMax(Float xMax) {
        if (xMax == null){
            mLiveData_x_max.setValue(null);
            return null;
        }
        else {
            Float xMin = mLiveData_x_max.getValue();
            Float xMax_old = mLiveData_x_max.getValue();
            if (xMin != null && xMin>xMax)
                return xMax_old;
            else
                mLiveData_x_max.setValue(xMax);
            return xMax;
        }
    }

    /***
     * 设置直线最小相关系数
     * @param CORR 相关系数
     */
    public void set_curve_minCORR(Float CORR) {
        mLiveData_minCORR.setValue(CORR);
    }
    /***
     * 设置点的x坐标
     * @param x x坐标
     */
    @Override
    public void set_point_x(Double x){
        Point point = mLiveData_Point.getValue();
        if (point == null){
            point = new Point();
        }
        point.setX_value((float) Math.log10(x));//对x取对数
        mLiveData_Point.setValue(point);
    }

   /***
     * 设置点的y坐标
     * @param y y坐标
     */
    @Override
    public void set_point_y(Double y){
        Point point = mLiveData_Point.getValue();
        if (point == null){
            point = new Point();
        }
        point.setY_value(y.floatValue());
        mLiveData_Point.setValue(point);
    }

    /***
     * 设置添加点的时间
     * @param time 时间
     */
    public void set_point_time(String time){
        Point point = mLiveData_Point.getValue();
        if (point == null){
            point = new Point();
        }
        point.setAdd_time(time);
        mLiveData_Point.setValue(point);
    }
    /***
     * 设置相关系数
     * @param corr 相关系数
     */
    @Override
    public void set_curve_CORR(Float corr) {
        mLiveData_CORR.setValue(corr);
    }
    /***
     * 给mLiveData_Curve设置sample_id
     * @param position 样品在列表中的位置
     */
    public void setCurveSample(int position){
        Integer type = mLiveData_curveType.getValue();
        if (type != null)
            set_curve_name(position, type);
        mLiveData_samplePosition.setValue(position);
    }
    /**
     * 给 mLiveData_Curve 设置 type
     * @param type 曲线类型
     */
    public void setType(int type) {
        Integer samplePosition = mLiveData_samplePosition.getValue();
        if (samplePosition != null)
            set_curve_name(samplePosition, type);
        // 如果类型发生变化，更新 Y 轴单位
        if (mLiveData_curveType.getValue() !=null && mLiveData_curveType.getValue() != type) {
            set_curve_yUnit(getYUnitByType(type));
        }
        // 更新 LiveData
        mLiveData_curveType.setValue(type); // 确保 LiveData 的类型值是最新的
    }

    public void setExpression(Expression expression){
        mLiveData_Expression.setValue(expression);
    }

    public void setDescription(String description) {
        if (description != null)
            mLiveData_description.setValue(description);
    }

    /***
     * 通知COrr值是否属于正常范围
     */
    public void noticeCorr(){
        Float corr = mLiveData_CORR.getValue();
        Float minCORR = mLiveData_minCORR.getValue();
        Log.d(TAG, "currentCorr: "+corr +"   minCorr:"+minCORR);
        if (minCORR == null || corr == null) return;
        if (corr < minCORR){
            mLiveData_CO_noticeCorr.setValue(getString(R.string.toast_abnormal_LessThanMin));
        }
        else{
            mLiveData_CO_noticeCorr.setValue(getString(R.string.toast_normal));
        }
    }
    /**
     * 向图表中添加一个点。如果该点已存在，则更新该点。
     */
    public void addPoint() {
        // 从 LiveData 中获取当前的点列表
        List<Point> pointList = mLiveData_PointList.getValue();
        // 从 LiveData 中获取要添加或更新的点
        Point point = mLiveData_Point.getValue();
        // 如果点列表为空，则初始化一个新列表
        if (pointList == null) {
            Log.d(TAG, "addPoint: pointList is null,初始化列表");
            pointList = new ArrayList<>();
        }
        if (point == null) return;
        // 检验point x值、y值的有效性
        if (point.getX_value().isInfinite() || point.getX_value().isNaN()){
            setToast(getString(R.string.toast_curve_XValue_invalid));
            return;
        }
        if (point.getY_value().isInfinite() || point.getY_value().isNaN()){
            setToast(getString(R.string.toast_curve_YValue_invalid));
            return;
        }
        point.setAdd_time(TimeUtil.getCurrentDateTime());// 设置点的添加时间
        // 遍历点列表，检查是否存在相同的点
        for (int i = 0; i < pointList.size(); i++) {
            Point existingPoint = pointList.get(i);
            // 使用 Float.compare 进行浮点数比较，避免精度问题
            //if (Float.compare(existingPoint.getX_value(), point.getX_value()) == 0) {
            //两个值误差精度在误差范围类视为相同值
            if (Math.abs(existingPoint.getX_value() - point.getX_value()) <= tolerance) {
                Log.d(TAG, "更新点：" +existingPoint.toString() +"->" + point.toString());
                existingPoint.setY_value(point.getY_value());
                existingPoint.setAdd_time(point.getAdd_time());
                // 如果找到相同的点，进行更新
                pointList.set(i, existingPoint);
                setToast(getString(R.string.toast_curve_updatePoint));
                // 将更新后的点列表发布到 LiveData
                mLiveData_PointList.setValue(pointList);
                return ;
            }
        }
        // 检查列表长度，避免OOM
        if (pointList.size() >= MAX_POINT_COUNT) {
            setToast(getString(R.string.toast_curve_maxPoint)); // 提示“已达最大点数”
            return;
        }
        // 如果没有找到相同的点，添加新点
        pointList.add(new Point(point));
        Log.d(TAG, "添加点："+point.toString()+",列表长度："+pointList.size());
        // 对列表进行升序排序
        Collections.sort(pointList, (p1, p2) -> Float.compare(p1.getX_value(), p2.getX_value()));
        // 将更新后的点列表发布到 LiveData
        mLiveData_PointList.setValue(pointList);
        setToast(getString(R.string.toast_curve_addPoint));
    }
    /***
     * 根据索引删除图表中一个点
     * @param position 索引
     * @return 返回被删除的点
     */
    public Point removePoint(int position) {
        // 从 LiveData 中获取当前的点列表
        List<Point> pointList = mLiveData_PointList.getValue();
        // 如果列表为空或者 position 超出范围，返回 false
        if (pointList == null || pointList.isEmpty() || position < 0 || position >= pointList.size()) {
            return null;
        }
        // 删除并返回被删除的点
        Point deletedPoint = pointList.remove(position);
        if (deletedPoint != null)
            Log.d(TAG, "删除点："+deletedPoint.toString());
        mLiveData_PointList.setValue(pointList);
        return deletedPoint;
    }
    /***
     * 通过x值计算y值
     * @param y x值
     * @return y值
     */
    public Float calculateCo_toY(Float y){
        Log.d(TAG, "计算y值："+y);
        Expression expression = mLiveData_Expression.getValue();
        if (expression == null){
            return null;
        }
        else {
            Log.d(TAG, "表达式："+expression.toString());
            return expression.calculateX_toY(y);
        }
    }
    /***
     * 更新样品列表
     */
    public void updateSampleList(){
        List<Sample> sampleList = MyApplication.DATABASE_INSTANCE.getSampleDao().getAll_Available();
        mLiveData_sampleList.setValue(sampleList);
    }
    /***
     * 根据曲线类型得到y轴默认单位
     * @param type 曲线类型
     * @return y轴默认单位
     */
    public String getYUnitByType(int type) {
        switch (type) {
            case 1:
                return getString(R.string.unit_mA);
            case 2:
                return getString(R.string.unit_blue);
            case 3:
                return getString(R.string.unit_degree);
            default:
                return "";
        }
    }
    public Integer getCurveType(){
        return mLiveData_curveType.getValue();
    }
    /***
     * 通过样品id得到样品名
     * @param id 样品id
     * @return 样品名
     */
    public String getSampleNameById(Integer id){
        List<Sample> sampleList = mLiveData_sampleList.getValue();
        if (sampleList!= null){
            for (Sample sample : sampleList){
                if (sample.getId() == id){
                    return sample.getName();
                }
            }
        }
        return "";
    }
    /***
     * 返回选中样品位置index
     * @return 样品位置index
     */
    public int getSelectSamplePosition(){
        List<Sample> sampleList = mLiveData_sampleList.getValue();
        StandardCurve curve = mLiveData_Curve.getValue();
        if (sampleList!= null && curve != null){
            int sample_id = curve.getSample_id();
            for (int i = 0; i < sampleList.size(); i++){
                if (sampleList.get(i).getId() == sample_id){
                    return i;
                }
            }
        }
        return -1;
    }

    public StandardCurve getCurve() {
        return mLiveData_Curve.getValue();
    }

    public String getNoticeCorr() {
        return mLiveData_CO_noticeCorr.getValue();
    }

    public void restoreXUnit() {
        if (mLiveData_Curve.getValue() != null) {
            String x_unit = mLiveData_Curve.getValue().getX_axis_unit();
            if (x_unit != null)
                mLiveData_xUnit.setValue(x_unit);
        }
    }

    public void restoreCurveName() {
        if (mLiveData_Curve.getValue() != null) {
            String curve_name = mLiveData_Curve.getValue().getName();
            if (curve_name != null)
                mLiveData_curveName.setValue(curve_name);
        }
    }

    public void restoreYUnit() {
        if (mLiveData_Curve.getValue() != null) {
            String y_unit = mLiveData_Curve.getValue().getY_axis_unit();
            if (y_unit != null)
                mLiveData_yUnit.setValue(y_unit);
        }
    }

    public void restoreXMin() {
        if (mLiveData_Curve.getValue() != null) {
            Float x_min = mLiveData_Curve.getValue().getMin_CO();
            if (x_min != null)
                mLiveData_x_min.setValue(x_min);
        }
    }

    public void restoreXMax() {
        if (mLiveData_Curve.getValue() != null) {
            Float x_max = mLiveData_Curve.getValue().getMax_CO();
            if (x_max != null)
                mLiveData_x_max.setValue(x_max);
        }
    }

    public void restoreMinCORR() {
        if (mLiveData_Curve.getValue() != null) {
            Float min_CORR = mLiveData_Curve.getValue().getMinCorr();
            if (min_CORR != null)
                mLiveData_minCORR.setValue(min_CORR);
        }
    }
}
