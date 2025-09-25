package com.lsy.chemicaltest_new.models;

import static com.lsy.chemicaltest_new.utils.DynamicStringUtils.getString;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Transaction;

import com.github.mikephil.charting.data.Entry;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.database.DataRepository;
import com.lsy.chemicaltest_new.domain.ColoTestResult;
import com.lsy.chemicaltest_new.domain.HSV;
import com.lsy.chemicaltest_new.domain.Point;
import com.lsy.chemicaltest_new.domain.RGB;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.utils.CombinedChartUtils;
import com.lsy.chemicaltest_new.utils.LiveDataUtils;
import com.lsy.chemicaltest_new.utils.NumberUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

public class ColoViewModel extends AndroidViewModel {
    private static final String TAG = "ColoViewModel";
    MediatorLiveData<ColoTestResult> mLiveData_coloTestResult = new MediatorLiveData<>();
    MutableLiveData<StandardCurve> mLiveData_curve = new MutableLiveData<>();
    MutableLiveData<Bitmap> mLiveData_originalImage = new MutableLiveData<>();
    MutableLiveData<Bitmap> mLiveData_cropImage = new MutableLiveData<>();
    MutableLiveData<Integer> mLiveData_correctedColor = new MutableLiveData<>();
    MutableLiveData<RGB> mLiveData_rgb = new MutableLiveData<>();
    MutableLiveData<HSV> mLiveData_hsv = new MutableLiveData<>();
    MutableLiveData<Float> mLiveData_CO = new MutableLiveData<>();
    MutableLiveData<String> mLiveData_diseaseAnal = new MutableLiveData<>();
    MutableLiveData<float[]> mLiveData_confidenceInterval = new MutableLiveData<>();
    MutableLiveData<String> mLiveData_toast = new MutableLiveData<>();
    MutableLiveData<String> mLiveData_notice = new MutableLiveData<>();
    public MutableLiveData<String> getLiveData_toast(){
        return mLiveData_toast;
    }
    public void setToast(String toast){
        LiveDataUtils.safeUpdate(mLiveData_toast,toast);
    }

    public MediatorLiveData<ColoTestResult> getLiveData_coloTestResult(){
        return mLiveData_coloTestResult;
    }
    public MutableLiveData<StandardCurve> getLiveData_curve(){
        return mLiveData_curve;
    }
    public MutableLiveData<Bitmap> getLiveData_originalImage(){
        return mLiveData_originalImage;
    }
    public MutableLiveData<Bitmap> getLiveData_cropImage(){
        return mLiveData_cropImage;
    }
    public MutableLiveData<Integer> getLiveData_correctedColor(){
        return mLiveData_correctedColor;
    }
    public MutableLiveData<RGB> getLiveData_rgb(){
        return mLiveData_rgb;
    }
    public MutableLiveData<HSV> getLiveData_hsv(){
        return mLiveData_hsv;
    }
    public MutableLiveData<Float> getLiveData_CO(){
        return mLiveData_CO;
    }
    public MutableLiveData<float[]> getLiveData_confidenceInterval(){
        return mLiveData_confidenceInterval;
    }
    public MutableLiveData<String> getLiveData_diseaseAnal(){
        return mLiveData_diseaseAnal;
    }
    public MutableLiveData<String> getLiveData_notice(){
        return mLiveData_notice;
    }

