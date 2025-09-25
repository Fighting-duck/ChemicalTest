package com.lsy.chemicaltest_new.models;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.room.Transaction;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.domain.Expression;
import com.lsy.chemicaltest_new.domain.History_multiple;
import com.lsy.chemicaltest_new.domain.Point;
import com.lsy.chemicaltest_new.domain.Sample;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.utils.LiveDataUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
                    completeCurve(curve);
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
    /**
     * 为标准曲线加载关联数据(并行加载)
     * @param curve 要加载数据的曲线（不可为null）
     */
    public void completeCurve(StandardCurve curve) {
        if (curve == null) return;
        try {
            // 并行加载关联数据
            CompletableFuture<Sample> sampleFuture = CompletableFuture.supplyAsync(
                    () -> MyApplication.DATABASE_INSTANCE.getSampleDao().findById(curve.getSample_id()),
                    MyApplication.DB_EXECUTOR
            );

            CompletableFuture<List<Point>> pointsFuture = CompletableFuture.supplyAsync(
                    () -> StandardCurve.getPointList(curve.getPoint_set()),
                    MyApplication.DB_EXECUTOR
            );

            CompletableFuture<Expression> expressionFuture = CompletableFuture.supplyAsync(
                    () -> Expression.buildExpression(curve.getExpression()),
                    MyApplication.DB_EXECUTOR
            );

            // 等待所有结果（带超时）
            curve.setSample(sampleFuture.get(1, TimeUnit.SECONDS));
            curve.setPointList(pointsFuture.get(1, TimeUnit.SECONDS));
            curve.setFormula(expressionFuture.get(1, TimeUnit.SECONDS));

        } catch (Exception e) {
            Log.w(TAG, "Complete curve failed: " + e.getMessage());
        }

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
                for (StandardCurve curve : deleteCurves){
                    //1. 查看该曲线是否在检测实验中用过
                    List<History_multiple> histories = MyApplication.DATABASE_INSTANCE.getHistory_multipleDao().findByCurveId(curve.getId());
                    if (histories!=null && !histories.isEmpty()){
                        // 2.用过则将validity设为0 curve.setValidity(0);
                        Log.d("deleteCurve", "update curveId=" + curve+" 's validity=0.");
                        MyApplication.DATABASE_INSTANCE.getStandardCurveDao().updateValidity(curve.getId(),0);
                    }
                    else {
                        // 2.没用过则删除
                        Log.d("deleteCurve", "delete curveId=" + curve);
                        // 硬删除（带事务）
                        MyApplication.DATABASE_INSTANCE.runInTransaction(() -> {
                            // 先删除关联点
                            List<Point> points = curve.getPointList();
                            if (points != null && !points.isEmpty()) {
                                MyApplication.DATABASE_INSTANCE.getPointDao()
                                        .delete(points.toArray(new Point[0]));
                            }
                            // 再删除曲线
                            MyApplication.DATABASE_INSTANCE.getStandardCurveDao()
                                    .deleteById(curve.getId());
                        });
                    }
                    // 3.更新UI数据（主线程）
                    if (curves!=null && !curves.isEmpty()){
                        curves.removeIf(curveToRemove -> curveToRemove.getId() == curve.getId());
                    }
                }
                liveData_showCurves.postValue(curves);
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
