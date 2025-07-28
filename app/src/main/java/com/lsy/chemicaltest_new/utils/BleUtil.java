package com.lsy.chemicaltest_new.utils;

import static com.blankj.utilcode.util.StringUtils.getString;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleRssiCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.domain.DMM_INFO;
import com.lsy.chemicaltest_new.domain.DMM_ReturnResult;
import com.lsy.chemicaltest_new.models.ElecViewModel;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BleUtil {
    //启用通知特征符
    private static final String CCCD_UUID = "00002902-0000-1000-8000-00805f9b34fb";
    //设备名过滤列表
    private static final String[] DEVICE_NAME_LIST = {"UT60BT"};
    private static final String TAG = "BleUtil" ;

    private final Integer readingNum = 14;//测量次数
    private final long readingTimeInterval = 4000/40;//测量时间间隔ms 电流

    private final Activity mActivity;
    private Context mContext;
    private BleDevice mBleDevice = null;

    private ElecViewModel mViewModel;
    //控制测量参数
    private Boolean mIsRecode = false;//正在记录数据。。。，记录中为ture，测量结束后恢复为false
    private Boolean mIsFirstTest = true;//仅第一次测量时为true,第一次测量结束后恢复为false
    private Integer mTestControlled = 0;//控制测试次数
    private Float mOldDegree = null;
    private Boolean lock = false;//锁
    private long mCurrentTime;

    public BleUtil(Activity activity,ElecViewModel viewModel) {
        mActivity = activity;
        mContext = activity.getApplicationContext();
        mViewModel = viewModel;
        // FastBle初始化及配置
        BleManager.getInstance().init(mActivity.getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)//设置连接重试次数和间隔
                .setOperateTimeout(5000);//设置操作超时
        setScanRule();
    }

    //判断当前Android设备是否支持BLE
    public boolean isSupportBle() {
        return BleManager.getInstance().isSupportBle();
    }
    //判断是否开启蓝牙
    public boolean isOpenBle() {
        return BleManager.getInstance().isBlueEnable();
    }
    //是否连接蓝牙
    public boolean isConnected() {
        if (mBleDevice!=null)
            return BleManager.getInstance().isConnected(mBleDevice);
        return false;
    }
    //摧毁蓝牙连接（在不需要蓝牙功能时调用）
    public void onDestroy() {
        mIsRecode = false;
        // 关闭蓝牙连接
        if (mBleDevice != null) {
            BleManager.getInstance().disconnect(mBleDevice);
        }
        // 释放资源
        BleManager.getInstance().destroy();
    }

    //设置扫描规则
    public void setScanRule() {
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                .setDeviceName(true, DEVICE_NAME_LIST)         // 只扫描指定广播名的设备，可选
                .setAutoConnect(false)
                .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    //打开蓝牙 同步打开
    @SuppressLint("MissingPermission")
    public void openBle() {
        //开启蓝牙
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.startActivity(intent);
    }
    //开始搜索蓝牙
    @SuppressLint("MissingPermission")
    public void startScan() {
        //先断开当前连接
        if (mBleDevice!= null) {
            BleManager.getInstance().disconnect(mBleDevice);
        }
        //开始搜索
        //默认扫描10s
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            //回到主线程，参数表示本次扫描动作是否开启成功
            public void onScanStarted(boolean success) {
                if (success) {
                    Log.i(TAG, "开始扫描...");
                    mViewModel.clearDevices();
                } else {
                    Log.i(TAG, "蓝牙未打开！！！");
                }
            }

            @Override
            //扫描过程中所有被扫描到的设备回调 （同一设备出现多次）
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
                //对扫描到的设备进行处理
            }

            @Override
            //扫描过程中的所有过滤后的设备回调（同一设备出现一次）
            public void onScanning(BleDevice bleDevice) {
                //对扫描到的设备进行处理
                Log.d(TAG,"发现设备："+bleDevice.getName());
                mViewModel.addDevice(bleDevice);
            }

            @Override
            //本次扫描时段内所有被扫描且过滤后的设备集合  相当于onScanning设备之和
            public void onScanFinished(List<BleDevice> devices) {
                //对扫描过滤后的设备们的处理
                Log.i(TAG, "结束扫描...");
                Log.d(TAG,"发现设备数量："+devices.size());
                if (devices.isEmpty()) {
                    mViewModel.clearDevices();
                }
            }
        });
    }
    //停止搜索蓝牙
    public void stopScan() {
        BleManager.getInstance().cancelScan();
    }
    //连接蓝牙
    public void connectBle(BleDevice bleDevice) {
        //开始连接
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            //开始进行连接
            public void onStartConnect() {
                Log.d(TAG, "正在连接");
            }
            @Override
            //连接不成功
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                Log.d(TAG, "连接不成功");
            }
            @SuppressLint("MissingPermission")
            @Override
            //连接成功并发现服务
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Log.d(TAG, "连接成功");
                mViewModel.setIsConnected(true);
                mBleDevice = bleDevice;

               /* //使万用表发送显示值给手机
                byte[] data = DMM_INFO.createCommand(DMM_INFO.COMMAND_SEND);
                writeValue(bleDevice,
                        DMM_INFO.UUID_SERVICE_WRITE,DMM_INFO.UUID_CHARACTERISTIC_WRITE,data
                );

                // 找到目标服务和特征
                BluetoothGattService service = gatt.getService(UUID.fromString(DMM_INFO.UUID_SERVICE_NOTIFY));
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(
                            UUID.fromString(DMM_INFO.UUID_CHARACTERISTIC_NOTIFY)
                    );
                    if (characteristic != null) {
                        *//*setCharacteristicNotification(read,gatt,true);*//*
                        // 启用通知
                        enableNotification(gatt, characteristic);
                    } else {
                        Log.e(TAG, "未找到通知特征");
                    }
                }*/

                // TODO 开始通知
                //连接成功，间隔0.5s进行通知
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mViewModel.setConnectDevice(bleDevice);
                    }
                }, 500);
            }
            @Override
            //连接断开，特指连接后再断开的情况
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                mViewModel.setIsConnected(false);

                if (!isActiveDisConnected) return;
                Log.d(TAG, "连接断开");
            }
        });
    }


    /***
     * 启用Notify模式
     * @param gatt
     * @param characteristic
     */
    @SuppressLint("MissingPermission")
    public void enableNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (gatt == null || characteristic == null) {
            Log.e(TAG, "BluetoothGatt 或 Characteristic 未初始化");
            return;
        }

        // 启用通知
        boolean success = gatt.setCharacteristicNotification(characteristic, true);
        if (!success) {
            Log.e(TAG, "无法启用通知");
            return;
        }

        // 获取描述符并设置为 ENABLE_NOTIFICATION_VALUE
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                UUID.fromString(DMM_INFO.UUID_OPEN_NOTIFY) // 通知描述符的标准 UUID
        );
        if (descriptor == null) {
            Log.e(TAG, "未找到通知描述符");
            return;
        }

        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        boolean writeSuccess = gatt.writeDescriptor(descriptor);
        if (!writeSuccess) {
            Log.e(TAG, "无法写入描述符，通知可能无法启用");
        } else {
            Log.d(TAG, "通知已启用");
        }
    }
    public void openNotify(BleDevice bleDevice) {
        //打开通知
        this.openNotify(bleDevice,DMM_INFO.UUID_SERVICE_NOTIFY,DMM_INFO.UUID_CHARACTERISTIC_NOTIFY);
    }
    public Boolean stopNotify(){
        if (mBleDevice!=null && isConnected())
            return BleManager.getInstance().stopNotify(mBleDevice,DMM_INFO.UUID_SERVICE_NOTIFY,DMM_INFO.UUID_CHARACTERISTIC_NOTIFY);
        else return false;
    }

    /***
     * 给指定蓝牙的某个端口写数据
     * @param bleDevice 蓝牙设备
     * @param uuid_service
     * @param uuid_characteristic_write
     * @param data 发送的数据
     */
    public void writeValue(BleDevice bleDevice, String uuid_service, String uuid_characteristic_write,byte[] data){
        BleManager.getInstance().write(
                bleDevice,
                uuid_service,
                uuid_characteristic_write,
                data,
                new BleWriteCallback() {
                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        // 发送数据到设备成功（分包发送的情况下，可以通过方法中返回的参数可以查看发送进度）
                        Log.d(TAG,"数据发送成功");
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        // 发送数据到设备失败
                        Log.d(TAG,"数据发送失败");
                    }
                });
    }
    public void startTest(){
        mIsFirstTest = true;//恢复初始状态
        mIsRecode = true;//开始记录
        mTestControlled = 0;//恢复初始测量次数
    }

    //读取RSSI
    public void readRSSI(BleDevice bleDevice) {
        if (bleDevice==null) return;
        BleManager.getInstance().readRssi(bleDevice, new BleRssiCallback() {
            @Override
            public void onRssiFailure(BleException exception) {
                // 读取设备的信号强度失败
                // 请检查硬件是否支持读取RSSI
                Log.e(TAG, "读取RSSI失败！！");
            }
            @Override
            public void onRssiSuccess(int rssi) {
                // 读取设备的信号强度成功
                Log.i(TAG, "RSSI值： " + rssi);
            }
        });
    }
    private Handler mHandler = new Handler(Looper.getMainLooper());

    //打开notify
    public void openNotify(BleDevice bleDevice, String uuid_service, String uuid_characteristic_notify) {
        BleManager.getInstance().notify(
                bleDevice,
                uuid_service,
                uuid_characteristic_notify,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        // 打开通知操作成功
                        Log.d(TAG,"打开通知操作成功");
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        // 打开通知操作失败
                        Log.d(TAG,"打开通知操作失败");
                    }
                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        // 打开通知后，设备发过来的数据将在这里出现
                        // TODO: 处理返回的数据
                        Log.d(TAG,"二进制数据："+ Arrays.toString(data));
                        DMM_ReturnResult result = DMM_INFO.analysisResult(data);
                        if (result == null){
                            Log.e(TAG,"挡位不对！");
                            return;
                        }
                        Log.d(TAG,"分析后数据："+result.toString());
                        mViewModel.setGearAndMileage(result.getBleDeviceInfo());
                        mViewModel.setCurrentTestValue(result.getTestValue());
                        if (result.getTestValue().getValue() == Float.MAX_VALUE) return;
                        //获取当前时间值
                        long currentTime = System.currentTimeMillis();//ms
                        float xValue = TimeUtil.timeStrToNum(currentTime);//提取时间
                        float yValue = result.getTestValue().getValue();//电流值或温度值
                        mViewModel.addEntryInLast(xValue, yValue);//图表显示
                        if (mIsRecode){
                            processResult(result,mIsFirstTest,currentTime,xValue);
                            mIsFirstTest = false;
                        }
                    }
                });
    }

    /***
     * 处理万用表返回结果,依据结果的档位，判断是进行温度检测还是电流检测
     * @param result 万用表返回数据
     */
    public void processResult(DMM_ReturnResult result,Boolean isFirstTest,long currentTime,float xValue) {
        if (!Objects.equals(result.getBleDeviceInfo().getGear(), getString(R.string.multimeter_Celsius))) {
            //电信号检测
            if (lock){
                lock = false;//上锁
                mHandler.postDelayed(() -> checkCurrent(result,xValue), readingTimeInterval);
            }
            if (isFirstTest) {
                Log.d(TAG, "=======================电流检测开始============================");
                mViewModel.updateBleDeviceInfo_Elec(result.getBleDeviceInfo());
                lock = true;//解锁
            }

        } else {
            Log.d(TAG, "温度检测，当前温度：" + result.getTestValue().toString());
            //温度检测
            if (isFirstTest) {
                Log.d(TAG, "=======================温度检测开始============================");
                mOldDegree = result.getTestValue().getValue();//电流值或温度值
                mCurrentTime = currentTime;
                mViewModel.updateBleDeviceInfo_Degree(result.getBleDeviceInfo());
                lock = true;
            }
            if (currentTime - mCurrentTime > 4000){
                checkTemperature(result,currentTime);
            }
        }
    }

    /***
     * 该方法在 4 秒后被调用，用于检查温度是否有变化。若有变化，就再次设置定时器；若没有变化，就结束温度检测。
     * @param result  万用表返回数据
     */
    private void checkTemperature(DMM_ReturnResult result,long currentTime) {
        float yValue = result.getTestValue().getValue();
        if (yValue != mOldDegree) {
            Log.d(TAG, "温度改变，当前oldDegree温度：" +mOldDegree+" -> "+ yValue);
            mViewModel.setToast("温度改变!");
            mCurrentTime = currentTime;
            mOldDegree = yValue;
        } else {
            mViewModel.setDegree(yValue);
            mIsRecode = false;
            Log.d(TAG, "温度检测结束,温度值为：" + yValue + "℃");
        }
        lock = true;
    }

    /***
     * 该方法在 readingTimeInterval 时间后被调用，用于检查电流测量是否达到次数上限。若未达到，就记录当前值并设置下一次定时器；若达到，就结束电流检测。
     * @param result 万用表返回数据
     */
    private void checkCurrent(DMM_ReturnResult result,float xValue) {
        Log.d(TAG, "电流检测，当前第" + mTestControlled + "次电流测量，当前电流：" + result.getTestValue().toString());
        mTestControlled++;
        if (mTestControlled > readingNum) {
            mIsRecode = false;
            Log.d(TAG, "电流检测结束,最大电流值为：" + result.getTestValue());
        } else {
            // 获取当前时间（确保每次都是最新时间）
            result.getTestValue().setTestTime(TimeUtil.timeNumToStr(xValue));
            mViewModel.addValueToList(result.getTestValue());
        }
        lock = true;//解锁
    }
}
