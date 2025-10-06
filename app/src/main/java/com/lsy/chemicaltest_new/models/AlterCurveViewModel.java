package com.lsy.chemicaltest_new.models;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.room.Transaction;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.database.AppDatabase;
import com.lsy.chemicaltest_new.domain.Point;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.implement.StandardCurveDataImpl;
import com.lsy.chemicaltest_new.utils.LiveDataUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class AlterCurveViewModel extends ViewModel {
    private static final String TAG = "AlterCurveViewModel";
    public enum AlterState {
        LOADING,
        SUCCESS,
        ERROR,
        EMPTY
    }
    // 用于取消正在进行的任务
    private final ConcurrentHashMap<UUID, Future<?>> pendingTasks = new ConcurrentHashMap<>();
    StandardCurve mOldCurve;
    private MutableLiveData<String> mLiveData_toast = new MutableLiveData<>();
    public MutableLiveData<AlterState> mLiveData_alterState = new MutableLiveData<>();
    Context mContext;
    public void setContext(Context context){
        mContext = context;
    }

    public MutableLiveData<String> getLiveData_toast(){
        return mLiveData_toast;
    }
    public MutableLiveData<AlterState> getLiveData_alterState(){
        return mLiveData_alterState;
    }
    public void setToast(String toast){
        LiveDataUtils.safeUpdate(mLiveData_toast,toast);
    }

    public StandardCurve getOldCurve() {
        return mOldCurve;
    }

    public void setOldCurve(StandardCurve mOldCurve) {
        this.mOldCurve = mOldCurve;
    }
    /***
     * 在数据库中修改曲线
     * @param newCurve 新的曲线
     */
    @Transaction
    public void AlterStandardCurve(@NonNull StandardCurve newCurve) {
        //参数检验
        Objects.requireNonNull(newCurve, "Curve cannot be null");

        Log.d(TAG, "AlterStandardCurve: newCurve:" + newCurve);
        Log.d(TAG, "AlterStandardCurve: mOldCurve:" + mOldCurve);

        if (mOldCurve == null) {
            setToast(mContext.getString(R.string.toast_update_fail));
            return;
        }
        final UUID taskId = UUID.randomUUID();

        Future<?> future = MyApplication.DB_EXECUTOR.submit(() -> {
            try{
                // 1. 查看是否有相同曲线名曲线
                if (!newCurve.getName().equals(mOldCurve.getName())) {
                    StandardCurve curve = MyApplication.DATABASE_INSTANCE.getStandardCurveDao().findByName(newCurve.getName());
                    if (curve != null) {
                        setToast(mContext.getString(R.string.toast_curve_NameRepeat));
                        mLiveData_alterState.postValue(AlterState.ERROR);
                        return;
                    }
                }
                Log.d(TAG, "AlterStandardCurve: newCurve:" + "无相同曲线名曲线");
                // 2. 检查点集是否改变
                List<Point> newPointList = newCurve.getPointList();
                if (newPointList == null || newPointList.size() < 2) {
                    setToast(mContext.getString(R.string.toast_curve_atLeastTwoPoints));
                    mLiveData_alterState.postValue(AlterState.ERROR);
                    return;
                }
                // 3.1 如果点集未改变，直接更新曲线
                if (newPointList.equals(mOldCurve.getPointList())) {
                    Log.d(TAG, "AlterStandardCurve: newCurve:" + "点集无改变");
                    MyApplication.DATABASE_INSTANCE.getStandardCurveDao().update(newCurve);
                    setToast(mContext.getString(R.string.toast_update_success));
                    mLiveData_alterState.postValue(AlterState.SUCCESS);
                    return;
                }
                Log.d(TAG, "AlterStandardCurve: newCurve:" + "点集改变");
                // 3.2 如果点集改变，则更新点集
                List<Long> points_id = StandardCurveDataImpl.getInstance().alterPoints(mOldCurve.getPointList(), newPointList);
                if (points_id == null || points_id.isEmpty()) {
                    setToast(mContext.getString(R.string.toast_update_fail_exception));
                    mLiveData_alterState.postValue(AlterState.ERROR);
                    return;
                }
                newCurve.setPoint_set(points_id.toString());
                // 4. 更新曲线
                MyApplication.DATABASE_INSTANCE.getStandardCurveDao().update(newCurve);
                setToast(mContext.getString(R.string.toast_update_success));
                mLiveData_alterState.postValue(AlterState.SUCCESS);
                pendingTasks.remove(taskId); // 任务完成时移除
            }catch (Exception e) {
                setToast(mContext.getString(R.string.toast_update_fail_exception));
                mLiveData_alterState.postValue(AlterState.ERROR);
                pendingTasks.remove(taskId); // 任务完成时移除
            }
        });
        pendingTasks.put(taskId, future);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        shutdownExecutorGracefully();
    }
    /**
     * 优雅关闭方案（允许完成已提交的任务）
     */
    private void shutdownExecutorGracefully() {
        // 取消所有pending任务
        pendingTasks.forEach((id, future) -> {
            if (!future.isDone()) {
                future.cancel(true); // 中断运行中的任务
            }
        });
        pendingTasks.clear();
    }
}
