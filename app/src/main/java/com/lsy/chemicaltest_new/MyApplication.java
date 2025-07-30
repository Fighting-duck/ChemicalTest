package com.lsy.chemicaltest_new;

import static com.blankj.utilcode.util.StringUtils.getString;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.blankj.utilcode.util.Utils;
import com.github.mikephil.charting.data.Entry;
import com.lsy.chemicaltest_new.database.AppDatabase;
import com.lsy.chemicaltest_new.domain.CurveSetting;
import com.lsy.chemicaltest_new.domain.Experimenter;
import com.lsy.chemicaltest_new.domain.Expression;
import com.lsy.chemicaltest_new.domain.Point;
import com.lsy.chemicaltest_new.domain.Sample;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.domain.language.ContextWrapper;
import com.lsy.chemicaltest_new.database.SharePreferencesManager;
import com.lsy.chemicaltest_new.utils.DynamicStringUtils;
import com.lsy.chemicaltest_new.utils.PhotoUtil;
import com.lsy.chemicaltest_new.utils.SavedDisplay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    public static MyApplication INSTANCE;//得到Application唯一实例
    // 使用静态线程池避免频繁创建,避免 OOM 风险
    public static ExecutorService DB_EXECUTOR = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() // 根据CPU核心数动态设置
    );
    private List<SavedDisplay> displays = new LinkedList<>();
    public static AppDatabase DATABASE_INSTANCE;//数据库
    private boolean isSaveColoOriginalImage = false;
    private boolean isUpdateHistory = true;//用户是否更新了历史记录（删除更新）
    private boolean isUpdateSample = true;//用户是否更新了样本（添加或删除）


    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        DATABASE_INSTANCE = AppDatabase.getInstance(this);
        setDefaultProperties();

        // Application 中初始化
        DynamicStringUtils.init(Utils.getApp());

        SharePreferencesManager sharePreferencesManager = new SharePreferencesManager(this);
        if (sharePreferencesManager.isFirstLaunch()){ //若APP为首次启动
           DB_EXECUTOR.execute(() -> {
               initSampleExample(this);
               sharePreferencesManager.setFirstLaunch(false);
           });
        }

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // 关闭线程池
        DB_EXECUTOR.shutdown(); // 停止接受新任务

        try {
            // 等待现有任务完成（最大30秒）
            if (!DB_EXECUTOR.awaitTermination(30, TimeUnit.SECONDS)) {
                DB_EXECUTOR.shutdownNow(); // 强制终止
            }
        } catch (InterruptedException e) {
            // 如果等待过程中被中断，也强制关闭
            DB_EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        SharePreferencesManager sharePreferencesManager = new SharePreferencesManager(base);
        String languageCode = sharePreferencesManager.getLanguage();
        String userName = sharePreferencesManager.getUserName();
        // 如果语言设置为空，默认使用中文
        if (languageCode == null || languageCode.isEmpty())
            languageCode = SharePreferencesManager.LANGUAGE_CODE_CH;
        // 如果用户名未设置，初始化为 "Experimenter"
        if (userName == null || userName.isEmpty())
            sharePreferencesManager.setUserName("Experimenter");

        Log.d(TAG, "用户名: " + sharePreferencesManager.getUserName());
        super.attachBaseContext(ContextWrapper.wrap(base, languageCode));
    }

    public void setDefaultProperties(){
        // 添加曲线设置
       Integer count = DATABASE_INSTANCE.getCurveSettingDao().getCount();
       if (count == 0){
           CurveSetting curveSetting = new CurveSetting();
           curveSetting.setX_axis_unit(getString(R.string.unit_mol));
           curveSetting.setMin_CO(0.0f);
           curveSetting.setMax_CO(10.0f);
           curveSetting.setMinCorr(90.0f);
           DATABASE_INSTANCE.getCurveSettingDao().insert(curveSetting);
       }
       else
           Log.d(TAG, "setCurveDefaultProperties: " + count);
    }
    /**
     * 初始化样本数据
     */
    public void initSampleExample(Context context){
        if (context == null) {
            Log.e(TAG, "Context is null, cannot initialize sample example");
            return;
        }
        try{
            // 添加样本数据
            Sample sample = new Sample();
            sample.setName("天然轻质石油");
            sample.setDescription("轻质油一般为褐色，也有棕黄色");
            //保存图片
            Bitmap image = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_example_sample);
            String path = PhotoUtil.saveBitmapToFile(this, image, "sample_");
            sample.setImagePath(path);
            Long sample_id = DATABASE_INSTANCE.getSampleDao().add(sample);
            // 随机生成三条直线点集,并保存在数据库中
            Expression expression_elec = new Expression(0.235f, -0.567f);
            Expression expression_colo = new Expression(0.457f, -0.482f);
            Expression expression_thermal = new Expression(-0.723f, 0.426f);

            float minX = 0.0f;
            float maxX = 10.0f;
            float corr = 92.0f;
            List<Point> points_elec = createPoints(expression_elec,minX, maxX, corr, 20);
            List<Point> points_colo = createPoints(expression_colo,minX, maxX, corr, 13);
            List<Point> points_thermal = createPoints(expression_thermal,minX, maxX, corr, 15);

            List<Long> pointIds_elec = savePoints(points_elec);
            List<Long> pointIds_colo = savePoints(points_colo);
            List<Long> pointIds_thermal = savePoints(points_thermal);

            // 添加样品的标准曲线
            // 共同点
            StandardCurve curve = new StandardCurve();
            curve.setSample_id(Math.toIntExact(sample_id));
            curve.setCORR(corr);
            curve.setX_axis_unit(getString(R.string.unit_mol));
            curve.setMin_CO(0.0f);
            curve.setMax_CO(10.0f);
            curve.setMinCorr(90.0f);
            // 1. 电信号曲线
            curve.setName(sample.getName()+"-"+getString(R.string.elec)+"-标准直线");
            curve.setType(1);
            curve.setPoint_set(pointIds_elec.toString());
            curve.setExpression(expression_elec.getK() + "," + expression_elec.getB());
            curve.setY_axis_unit(getString(R.string.unit_mA));
            curve.setDescription("一般而言，随着轻质石油中目标电活性成分浓度升高，检测电流呈线性或特定函数关系增强。");
            DATABASE_INSTANCE.getStandardCurveDao().add(curve);
            // 2. 比色曲线
            curve.setName(sample.getName()+"-"+getString(R.string.colo)+"-标准直线");
            curve.setType(2);
            curve.setPoint_set(pointIds_colo.toString());
            curve.setExpression(expression_colo.getK() + "," + expression_colo.getB());
            curve.setY_axis_unit(getString(R.string.unit_blue));
            curve.setDescription("天然轻质石油颜色多样，常见为褐色、棕黄色等，其颜色主要由胶质、沥青质等含量决定，含量越高颜色越深。");
            DATABASE_INSTANCE.getStandardCurveDao().add(curve);
            // 3. 光热曲线
            curve.setName(sample.getName()+"-"+getString(R.string.thermal)+"-标准直线");
            curve.setType(3);
            curve.setPoint_set(pointIds_thermal.toString());
            curve.setExpression(expression_thermal.getK() + "," + expression_thermal.getB());
            curve.setY_axis_unit(getString(R.string.unit_degree));
            curve.setDescription("在常见温度范围（-20℃-50℃）内，随着温度升高，轻质石油密度呈近似线性下降。");
            DATABASE_INSTANCE.getStandardCurveDao().add(curve);
        }
        catch (Exception e){
            Log.e(TAG, "addStandardCurve: " + e.getMessage());
        }
    }
    /***
     * 随机生成曲线y=kx+b的n个点,x取值范围（minx,maxX）并且这n个点的拟合系数为corr
     * @param expression 曲线k,b
     * @param minX x的最小值
     * @param maxX x的最大值
     * @param corr 目标相关系数(0到100之间)
     * @param n 生成点的数量
     * @return 生成的点集
     */
    @SuppressLint("SimpleDateFormat")
    private List<Point> createPoints(Expression  expression, float minX, float maxX, float corr, int n) {
        List<Point> points = new ArrayList<>();
        Random random = new Random();

        // 参数验证
        if (minX >= maxX) {
            throw new IllegalArgumentException("minX must be less than maxX");
        }
        if (n <= 0) {
            throw new IllegalArgumentException("n must be a positive integer");
        }
        // 将0-100的相关系数转换为-1到1的范围
        corr = Math.max(0, Math.min(100, corr)) / 100.0f;
        // 确保不为±1，避免除零错误
        corr = Math.max(-0.99f, Math.min(0.99f, corr));

        // 生成均匀分布的x值
        float[] xValues = new float[n];
        float step = (maxX - minX) / (n - 1);
        for (int i = 0; i < n; i++) {
            xValues[i] = minX + i * step;
        }

        // 计算x的统计特性
        float meanX = (minX + maxX) / 2;
        float sumSqDevX = 0;
        for (float x : xValues) {
            sumSqDevX += (float) Math.pow(x - meanX, 2);
        }
        float stdX = (float) Math.sqrt(sumSqDevX / (n - 1));

        // 计算所需的噪声标准差
        float k = expression.getK();
        float b = expression.getB();
        float noiseStd = (float) (Math.abs(k) * stdX * Math.sqrt(1.0 / (corr * corr) - 1));

        // 生成具有指定相关系数的点
        long baseTime = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            float x = xValues[i];

            // 计算理论y值
            float yTheoretical = k * x + b;

            // 添加高斯噪声
            float noise = (float) (random.nextGaussian() * noiseStd);
            float y = yTheoretical + noise;

            // 生成唯一时间戳
            Date currentTime = new Date(baseTime + i * 1000L);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String add_time = formatter.format(currentTime);

            points.add(new Point(x, y, add_time));
        }

        return points;
    }

    /***
     * 在数据库中保存曲线的point
     * @return 所有point的id
     */
    private List<Long> savePoints(List<Point> pointList) {
        long[] ids = DATABASE_INSTANCE.getPointDao().insertAllWithRollback(pointList);

        // 将基本类型long数组转换为包装类型Long列表
        List<Long> result = new ArrayList<>(ids.length);
        for (long id : ids) {
            result.add(id); // 自动装箱从long转换为Long
        }

        return result;
    }

    public boolean isUpdateHistory() {
        return isUpdateHistory;
    }
    public void setUpdateHistory(boolean updateHistory) {
        isUpdateHistory = updateHistory;
    }

    public boolean isUpdateSample() {
        return isUpdateSample;
    }
    public void setUpdateSample(boolean updateSample) {
        isUpdateSample = updateSample;
    }

    public boolean getIsSaveColoOriginalImage() {
        return isSaveColoOriginalImage;
    }
    public void setIsSaveColoOriginalImage(boolean isSaveColoOriginalImage) {
        this.isSaveColoOriginalImage = isSaveColoOriginalImage;
    }
}


