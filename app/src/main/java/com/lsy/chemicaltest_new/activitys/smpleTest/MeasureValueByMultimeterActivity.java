package com.lsy.chemicaltest_new.activitys.smpleTest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;

import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.adapters.TestValueAdapter;
import com.lsy.chemicaltest_new.databinding.ActivityMeasureValueByMultimeterBinding;
import com.lsy.chemicaltest_new.domain.BleDeviceInfo;
import com.lsy.chemicaltest_new.domain.TestValue;
import com.lsy.chemicaltest_new.fragments.ConnectMultimeterFragment;
import com.lsy.chemicaltest_new.models.MeasureValueByMultimeterViewModel;
import com.lsy.chemicaltest_new.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MeasureValueByMultimeterActivity extends AppCompatActivity {
    public static final String TAG = "MeasureValueByMultimeterActivity";
    private ActivityMeasureValueByMultimeterBinding mBinding;
    private Context mContext;
    private ConnectMultimeterFragment mFragment;
    private MeasureValueByMultimeterViewModel mViewModel;
    private ConnectMultimeterFragment.ShowModel mShowModel;
    private TestValueAdapter mTestValueAdapter;
    public static final String GET_SHOW_MODE = "showModel";
    // 返回数据key
    public static final String RETURN_TEST_VALUE = "result_testValue";//最大电流值/衡定温度值
    public static final String RETURN_ELEC_VALUE_LIST = "result_ValueList";//测试电流值列表
    public static final String RETURN_BLE_DEVICE_INFO = "result_bleDeviceInfo";//万用表设备信息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMeasureValueByMultimeterBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mContext = this;
        mViewModel = new ViewModelProvider(this).get(MeasureValueByMultimeterViewModel.class);
        handleIntent(getIntent());
    }

    @Override
    public void onStart() {
        super.onStart();
        createMultimeterDialogFragment();
        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // 统一处理 Intent 数据的方法
    private void handleIntent(Intent intent) {
        if (intent != null) {
            // 获取传递的参数,强制类型转换
            mShowModel = (ConnectMultimeterFragment.ShowModel) intent.getSerializableExtra(GET_SHOW_MODE);
            // 根据数据更新界面或逻辑
            if (mShowModel != null){
                switch (mShowModel){
                    case ELEC:
                        mBinding.llElec.setVisibility(View.VISIBLE);
                        mBinding.tvResultValuePrompt.setText(getString(R.string.text_MaxValue_4));
                        break;
                    case TEMPERATURE:
                        mBinding.llElec.setVisibility(View.GONE);
                        mBinding.tvResultValuePrompt.setText(getString(R.string.text_temperature));
                        break;
                }
            }
        }
    }
    /**
     * 创建连接万用表Fragment
     */
    public void createMultimeterDialogFragment(){
        // 检查容器是否存在
        View container = mBinding.getRoot().findViewById(R.id.fcv_ConnectMultimeterFragment);
        if (container == null) {
            Log.e(TAG, "Fragment container not found!");
            return;
        }
        //添加另一个布局
        mFragment = ConnectMultimeterFragment.newInstance(ConnectMultimeterFragment.ShowModel.ELEC);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fcv_ConnectMultimeterFragment, mFragment);
        // 不调用 addToBackStack(null);
        transaction.commit();
    }

    private void initUI() {
        //recycleView
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext,1,GridLayoutManager.HORIZONTAL, false);
        mTestValueAdapter = new TestValueAdapter();
        mBinding.rvTestValue.setLayoutManager(gridLayoutManager);
        mBinding.rvTestValue.setAdapter(mTestValueAdapter);
        DividerItemDecoration decoration = new DividerItemDecoration(mContext, DividerItemDecoration.HORIZONTAL);
        mBinding.rvTestValue.addItemDecoration(decoration);

        //添加监听器
        mBinding.btnStartTest.setOnClickListener(this::onCLick);
        mBinding.ivBack.setOnClickListener(this::onCLick);
        mBinding.ivSave.setOnClickListener(this::onCLick);

        //监听万用表数据
        mFragment.setOnMeasureValueListener(new ConnectMultimeterFragment.OnFragmentMultimeterListener() {
            @Override
            //检测万用表实时数据变化
            public void onMeasureValue(TestValue testValue) {
               if (mIsRecode){
                   processResult(testValue);
               }
            }

            @Override
            //监听万用表设备信息
            public void onDeviceInfo(BleDeviceInfo bleDeviceInfo) {
                if (bleDeviceInfo!=null){
                    // 若当前显示状态与当前万用表档位相匹配，则可以测量
                    if ((mShowModel == ConnectMultimeterFragment.ShowModel.ELEC &&
                            Objects.equals(bleDeviceInfo.getGear(), getString(R.string.multimeter_DcuA)))
                    || (mShowModel == ConnectMultimeterFragment.ShowModel.TEMPERATURE &&
                            Objects.equals(bleDeviceInfo.getGear(), getString(R.string.multimeter_Celsius)))){
                        mBinding.btnStartTest.setEnabled(true);
                        mBinding.tvPromptEnable.setVisibility(View.GONE);
                        mViewModel.setBleDeviceInfo_Elec(bleDeviceInfo);
                    }
                    else{
                        mBinding.btnStartTest.setEnabled(false);
                        mBinding.tvPromptEnable.setVisibility(View.VISIBLE);
                    }
                }
                else { mBinding.btnStartTest.setEnabled(false);
                    mBinding.tvPromptEnable.setVisibility(View.GONE);
                    mViewModel.setBleDeviceInfo_Elec(null);
                }
            }
        });

        //设置观察者
        mViewModel.getLiveData_toast().observe(this, toast -> {
            if (toast != null){
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
                mViewModel.setToast(null);
            }
        });
        //开始测试
        mViewModel.getLiveData_TestValueList().observe(this, new Observer<List<TestValue>>() {
            @Override
            public void onChanged(List<TestValue> values) {
                if (values==null){
                    mBinding.tvStartTime.setText("");
                    mBinding.tvEndTime.setText("");
                    mTestValueAdapter.clear();
                    return;
                }
                mTestValueAdapter.update(values);
                int listSize = values.size();
                if(listSize>1) {
                    mBinding.tvStartTime.setText(values.get(0).getTestTime());
                    if (listSize==14) {
                        mBinding.tvEndTime.setText(values.get(13).getTestTime());
                        TestValue maxValue = getMaxValue(values);
                        mViewModel.setResultValue(maxValue);
                    }
                }

            }
        });
        // 最大电流值 或 温度值
        mViewModel.getLiveData_ResultValue().observe(this, maxValue -> {
            if (maxValue!=null){
                mBinding.tvResultValue.setText(maxValue.toString());
                mViewModel.setToast(getString(R.string.toast_elec_testOver));
                mBinding.pbTestDegree.setVisibility(View.GONE);
            }
            else
                mBinding.tvResultValue.setText(getString(R.string.default_no));
        });
        mViewModel.getLiveData_BleDeviceInfo().observe(this, bleDeviceInfo -> {
            
        });
    }

    private void onCLick(View view) {
        int id = view.getId();
        if (id == mBinding.ivBack.getId()) {
            // TODO : 取消
            finish();
        } else if (id == mBinding.ivSave.getId()) {
            // TODO : 确认
            Intent returnIntent = new Intent();
            // 根据当前显示模式（电信号/温度）处理数据
            switch (mShowModel){
                case ELEC:
                    // 获取测量值列表并传递（若不为空）
                    List<TestValue> values = mViewModel.getValueList();
                    if (values!=null && !values.isEmpty()){
                        returnIntent.putParcelableArrayListExtra(RETURN_ELEC_VALUE_LIST, new ArrayList<>(values));// 14次测量值列表
                    }
                    break;
                case TEMPERATURE:

                    break;
            }
            // 返回测量值
            TestValue resultValue = mViewModel.getResultValue();
            if (resultValue==null){
                mViewModel.setToast("测量值不存在！");
                return;
            }
            else returnIntent.putExtra(RETURN_TEST_VALUE, resultValue);
            // 返回设备信息
            BleDeviceInfo bleDeviceInfo = mViewModel.getBleDeviceInfo_Elec();
            returnIntent.putExtra(RETURN_BLE_DEVICE_INFO, bleDeviceInfo);// 设备信息
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
        else if (id == mBinding.btnStartTest.getId()) {
            if (mShowModel == ConnectMultimeterFragment.ShowModel.ELEC){
                mTestValueAdapter.clear();
            }
            else if (mShowModel == ConnectMultimeterFragment.ShowModel.TEMPERATURE){
                mViewModel.setToast(getString(R.string.toast_elec_testing));
            }
            mIsRecode = true;
        }
    }
    /**
     * 获取最大值
     * @param testValueList 测试值列表
     * @return 最大值
     */
    public TestValue getMaxValue(List<TestValue> testValueList){
        if (testValueList==null || testValueList.isEmpty())
            return null;
        TestValue maxValue = testValueList.get(0);
        for (TestValue value : testValueList) {
            if (value.getValue()>maxValue.getValue()){
                maxValue = value;
            }
        }
        return maxValue;
    }

    //控制测量参数
    private Boolean mIsRecode = false;//正在记录数据。。。，记录中为ture，测量结束后恢复为false
    private Boolean mIsFirstTest = true;//仅第一次测量时为true,第一次测量结束后恢复为false
    private Integer mTestControlled = 0;//控制测试次数
    private Float mOldDegree = null;
    private Boolean lock = false;//锁
    private long mOldTime;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private final Integer readingNum = 14;//测量次数
    private final long readingTimeInterval = 4000/40;//测量时间间隔ms 电流

    /***
     * 处理万用表返回结果,依据结果的档位，判断是进行温度检测还是电流检测
     * @param testValue 万用表返回数据
     */
    public void processResult(TestValue testValue) {
        if (!Objects.equals(testValue.getUnit(), getString(R.string.unit_degree))) {
            //电信号检测
            if (lock){
                lock = false;//上锁
                mHandler.postDelayed(() -> checkCurrent(testValue), readingTimeInterval);
            }
            if (mIsFirstTest) {
                Log.d(TAG, "=======================电流检测开始============================");
                //mViewModel.updateBleDeviceInfo_Elec(result.getBleDeviceInfo());
                lock = true;//解锁
            }

        } else {
            Long  currentTime = testValue.getTime_long();
            Log.d(TAG, "温度检测，当前温度：" + testValue.toString());
            //温度检测
            if (mIsFirstTest) {
                Log.d(TAG, "=======================温度检测开始============================");
                mOldDegree = testValue.getValue();//电流值或温度值
                mOldTime = testValue.getTime_long();
                //mViewModel.updateBleDeviceInfo_Degree(result.getBleDeviceInfo());
                lock = true;
                mIsFirstTest = false;
            }
            if (currentTime - mOldTime > 4000){
                checkTemperature(testValue,currentTime);
            }
        }
    }

    /***
     * 该方法在 4 秒后被调用，用于检查温度是否有变化。若有变化，就再次设置定时器；若没有变化，就结束温度检测。
     * @param testValue  万用表返回数据
     */
    private void checkTemperature(TestValue testValue,long currentTime) {
        float yValue = testValue.getValue();
        if (yValue != mOldDegree) {
            Log.d(TAG, "温度改变，当前oldDegree温度：" +mOldDegree+" -> "+ yValue);
            mViewModel.setToast("温度改变!");
            mOldTime = currentTime;
            mOldDegree = yValue;
        } else {
            mViewModel.setResultValue(testValue);
            mIsRecode = false;
            Log.d(TAG, "温度检测结束,温度值为：" + yValue + "℃");
        }
        lock = true;
    }

    /***
     * 该方法在 readingTimeInterval 时间后被调用，用于检查电流测量是否达到次数上限。若未达到，就记录当前值并设置下一次定时器；若达到，就结束电流检测。
     * @param testValue 万用表返回数据
     */
    private void checkCurrent(TestValue testValue) {
        Log.d(TAG, "电流检测，当前第" + mTestControlled + "次电流测量，当前电流：" + testValue.toString());
        mTestControlled++;
        if (mTestControlled > readingNum) {
            mIsRecode = false;
            Log.d(TAG, "电流检测结束,最大电流值为：" + testValue);
        } else {
            // 获取当前时间（确保每次都是最新时间）
            Long currentTime = testValue.getTime_long();
            float xValue = TimeUtil.timeStrToNum(currentTime);
            testValue.setTestTime(TimeUtil.timeNumToStr(xValue));
            mViewModel.addValueToList(testValue);
        }
        lock = true;//解锁
    }
}