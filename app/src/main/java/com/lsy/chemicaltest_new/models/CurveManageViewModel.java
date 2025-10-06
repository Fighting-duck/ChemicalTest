package com.lsy.chemicaltest_new.models;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.room.Transaction;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.domain.History_multiple;
import com.lsy.chemicaltest_new.domain.Point;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.implement.StandardCurveDataImpl;
import com.lsy.chemicaltest_new.utils.LiveDataUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class CurveManageViewModel extends ViewModel {
    private static final String TAG = "CurveManageViewModel";
    // 用于取消正在进行的任务
    private final ConcurrentHashMap<UUID, Future<?>> pendingTasks = new ConcurrentHashMap<>();
    MutableLiveData<List<StandardCurve>> liveData_showCurves = new MutableLiveData<>();
    private MutableLiveData<String> mLiveData_toast = new MutableLiveData<>();

    public MutableLiveData<String> getLiveData_toast(){
        return mLiveData_toast;
    }
    public  MutableLiveData<List<StandardCurve>> getLiveData_showCurves() {
        return liveData_showCurves;
    }

    public void setToast(String prompt){
        LiveDataUtils.safeUpdate(mLiveData_toast,prompt);
    }

    public interface UpdateCallback {
        void onUpdateCompleted();//更新成功
        void onEmptyCurve();//直线为空
        void onUpdateFailed(Exception e);//报错
    }
    /**
     * 更新标准曲线列表（线程安全 + 数据库事务）
     * @param filter 过滤条件（可为null）
     * @param callback 结果回调（不可为null）
     */
    public void updateCurves(String filter,@NonNull CurveManageViewModel.UpdateCallback callback) {
        // 参数校验
        Objects.requireNonNull(callback, "Callback cannot be null");
        MyApplication.DB_EXECUTOR.execute(() -> {
            List<StandardCurve> curves;
            try{
                // 1. 从数据库中读取标准曲线
                if (filter != null && !filter.trim().isEmpty()){
                    //从新读取数据库
                    curves = MyApplication.DATABASE_INSTANCE.getStandardCurveDao().findByFuzzy(filter);
                }
                else curves =  MyApplication.DATABASE_INSTANCE.getStandardCurveDao().getAll_Available();

                // 2. 如果标准曲线为空，则更新LiveData并返回
                if (curves == null || curves.isEmpty()) {
                    Log.d(TAG, "updateCurves: curves is null");
                    liveData_showCurves.postValue(Collections.emptyList());
                    callback.onEmptyCurve();
                    return;
                }

                // 3. 遍历标准曲线，为每个曲线加载关联数据
                for (StandardCurve curve : curves){
                    StandardCurveDataImpl.getInstance().completeCurve(curve);
                }

                // 3.更新LiveData
                Log.d(TAG, "Curves count: " + curves.size()); // 只记录数量而非敏感数据
                liveData_showCurves.postValue(curves);

                new Handler(Looper.getMainLooper()).post(callback::onUpdateCompleted);
            }catch (Exception e){
                callback.onUpdateFailed(e);
            }
        });
    }

    public List<StandardCurve> getCurves() {
       return liveData_showCurves.getValue();
    }
    public void setCurves(List<StandardCurve> curves){
        liveData_showCurves.setValue(curves);
    }



    public interface DeleteCallback {
        void onDeleteCompleted();//删除成功
        void onDeleteFailed(Exception e);//报错
    }
    /**
     * 批量删除标准曲线（线程安全 + 数据库事务）
     * @param deleteCurves 要删除的曲线（不可为null）
     * @param callback 结果回调（不可为null）
     */
    @Transaction
    public void deleteCurves(List<StandardCurve> deleteCurves, DeleteCallback callback) {
        // 参数校验
        Objects.requireNonNull(deleteCurves, "Curve cannot be null");
        Objects.requireNonNull(callback, "Callback cannot be null");

        final UUID taskId = UUID.randomUUID();
        Future<?> future = MyApplication.DB_EXECUTOR.submit(() -> {
            try{
                List<StandardCurve> curves = liveData_showCurves.getValue();
                List<StandardCurve> newCurves = StandardCurveDataImpl.getInstance().deleteCurves(curves,deleteCurves);
                liveData_showCurves.postValue(newCurves);
                // 成功回调
                new Handler(Looper.getMainLooper()).post(callback::onDeleteCompleted);
                pendingTasks.remove(taskId); // 任务完成时移除
            }catch (Exception e){
                new Handler(Looper.getMainLooper()).post(() -> callback.onDeleteFailed(e));
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
