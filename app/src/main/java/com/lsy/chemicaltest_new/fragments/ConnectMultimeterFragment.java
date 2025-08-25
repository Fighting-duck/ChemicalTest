package com.lsy.chemicaltest_new.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.clj.fastble.data.BleDevice;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.activitys.smpleTest.ElectricalTestActivity;
import com.lsy.chemicaltest_new.adapters.DeviceAdapter;
import com.lsy.chemicaltest_new.database.DataRepository;
import com.lsy.chemicaltest_new.databinding.FragmentConnectMultimeterBinding;
import com.lsy.chemicaltest_new.databinding.FragmentStandardCurveBinding;
import com.lsy.chemicaltest_new.domain.BleDeviceInfo;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.domain.TestValue;
import com.lsy.chemicaltest_new.models.ConnectMultimeterViewModel;
import com.lsy.chemicaltest_new.models.StandardCurveViewModel;
import com.lsy.chemicaltest_new.utils.BleUtil;
import com.lsy.chemicaltest_new.utils.BleUtil_new;
import com.lsy.chemicaltest_new.utils.LineChartUtil;
import com.lsy.chemicaltest_new.utils.TimeUtil;

import java.util.List;
import java.util.Objects;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

/**
 * 使用蓝牙连接万用表，并获取数据
 */
public class ConnectMultimeterFragment extends Fragment implements EasyPermissions.PermissionCallbacks  {
    private static final String TAG = "ConnectMultimeterFragment";
    // 当前对话框显示模式
    public enum ShowModel {
        ELEC,    // 测量电信号
        TEMPERATURE,    // 测量温度
    }
    // 定义接口
    public interface OnFragmentMultimeterListener {
        void onMeasureValue(TestValue testValue);//获取实时测量值
        void onDeviceInfo(BleDeviceInfo bleDeviceInfo);//获取当前设备信息
    }
    private OnFragmentMultimeterListener mListener;
    private FragmentConnectMultimeterBinding mBinding;
    private static ShowModel mShowModel;
    private Activity mActivity;
    private Context mContext;
    private ConnectMultimeterViewModel mViewModel;
    private BleUtil_new mBleUtil;
    private Handler mHandler = new Handler();
    private final static int RC_BLE_PERMISSIONS  = 1000;
    private DeviceAdapter mDeviceAdapter;

    // 设置监听器 监听曲线选择变化
    public void setOnMeasureValueListener(OnFragmentMultimeterListener listener) {
        this.mListener = listener;
    }

    public ConnectMultimeterFragment(){}

