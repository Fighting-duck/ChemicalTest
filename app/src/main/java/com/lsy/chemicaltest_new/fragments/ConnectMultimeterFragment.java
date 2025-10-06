package com.lsy.chemicaltest_new.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.clj.fastble.data.BleDevice;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.adapters.DeviceAdapter;
import com.lsy.chemicaltest_new.database.DataRepository;
import com.lsy.chemicaltest_new.databinding.FragmentConnectMultimeterBinding;
import com.lsy.chemicaltest_new.interfaces.OnFragmentMultimeterListener;
import com.lsy.chemicaltest_new.models.ConnectMultimeterViewModel;
import com.lsy.chemicaltest_new.utils.BleUtil;
import com.lsy.chemicaltest_new.utils.LineChartUtil;
import com.lsy.chemicaltest_new.utils.PermissionManager;
import com.lsy.chemicaltest_new.utils.TimeUtil;

import java.util.List;

/**
 * 使用蓝牙连接万用表，并获取数据
 */
public class ConnectMultimeterFragment extends Fragment  {
    private static final String TAG = "ConnectMultimeterFragment";
    // 当前对话框显示模式
    public enum ShowModel {
        ELEC,    // 测量电信号
        TEMPERATURE,    // 测量温度
    }
    private OnFragmentMultimeterListener mListener;
    private FragmentConnectMultimeterBinding mBinding;
    private static ShowModel mShowModel;
    private Activity mActivity;
    private Context mContext;
    private ConnectMultimeterViewModel mViewModel;
    private BleUtil mBleUtil;
    private Handler mHandler = new Handler();
    private final static int RC_BLE_PERMISSIONS  = 1000;
    private DeviceAdapter mDeviceAdapter;
    private PermissionManager permissionManager;

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
        // 初始化蓝牙工具类
        if (getActivity() != null)
            mBleUtil = new BleUtil(getActivity(),mViewModel);
        else
            mViewModel.setToast(getString(R.string.toast_system_fail));
        // 权限管理器初始化和权限请求
        permissionManager = new PermissionManager(
                getActivity(),
                new PermissionManager.PermissionCallback() {
                    @Override
                    public void onPermissionGranted() {
                        onBluetoothPermissionsGranted();
                    }

                    @Override
                    public void onPermissionDenied() {
                        // 权限被拒绝

                    }
                },
                R.string.permission_dialog_title,
                R.string.permission_dialog_needBleAndLocation
        );
        // 检查蓝牙权限
        permissionManager.checkBluetoothPermissions();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //mBinding = FragmentConnectMultimeterBinding.inflate(getLayoutInflater());
        mBinding = FragmentConnectMultimeterBinding.inflate(inflater, container, false);
        mActivity = getActivity();
        // 初始化适配器DeviceAdapter
        if (mActivity != null){
            mDeviceAdapter = new DeviceAdapter();
            mBinding.rvDevices.setAdapter(mDeviceAdapter);
        }
        else {
            mViewModel.setToast(getString(R.string.toast_system_fail));
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
        //将x轴的数值转换为字符串形式的时间表示
        mBinding.lcChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                //将value转化为时间
                return TimeUtil.timeNumToStr(value);
            }
        });

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
                    mBinding.tvConnectionSituation.setText(R.string.measureValue_connection_true);
                    mBinding.btnDisConnect.setEnabled(true);
                }
                else {
                    mBinding.tvConnectionSituation.setText(R.string.measureValue_connection_false);
                    mBinding.btnDisConnect.setEnabled(false);
                }
            }
        });
        //挡位、里程
        mViewModel.getLiveData_BleDeviceInfo().observe(this, bleDeviceInfo -> {
            if (bleDeviceInfo!=null){
                mBinding.tvCurrentGear.setText(bleDeviceInfo.getGear());
                mBinding.tvMileage.setText(bleDeviceInfo.getMileage());
                mListener.onDeviceInfo(bleDeviceInfo);//向上传递设备信息
            }
            else {
                //复原
                mBinding.tvCurrentGear.setText(R.string.default_no);
                mBinding.tvMileage.setText(R.string.default_no);
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
                mBinding.pbStartScan.setVisibility(View.VISIBLE);
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
        mViewModel.clearDevices();//清空设备列表
        mBleUtil.startScan();
        // 10秒后停止扫描指示器
        mHandler.postDelayed(() -> {
            if (!isAdded()) return;//先检查Fragment是否已附着到Activity

            if (!mBleUtil.isConnected()) { // 检查是否成功连接
                mBleUtil.stopScan();  // 超时后停止扫描
            }
            if (mBinding!=null)
                mBinding.pbStartScan.setVisibility(View.INVISIBLE);
        }, 10_000); // 15秒超时
    }

    private void onCLick(View view) {
        int id = view.getId();
        if (id ==mBinding.btnTestConnect.getId()){
            // TODO : BLE测试连接
            permissionManager.checkBluetoothPermissions();
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        permissionManager.handleActivityResult(requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManager.handleRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}