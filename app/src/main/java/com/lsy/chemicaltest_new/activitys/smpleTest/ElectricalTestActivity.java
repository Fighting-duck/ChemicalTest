package com.lsy.chemicaltest_new.activitys.smpleTest;

import static com.blankj.utilcode.util.StringUtils.getString;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.activitys.BaseActivity;
import com.lsy.chemicaltest_new.adapters.TestValueAdapter;
import com.lsy.chemicaltest_new.database.DataRepository;
import com.lsy.chemicaltest_new.databinding.ActivityElectricalTestBinding;
import com.lsy.chemicaltest_new.adapters.DeviceAdapter;
import com.lsy.chemicaltest_new.domain.ElecTestResult;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.domain.Temperature_Elec;
import com.lsy.chemicaltest_new.domain.TestValue;
import com.lsy.chemicaltest_new.domain.dialog.CurveDetailDialog;
import com.lsy.chemicaltest_new.fragments.SelectCurveFragment;
import com.lsy.chemicaltest_new.models.ElecViewModel;
import com.lsy.chemicaltest_new.utils.BleUtil;
import com.lsy.chemicaltest_new.utils.LineChartUtil;
import com.lsy.chemicaltest_new.utils.TimeUtil;

import java.util.List;
import java.util.Objects;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public class ElectricalTestActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {
    private static final String TAG = "ElectricalTestActivity";
    private ActivityElectricalTestBinding mBinding;
    private Context mContext;
    private BleUtil mBleUtil;
    private DeviceAdapter mDeviceAdapter;
    private ElecViewModel mViewModel;
    private TestValueAdapter mTestValueAdapter;
    private Handler mHandler = new Handler();
    private final static int RC_BLE_PERMISSIONS  = 1000;
    //蓝牙状态监听
    private final BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    mViewModel.setToast(getString(R.string.toast_elec_closeBLE));
                    mBleUtil.onDestroy();
                    break;
                case BluetoothAdapter.STATE_ON:
                    mViewModel.setToast(getString(R.string.toast_elec_openDevice));
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    // 蓝牙正在关闭（可选处理）
                    break;
            }
        }
    };
    private boolean mIsReceiverRegistered = false; // 控制注册状态的标志


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityElectricalTestBinding.inflate(getLayoutInflater());
        mContext = this;
        setContentView(mBinding.getRoot());

        //mViewModel = MyApplication.INSTANCE.getElecViewModel();
        mViewModel = new ViewModelProvider(this).get(ElecViewModel.class);
        mViewModel.setContext(this);
        mBleUtil = new BleUtil(ElectricalTestActivity.this,mViewModel);
        mDeviceAdapter = new DeviceAdapter(mBleUtil);
        mBinding.rvDevices.setAdapter(mDeviceAdapter);
        initUI();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mIsReceiverRegistered){
            //注册蓝牙状态监听
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBluetoothStateReceiver, filter);
            mIsReceiverRegistered = true;
        }
        restoreData();
    }

    private void restoreData() {
        ElecTestResult elecTestResult = DataRepository.getInstance().getElecTestResult();
        Temperature_Elec temperature_elec = DataRepository.getInstance().getTemperature_Elec();
        if (elecTestResult!=null){
            mViewModel.setStandardCurve_Elec(elecTestResult.getStandardCurve());
            List<TestValue> testValueList = elecTestResult.getTestValueList();
            if (!testValueList.isEmpty()){
                mViewModel.setTestValueList(testValueList);
            }
            mViewModel.setDiseaseAnal_Elec(elecTestResult.getDiseaseAnal());
        }
        if (temperature_elec!=null){
            mViewModel.setStandardCurve_Degree(temperature_elec.getStandardCurve());
            mViewModel.setDegree(temperature_elec.getTemperature());
            mViewModel.setCOTemperature(temperature_elec.getDetectionCo());
            mViewModel.setDiseaseAnal_temperature(temperature_elec.getDiseaseAnal());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsReceiverRegistered){
            //注销蓝牙状态监听
            try {
                unregisterReceiver(mBluetoothStateReceiver);
                mIsReceiverRegistered = false;
            } catch (IllegalArgumentException e) {
                // 未注册时会抛出异常，忽略
                Log.e(TAG, "Receiver not registered", e);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBleUtil.stopNotify();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBleUtil.onDestroy();
    }

    @SuppressLint("SetTextI18n")
    private void initUI() {
        //recycleView
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext,1,GridLayoutManager.HORIZONTAL, false);
        mTestValueAdapter = new TestValueAdapter();
        mBinding.rvTestValue.setLayoutManager(gridLayoutManager);
        mBinding.rvTestValue.setAdapter(mTestValueAdapter);
        DividerItemDecoration decoration = new DividerItemDecoration(mContext, DividerItemDecoration.HORIZONTAL);
        mBinding.rvTestValue.addItemDecoration(decoration);

        LineChartUtil.setLineChart(mBinding.lcChart);
        mBinding.lcChart.getLegend().setEnabled(false);//图例不可用
        IAxisValueFormatter formatter = new IAxisValueFormatter() {   //将x轴的数值转换为字符串形式的时间表示
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                //将value转化为时间
                return TimeUtil.timeNumToStr(value);
            }
        };
        mBinding.lcChart.getXAxis().setValueFormatter(formatter);//设置X轴值为字符串

        //设置监听器
        mBinding.ivBack.setOnClickListener(this::onCLick);
        mBinding.btnTestConnect.setOnClickListener(this::onCLick);
        mBinding.btnStartTest.setOnClickListener(this::onCLick);
        mBinding.ivSave.setOnClickListener(this::onCLick);
        mBinding.btnStartAnalElec.setOnClickListener(this::onCLick);
        mBinding.btnStartAnalTemperature.setOnClickListener(this::onCLick);
        mBinding.btnSelectElecCurve.setOnClickListener(this::onCLick);
        mBinding.btnSelectDegreeCurve.setOnClickListener(this::onCLick);
        mBinding.tvElecCurve.setOnClickListener(this::onCLick);
        mBinding.tvDegreeCurve.setOnClickListener(this::onCLick);
        mBinding.ivNoticeElec.setOnClickListener(this::onCLick);
        mBinding.ivNoticeDegree.setOnClickListener(this::onCLick);

        //设置观察者
        mViewModel.getLiveData_toast().observe(this, toast -> {
            if (toast != null){
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
                mViewModel.setToast(null);
            }
        });
        mViewModel.getLiveData_StandardCurve_Elec().observe(this, curve -> {
            if (curve!= null){
                mBinding.tvElecCurve.setText(curve.getName());
            }
            else
                mBinding.tvElecCurve.setText(R.string.default_no);
        });
        mViewModel.getLiveData_StandardCurve_Degree().observe(this, curve -> {
            if (curve!= null){
                mBinding.tvDegreeCurve.setText(curve.getName());
            }
            else
                mBinding.tvDegreeCurve.setText(R.string.default_no);
        });
        //万能表连接情况
        mViewModel.getLiveData_isConnectDevice().observe(this, state -> {
            if (state!=null){
                if (state) mBinding.tvConnectionSituation.setText(R.string.text_connection_true);
                else mBinding.tvConnectionSituation.setText(R.string.text_connection_false);
            }
        });
        //挡位、里程
        mViewModel.getLiveData_BleDeviceInfo().observe(this, bleDeviceInfo -> {
            if (bleDeviceInfo!=null){
                mBinding.tvCurrentGear.setText(bleDeviceInfo.getGear());
                mBinding.tvMileage.setText(bleDeviceInfo.getMileage());
                mBinding.btnStartTest.setEnabled(true);
                if (Objects.equals(bleDeviceInfo.getGear(), getString(R.string.multimeter_Celsius)))
                    mBinding.btnStartTest.setText(R.string.text_StartTest_temperature);
                else
                    mBinding.btnStartTest.setText(R.string.text_StartTest_elec);
            }
            else {
                //复原
                mBinding.tvCurrentGear.setText(R.string.default_no);
                mBinding.tvMileage.setText(R.string.default_no);
                mBinding.btnStartTest.setText(R.string.text_StartTest);
                mBinding.btnStartTest.setEnabled(false);
            }
        });
        //实时测量值
        mViewModel.getLiveData_RealTimeValue().observe(this, testValue -> {
            if (testValue!= null){
                if (testValue.getValue() == Float.MAX_VALUE){
                    mBinding.tvCurrentValue.setText("0L "+testValue.getUnit());
                    return;
                }
                mBinding.tvCurrentValue.setText(testValue.toString());
            }

        });
        //设备列表
        mViewModel.getLiveData_BleDeviceList().observe(this, bleDevices -> {
            if (bleDevices == null){
                mDeviceAdapter.clear();
            }
            else
                mDeviceAdapter.update(bleDevices);
        });
        //连接蓝牙设备
        mViewModel.getLiveData_ConnectBleDevice().observe(this, bleDevice -> {
            if (bleDevice!= null){
                mBleUtil.openNotify(bleDevice);
            }
        });
        //图表显示当前值
        mViewModel.getLiveData_Lines().observe(this, lineData -> {
            if (lineData!=null){
                mBinding.lcChart.getAxisLeft().setAxisMaximum(lineData.getYMax()+5 );
                mBinding.lcChart.setData(lineData);
                mBinding.lcChart.invalidate();
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
                        mViewModel.setMaxValue(maxValue);
                    }
                }

            }
        });
        // 最大值
        mViewModel.getLiveData_MaxValue().observe(this, maxValue -> {
            if (maxValue!=null){
                mBinding.tvMaxValue4.setText(maxValue.toString());
                mViewModel.calculate_ElecCO();//使用电流计算浓度
            }
            else
                mBinding.tvMaxValue4.setText(getString(R.string.default_no));
        });
        mViewModel.getLiveData_Degree().observe(this, degree -> {
            if (degree!=null){
                mBinding.tvCentigrade.setText(degree.toString());
                mBinding.pbTestDegree.setVisibility(View.GONE);
                mViewModel.setToast(getString(R.string.toast_elec_testOver));
                mBinding.btnStartAnalTemperature.setVisibility(View.VISIBLE);
                mViewModel.calculate_DegreeCO();//计算浓度
            }
            else {
                mBinding.tvCentigrade.setText(getString(R.string.default_no));
            }
        });
        mViewModel.getLiveData_COElec().observe(this, CO -> {
            if (CO != null){
                String unit = mViewModel.getUnit_elec();
                mBinding.tvConcentration.setText(CO +" "+unit);
                mBinding.btnStartAnalElec.setEnabled(true);
            }
            else{
                mBinding.tvConcentration.setText(getString(R.string.default_no));
                mBinding.btnStartAnalElec.setEnabled(false);
                mBinding.ivNoticeElec.setVisibility(View.GONE);
            }
        });
        mViewModel.getLiveData_DiseaseAnalElec().observe(this, result -> {
            if (result!= null)  // 病害分析结果不为空时，显示
                 mBinding.tvDiseaseAnalysisElec.setText(result);
        });
        mViewModel.getLiveData_COTemperature().observe(this, CO -> {
            if (CO != null){
                String unit = mViewModel.getUnit_degree();
                mBinding.tvConcentrationDegree.setText(CO + " "+ unit);
                mBinding.btnStartAnalTemperature.setEnabled(true);
            }
            else{
                mBinding.tvConcentrationDegree.setText(getString(R.string.default_no));
                mBinding.btnStartAnalTemperature.setEnabled(false);
                mBinding.ivNoticeDegree.setVisibility(View.GONE);
            }
        });
        mViewModel.getLiveData_DiseaseAnalTemperature().observe(this, result -> {
            if (result!= null)  // 病害分析结果不为空时，显示
                mBinding.tvDiseaseAnalysisTemperature.setText(result);
        });
        mViewModel.getLiveData_CO_noticeElec().observe(this, result -> {
            if (result != null){
                mBinding.ivNoticeElec.setVisibility(View.VISIBLE);
                if (result.equals(getString(R.string.toast_normal))){
                    mBinding.ivNoticeElec.setImageResource(R.drawable.icon_notice_normal);
                }
                else {
                    mBinding.ivNoticeElec.setImageResource(R.drawable.icon_notice_abnormal);
                }
            }
        });
        mViewModel.getLiveData_CO_noticeDegree().observe(this, result -> {
            if (result != null){
                mBinding.ivNoticeDegree.setVisibility(View.VISIBLE);
                if (result.equals(getString(R.string.toast_normal))){
                    mBinding.ivNoticeElec.setImageResource(R.drawable.icon_notice_normal);
                }
                else {
                    mBinding.ivNoticeDegree.setImageResource(R.drawable.icon_notice_abnormal);
                }
            }
        });
        mViewModel.getLiveData_ElecTestResult().observe(this, elecTestResult -> {
            if (elecTestResult!=null)
                Log.d(TAG, "elecTestResult:"+elecTestResult.toString());
        });
        mViewModel.getLiveData_Temperature_Elec().observe(this, temperature_elec -> {
            if (temperature_elec!=null){
                Log.d(TAG, "Temperature_Elec:"+temperature_elec.toString());
            }
        });
    }

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


    @SuppressLint("NewApi")
    private void onCLick(View view) {
        int id = view.getId();
        if (id ==mBinding.ivBack.getId()){
            finish();
            mViewModel.clearAll();
        }
        else if (id==mBinding.btnSelectElecCurve.getId()){
            showSelectDialog("select_curve_electrical");
        }
        else if (id==mBinding.btnSelectDegreeCurve.getId()){
            showSelectDialog("select_curve_degree");
        }
        else if (id==mBinding.tvElecCurve.getId()){
            if (mViewModel.getLiveData_StandardCurve_Elec().getValue()!=null) {
                CurveDetailDialog curveDetailDialog = new CurveDetailDialog(mContext,mViewModel.getLiveData_StandardCurve_Elec().getValue());
                curveDetailDialog.show();
            }
        }
        else if (id==mBinding.tvDegreeCurve.getId()){
            if (mViewModel.getLiveData_StandardCurve_Degree().getValue()!=null){
                StandardCurve standardCurve = mViewModel.getLiveData_StandardCurve_Degree().getValue();
                CurveDetailDialog curveDetailDialog = new CurveDetailDialog(mContext,mViewModel.getLiveData_StandardCurve_Degree().getValue());
                curveDetailDialog.show();
            }
        }
        else if (id==mBinding.btnTestConnect.getId()){
            // TODO : BLE测试连接
            checkBluetoothPermissions();
        }
        else if (id==mBinding.btnStartTest.getId()){
            if (mBleUtil != null){
                //判断是什么测量 无论是否选择直线，都可以做实验
                if (mViewModel.getLiveData_BleDeviceInfo().getValue()!=null) {
                    //测量温度  温度档+选择了温度曲线
                    if (Objects.equals(mViewModel.getLiveData_BleDeviceInfo().getValue().getGear(), getString(R.string.multimeter_Celsius)) //&& mViewModel.getLiveData_StandardCurve_Degree().getValue()!=null
                            ){
                        mViewModel.setDegree(null);
                        mBinding.pbTestDegree.setVisibility(View.VISIBLE);
                        mBinding.btnStartAnalTemperature.setEnabled(false);
                        mBleUtil.startTest();//开始测量
                        mViewModel.setToast(getString(R.string.toast_elec_testing));
                    }
                    else if(!Objects.equals(mViewModel.getLiveData_BleDeviceInfo().getValue().getGear(), getString(R.string.multimeter_Celsius)) //&& mViewModel.getLiveData_StandardCurve_Elec().getValue()!=null
                    ) {
                        mViewModel.setMaxValue(null);//清空最大值
                        mViewModel.clearValueList();//清空列表
                        mBleUtil.startTest();//开始测量
                    }
                    else mViewModel.setToast(getString(R.string.toast_pleaseSelectCurve));
                }
            }
        }
        else if (id==mBinding.btnStartAnalElec.getId()){
            mViewModel.diseaseAnalElec();//病害分析
        }
        else if (id==mBinding.btnStartAnalTemperature.getId()){
            mViewModel.diseaseAnalDegree();//病害分析
        }
        else if (id==mBinding.ivSave.getId()){
            //保存
            mViewModel.save();
            finish();
        }
        else if (id==mBinding.ivNoticeElec.getId()){
            if (mViewModel.getLiveData_CO_noticeElec().getValue()!=null)
                mViewModel.setToast(mViewModel.getLiveData_CO_noticeElec().getValue());
        }
        else if (id==mBinding.ivNoticeDegree.getId()){
            if (mViewModel.getLiveData_CO_noticeDegree().getValue()!=null)
                mViewModel.setToast(mViewModel.getLiveData_CO_noticeDegree().getValue());
        }
    }
    //  显示选择曲线对话框
    private void showSelectDialog(String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SelectCurveFragment existingFragment = (SelectCurveFragment) fragmentManager.findFragmentByTag(tag);
        if (existingFragment == null) {
            SelectCurveFragment dialogFragment = getSelectCurveFragment(tag);
            dialogFragment.show(fragmentManager, tag);
        }else {
            if (!existingFragment.isVisible()) {
                existingFragment.show(fragmentManager, tag);
            }
        }
    }
    // 获取选择曲线对话框
    @NonNull
    private SelectCurveFragment getSelectCurveFragment(String tag) {
        SelectCurveFragment dialogFragment;
        if(Objects.equals(tag, "select_curve_electrical"))
            dialogFragment = new SelectCurveFragment(1);
        else
            dialogFragment = new SelectCurveFragment(3);
        dialogFragment.setOnSelectCurveListener(new SelectCurveFragment.OnFragmentChangeListener() {
            @Override
            public void onSelectCurve(StandardCurve selectCurve) {
                if (Objects.equals(dialogFragment.getTag(), "select_curve_electrical"))
                    mViewModel.setElecCurveAndCalculateCO(selectCurve);
                else if (Objects.equals(dialogFragment.getTag(), "select_curve_degree"))
                    mViewModel.setDegreeCurveAndCalculateCO(selectCurve);
            }
        });
        return dialogFragment;
    }

    // 开始蓝牙扫描
    private void startBleScanning() {
        mBleUtil.startScan();
        mBinding.pbStartScan.setVisibility(View.VISIBLE);
        // 10秒后停止扫描指示器
        mHandler.postDelayed(() -> {
            if (!mBleUtil.isConnected()) { // 检查是否成功连接
                mBleUtil.stopScan();  // 超时后停止扫描
                mViewModel.setToast(getString(R.string.toast_elec_timeOut));
            }
            mBinding.pbStartScan.setVisibility(View.INVISIBLE);
        }, 10_000); // 15秒超时
    }
    /**
     * 检查蓝牙相关权限
     */
    private void checkBluetoothPermissions() {
        // 需要请求的权限列表
        String[] perms;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ 需要 BLUETOOTH_SCAN 和 BLUETOOTH_CONNECT
            perms = new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION // 部分设备仍需定位
            };
        } else {
            // 旧版本只需要定位权限
            perms = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        }

        if (EasyPermissions.hasPermissions(this, perms)) {
            // 已有权限
            onBluetoothPermissionsGranted();
        } else {
            // 请求权限
            requestBluetoothPermissions();
        }
    }


    /**
     * 请求权限（带权限解释）
     */
    @AfterPermissionGranted(RC_BLE_PERMISSIONS)
    private void requestBluetoothPermissions() {
        String[] perms;
        String rationale;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms = new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
            rationale = getString(R.string.permission_dialog_needBleAndLocation);
        } else {
            perms = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
            rationale = getString(R.string.permission_dialog_needLocation);
        }

        if (EasyPermissions.hasPermissions(this, perms)) {
            onBluetoothPermissionsGranted();
        } else {
            EasyPermissions.requestPermissions(
                    new PermissionRequest.Builder(this, RC_BLE_PERMISSIONS, perms)
                            .setRationale(rationale)
                            .setPositiveButtonText(getString(R.string.permission_dialog_positive_continue))
                            .setNegativeButtonText(getString(R.string.permission_dialog_negative))
                            .build());
        }
    }

    /**
     * 权限全部授予后的操作
     */
    private void onBluetoothPermissionsGranted() {
        Log.d(TAG, "蓝牙权限已授予");
        if (!mBleUtil.isOpenBle()) {
            mBleUtil.openBle(); // 打开蓝牙
        } else {
            startBleScanning();
        }
    }
    // ================ EasyPermissions 回调 ================
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == RC_BLE_PERMISSIONS) {
            Log.d(TAG, "用户授予了部分权限: " + perms);
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.w(TAG, "用户拒绝了权限: " + perms);
        mViewModel.setToast(getString(R.string.toast_permission_deny));

        // 检查是否永久拒绝
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this)
                    .setTitle(getString(R.string.permission_dialog_title_needPermission))
                    .setRationale(getString(R.string.permission_dialog_toSetting))
                    .build()
                    .show();
        }
    }
}