    public ConnectMultimeterFragment(ShowModel showModel) {
        // Required empty public constructor
        mShowModel = showModel;
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param showModel 显示模式
     * @return A new instance of fragment ConnectMultimeterFragment.
     */
    public static ConnectMultimeterFragment newInstance(ShowModel showModel) {
        ConnectMultimeterFragment fragment = new ConnectMultimeterFragment();
        Bundle args = new Bundle();
        // 存储枚举类型的参数
        args.putSerializable("CurrentShowModel", showModel);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mShowModel = (ShowModel) getArguments().getSerializable("CurrentShowModel");
        }
        mContext = getContext();
        mViewModel = new ViewModelProvider(this).get(ConnectMultimeterViewModel.class);
        mViewModel.setContext(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentConnectMultimeterBinding.inflate(getLayoutInflater());
        mActivity = getActivity();
        if (mActivity != null){
            mBleUtil = new BleUtil_new(mActivity,mViewModel);
            mDeviceAdapter = new DeviceAdapter();
            mBinding.rvDevices.setAdapter(mDeviceAdapter);
        }
        else {
            mViewModel.setToast("内部错误");
        }
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: 恢复数据");
        //恢复数据
        List<BleDevice> bleDeviceList = mViewModel.getBleDeviceList();
        BleDevice connectBleDevice = mViewModel.getConnectBleDevice();
        if (bleDeviceList != null){
            mDeviceAdapter.update(bleDeviceList);
        }
        if (connectBleDevice != null){
            mBleUtil.openNotify(connectBleDevice);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        initUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        BleDevice bleDevice = DataRepository.getInstance().getBleDevice();
        if (bleDevice != null){
            mDeviceAdapter.clear();
            mDeviceAdapter.add(bleDevice);
            mBleUtil.openNotify(bleDevice);
        }
    }

    private void initUI() {
        /*
        * 设置图表
        * */
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

        /*
        * 设置监听器
        * */
        mBinding.btnTestConnect.setOnClickListener(this::onCLick);
        mBinding.btnDisConnect.setOnClickListener(this::onCLick);
        mDeviceAdapter.setOnDeviceClickListener(new DeviceAdapter.OnDeviceClickListener() {
            @Override
            public void onDeviceClick(BleDevice device) {
                mBleUtil.connectBle(device);
            }
        });

        //设置观察者
        mViewModel.getLiveData_toast().observe(this, toast -> {
            if (toast != null){
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
                mViewModel.setToast(null);
            }
        });
        //万能表连接情况
        mViewModel.getLiveData_isConnectDevice().observe(this, state -> {
            if (state!=null){
                if (state) {
                    mBinding.tvConnectionSituation.setText(R.string.text_connection_true);
                    mBinding.btnDisConnect.setEnabled(true);
                }
                else {
                    mBinding.tvConnectionSituation.setText(R.string.text_connection_false);
                    mBinding.btnDisConnect.setEnabled(false);
                }
            }
        });
        //挡位、里程
        mViewModel.getLiveData_BleDeviceInfo().observe(this, bleDeviceInfo -> {
            if (bleDeviceInfo!=null){
                mBinding.tvCurrentGear.setText(bleDeviceInfo.getGear());
                mBinding.tvMileage.setText(bleDeviceInfo.getMileage());
                /*mBinding.btnStartTest.setEnabled(true);
                if (Objects.equals(bleDeviceInfo.getGear(), getString(R.string.multimeter_Celsius)))
                    mBinding.btnStartTest.setText(R.string.text_StartTest_temperature);
                else
                    mBinding.btnStartTest.setText(R.string.text_StartTest_elec);*/
                mListener.onDeviceInfo(bleDeviceInfo);//向上传递设备信息
            }
            else {
                //复原
                mBinding.tvCurrentGear.setText(R.string.default_no);
                mBinding.tvMileage.setText(R.string.default_no);
/*                mBinding.btnStartTest.setText(R.string.text_StartTest);
                mBinding.btnStartTest.setEnabled(false);*/
                mListener.onDeviceInfo(null);//向上传递设备信息
            }
        });
        //实时测量值
        mViewModel.getLiveData_RealTimeValue().observe(this, testValue -> {
            if (testValue!= null){
                if (testValue.getValue() == Float.MAX_VALUE){
                    mBinding.tvCurrentValue.setText("0L ");
                    return;
                }
                mBinding.tvCurrentValue.setText(testValue.toString());
                mListener.onMeasureValue(testValue);
            }
            else mBinding.tvCurrentValue.setText(getString(R.string.default_no));

        });
        //设备列表
        mViewModel.getLiveData_BleDeviceList().observe(this, bleDevices -> {
            if (bleDevices == null){
                mDeviceAdapter.clear();
            }
            else{
                Log.d(TAG, "onViewCreated: 刷新设备列表"+bleDevices.size());
                mDeviceAdapter.update(bleDevices);
            }

        });
        //连接蓝牙设备
        mViewModel.getLiveData_ConnectBleDevice().observe(this, bleDevice -> {
            if (bleDevice!= null){
                mBleUtil.openNotify(bleDevice);
                DataRepository.getInstance().setBleDevice(bleDevice);//保存蓝牙设备
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
    }
    // 开始蓝牙扫描
    private void startBleScanning() {
        Log.d(TAG, "startBleScanning: ");
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

    private void onCLick(View view) {
        int id = view.getId();
        if (id ==mBinding.btnTestConnect.getId()){
            // TODO : BLE测试连接
            checkBluetoothPermissions();
        }else if (id ==mBinding.btnDisConnect.getId()){
            // TODO : BLE断开连接
            mBleUtil.stopNotify();
            mBleUtil.onDestroy();
            mViewModel.setConnectDevice(null);
            mViewModel.setIsConnected(false);
            mViewModel.setGearAndMileage(null);
            mViewModel.setCurrentTestValue( null);
            mListener.onDeviceInfo(null);
        }

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

        if (EasyPermissions.hasPermissions(mContext, perms)) {
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

        if (EasyPermissions.hasPermissions(mContext, perms)) {
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