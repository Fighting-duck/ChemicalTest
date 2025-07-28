package com.lsy.chemicaltest_new.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.domain.BleDeviceInfo;
import com.lsy.chemicaltest_new.domain.ColoTestResult;
import com.lsy.chemicaltest_new.domain.ElecTestResult;
import com.lsy.chemicaltest_new.domain.Experimenter;
import com.lsy.chemicaltest_new.domain.Expression;
import com.lsy.chemicaltest_new.domain.HSV;
import com.lsy.chemicaltest_new.domain.History_multiple;
import com.lsy.chemicaltest_new.domain.Point;
import com.lsy.chemicaltest_new.domain.RGB;
import com.lsy.chemicaltest_new.domain.Sample;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.domain.Temperature_Elec;
import com.lsy.chemicaltest_new.domain.ThermalTestResult;
import com.lsy.chemicaltest_new.utils.LiveDataUtils;
import com.lsy.chemicaltest_new.utils.PhotoUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class HistoryViewModel extends ViewModel {
    private static final String TAG = "HistoryViewModel";
     Context mContext;
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
    public void  setContext(Context mContext){
        this.mContext = mContext;
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
            uniqueTimes.add(0, mContext.getString(R.string.history_unlimitedTime));  // 在开头添加特殊项
            LiveDataUtils.safeUpdate(mLiveData_uniqueTimes, uniqueTimes);
        },"加载时间列表时出错");
    }
    /***
     * 更新样品列表
     */
    public void updateSampleList(){
        executeTask(() -> {
            List<String> samples = new ArrayList<>();
            List<String> sampleName = MyApplication.DATABASE_INSTANCE.getSampleDao().getAll_AvailableName();
            samples.add(mContext.getString(R.string.history_unlimitedSamples));
            samples.addAll(sampleName);
            LiveDataUtils.safeUpdate(mLiveData_sampleName, samples);
        },"加载样本列表时出错");
    }
    public Integer getSampleIdByName(String sampleName) {
        return MyApplication.DATABASE_INSTANCE.getSampleDao().getSampleByName(sampleName);
    }
    private List<History_multiple> getFilledHistories(List<History_multiple> histories) {
        if (histories == null || histories.isEmpty()){
            return Collections.emptyList();
        }

        return histories.parallelStream()
                .parallel()// 显式启用并行流
                .map(history -> {
                    // 处理逻辑
                    try {
                        // 使用局部变量防止潜在的并发问题
                        History_multiple processedHistory = new History_multiple(history);

                        Integer experiment_id = processedHistory.getExperimenter_id();
                        String sample_ids = processedHistory.getSample_ids();
                        Integer elec_id = processedHistory.getElec_id();
                        Integer degree_id = processedHistory.getDegree_id();
                        Integer colo_id = processedHistory.getColo_id();
                        Integer thermal_id = processedHistory.getThermal_id();
                        // 并行查询各个关联数据
                        //查询做实验人员
                        /*if (experiment_id != null) {
                            Experimenter experimenter = MyApplication.DATABASE_INSTANCE.getExperimenterDao().findById(experiment_id);
                            processedHistory.setExperimenter(experimenter);
                        }*/
                        if (sample_ids != null && !sample_ids.isEmpty()) {
                            //查询实验样本
                            String[] samples = sample_ids.split(",");
                            //提取唯一样本id
                            Set<Integer> uniqueSampleIds = Arrays.stream(samples)
                                    .map(Integer::parseInt)
                                    .collect(Collectors.toSet());
                            //查询实验样本
                            List<Sample> samplesList = uniqueSampleIds.parallelStream()
                                    .map(sampleId -> MyApplication.DATABASE_INSTANCE.getSampleDao().findById(sampleId))
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList());

                            processedHistory.setSampleList(samplesList);
                        }
                        if (elec_id != null) {
                            processElecTestResult(processedHistory, elec_id);
                        }
                        if (degree_id != null) {
                            processTemperatureElec(processedHistory, degree_id);
                        }
                        if (colo_id != null) {
                            processColoTestResult(processedHistory, colo_id);
                        }
                        if (thermal_id != null) {
                            processThermalTestResult(processedHistory, thermal_id);
                        }
                        return processedHistory;
                    } catch (Exception e) {
                        // 记录异常但继续处理其他元素
                        Log.e("HistoryProcessor", "Error processing history: " + history.getId(), e);
                        return history; // 返回原始对象或根据需要处理
                    }
                })
                .collect(Collectors.toList());
    }
    // 使用同步方法确保关键数据访问的线程安全
    private synchronized void processElecTestResult(History_multiple history, Integer elec_id) {
        ElecTestResult elec_result = MyApplication.DATABASE_INSTANCE.getElecTestResultDao().findById(elec_id);
        if (elec_result != null) {
            //1. 获取直线详细信息
//                StandardCurve curve = MyApplication.DATABASE_INSTANCE.getStandardCurveDao().findById(elec_result.getStandard_curve_id());
//                elec_result.setStandardCurve(getStandardCurve(curve));
            //2. 获取蓝牙设备信息
            BleDeviceInfo bleDeviceInfo = MyApplication.DATABASE_INSTANCE.getBleDeviceInfoDao().findById(elec_result.getBleDeviceInfo_id());
            elec_result.setBleDeviceInfo(bleDeviceInfo);
            //3. 14次测量值
            List<Float> floatList = ElecTestResult.getPointList(elec_result.getFourteen_measurements_list())
                    .parallelStream()
                    .map(Float::parseFloat)
                    .collect(Collectors.toList());
            elec_result.setValueList(floatList);
            //14次测量值+时间+单位
            elec_result.setTestValueList(
                    elec_result.getFourteen_measurements_list(),
                    elec_result.getFourteen_times_list(),
                    bleDeviceInfo != null ? bleDeviceInfo.getUnit() : ""
            );
            history.setElecTestResult(elec_result);
        }
    }
    private synchronized void processTemperatureElec(History_multiple history, Integer degree_id) {
        Temperature_Elec temperature_elec = MyApplication.DATABASE_INSTANCE.getElecTemperatureDao().findById(degree_id);
        if (temperature_elec != null) {
            //1. 获取直线详细信息
//                StandardCurve curve = MyApplication.DATABASE_INSTANCE.getStandardCurveDao().findById(temperature_elec.getStandard_curve_id());
//                temperature_elec.setStandardCurve(getStandardCurve(curve));
            //2. 获取蓝牙设备信息
            BleDeviceInfo bleDeviceInfo = MyApplication.DATABASE_INSTANCE.getBleDeviceInfoDao().findById(temperature_elec.getBleDeviceInfo_id());
            temperature_elec.setBleDeviceInfo(bleDeviceInfo);
            history.setTemperature_elec(temperature_elec);
        }
    }
    private synchronized void processColoTestResult(History_multiple history, Integer colo_id) {
        ColoTestResult colo_result = MyApplication.DATABASE_INSTANCE.getColoTestResultDao().findById(colo_id);
        if (colo_result != null) {
            //获取直线信息
//                StandardCurve curve = MyApplication.DATABASE_INSTANCE.getStandardCurveDao().findById(colo_result.getStandard_curve_id());
//                colo_result.setStandardCurve(getStandardCurve(curve));
            //加载原始图片、框选图片
            // 图片处理可能比较耗时，保持同步
            Bitmap originalBitmap = PhotoUtil.getBitmapFromPath(colo_result.getOriginalImage_path());
            Bitmap cropBitmap = PhotoUtil.getBitmapFromPath(colo_result.getCropImage_path());
            colo_result.setOriginalImage(originalBitmap);
            colo_result.setCropImage(cropBitmap);
            //设置RGB、HSV
            RGB rgb = RGB.fromColor(colo_result.getCorrectedColor());
            List<String> strList = ColoTestResult.getHSVList(colo_result.getHSV_value());
            List<Float> hsvList = new ArrayList<>();
            for(String str:strList){
                hsvList.add(Float.parseFloat(str));
            }
            //等价于  先转化为流，再使用map对流中每个元素转换为浮点数，再使用collect()重新收集为列表  缺点：对简单任务开销大  优点：对复杂数据，表现更优
            /*List<Float> hsvList = ColoTestResult.getHSVList(colo_result.getHSV_value())
                    .parallelStream()
                    .map(Float::parseFloat)
                    .collect(Collectors.toList());*/

            HSV hsv = new HSV(hsvList.get(0), hsvList.get(1), hsvList.get(2));
            colo_result.setRGB(rgb);
            colo_result.setHsv(hsv);
            history.setColoTestResult(colo_result);
        }
    }
    private synchronized void processThermalTestResult(History_multiple history, Integer thermal_id) {
        ThermalTestResult thermal_result = MyApplication.DATABASE_INSTANCE.getThermalTestResultDao().findById(thermal_id);
        if (thermal_result != null) {
            //获取直线信息
//                StandardCurve curve = MyApplication.DATABASE_INSTANCE.getStandardCurveDao().findById(thermal_result.getStandard_curve_id());
//                thermal_result.setStandardCurve(getStandardCurve(curve));
            //获取热成像图像
            Bitmap thermalBitmap = PhotoUtil.getBitmapFromPath(thermal_result.getThermalBitmap_path());
            thermal_result.setThermalBitmap(thermalBitmap);
            history.setThermalTestResult(thermal_result);
        }
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
            List<History_multiple> fillHistories = getFilledHistories(histories);
            Log.d(TAG, "histories中条目个数："+fillHistories.size());
            LiveDataUtils.safeUpdate(mLiveData_histories, fillHistories);
        },"加载历史记录出错啦！");
    }

    public interface UpdateCallback {
        void onUpdateCompleted();
        void onUpdateFailed(Exception e);
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
                    callback.onUpdateCompleted();
                });
                // 清理任务
                pendingTasks.remove(taskId);
            } catch (Exception e) {
                // 错误处理
                new Handler(Looper.getMainLooper()).post(() -> {
                    LiveDataUtils.safeUpdate(mLiveData_historyState, DataState.ERROR);
                    callback.onUpdateFailed(e);
                });
            } finally {
                // 清理任务
                pendingTasks.remove(taskId);
            }
        });
        pendingTasks.put(taskId, future);
    }

    /**
     * 从数据库中加载历史记录（模糊查询）
     * @param date 日期
     * @param sampleId 样本id
     * @return List<History_multiple>
     * @throws Exception
     */
    private List<History_multiple> loadHistoriesFromDatabase(String date, Integer sampleId)
            throws Exception {
        // 设置5秒超时
        Future<List<History_multiple>> future = MyApplication.DB_EXECUTOR.submit(() -> {
            if (date == null && sampleId == null) {
                return MyApplication.DATABASE_INSTANCE.getHistory_multipleDao().getAll();
            } else if (date == null) {
                return MyApplication.DATABASE_INSTANCE.getHistory_multipleDao()
                        .findAllBySampleId(String.valueOf(sampleId));
            } else if (sampleId == null) {
                return MyApplication.DATABASE_INSTANCE.getHistory_multipleDao()
                        .findAllByDate(date);
            } else {
                return MyApplication.DATABASE_INSTANCE.getHistory_multipleDao()
                        .findAllByDateAndSampleId(date, String.valueOf(sampleId));
            }
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
        List<History_multiple> filledHistories = getFilledHistories(rawHistories);

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
    public interface CheckHistoryCallback {
        void onUpdateSuccess();
        void onUpdateFailure(Exception e);
    }

    /***
     * 并行加载历史记录
     * @param history_multiple 历史记录
     */
    public void setPreviewHistory(@NonNull History_multiple history_multiple, @NonNull CheckHistoryCallback callback) {
        Objects.requireNonNull(history_multiple, "history_multiple cannot be null");
        Objects.requireNonNull(callback, "callback cannot be null");

        // 使用CountDownLatch确保所有并行任务完成
        final CountDownLatch latch = new CountDownLatch(4);
        final AtomicReference<Throwable> errorRef = new AtomicReference<>();

        // 并行加载电流检测结果
        MyApplication.DB_EXECUTOR.execute(() -> {
            try {
                loadElecResult(history_multiple);
            } catch (Exception e) {
                errorRef.set(e);
            } finally {
                latch.countDown();
            }
        });

        // 并行加载温度检测结果
        MyApplication.DB_EXECUTOR.execute(() -> {
            try {
                loadTempResult(history_multiple);
            } catch (Exception e) {
                errorRef.set(e);
            } finally {
                latch.countDown();
            }
        });

        // 并行加载比色检测结果
        MyApplication.DB_EXECUTOR.execute(() -> {
            try {
                loadColoResult(history_multiple);
            } catch (Exception e) {
                errorRef.set(e);
            } finally {
                latch.countDown();
            }
        });

        // 并行加载热力检测结果
        MyApplication.DB_EXECUTOR.execute(() -> {
            try {
                loadThermalResult(history_multiple);
            } catch (Exception e) {
                errorRef.set(e);
            } finally {
                latch.countDown();
            }
        });

        // 等待所有任务完成 (可考虑添加超时)
        MyApplication.DB_EXECUTOR.execute(() -> {
            try {
                // 设置3秒超时限制
                if (!latch.await(3, TimeUnit.SECONDS)) {
                    errorRef.set(new TimeoutException("数据加载超时"));
                }

                if (errorRef.get() != null) {
                    // 处理错误情况（包括超时）
                    Log.e(TAG, "数据加载异常: " + errorRef.get().getMessage());
                } else {
                    //LiveDataUtils.safeUpdate(mLiveData_history, history_multiple);
                    callback.onUpdateSuccess();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                errorRef.set(e);
                callback.onUpdateFailure(e);
            }
        });
    }

    /**
     * 加载电信号检测结果
     * @param history 历史记录
     */
    private void loadElecResult(History_multiple history) {
        ElecTestResult result = history.getElecTestResult();
        if (result != null  && result.getStandard_curve_id() != null) {
            StandardCurve curve = MyApplication.DATABASE_INSTANCE
                    .getStandardCurveDao()
                    .findById(result.getStandard_curve_id());
            result.setStandardCurve(getStandardCurve(curve));
            history.setElecTestResult(result);
        }
    }

    /**
     * 加载温度检测结果
     * @param history 历史记录
     */
    private void loadTempResult(History_multiple history) {
        Temperature_Elec result = history.getTemperature_elec();
        if (result != null && result.getStandard_curve_id() != null) {
            StandardCurve curve = MyApplication.DATABASE_INSTANCE
                    .getStandardCurveDao()
                    .findById(result.getStandard_curve_id());
            result.setStandardCurve(getStandardCurve(curve));
            history.setTemperature_elec(result);
        }
    }
    /**
     * 加载比色检测结果
     * @param history 历史记录
     */
    private void loadColoResult(History_multiple history) {
        ColoTestResult result = history.getColoTestResult();
        if (result != null && result.getStandard_curve_id() != null) {
            StandardCurve curve = MyApplication.DATABASE_INSTANCE
                    .getStandardCurveDao()
                    .findById(result.getStandard_curve_id());
            result.setStandardCurve(getStandardCurve(curve));
            history.setColoTestResult(result);
        }
    }

    /**
     * 加载光热图像检测结果
     * @param history 历史记录
     */
    private void loadThermalResult(History_multiple history) {
        ThermalTestResult result = history.getThermalTestResult();
        if (result != null &&  result.getStandard_curve_id() != null) {
            StandardCurve curve = MyApplication.DATABASE_INSTANCE
                    .getStandardCurveDao()
                    .findById(result.getStandard_curve_id());
            result.setStandardCurve(getStandardCurve(curve));
            history.setThermalTestResult(result);
        }
    }

    /**
     * 根据传入的 StandardCurve 对象，获取并设置其标准曲线的相关数据和表达式。
     *
     * @param curve 传入的标准曲线对象，需要包含有效的 point_set_id 和 expression 字符串。
     * @return 返回更新后的 StandardCurve 对象。
     */
    public StandardCurve getStandardCurve(StandardCurve curve){
        //1.1 获取pointList
        List<Point> pointList = StandardCurve.getPointList(curve.getPoint_set());
        curve.setPointList(pointList);
        //1.2 构造expression
        String str_expression = curve.getExpression();
        Expression expression = Expression.buildExpression(str_expression);
        curve.setFormula(expression);
        //1.3 获取样品信息
        Sample sample = MyApplication.DATABASE_INSTANCE.getSampleDao().findById(curve.getSample_id());
        curve.setSample(sample);
        return  curve;
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
}