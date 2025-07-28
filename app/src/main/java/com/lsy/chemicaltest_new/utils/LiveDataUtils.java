package com.lsy.chemicaltest_new.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * LiveData线程安全工具类
 */
public class LiveDataUtils {

    /* 通用安全更新方法（支持null值）
        常规的 LiveData 值更新需求
        不确定当前线程环境时（比如回调可能来自任意线程）
        简单的值更新（不需要复杂计算或错误处理）
     */
    public static <T> void safeUpdate(MutableLiveData<T> liveData, T value) {
        if (liveData == null) return;

        if (isMainThread()) {
            liveData.setValue(value);
        } else {
            liveData.postValue(value);
        }
    }

    /**
     * 批处理更新（避免多次触发Observer）
     * 使用场景：
     * 需要先执行耗时计算再更新 LiveData 的场景
     * 避免多次触发 Observer（比如连续更新多个字段时）
     * 合并多个操作为一个原子更新
     *
     * @param liveData
     * @param supplier
     * @param <T>
     */
    public static <T> void batchUpdate(MutableLiveData<T> liveData, Supplier<T> supplier) {
        if (isMainThread()) {
            liveData.setValue(supplier.get());
        } else {
            new Handler(Looper.getMainLooper()).post(() ->
                    liveData.setValue(supplier.get())
            );
        }
    }

    private static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    /**
     * 带错误处理的更新
     * 使用场景：
     * 不确定更新过程是否可能抛出异常
     * 需要记录或处理更新失败的情况
     * 关键数据更新（比如支付状态）
      */
    public static <T> void safeUpdateWithCatch(
            MutableLiveData<T> liveData,
            T value,
            Consumer<Exception> errorHandler
    ) {
        try {
            safeUpdate(liveData, value);
        } catch (Exception e) {
            errorHandler.accept(e);
        }
    }

    /**
     * 带超时保护的更新
     * 使用场景：
     * 主线程可能阻塞（如ANR风险）的环境
     * 更新操作必须在限定时间内完成
     * 防止因主线程繁忙导致更新丢失
      */
    public static <T> void updateWithTimeout(
            MutableLiveData<T> liveData,
            T value,
            long timeoutMillis
    ) {
        final CountDownLatch latch = new CountDownLatch(1);
        Runnable updateTask = () -> {
            try {
                liveData.setValue(value);
            } finally {
                latch.countDown();
            }
        };

        if (isMainThread()) {
            updateTask.run();
        } else {
            new Handler(Looper.getMainLooper()).post(updateTask);
        }

        try {
            latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}

