package com.lsy.chemicaltest_new.models;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.room.Transaction;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.domain.History_multiple;
import com.lsy.chemicaltest_new.implement.MultiHistoryDataImpl;
import com.lsy.chemicaltest_new.interfaces.DeleteCallback;
import com.lsy.chemicaltest_new.utils.LiveDataUtils;

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

    public History_multiple getHistory() {
        return mLiveData_history.getValue();
    }
    /***
     * 删除历史记录
     * @param callback 回调接口
     */
    @Transaction
    public void deleteHistory(@NonNull DeleteCallback callback) {
        // 参数检验
        Objects.requireNonNull(callback, "Callback cannot be null");

        // 获取历史记录
        History_multiple history_multiple = mLiveData_history.getValue();
        if (history_multiple == null) {
            return;
        }

        final  UUID taskId = UUID.randomUUID();
        Future<?> future = MyApplication.DB_EXECUTOR.submit(() -> {
            Exception error = null;
            try {
                MyApplication.DATABASE_INSTANCE.runInTransaction(() -> {
                    // 1. 事务性删除所有历史记录
                    MultiHistoryDataImpl.getInstance().deleteHistory(history_multiple);
                });
                MyApplication.INSTANCE.setUpdateHistory(true);
            } catch (Exception e) {
                error = e;
            } finally {
                if (error == null) {
                    callback.onDeleteSuccess();
                } else {
                    callback.onDeleteFailure(error);
                }
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