    public ColoViewModel(Application application){
        super(application);
        initLiveDataSources();
    }
    private void initLiveDataSources() {
        Log.d(TAG, "initLiveDataSources");
        mLiveData_coloTestResult.setValue(new ColoTestResult());
        // 使用Kotlin扩展函数风格的辅助方法简化添加源的过程
        addLiveDataSource(mLiveData_curve, this::updateCurve);
        addLiveDataSource(mLiveData_originalImage, this::updateOriginalImage);
        addLiveDataSource(mLiveData_cropImage, this::updateCropImage);
        addLiveDataSource(mLiveData_correctedColor, this::updateCorrectedColor);
        addLiveDataSource(mLiveData_rgb, this::updateRGB);
        addLiveDataSource(mLiveData_hsv, this::updateHSV);
        addLiveDataSource(mLiveData_CO, this::updateCO);
        addLiveDataSource(mLiveData_diseaseAnal, this::updateDiseaseAnal);
    }
    // 通用的LiveData源添加方法
    private <T> void addLiveDataSource(LiveData<T> source, Consumer<T> onChanged) {
        mLiveData_coloTestResult.addSource(source, value -> {
            Log.d(TAG, "Received update from source: " + value);
            ColoTestResult current = mLiveData_coloTestResult.getValue();
            if (current == null) {
                current = new ColoTestResult();
                Log.w(TAG, "Had to create new ColoTestResult");
            }
            onChanged.accept(value);
            mLiveData_coloTestResult.setValue(current);
        });
    }
    // 使用Builder模式简化对象更新
    private void updateColoTestResult(Consumer<ColoTestResult> updater) {
        ColoTestResult current = mLiveData_coloTestResult.getValue();
        if (current == null) {
            current = new ColoTestResult();
        }
        updater.accept(current);
        mLiveData_coloTestResult.setValue(current);
    }

    private  void updateCO(Float CO) {
        updateColoTestResult(result->result.setDetectionCo(CO));
    }

    private void updateCurve(StandardCurve curve) {
        updateColoTestResult(result->{
            result.setStandard_curve_id(curve.getId());
            result.setStandardCurve(curve);
        });
    }
    // Bitmap处理需要加入资源管理
    void updateOriginalImage(Bitmap bitmap) {
        // 先释放旧的Bitmap
        ColoTestResult current = mLiveData_coloTestResult.getValue();
        if (current != null && current.getOriginalImage() != null) {
            current.getOriginalImage().recycle();
        }
        updateColoTestResult(result -> result.setOriginalImage(bitmap));
    }
    private  void updateCropImage(Bitmap bitmap) {
        // 先释放旧的Bitmap
        ColoTestResult current = mLiveData_coloTestResult.getValue();
        if (current != null && current.getCropImage() != null) {
            current.getCropImage().recycle();
        }
        updateColoTestResult(result -> result.setCropImage(bitmap));
    }
    private  void updateCorrectedColor(Integer color) {
        updateColoTestResult(result -> result.setCorrectedColor(color));
    }

    private  void updateRGB(RGB rgb) {
        updateColoTestResult(result -> result.setRGB(rgb));
    }
    private  void updateHSV(HSV hsv) {
        updateColoTestResult(result -> {
            result.setHsv(hsv);
            result.setHSV_value(hsv.toString());
        });
    }
    private  void updateDiseaseAnal(String diseaseAnal) {
        updateColoTestResult(result -> result.setDiseaseAnal(diseaseAnal));
    }
    /***
     * 设置标准曲线
     * @param showCurves 标准曲线
     */
    public void setStandardCurve(StandardCurve showCurves) {
        Log.d(TAG, "setStandardCurve: " + showCurves);
        mLiveData_curve.setValue(showCurves);
    }

    /***
     * 设置标准曲线和计算浓度值
     * @param showCurve 标准曲线
     */
    public void setStandardCurveAndCalculateCO(StandardCurve showCurve) {
        Log.d(TAG, "setStandardCurveAndCalculateCO: " + showCurve);
        mLiveData_curve.setValue(showCurve);
        RGB rgb = mLiveData_rgb.getValue();
        if (rgb!=null){
            setToast(getString(R.string.toast_updateCO));
            // 使用Blue分量计算浓度值
            calculateCO(rgb.getBlue());
            // 进行病害分析
            diseaseAnal();
        }
    }

