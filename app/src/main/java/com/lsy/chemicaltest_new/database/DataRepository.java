package com.lsy.chemicaltest_new.database;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;

import com.clj.fastble.data.BleDevice;
import com.lsy.chemicaltest_new.domain.ColoTestResult;
import com.lsy.chemicaltest_new.domain.ElecTestResult;
import com.lsy.chemicaltest_new.domain.History_multiple;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.domain.Temperature_Elec;
import com.lsy.chemicaltest_new.domain.ThermalTestResult;

import java.lang.ref.SoftReference;
import java.util.Objects;

// 单例数据仓库
public class DataRepository {
    private static final String TAG = "DataRepository";
    private static final int MAX_CACHE_SIZE = 10; // Bitmap缓存最大数量

    // 单例实例（volatile保证可见性）
    private static volatile DataRepository INSTANCE;

    // 专用锁对象（避免锁this带来的潜在风险）
    private final Object lock = new Object();

    // 数据域（volatile保证可见性）
    private volatile Temperature_Elec mTemperature_Elec;
    private volatile ColoTestResult mColoTestResult;
    private volatile ElecTestResult mElecTestResult;
    private volatile ThermalTestResult mThermalTestResult;
    private volatile StandardCurve mStandardCurve;
    private volatile History_multiple mHistory_multiple;
    private volatile BleDevice mCurrentConnectDevice;// 当前连接的蓝牙设备

