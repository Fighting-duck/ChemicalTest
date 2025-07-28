package com.lsy.chemicaltest_new.models;

import android.content.Context;
import android.database.sqlite.SQLiteDatabaseLockedException;

import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.room.Transaction;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.domain.Point;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.utils.LiveDataUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;


public class AddCurveViewModel extends ViewModel{
    // 用于取消正在进行的任务
    private final ConcurrentHashMap<UUID, Future<?>> pendingTasks = new ConcurrentHashMap<>();
    public enum SaveState {
        LOADING,
        SUCCESS,
        ERROR,
        EMPTY
    }
    private static final String TAG = "AddCurveViewModel";
    Context mContext;
    public void setContext(Context context){
        mContext = context;
    }
    private final MutableLiveData<String> mLiveData_toast = new MutableLiveData<>();
    private final MutableLiveData<SaveState> mLiveData_saveState = new MutableLiveData<>(SaveState.EMPTY);
    public MutableLiveData<String> getLiveData_toast(){
        return mLiveData_toast;
    }
    public MutableLiveData<SaveState> getLiveData_saveState(){
        return mLiveData_saveState;
    }
    public void setToast(String toast){
        LiveDataUtils.safeUpdate(mLiveData_toast,toast);
    }

    /**
     * 保存曲线
     * @param curve 曲线
     */
    public void saveStandardCurve(@NonNull StandardCurve curve) {
        //参数检验
        Objects.requireNonNull(curve, "Curve cannot be null");

        final UUID taskId = UUID.randomUUID();

        Future<?> future = MyApplication.DB_EXECUTOR.submit(() -> {
            try{
                // 1. 查看是否有相同曲线名曲线（去前后空格）
                StandardCurve curve1 = MyApplication.DATABASE_INSTANCE.getStandardCurveDao().findByName(curve.getName().trim());
                if (curve1 != null) {
                    setToast(mContext.getString(R.string.toast_curve_NameRepeat));
                    mLiveData_saveState.postValue(SaveState.ERROR);
                    return;
                }
                // 2.检验点数据
                List<Point> pointList = curve.getPointList();
                if (pointList == null || pointList.size() < 2) {
                    setToast(mContext.getString(R.string.toast_curve_atLeastTwoPoints));
                    mLiveData_saveState.postValue(SaveState.ERROR);
                    return;
                }
                // 3. 开启事务保存数据（带重试逻辑）
                saveWithRetry(curve, pointList,3);
                pendingTasks.remove(taskId); // 任务完成时移除
            }catch (Exception e){
                Log.e(TAG, "保存失败: " + e.getMessage(), e);
                setToast(mContext.getString(R.string.toast_save_fail));
                mLiveData_saveState.postValue(SaveState.ERROR);
                pendingTasks.remove(taskId); // 任务完成时移除
            }
        });
        pendingTasks.put(taskId, future);
    }
    /***
     * 保存曲线(带重复策略)
     * @param curve 曲线
     * @param pointList 点集
     * @param retryCount 重试次数
     */
    @Transaction
    private void saveWithRetry(StandardCurve curve, List<Point> pointList, int retryCount) {
        try {
            // 1.保存点数据
            List<Long> pointIds = savePoints(pointList);
            if (pointIds == null || pointIds.size() != pointList.size()) {
                throw new IllegalStateException("保存点失败");
            }
            // 2.保存点集到数据库
            String str_ids = pointIds.toString();//点集保存在数据库中形成的id字符串
            curve.setPoint_set(str_ids);

            // 3.保存曲线主体（使用ABORT冲突策略）
            long curveId = MyApplication.DATABASE_INSTANCE.getStandardCurveDao().add(curve);
            if (curveId <= 0) {
                throw new IllegalStateException("保存曲线失败");
            }
            // 4.保存成功
            mLiveData_saveState.postValue(SaveState.SUCCESS);
        }catch (SQLiteDatabaseLockedException e){
            if (retryCount > 0) {
                Log.w(TAG, "数据库锁等待，剩余重试次数: " + retryCount);
                long delayMillis = (long) (100 * Math.pow(2, 3 - retryCount)); // 指数退避策略
                SystemClock.sleep(delayMillis);
                // 尝试重新执行保存操作
                saveWithRetry(curve, pointList, retryCount - 1);
            } else {
                setToast(mContext.getString(R.string.toast_save_fail_tryAgain));
                mLiveData_saveState.postValue(SaveState.ERROR);
            }
        } catch (Exception e) { // 对其他异常的处理
            Log.e(TAG, "保存失败: " + e.getMessage(), e);
            setToast(mContext.getString(R.string.toast_save_fail_exception));
            mLiveData_saveState.postValue(SaveState.ERROR);
        }
    }
    /***
     * 在数据库中保存曲线的point
     * @return 所有point的id
     */
    public List<Long> savePoints(List<Point> pointList) {
        long[] ids = MyApplication.DATABASE_INSTANCE.getPointDao().insertAllWithRollback(pointList);

        // 将基本类型long数组转换为包装类型Long列表
        List<Long> result = new ArrayList<>(ids.length);
        for (long id : ids) {
            result.add(id); // 自动装箱从long转换为Long
        }

        return result;
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
