package com.lsy.chemicaltest_new.implement;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Transaction;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.domain.BleDeviceInfo;
import com.lsy.chemicaltest_new.domain.ColoTestResult;
import com.lsy.chemicaltest_new.domain.ElecTestResult;
import com.lsy.chemicaltest_new.domain.Expression;
import com.lsy.chemicaltest_new.domain.HSV;
import com.lsy.chemicaltest_new.domain.History_multiple;
import com.lsy.chemicaltest_new.domain.Point;
import com.lsy.chemicaltest_new.domain.RGB;
import com.lsy.chemicaltest_new.domain.Sample;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.domain.Temperature_Elec;
import com.lsy.chemicaltest_new.domain.ThermalTestResult;
import com.lsy.chemicaltest_new.interfaces.MultiHistory_Data;
import com.lsy.chemicaltest_new.interfaces.UpdateCallback;
import com.lsy.chemicaltest_new.utils.PhotoUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class MultiHistoryDataImpl implements MultiHistory_Data {
    private static final String TAG = "MultiHistoryDataImpl";
    private static MultiHistory_Data instance;
    private MultiHistoryDataImpl(){}
    public static MultiHistory_Data getInstance(){
        if (instance == null) {
            instance = new MultiHistoryDataImpl();
        }
        return instance;
    }

    @Override
    public void saveHistory(Context context, History_multiple history_multiple) {
        // 1.创建历史记录对象
        ElecTestResult elec_result = history_multiple.getElecTestResult();
        Temperature_Elec elec_degree = history_multiple.getTemperature_elec();
        ColoTestResult colo_result = history_multiple.getColoTestResult();
        ThermalTestResult thermal_result = history_multiple.getThermalTestResult();
        // 2.保存实验结果并获取相关信息
        StringBuilder sampleIdsBuilder = new StringBuilder();
        StringBuilder curveIdsBuilder = new StringBuilder();
        //String sampleIds = "";
        Integer elec_id = null,elec_degree_id=null,colo_id = null,thermal_id = null;
        // 2.1保存电信号检测结果
        if (elec_result != null) {
            elec_id = saveElec(elec_result);
            Optional.ofNullable(elec_result.getStandardCurve())
                    .map(StandardCurve::getSample_id)
                    .ifPresent(sampleId -> {
                        appendSampleId(sampleIdsBuilder, sampleId.toString());
                    });
            Optional.ofNullable(elec_result.getStandardCurve())
                    .map(StandardCurve::getId)
                    .ifPresent(curveId -> {
                        appendSampleId(curveIdsBuilder, curveId.toString());
                    });
        }
        // 2.2保存万用表测温度检测结果
        if (elec_degree != null){
            elec_degree_id = saveElecDegree(elec_degree);
            Optional.ofNullable(elec_degree.getStandardCurve())
                    .map(StandardCurve::getSample_id)
                    .ifPresent(sampleId -> {
                        appendSampleId(sampleIdsBuilder, sampleId.toString());
                    });
            Optional.ofNullable(elec_degree.getStandardCurve())
                    .map(StandardCurve::getId)
                    .ifPresent(curveId -> {
                        appendSampleId(curveIdsBuilder, curveId.toString());
                    });
        }
        // 2.3保存比色实验结果
        if (colo_result != null) {
            colo_id = saveColo(context, colo_result);
            Optional.ofNullable(colo_result.getStandardCurve())
                    .map(StandardCurve::getSample_id)
                    .ifPresent(sampleId -> {
                        appendSampleId(sampleIdsBuilder, sampleId.toString());
                    });
            Optional.ofNullable(colo_result.getStandardCurve())
                    .map(StandardCurve::getId)
                    .ifPresent(curveId -> {
                        appendSampleId(curveIdsBuilder, curveId.toString());
                    });
        }
        // 2.4保存光热实验结果
        if (thermal_result != null) {
            thermal_id = saveThermal(context, thermal_result);
            Optional.ofNullable(thermal_result.getStandardCurve())
                    .map(StandardCurve::getSample_id)
                    .ifPresent(sampleId -> {
                        appendSampleId(sampleIdsBuilder, sampleId.toString());
                    });
        }
        // 3.设置历史记录对象属性
        history_multiple.setSaveTime(getCurrentFormattedTime());
        history_multiple.setSample_ids(sampleIdsBuilder.toString());
        history_multiple.setCurve_ids(curveIdsBuilder.toString());
        history_multiple.setElec_id(elec_id);
        history_multiple.setDegree_id(elec_degree_id);
        history_multiple.setColo_id(colo_id);
        history_multiple.setThermal_id(thermal_id);

        // 4.保存历史记录
        MyApplication.DATABASE_INSTANCE.getHistory_multipleDao().add(history_multiple);
    }
    /***
     * 安全追加样本ID
     * @param builder 样本ID字符串构建器
     * @param sampleId 样本ID
     */
    private void appendSampleId(StringBuilder builder, String sampleId) {
        if (builder.length() > 0) {
            builder.append(",");
        }
        builder.append(sampleId);
    }

    /**
     * 获取当前格式化时间
     * @return 当前格式化时间
     */
    private String getCurrentFormattedTime() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new Date());
    }

    /***
     * 保存万用表设备信息
     * @param bleDeviceInfo 万用表设备信息
     * @return 保存成功返回数据库ID，失败返回null
     */
    private Integer saveDeviceInfo(BleDeviceInfo bleDeviceInfo){
        if (bleDeviceInfo == null) return null;
        try {
            // 查询是否有相同蓝牙设备信息
            BleDeviceInfo bleDeviceInfo1 = MyApplication.DATABASE_INSTANCE.getBleDeviceInfoDao().findByGear(bleDeviceInfo.getGear());
            if (bleDeviceInfo1!=null){
                return bleDeviceInfo1.getId();
            }
            // 如果没有找到，就插入新设备信息
            Long deviceInfo_id = MyApplication.DATABASE_INSTANCE.getBleDeviceInfoDao().add(bleDeviceInfo);
            return Math.toIntExact(deviceInfo_id);
        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG, "保存设备信息失败:"+e);
            return null;
        }
    }

    /***
     * 保存万用表测温度实验结果
     * @param result 万用表测温度实验结果
     * @return 保存成功返回数据库ID，失败返回null
     */
    private Integer saveElecDegree(Temperature_Elec result) {
        if (result == null) return null;
        try {
            //1. 保存蓝牙设备信息
            Integer bleDeviceInfo_id = saveDeviceInfo(result.getBleDeviceInfo());
            result.setBleDeviceInfo_id(bleDeviceInfo_id);
            Log.d(TAG, "电信号保存结果："+result.toString());
            //2. 保存检测结果
            Long id = MyApplication.DATABASE_INSTANCE.getElecTemperatureDao().add(result);
            return Math.toIntExact(id);
        }catch (Exception e){
            Log.d(TAG, "保存温度结果失败:"+e);
            return null;
        }

    }

    /***
     * 保存实验结果并获取相关信息
     * @param result 电化学结果
     * @return 保存后的电化学结果在数据库中id
     */
    @Transaction
    @SuppressLint("SimpleDateFormat")
    public Integer saveElec(ElecTestResult result){
        if (result == null) return  null;
        try {
            //1. 保存蓝牙设备信息
            Integer bleDeviceInfo_id = saveDeviceInfo(result.getBleDeviceInfo());
            result.setBleDeviceInfo_id(bleDeviceInfo_id);
            Log.d(TAG, "电信号保存结果："+result.toString());
            //2. 保存检测结果
            Long id = MyApplication.DATABASE_INSTANCE.getElecTestResultDao().add(result);
            return Math.toIntExact(id);
        }catch (Exception e){
            Log.d(TAG, "保存电信号结果失败:"+e);
            return null;
        }
    }
    /***
     * 保存实验结果并获取相关信息
     * @param result 比色结果
     * @return 保存后的比色结果在数据库中id
     */
    @Transaction
    @SuppressLint("SimpleDateFormat")
    public Integer saveColo(Context context,ColoTestResult result) {
        if (result == null) return  null;

        Bitmap originalBitmap = null;
        Bitmap cropBitmap = null;
        try {
            // 1. 保存原图片和裁剪图片到本地
            originalBitmap = result.getOriginalImage();
            cropBitmap = result.getCropImage();
            // 1.1保存原图
            if (MyApplication.INSTANCE.getIsSaveColoOriginalImage() && originalBitmap != null) {
                String originalPath = PhotoUtil.saveBitmapToFile(context, originalBitmap, "coloTest_original_");
                result.setOriginalImage_path(originalPath);
            }
            // 1.2保存裁剪图片
            if (cropBitmap != null) {
                String cropPath = PhotoUtil.saveBitmapToFile(context, cropBitmap, "coloTest_crop_");
                result.setCropImage_path(cropPath);
            }
            // 2. 保存 ColoTestResult 到本地
            Long id = MyApplication.DATABASE_INSTANCE.getColoTestResultDao().add(result);
            return Math.toIntExact(id);
        }catch (Exception e) {
            Log.d(TAG, "保存比色结果失败:" + e);
            return null;
        }
    }
    /***
     * 保存实验结果并获取相关信息
     * @param result 光热图像分析结果
     * @return 保存后的光热图像分析结果在数据库中id
     */
    @Transaction
    @SuppressLint("SimpleDateFormat")
    public Integer saveThermal(Context context,ThermalTestResult result){
        if (result == null) return  null;

        Bitmap thermalBitmap = null;
        try {
            //1.保存光热图像
            thermalBitmap = result.getThermalBitmap();
            if (thermalBitmap == null) return null;
            String path = PhotoUtil.saveBitmapToFile(context,thermalBitmap,"thermal_");
            if (path==null) return null;
            //2. 保存结果
            result.setThermalBitmap_path(path);
            Long id = MyApplication.DATABASE_INSTANCE.getThermalTestResultDao().add(result);
            return  Math.toIntExact(id);
        }catch (Exception e){
            Log.d(TAG, "保存光热图像分析结果失败:"+e);
            return null;
        }
    }

    @Override
    public void deleteHistory(History_multiple history_multiple) {
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
            String thermalImagePath = history_multiple.getThermalTestResult().getThermalBitmap_path();
            if (thermalImagePath != null) PhotoUtil.deleteImage(thermalImagePath);
            // 删除数据库记录
            MyApplication.DATABASE_INSTANCE.getThermalTestResultDao().deleteById(history_multiple.getThermal_id());
        }
    }

    @Override
    public void deleteHistories(List<History_multiple> histories) {
        for (History_multiple history_multiple : histories){
            deleteHistory(history_multiple);
        }
    }

    @Override
    public void fillPreviewHistory_curve(@NonNull History_multiple history_multiple, @NonNull UpdateCallback callback) {
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
        List<Point> pointList = StandardCurveDataImpl.getInstance().getPointList(curve.getPoint_set());
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

    @Override
    public List<History_multiple> fillAllHistories(List<History_multiple> histories) {
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

    @Override
    public List<History_multiple> FuzzySearch(String date, Integer sampleId) {
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
    }
}