    // Bitmap缓存（使用软引用防止内存泄漏）
    private final SparseArray<SoftReference<Bitmap>> cache = new SparseArray<>();
    private int idCounter = 0;
    // 私有构造方法
    private DataRepository() {
        // 初始化代码（如有需要）
    }
    // 双重校验锁单例
    public static DataRepository getInstance() {
        if (INSTANCE == null) {
            synchronized (DataRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DataRepository();
                }
            }
        }
        return INSTANCE;
    }
    //region Bitmap缓存管理
    public int cacheBitmap(Bitmap bitmap) {
        synchronized (lock) {
            // 清理无效引用
            cleanNullReferences();

            // 超过限制时移除最旧的缓存
            if (cache.size() >= MAX_CACHE_SIZE) {
                cache.removeAt(0);
            }

            // 生成ID（处理溢出）
            if (idCounter == Integer.MAX_VALUE) {
                idCounter = 0;
            }
            int id = idCounter++;

            cache.put(id, new SoftReference<>(bitmap));
            Log.d(TAG, "Cached bitmap with id: " + id);
            return id;
        }
    }
    public Bitmap getAndRemoveBitmap(int id) {
        synchronized (lock) {
            SoftReference<Bitmap> ref = cache.get(id);
            Bitmap bitmap = ref != null ? ref.get() : null;
            cache.remove(id);
            Log.d(TAG, "Retrieved bitmap with id: " + id);
            return bitmap;
        }
    }
    private void cleanNullReferences() {
        for (int i = cache.size() - 1; i >= 0; i--) {
            if (cache.valueAt(i).get() == null) {
                cache.removeAt(i);
            }
        }
    }
    //endregion
    //region 数据访问方法（线程安全+防御性拷贝）
    public Temperature_Elec getTemperature_Elec() {
        synchronized (lock) {
            Temperature_Elec result = mTemperature_Elec != null ?
                    new Temperature_Elec(mTemperature_Elec) : null;
            Log.d(TAG,"getTemperature_Elec:"+ result);
            return result;
        }
    }
    public void setTemperature_Elec(Temperature_Elec temperature_Elec) {
        synchronized (lock) {
            if (!Objects.equals(this.mTemperature_Elec, temperature_Elec)) {
                this.mTemperature_Elec = temperature_Elec != null ?
                        new Temperature_Elec(temperature_Elec) : null;
                Log.d(TAG,"setTemperature_Elec:"+mTemperature_Elec);
            }
        }
    }


    public ColoTestResult getColoTestResult() {
        synchronized (lock) {
            ColoTestResult result = mColoTestResult != null ?
                    new ColoTestResult(mColoTestResult) : null;
            Log.d(TAG,"getTemperature_Elec:"+ result);
            return result;
        }
    }

    public void setColoTestResult(ColoTestResult coloTestResult) {
        synchronized (lock) {
            if (!Objects.equals(this.mColoTestResult, coloTestResult)) {
                this.mColoTestResult = coloTestResult != null ?
                        new ColoTestResult(coloTestResult) : null;
                Log.d(TAG,"setColoTestResult:"+mColoTestResult);
            }
        }
    }

    public ElecTestResult getElecTestResult() {
        synchronized (lock) {
            ElecTestResult result = mElecTestResult != null ?
                    new ElecTestResult(mElecTestResult) : null;
            Log.d(TAG,"getElecTestResult:"+ result);
            return result;
        }
    }

    public void setElecTestResult(ElecTestResult elecTestResult) {
        synchronized (lock) {
            if (!Objects.equals(this.mElecTestResult, elecTestResult)) {
                this.mElecTestResult = elecTestResult != null ?
                        new ElecTestResult(elecTestResult) : null;
                Log.d(TAG,"setmElecTestResult:"+mElecTestResult);
            }
        }
    }

    public ThermalTestResult getThermalTestResult() {
        synchronized (lock) {
            ThermalTestResult result = mThermalTestResult != null ?
                    new ThermalTestResult(mThermalTestResult) : null;
            Log.d(TAG, "getThermalTestResult: "+result);
            return result;
        }
    }

    public void setThermalTestResult(ThermalTestResult thermalTestResult) {
        synchronized (lock) {
            if (!Objects.equals(this.mThermalTestResult, thermalTestResult)) {
                this.mThermalTestResult = thermalTestResult != null ?
                        new ThermalTestResult(thermalTestResult) : null;
                Log.d(TAG,"setThermalTestResult:"+mThermalTestResult);
            }
        }
    }

    public StandardCurve getStandardCurve() {
        synchronized (lock) {
            StandardCurve result = mStandardCurve != null ?
                    new StandardCurve(mStandardCurve) : null;
            Log.d(TAG, "getStandardCurve: "+result);
            return result;
        }
    }

    public void setStandardCurve(StandardCurve standardCurve) {
        synchronized (lock) {
            if (!Objects.equals(this.mStandardCurve, standardCurve)) {
                this.mStandardCurve = standardCurve != null ?
                        new StandardCurve(standardCurve) : null;
                Log.d(TAG,"setStandardCurve:"+mStandardCurve);
            }
        }
    }

    public History_multiple getHistory_multiple() {
        synchronized (lock) {
            History_multiple result = mHistory_multiple != null ?
                    new History_multiple(mHistory_multiple) : null;
            Log.d(TAG, "getHistory_multiple: "+result);
            return result;
        }
    }

    public void setHistory_multiple(History_multiple history_multiple) {
        synchronized (lock) {
            if (!Objects.equals(this.mHistory_multiple, history_multiple)) {
                this.mHistory_multiple = history_multiple != null ?
                        new History_multiple(history_multiple) : null;
                Log.d(TAG,"setStandardCurve:"+mHistory_multiple);
            }
        }
    }

    public BleDevice getBleDevice() {
        synchronized (lock) {
            BleDevice result = mCurrentConnectDevice != null ?
                    new BleDevice(mCurrentConnectDevice.getDevice(),
                            mCurrentConnectDevice.getRssi(),
                            mCurrentConnectDevice.getScanRecord(),
                            mCurrentConnectDevice.getTimestampNanos()) : null;
            Log.d(TAG, "getBleDevice: "+result);
            return result;
        }
    }

    public void setBleDevice(BleDevice bleDevice) {
        synchronized (lock) {
            if (!Objects.equals(this.mCurrentConnectDevice, bleDevice)) {
                this.mCurrentConnectDevice = bleDevice != null ?
                        new BleDevice(bleDevice.getDevice(),
                                bleDevice.getRssi(),
                                bleDevice.getScanRecord(),
                                bleDevice.getTimestampNanos()) : null;
                Log.d(TAG,"setBleDevice:"+mCurrentConnectDevice);
            }
        }
    }
    //region 资源清理
    public void clearAll() {
        synchronized (lock) {
            // 清除数据引用
            mTemperature_Elec = null;
            mColoTestResult = null;
            mElecTestResult = null;
            mThermalTestResult = null;
            mStandardCurve = null;
            mHistory_multiple = null;

            // 清除Bitmap缓存
            cache.clear();
            idCounter = 0;

            Log.d(TAG,"clearAll "+ " All data cleared");
        }
    }

}
