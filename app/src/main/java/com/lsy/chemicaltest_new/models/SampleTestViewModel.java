package com.lsy.chemicaltest_new.models;

import static com.lsy.chemicaltest_new.utils.DynamicStringUtils.getString;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.lsy.chemicaltest_new.domain.ColoTestResult;
import com.lsy.chemicaltest_new.domain.ElecTestResult;
import com.lsy.chemicaltest_new.domain.History_multiple;
import com.lsy.chemicaltest_new.domain.Temperature_Elec;
import com.lsy.chemicaltest_new.domain.ThermalTestResult;
import com.lsy.chemicaltest_new.implement.MultiHistoryDataImpl;
import com.lsy.chemicaltest_new.utils.LiveDataUtils;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class SampleTestViewModel extends ViewModel {
    private static final String TAG = "SampleTestViewModel";
    // 添加任务管理
    private final ConcurrentHashMap<UUID, Future<?>> pendingTasks = new ConcurrentHashMap<>();
    // 保存数据状态枚举
    public enum SaveState {
        LOADING,    // 加载中
        SUCCESS,    // 成功
        ERROR,       // 错误
        EMPTY
    }
    MutableLiveData<History_multiple> mLiveData_history = new MutableLiveData<>();
    private final MutableLiveData<Float> mLiveData_credibility = new MutableLiveData<>();//可信度分析
    private final MutableLiveData<SaveState> mLiveData_saveState = new MutableLiveData<>(SaveState.EMPTY);
    Context mContext;
    public void setContext(Context context){
        mContext = context;
    }
    public MutableLiveData<History_multiple> getLiveData_history() {
        return mLiveData_history;
    }
    public LiveData<Float> getLiveData_credibility() {
        return mLiveData_credibility;
    }
    public LiveData<SaveState> getLiveData_saveState() {
        return mLiveData_saveState;
    }
    public void setLiveData_saveState(SaveState state) {
        mLiveData_saveState.postValue(state);
    }


    private MutableLiveData<String> mLiveData_toast = new MutableLiveData<>();
    public MutableLiveData<String> getLiveData_toast(){
        return mLiveData_toast;
    }

    private void executeTask(Runnable task,String prompt) {
        UUID taskId = UUID.randomUUID();
        Future<?> future = MyApplication.DB_EXECUTOR.submit(() -> {
            try {
                task.run();
            } catch (Exception e){
                setToast(prompt);
                e.printStackTrace();
                Log.e(TAG,e+"");
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

    public void setToast(String prompt) {
        LiveDataUtils.safeUpdate(mLiveData_toast, prompt);
    }
    public void setLiveData_history(ElecTestResult elec_result,Temperature_Elec temperature_elec,
                                    ColoTestResult colo_result, ThermalTestResult thermal_result){
        History_multiple history = mLiveData_history.getValue();
        if (history == null){
            history = new History_multiple();
        }
        history.setElecTestResult(elec_result);
        history.setTemperature_elec(temperature_elec);
        history.setColoTestResult(colo_result);
        history.setThermalTestResult(thermal_result);
        LiveDataUtils.safeUpdate(mLiveData_history,history);
    }

    /***
     * 可信度分析
     * 根据三个实验浓度相差值进行判断，返回一个Float类型的可信度
     */
    public void analyzeCredibility() {
        if (mLiveData_history.getValue() == null)
            return;
        if (mLiveData_history.getValue().getElecTestResult() == null ||
                mLiveData_history.getValue().getColoTestResult() == null ||
                mLiveData_history.getValue().getTemperature_elec() == null)
            return;

        // 获取三种检测方法的浓度值
        Float co_elec = mLiveData_history.getValue().getElecTestResult().getDetectionCo();
        Float co_colo = mLiveData_history.getValue().getColoTestResult().getDetectionCo();
        Float co_temperature = mLiveData_history.getValue().getTemperature_elec().getDetectionCo();

        Log.d(TAG, "co_elec: " + co_elec + ", co_colo: " + co_colo + ", co_temperature: " + co_temperature);
        // 检查是否有无效值（例如NaN或null）
        if (co_elec == null || co_colo == null || co_temperature == null) {
            setToast(getString(R.string.toast_credibilityAnalysis_false));
            return;
        }
        if (co_elec.isInfinite() || co_colo.isInfinite() || co_temperature.isInfinite()){
            mLiveData_credibility.setValue(0.0f);
            return;
        }

        // 为不同检测方法分配权重（根据方法可靠性确定）
        float weight_elec = 0.5f;   // 电化学方法权重
        float weight_colo = 0.3f;   // 比色法权重
        float weight_temp = 0.2f;   // 温度法权重

        // 计算加权平均值
        float weightedAvg = co_elec * weight_elec + co_colo * weight_colo + co_temperature * weight_temp;

        // 计算每种方法与加权平均值的偏差
        float deviation1 = Math.abs(co_elec - weightedAvg) / weightedAvg * 100;
        float deviation2 = Math.abs(co_colo - weightedAvg) / weightedAvg * 100;
        float deviation3 = Math.abs(co_temperature - weightedAvg) / weightedAvg * 100;

        // 计算加权可信度（权重高的方法对最终可信度影响更大）
        float credibility = 100 - (deviation1 * weight_elec + deviation2 * weight_colo + deviation3 * weight_temp) * 2;

        // 确保可信度在合理范围内
        credibility = Math.max(0, Math.min(100, credibility));

        mLiveData_credibility.setValue(credibility);
    }

    public void setCredibility(Float credibility){
        mLiveData_credibility.setValue(credibility);
    }

    /**
     * 保存多种实验结果（电学、比色、光热）到数据库，并生成一条综合的历史记录。
     *
     * 该方法的主要功能是：
     * 1. 分别保存电学、颜色和热学实验结果，并获取对应的数据库 ID 和样本 ID。
     * 2. 检查所有实验结果的样本 ID 是否一致。
     * 3. 计算综合可信度。
     * 4. 创建并保存一条包含所有实验结果信息的历史记录。
     *
     * @param context 应用程序上下文，用于访问资源或进行其他需要上下文的操作。
     * @param elec_result 电学实验结果对象，可选。
     * @param colo_result 颜色实验结果对象，可选。
     * @param thermal_result 热学实验结果对象，可选。
     * @return 保存操作是否成功。目前方法始终返回 true，但实际使用中可能需要根据保存操作的结果进行调整。
     */
    @Transaction
    @SuppressLint("SimpleDateFormat")
    public void saveAll(@NonNull Context context, String remarks, ElecTestResult elec_result, Temperature_Elec elec_degree, ColoTestResult colo_result, ThermalTestResult thermal_result) {
        //参数验证
        Objects.requireNonNull(context, "Context cannot be null");

        if (elec_result == null && colo_result == null && thermal_result == null) {
            setToast(context.getString(R.string.toast_test_all_empty));
            LiveDataUtils.safeUpdate(mLiveData_saveState, SaveState.ERROR);
            return;
        }

        // 确保 LOADING 只设置一次
        if (mLiveData_saveState.getValue() != SaveState.LOADING) {
            LiveDataUtils.safeUpdate(mLiveData_saveState, SaveState.LOADING);
        }
        final UUID  taskId = UUID.randomUUID();
        Log.d(TAG,"Saving task started: "+taskId);
        Future<?> future = MyApplication.DB_EXECUTOR.submit(() -> {
            try{
                // 1.整理各实验结果
                History_multiple history = new History_multiple();
                history.setElecTestResult(elec_result);
                history.setTemperature_elec(elec_degree);
                history.setColoTestResult(colo_result);
                history.setThermalTestResult(thermal_result);
                Float credibility = mLiveData_credibility.getValue();
                if (credibility != null) {
                    history.setCredibility(credibility);
                }
                if (remarks != null) {
                    history.setRemarks(remarks);
                }
                // 2.保存各实验结果到数据库
                MultiHistoryDataImpl.getInstance().saveHistory(context,history);
                // 3.设置保存状态并通知观察者
                if (mLiveData_saveState.getValue() == SaveState.LOADING) {
                    synchronized (mLiveData_saveState) {
                        LiveDataUtils.safeUpdate(mLiveData_saveState, SaveState.SUCCESS);
                        // 延迟清除状态，确保 SUCCESS 被观察者接收
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            LiveDataUtils.safeUpdate(mLiveData_saveState, SaveState.EMPTY);
                        }, 100); // 延迟 100ms
                    }

                }
            }catch (Exception e) {
                Log.e(TAG,"保存数据时出错:"+ e);
                setToast(context.getString(R.string.toast_save_fail_exception));
                LiveDataUtils.safeUpdate(mLiveData_saveState, SaveState.ERROR);
            } finally {
                synchronized (pendingTasks) {
                    pendingTasks.remove(taskId);
                }
            }
        });
        synchronized (pendingTasks) {
            pendingTasks.put(taskId, future);
        }
    }


    public void setHistoryMultiple(History_multiple history_multiple) {
        mLiveData_history.setValue(history_multiple);
    }
}