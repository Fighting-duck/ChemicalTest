package com.lsy.chemicaltest_new.models;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.room.Transaction;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.domain.History_multiple;
import com.lsy.chemicaltest_new.utils.LiveDataUtils;
import com.lsy.chemicaltest_new.utils.PhotoUtil;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class HistoryPreviewViewModel extends ViewModel {
    private final String TAG = "HistoryPreviewViewModel";
    // 用于取消正在进行的任务
    private final ConcurrentHashMap<UUID, Future<?>> pendingTasks = new ConcurrentHashMap<>();
    private final MutableLiveData<History_multiple> mLiveData_history = new MutableLiveData<>();

    public LiveData<History_multiple> getLiveData_history() {
        return mLiveData_history;
    }
    private final MutableLiveData<String> mLiveData_toast = new MutableLiveData<>();
    public MutableLiveData<String> getLiveData_toast(){
        return mLiveData_toast;
    }
    public void setToast(String prompt){
        LiveDataUtils.safeUpdate(mLiveData_toast,prompt);
    }

    public void setHistory(History_multiple history) {
        LiveDataUtils.safeUpdate(mLiveData_history, history);
    }

    // 定义一个回调接口
    public interface DeleteHistoryCallback {
        void onDeleteSuccess();
        void onDeleteFailure(Exception e);
    }
    /***
     * 删除历史记录
     * @param callback 回调接口
     */
    @Transaction
    public void deleteHistory(@NonNull DeleteHistoryCallback callback) {
        // 参数检验
        Objects.requireNonNull(callback, "Callback cannot be null");

        // 获取历史记录
        History_multiple history_multiple = mLiveData_history.getValue();
        if (history_multiple == null) {
            return;
        }

        final  UUID taskId = UUID.randomUUID();
        Future<?> future = MyApplication.DB_EXECUTOR.submit(() -> {
            try{
                // 0.删除历史记录
                MyApplication.DATABASE_INSTANCE.getHistory_multipleDao().delete(history_multiple);//删除
                // 1.删除万用表检测电流历史记录
                if (history_multiple.getElec_id() != null) {
                    MyApplication.DATABASE_INSTANCE.getElecTestResultDao().deleteById(history_multiple.getElec_id());
                }
                // 2.删除万用表检测温度历史记录
                if (history_multiple.getDegree_id() != null) {
                    MyApplication.DATABASE_INSTANCE.getElecTemperatureDao().deleteById(history_multiple.getDegree_id());
                }
                // 3.删除比色图像检测历史记录
                if (history_multiple.getColo_id() != null && history_multiple.getColoTestResult()!=null) {
                    // 删除原始图像
                    String originalImage_path = history_multiple.getColoTestResult().getOriginalImage_path();
                    if (originalImage_path != null) PhotoUtil.deleteImage(originalImage_path);
                    // 删除裁剪图像
                    String cropImage_path = history_multiple.getColoTestResult().getCropImage_path();
                    if (cropImage_path != null) PhotoUtil.deleteImage(cropImage_path);
                    // 删除数据库记录
                    MyApplication.DATABASE_INSTANCE.getColoTestResultDao().deleteById(history_multiple.getColo_id());
                }
                // 4.删除热力图像检测历史记录
                if (history_multiple.getThermal_id() != null && history_multiple.getThermalTestResult()!=null) {
                    // 删除图像
                    String path = history_multiple.getThermalTestResult().getThermalBitmap_path();
                    if (path != null) PhotoUtil.deleteImage(path);
                    // 删除数据库记录
                    MyApplication.DATABASE_INSTANCE.getThermalTestResultDao().deleteById(history_multiple.getThermal_id());
                }
                MyApplication.INSTANCE.setUpdateHistory(true);
                callback.onDeleteSuccess();
                pendingTasks.remove(taskId);
            }catch (Exception e){
                MyApplication.DATABASE_INSTANCE.endTransaction();
                callback.onDeleteFailure(e);
                pendingTasks.remove(taskId);
            }
        });
        pendingTasks.put(taskId, future);
    }

    /**
     * 清空历史记录
     */
    public void clearAll() {
        mLiveData_history.setValue(null);
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