    /***
     * 设置原始图片
     * @param bitmap 原始图片
     */
    public void setOriginalImage(Bitmap bitmap){
        Log.d(TAG, "setOriginalImage: " + bitmap);
        mLiveData_originalImage.setValue(bitmap);
    }
    /***
     * 设置裁剪图片
     * @param bitmap 裁剪图片
     */
    public void setCropImage(Bitmap bitmap){
        Log.d(TAG, "setCropImage: " + bitmap);
        mLiveData_cropImage.setValue(bitmap);
    }

    //这里的color是RGB混合色
    public void setColor(int color) {
        mLiveData_correctedColor.setValue(color);
        //color转RGB
        RGB rgb = RGB.fromColor(color);
        mLiveData_rgb.setValue(rgb);
        //使用Blue分量计算浓度值
        calculateCO(rgb.getBlue());
        Log.d(TAG, "setColor: " + color +",  setRGB:"+rgb.toRGBString());
    }

    public void setRGB(RGB rgb){
        Log.d(TAG, "setRGB: " + rgb.toRGBString());
        mLiveData_rgb.setValue(rgb);
    }

    public void setHSV(HSV hsv){
        Log.d(TAG, "setHSV: " + hsv.toString());
        mLiveData_hsv.setValue(hsv);
    }
    public String getUnit() {
        StandardCurve curve = mLiveData_curve.getValue();
        return curve != null ? curve.getX_axis_unit() : "";
    }

    /***
     * 计算浓度值
     * @param blue 蓝色分量
     * @return 浓度值
     */
    public void calculateCO(int blue){
        StandardCurve curve = mLiveData_curve.getValue();
        if (curve != null) {
            Float co = curve.calculateX_toY((float) blue);
            mLiveData_CO.setValue(NumberUtils.roundCO(co));
            noticeCO(co,curve);
            calculateConfidenceInterval(curve, (float) blue);
        }
    }

    /***
     * 通知CO值是否属于正常范围
     * @param CO 浓度值
     * @param curve 标准曲线
     */
    private void noticeCO(Float CO,StandardCurve curve){
        if (CO < curve.getMin_CO()){
            mLiveData_notice.setValue(getString(R.string.toast_abnormal_CoLessThanNormalValue));
        }
        else if (CO > curve.getMax_CO()){
            mLiveData_notice.setValue(getString(R.string.toast_abnormal_CoGreaterThanNormalValue));
        }
        else
            mLiveData_notice.setValue(getString(R.string.toast_normal));
    }
    public void setCo(Float co){
        mLiveData_CO.setValue(co);
    }

    /***
     * 病害分析
     * @return 结果
     */
    public String diseaseAnal(){
        HSV hsv = mLiveData_hsv.getValue();
        RGB rgb = mLiveData_rgb.getValue();
        Float CO = mLiveData_CO.getValue();
        if (hsv != null && rgb != null && CO != null){
            //病害分析
            String result = "病害分析,blue值对应浓度值："+CO.toString();
            mLiveData_diseaseAnal.setValue(result);
            Log.d(TAG, "setDiseaseAnal: " + result);
            return result;
        }
        return null;
    }

    // 使用机器学习模型进行病害分析（示例）
/*    public void analyzeDiseaseAsync(HSV hsv) {
        CompletableFuture.supplyAsync(() -> {
            return DiseasePredictor.predict(hsv); // 假设的预测模型
        }, imageProcessor).thenAccept(result -> {
            safePost(mLiveData_diseaseAnal, result);
        });
    }*/

