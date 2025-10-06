package com.lsy.chemicaltest_new.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.domain.Expression;
import com.lsy.chemicaltest_new.domain.Point;
import com.lsy.chemicaltest_new.domain.Sample;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.implement.StandardCurveDataImpl;
import com.lsy.chemicaltest_new.utils.LiveDataUtils;

import java.util.ArrayList;
import java.util.List;

public class SelectCurveViewModel extends ViewModel {
    private final MutableLiveData<List<StandardCurve>> mLiveData_showCurves = new MutableLiveData<>();//在spinner中展示的curve
    private final MutableLiveData<StandardCurve> mLiveData_selectCurve = new MutableLiveData<>();//在spinner中选折的curve
    private MutableLiveData<String> mLiveData_toast = new MutableLiveData<>();
    public LiveData<List<StandardCurve>> getLiveData_showCurves() {
        return mLiveData_showCurves;
    }
    public LiveData<StandardCurve> getLiveData_selectCurve() {
        return mLiveData_selectCurve;
    }
    public MutableLiveData<String> getLiveData_toast(){
        return mLiveData_toast;
    }
    public void setToast(String prompt){
        LiveDataUtils.safeUpdate(mLiveData_toast,prompt);
    }


    /***
     * 根据曲线类型加载此类型全部曲线
     * @param type 曲线类型
     */
    public void updateShowCurves(int type) {
        List<StandardCurve> allCurves = MyApplication.DATABASE_INSTANCE.getStandardCurveDao().getAll_Available();
        if (allCurves == null) return;
        List<StandardCurve> showCurves = new ArrayList<>();
        if (type == 0) {
            // 当 type 为 0 时，就显示所有曲线
            showCurves = allCurves;
        }
        else{
            // 否则，筛选指定类型的曲线
            for (StandardCurve curve : allCurves) {
                if (curve.getType() == type) {
                    showCurves.add(curve);
                }
            }
        }
        mLiveData_showCurves.setValue(showCurves);
        // 选中第一条曲线，并在 mLiveData_showCurves 中更新
        selectStandardCurve(0);
    }

    /***
     * 获取选中的曲线
     * @return 选中的曲线
     */
    public StandardCurve getShowCurves() {
        return mLiveData_selectCurve.getValue();
    }

    /***
     * 选中某一曲线
     * @param position 选中的曲线在 mLiveData_allCurves 中的位置
     */
    public void selectStandardCurve(int position) {
        // 检查 mLiveData_showCurves 是否为空
        if (mLiveData_showCurves.getValue() == null || mLiveData_showCurves.getValue().isEmpty()) {
            return;
        }
        List<StandardCurve> curves = mLiveData_showCurves.getValue();
        if (position < 0 || position>=curves.size()){
            return;
        }
        StandardCurve selectCurve = curves.get(position);
        //获取样品
        Sample sample = MyApplication.DATABASE_INSTANCE.getSampleDao().findById(selectCurve.getSample_id());
        selectCurve.setSample(sample);
        //获取pointList
        List<Point> pointList = StandardCurveDataImpl.getInstance().getPointList(selectCurve.getPoint_set());
        selectCurve.setPointList(pointList);
        //构造expression
        String str_expression = selectCurve.getExpression();
        Expression expression = Expression.buildExpression(str_expression);
        selectCurve.setFormula(expression);
        mLiveData_selectCurve.setValue(selectCurve);
    }
}