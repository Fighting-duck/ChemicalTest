package com.lsy.chemicaltest_new.models;

import static com.lsy.chemicaltest_new.utils.DynamicStringUtils.getString;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.room.Transaction;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.database.AppDatabase;
import com.lsy.chemicaltest_new.database.DataRepository;
import com.lsy.chemicaltest_new.domain.CorrectCurveItem;
import com.lsy.chemicaltest_new.domain.Expression;
import com.lsy.chemicaltest_new.domain.Point;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.domain.TestValue;
import com.lsy.chemicaltest_new.implement.StandardCurveDataImpl;
import com.lsy.chemicaltest_new.utils.LiveDataUtils;
import com.lsy.chemicaltest_new.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class CorrectCurveViewModel extends ViewModel {
    public static final String TAG = "CorrectCurveViewModel";
    // 用于取消正在进行的任务
    private final ConcurrentHashMap<UUID, Future<?>> pendingTasks = new ConcurrentHashMap<>();
    MutableLiveData<List<CorrectCurveItem>> mLiveData_CurveItems = new MutableLiveData<>();//列表项
    MutableLiveData<StandardCurve> mLiveData_standardCurve = new MutableLiveData<>();
    MutableLiveData<List<Point>> mLiveData_pointList = new MutableLiveData<>();//old点列表
    MutableLiveData<List<Point>> mLiveData_correctedPointList = new MutableLiveData<>();//new点列表
    MutableLiveData<String> mLiveData_prompt = new MutableLiveData<>();

    public MutableLiveData<List<CorrectCurveItem>> getLiveData_CurveItems() {
        return mLiveData_CurveItems;
    }

    public void setCurveItems(List<CorrectCurveItem> curveItems) {
        this.mLiveData_CurveItems.setValue(curveItems);
    }

    public MutableLiveData<StandardCurve> getLiveData_standardCurve() {
        return mLiveData_standardCurve;
    }

    public void setStandardCurve(StandardCurve standardCurve) {
        this.mLiveData_standardCurve.setValue(standardCurve);
        // 初始化点列表
        List<Point> pointList = standardCurve.getPointList();
        setPointList(pointList);
        // 初始化列表项
        List<CorrectCurveItem> curveItems = new ArrayList<>();
        for (Point point : pointList) {
            CorrectCurveItem item = new CorrectCurveItem();
            item.setPoint(point);
            curveItems.add(item);
        }
        setCurveItems(curveItems);
    }

    public MutableLiveData<List<Point>> getLiveData_pointList() {
        return mLiveData_pointList;
    }

    public void setPointList(List<Point> pointList) {
        this.mLiveData_pointList.setValue(pointList);
    }

    public MutableLiveData<List<Point>> getLiveData_correctedPointList() {
        return mLiveData_correctedPointList;
    }

    public void setCorrectedPointList(List<Point> pointList) {
        this.mLiveData_correctedPointList.setValue(pointList);
    }

    public MutableLiveData<String> getLiveData_prompt() {
        return mLiveData_prompt;
    }

    public void setToast(String prompt) {
        LiveDataUtils.safeUpdate(mLiveData_prompt, prompt);
    }

    public void deleteItem(int position) {
        List<CorrectCurveItem> curveItems = mLiveData_CurveItems.getValue();
        List<Point> pointList = mLiveData_pointList.getValue();
        if (curveItems != null && pointList != null) {
            if (position >= 0 && position < curveItems.size()){
                pointList.remove(position);
                curveItems.remove(position);
                setPointList(pointList);
                setCurveItems(curveItems);
            }
        }
    }

    public void setTestValue(int mPosition, TestValue testValue) {
        List<CorrectCurveItem> curveItems = mLiveData_CurveItems.getValue();
        if (curveItems != null && !curveItems .isEmpty() && mPosition >= 0 && mPosition < curveItems.size()) {
            Log.d(TAG, "setTestValue: " + mPosition+"  "+testValue);
            CorrectCurveItem item = curveItems.get(mPosition);
            item.setCorrected_y(testValue.getValue());
            item.setAdd_time(testValue.getTestTime());
            setCurveItems(curveItems);
            setCorrectedPointList(CorrectCurveItem.getCorrectedPointList(curveItems));
        }
    }

    public void deleteTestValue(int position) {
        List<CorrectCurveItem> pointList = mLiveData_CurveItems.getValue();
        if (pointList != null && pointList.size() > position) {
            pointList.get(position).setCorrected_y(null);
            setCurveItems(pointList);
            setCorrectedPointList(CorrectCurveItem.getCorrectedPointList(pointList));
        }
    }

    public TestValue createTestValue(float value) {
        StandardCurve standardCurve = mLiveData_standardCurve.getValue();
        if (standardCurve != null) {
            TestValue testValue = new TestValue();
            testValue.setValue(value);
            testValue.setTestTime(TimeUtil.getCurrentDateTime());
            testValue.setUnit(standardCurve.getY_axis_unit());
            return testValue;
        }
        return null;
    }

    public Integer getCurveType() {
        StandardCurve standardCurve = mLiveData_standardCurve.getValue();
        return standardCurve == null ? null : standardCurve.getType();
    }

    public String getUnit_x() {
        StandardCurve standardCurve = mLiveData_standardCurve.getValue();
        return standardCurve == null ? null : standardCurve.getX_axis_unit();
    }
    @Transaction
    public void saveCurve(Expression  expression,Float r) {
        StandardCurve standardCurve = mLiveData_standardCurve.getValue();
        List<Point> oldPointList = mLiveData_pointList.getValue();
        List<Point> newPointList = mLiveData_correctedPointList.getValue();
        if (standardCurve == null || oldPointList == null) {
            setToast(getString(R.string.toast_update_fail));
            return;
        }
        if (newPointList==null || expression == null || r == null){
            return;
        }

        final UUID taskId = UUID.randomUUID();
        Future<?> future = MyApplication.DB_EXECUTOR.submit(() -> {
            try{
                standardCurve.setPointList(newPointList);
                standardCurve.setFormula(expression);
                standardCurve.setExpression(expression.getK() + "," + expression.getB());
                standardCurve.setCORR(r);
                // 保存点集
                List<Long> pointIds = StandardCurveDataImpl.getInstance().alterPoints(oldPointList,newPointList);
                standardCurve.setPoint_set(pointIds.toString());
                // 保存标准曲线
                MyApplication.DATABASE_INSTANCE.getStandardCurveDao().update(standardCurve);
                setToast(getString(R.string.toast_update_success));
                DataRepository.getInstance().setStandardCurve(standardCurve);
                pendingTasks.remove(taskId); // 任务完成时移除
            }catch (Exception e){
                setToast(getString(R.string.toast_update_fail));
                e.printStackTrace();
                pendingTasks.remove(taskId); // 任务完成时移除
            }
        });
        pendingTasks.put(taskId, future);
    }
}