    @Transaction
    @SuppressLint("SimpleDateFormat")
    public Boolean save() {
        try {
            // 1. 获取当前结果（线程安全获取）
            ColoTestResult result = mLiveData_coloTestResult.getValue();
            if (result == null){
                setToast(getString(R.string.toast_tempSave_fail));
                return false;
            }
            // 2. 查看是否选择曲线  不使用直线也可以保存
/*            StandardCurve curve = mLiveData_curve.getValue();
            if (curve == null) {
                mLiveData_toast.setValue(getString(R.string.toast_pleaseSelectCurve));
                return false;
            }*/
            // 3. 设置保存时间（当前时间）
            // 使用线程安全的格式化方式
            String dateString = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ZoneId.systemDefault())
                    .format(Instant.now());
            result.setDateTime(dateString);
            // 4. 更新LiveData
            mLiveData_coloTestResult.setValue(result);
            DataRepository.getInstance().setColoTestResult(result);//保存到数据仓库
            mLiveData_toast.setValue(getString(R.string.toast_tempSave_success));
            return true;
        }catch (Exception e){
            Log.e(TAG,"暂存数据时发生异常", e);
            setToast(getString(R.string.toast_tempSave_fail_exception));
            return false;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // 主动释放Bitmap资源
        releaseBitmaps();
    }
    private void releaseBitmaps() {
        Bitmap original = mLiveData_originalImage.getValue();
        Bitmap crop = mLiveData_cropImage.getValue();
        if (original != null && !original.isRecycled()) original.recycle();
        if (crop != null && !crop.isRecycled()) crop.recycle();
    }
    /***
     * 计算置信区间
     */
    public void calculateConfidenceInterval(StandardCurve curve,Float bValue){
        // 输入参数校验
        if (curve == null || bValue == null) {
            Log.w(TAG, "计算置信区间失败：标准曲线或b值为空");
            mLiveData_confidenceInterval.setValue(null);
            return;
        }
        List<Point> pointList = StandardCurve.getPointList(curve.getPoint_set());
        List<Entry> entries = Point.pointList_to_entryList(pointList);
        CombinedChartUtils.LinearRegressionResult linearRegressionResult = CombinedChartUtils.build_FitLine(entries);
        float[] interval_lgx = linearRegressionResult.inversePredictInterval(bValue);//逆预测,lgx值置信区间
        if (interval_lgx == null || interval_lgx.length != 2) {
            Log.w(TAG, "计算置信区间失败：逆预测结果格式不正确");
            mLiveData_confidenceInterval.setValue(null);
            return;
        }
        float[] interval_x = new float[2];// X值置信区间
        interval_x[0] = NumberUtils.roundCurve_lgX_avgY((float) Math.pow(10,interval_lgx[0]));
        interval_x[1] = NumberUtils.roundCurve_lgX_avgY((float) Math.pow(10,interval_lgx[1]));
        mLiveData_confidenceInterval.setValue(interval_x);
    }

    public boolean clearAll() {
        // 移除 MediatorLiveData 的所有数据源
        mLiveData_coloTestResult.removeSource(mLiveData_curve);
        mLiveData_coloTestResult.removeSource(mLiveData_originalImage);
        mLiveData_coloTestResult.removeSource(mLiveData_cropImage);
        mLiveData_coloTestResult.removeSource(mLiveData_correctedColor);
        mLiveData_coloTestResult.removeSource(mLiveData_rgb);
        mLiveData_coloTestResult.removeSource(mLiveData_hsv);
        mLiveData_coloTestResult.removeSource(mLiveData_CO);
        mLiveData_coloTestResult.removeSource(mLiveData_diseaseAnal);
        mLiveData_coloTestResult.removeSource(mLiveData_toast);
        // 清空单个 LiveData
        mLiveData_curve.setValue(null);
        mLiveData_originalImage.setValue(null);
        mLiveData_cropImage.setValue(null);
        mLiveData_correctedColor.setValue(null);
        mLiveData_rgb.setValue(null);
        mLiveData_hsv.setValue(null);
        mLiveData_CO.setValue(null);
        mLiveData_diseaseAnal.setValue(null);
        mLiveData_toast.setValue(null);
        // 将 mLiveData_coloTestResult 的值设置为 null 或默认值
        mLiveData_coloTestResult.setValue(null);
        return true;
    }
}
