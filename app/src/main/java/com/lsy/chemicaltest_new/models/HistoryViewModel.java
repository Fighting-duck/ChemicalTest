package com.lsy.chemicaltest_new.models;

import static com.lsy.chemicaltest_new.utils.DynamicStringUtils.getString;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.room.Transaction;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.domain.History_multiple;
import com.lsy.chemicaltest_new.implement.MultiHistoryDataImpl;
import com.lsy.chemicaltest_new.interfaces.DeleteCallback;
import com.lsy.chemicaltest_new.interfaces.UpdateCallback;
import com.lsy.chemicaltest_new.utils.LiveDataUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class HistoryViewModel extends ViewModel {
    private static final String TAG = "HistoryViewModel";
    // 数据状态枚举
    public enum DataState {
        LOADING,    // 加载中
        SUCCESS,    // 成功
        ERROR,      // 错误
        EMPTY       // 空数据
    }
    // 添加任务管理
    private final ConcurrentHashMap<UUID, Future<?>> pendingTasks = new ConcurrentHashMap<>();
    // 状态跟踪的LiveData
    private final MutableLiveData<DataState> mLiveData_historyState = new MutableLiveData<>(DataState.LOADING);
    MutableLiveData<List<History_multiple>> mLiveData_histories = new MutableLiveData<>(); //历史记录列表内容
    MutableLiveData<List<String>> mLiveData_uniqueTimes = new MutableLiveData<>();//mLiveData_dates去掉重复项 时间列表内容
    MutableLiveData<List<String>> mLiveData_sampleName = new MutableLiveData<>();//样本列表名
    MutableLiveData<String> mLiveData_currentDate = new MutableLiveData<>();//当前日期
    MutableLiveData<Integer> mLiveData_currentSampleId = new MutableLiveData<>();//当前样品名
    MutableLiveData<Boolean> mLiveData_IsAscend = new MutableLiveData<>();//是否升序排序


    public LiveData<DataState> getHistoryState() {
        return mLiveData_historyState;
    }
    public MutableLiveData<List<History_multiple>> getLiveData_histories(){
        return mLiveData_histories;
    }
    public MutableLiveData<List<String>> getLiveData_uniqueDates() {
        return mLiveData_uniqueTimes;
    }
    public MutableLiveData<List<String>> getLiveData_samples() {
        return mLiveData_sampleName;
    }

    private MutableLiveData<String> mLiveData_toast = new MutableLiveData<>();
    public MutableLiveData<String> getLiveData_toast(){
        return mLiveData_toast;
    }
    public MutableLiveData<Integer> getLiveData_currentSampleId() {
        return mLiveData_currentSampleId;
    }
    public MutableLiveData<String> getLiveData_currentDate() {
        return mLiveData_currentDate;
    }
    public MutableLiveData<Boolean> getIsAscend() {
        return mLiveData_IsAscend;
    }

    public void setIsAscend(Boolean isAscend){
        mLiveData_IsAscend.setValue(isAscend);
    }

    public void setToast(String prompt){
        LiveDataUtils.safeUpdate(mLiveData_toast,prompt);
    }
    private void executeTask(Runnable task,String prompt) {
        UUID taskId = UUID.randomUUID();
        Future<?> future = MyApplication.DB_EXECUTOR.submit(() -> {
            try {
                task.run();
            } catch (Exception e){
                setToast(prompt);
                Log.e(TAG,e.toString());
            } finally{
                pendingTasks.remove(taskId);
            }
        });
        pendingTasks.put(taskId, future);
    }
    @Override
    protected void onCleared() {
        super.onCleared();
        // 取消所有未完成的任务
        pendingTasks.forEach((id, future) -> {
            if (!future.isDone()) {
                future.cancel(true);
            }
        });
        pendingTasks.clear();
    }

    //更新时间列表
    public void updateUniqueDateList() {
        executeTask(() -> {
            List<String> allDate = MyApplication.DATABASE_INSTANCE.getHistory_multipleDao().getAllDate();
            List<String> uniqueTimes = new ArrayList<>();
            if (!allDate.isEmpty()) {
                uniqueTimes = allDate.stream()
                        .map(date -> date.split(" ")[0])  // 提取日期部分
                        .filter(datePart -> !"null".equals(datePart))  // 过滤掉"null"
                        .distinct()  // 去重
                        .collect(Collectors.toList());
            }
            uniqueTimes.add(0, getString(R.string.history_unlimitedTime));  // 在开头添加特殊项
            LiveDataUtils.safeUpdate(mLiveData_uniqueTimes, uniqueTimes);
        }, getString(R.string.toast_history_load_time_fail));
    }
    /***
     * 更新样品列表
     */
    public void updateSampleList(){
        executeTask(() -> {
            List<String> samples = new ArrayList<>();
            List<String> sampleName = MyApplication.DATABASE_INSTANCE.getSampleDao().getAll_AvailableName();
            samples.add(getString(R.string.history_unlimitedSamples));
            samples.addAll(sampleName);
            LiveDataUtils.safeUpdate(mLiveData_sampleName, samples);
        }, getString(R.string.toast_history_load_sample_fail));
    }
    public Integer getSampleIdByName(String sampleName) {
        return MyApplication.DATABASE_INSTANCE.getSampleDao().getSampleByName(sampleName);
    }

    /***
     * 从数据库中读取所有历史记录(模糊查询)
     * @param filter 模糊查询条件
     */
    public void updateHistories(String filter){
        if (filter == null || filter.isEmpty()) return;
        executeTask(() -> {
            List<History_multiple> histories = new ArrayList<>();
            //先在样品表中模糊查询是否有此样品
            List<Integer> sampleIds = MyApplication.DATABASE_INSTANCE.getSampleDao().getSampleIdLikeName(filter);
            if (!sampleIds.isEmpty()){
                for (Integer sampleId:sampleIds){
                    List<History_multiple> histories_sample = MyApplication.DATABASE_INSTANCE.
                            getHistory_multipleDao().findAllBySampleId(String.valueOf(sampleId));
                    histories.addAll(histories_sample);
                }
            }
            //再在历史记录表中模糊查询是否由此日期
            List<History_multiple> histories_date = MyApplication.DATABASE_INSTANCE.
                    getHistory_multipleDao().findAllByDate(filter);
            if (!histories_date.isEmpty())
                histories.addAll(histories_date);
            // 对列表进行去重操作
            histories = histories.stream().distinct().collect(Collectors.toList());
            List<History_multiple> fillHistories = MultiHistoryDataImpl.getInstance().fillAllHistories(histories);
            Log.d(TAG, "histories中条目个数："+fillHistories.size());
            LiveDataUtils.safeUpdate(mLiveData_histories, fillHistories);
        },getString(R.string.toast_history_load_history_fail));
    }
    /***
     * 通过日期和样本名称过滤历史记录
     */
    public void updateHistoriesByDateAndSample(@NonNull UpdateCallback callback) {
        Objects.requireNonNull(callback, "callback must not be null");
        // 取消之前的相同任务
        cancelPreviousTasks("historyQuery");
        // 准备任务ID
        final UUID taskId = UUID.randomUUID();
        // 记录开始时间用于性能监控
        final long startTime = System.currentTimeMillis();
        Future<?> future = MyApplication.DB_EXECUTOR.submit(() -> {
            try {
                LiveDataUtils.safeUpdate(mLiveData_historyState, DataState.LOADING);
                // 1. 准备筛选条件
                final String date = mLiveData_currentDate.getValue();
                final Integer sampleId = mLiveData_currentSampleId.getValue();
                // 2. 执行数据库查询（带超时控制）
                List<History_multiple> histories = loadHistoriesFromDatabase(date, sampleId);
                // 3. 处理结果数据(顺序/逆序)
                List<History_multiple> filledHistories = processHistoryData(histories);
                // 4. 更新UI数据
                updateUiData(filledHistories);

                // 5. 记录成功日志
                Log.d(TAG, String.format("成功加载%d条历史记录，耗时%dms",
                        filledHistories.size(),
                        System.currentTimeMillis() - startTime));

                // 6. 回调通知完成
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onUpdateSuccess();
                });
                // 清理任务
                pendingTasks.remove(taskId);
            } catch (Exception e) {
                // 错误处理
                new Handler(Looper.getMainLooper()).post(() -> {
                    LiveDataUtils.safeUpdate(mLiveData_historyState, DataState.ERROR);
                    callback.onUpdateFailure(e);
                });
            } finally {
                // 清理任务
                pendingTasks.remove(taskId);
            }
        });
        pendingTasks.put(taskId, future);
    }


    private List<History_multiple> loadHistoriesFromDatabase(String date, Integer sampleId)
            throws Exception {
        // 设置5秒超时
        Future<List<History_multiple>> future = MyApplication.DB_EXECUTOR.submit(() -> {
            List<History_multiple> histories = MultiHistoryDataImpl.getInstance().FuzzySearch(date, sampleId);
            return histories;
        });
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new TimeoutException("数据库查询超时");
        }
    }
    //加载历史记录
    private List<History_multiple> processHistoryData(List<History_multiple> rawHistories) {
        if (rawHistories == null || rawHistories.isEmpty()) {
            return Collections.emptyList();
        }

        // 填充详细信息
        List<History_multiple> filledHistories = MultiHistoryDataImpl.getInstance().fillAllHistories(rawHistories);

        // 处理排序
        Boolean isAscend = mLiveData_IsAscend.getValue();
        if (isAscend != null && !isAscend) {
            Collections.reverse(filledHistories);
        }

        return filledHistories;
    }

    /**
     * 在主线程更新历史记录
     * @param histories 历史记录
     */
    private void updateUiData(List<History_multiple> histories) {
        new Handler(Looper.getMainLooper()).post(() -> {
            LiveDataUtils.safeUpdate(mLiveData_histories, histories);
            if (histories.isEmpty()) {
                LiveDataUtils.safeUpdate(mLiveData_historyState, DataState.EMPTY);
            } else {
                LiveDataUtils.safeUpdate(mLiveData_historyState, DataState.SUCCESS);
            }
        });

        // 异步更新日期列表、样品列表
        updateUniqueDateList();
        updateSampleList();
    }
    // 取消所有pending任务
    private void cancelPreviousTasks(String taskType) {
        pendingTasks.forEach((id, future) -> {
            if (!future.isDone()) {
                future.cancel(true);
                Log.d(TAG, "已取消之前的" + taskType + "任务");
            }
        });
    }

    public void setCurrentSampleId(Integer SampleId) {
       mLiveData_currentSampleId.setValue(SampleId);
    }
    public Integer getCurrentSampleId() {
        return mLiveData_currentSampleId.getValue();
    }

    public void setCurrentDate(String date) {
        mLiveData_currentDate.setValue(date);
    }
    public String getCurrentDate() {
        return mLiveData_currentDate.getValue();
    }

    /**
     * 反转历史记录
     */
    public void reverseHistory() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<History_multiple> histories = mLiveData_histories.getValue();
            if (histories == null || histories.isEmpty()) {
                return;
            }
            Collections.reverse(histories);
            LiveDataUtils.safeUpdate(mLiveData_histories, histories);
        });
    }
    /***
     * 删除历史记录
     * @param callback 回调接口
     */
    @Transaction
    public void deleteHistories(List<History_multiple> histories,@NonNull DeleteCallback callback) {
        // 参数检验
        Objects.requireNonNull(histories, "histories cannot be null");
        Objects.requireNonNull(callback, "Callback cannot be null");

        final  UUID taskId = UUID.randomUUID();
        Future<?> future = MyApplication.DB_EXECUTOR.submit(() -> {
            Exception error = null;
            try {
                MyApplication.DATABASE_INSTANCE.runInTransaction(() -> {
                    // 1. 事务性删除所有历史记录
                    MultiHistoryDataImpl.getInstance().deleteHistories(histories);
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
}