package com.lsy.chemicaltest_new.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.util.Log;

import com.flir.flironesdk.Device;

import java.lang.reflect.Field;

/**
 * CustomDevice 类继承自 Device 类，主要用于处理 USB 设备的连接、权限管理和设备发现等操作。
 * 它通过反射机制调用 Device 类的私有方法，以实现对 USB 设备的连接和管理。
 */
public class CustomDevice extends Device {
    // 用于日志记录的标签
    private static String TAG = "CustomDevice";

    // 静态广播接收器，用于接收 USB 设备的相关广播事件
    private static final BroadcastReceiver newUsbReceiver = new BroadcastReceiver() {
        /**
         * 用于接收 USB 设备的连接、权限授予和断开连接等广播事件
         *
         * @param context 上下文对象，提供应用程序的全局信息，如资源访问、组件管理等。
         * @param intent  包含广播事件相关数据的 Intent 对象。
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取广播事件的动作类型
            String action = intent.getAction();
            // 从 Intent 中获取 USB 设备对象
            UsbDevice usbDevice = intent.getParcelableExtra("device");

            // 处理 USB 设备连接事件
            if ("android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(action)) {
                Log.i("Device", "Received USB Device connect event " + usbDeviceString(usbDevice));
                try {
                    // 通过反射获取 Device 类的 Class 对象
                    Class<?> deviceClass = Device.class;
                    // 通过反射获取 Device 类的 connectToArbitraryDevice 方法，该方法接收一个 Context 参数
                    java.lang.reflect.Method connectMethod = deviceClass.getDeclaredMethod("connectToArbitraryDevice", Context.class);
                    // 设置方法可访问，即使它是私有的
                    connectMethod.setAccessible(true);
                    // 调用 connectToArbitraryDevice 方法，尝试连接设备
                    connectMethod.invoke(null, context);
                } catch (Exception e) {
                    // 捕获并记录反射调用过程中出现的异常
                    Log.e("Device", "Error invoking connectToArbitraryDevice: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            // 处理 USB 权限授予事件
            else if ("com.android.example.USB_PERMISSION".equals(action)) {
                // 从 Intent 中获取权限授予状态，默认值为 false
                boolean PERMISSION_SUCCESSFULLY_GRANTED = intent.getBooleanExtra("permission", false);
                if (PERMISSION_SUCCESSFULLY_GRANTED) {
                    Log.i("Device", "Permission granted for USB device " + usbDeviceString(usbDevice));
                    try {
                        // 通过反射获取 Device 类的 Class 对象
                        Class<?> deviceClass = Device.class;
                        // 通过反射获取 Device 类的 connectToArbitraryDevice 方法，该方法接收一个 Context 参数
                        java.lang.reflect.Method connectMethod = deviceClass.getDeclaredMethod("connectToArbitraryDevice", Context.class);
                        // 设置方法可访问，即使它是私有的
                        connectMethod.setAccessible(true);
                        // 调用 connectToArbitraryDevice 方法，尝试连接设备
                        connectMethod.invoke(null, context);
                    } catch (Exception e) {
                        // 捕获并记录反射调用过程中出现的异常
                        Log.e("Device", "Error invoking connectToArbitraryDevice: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    // 记录权限被拒绝的日志
                    Log.w("Device", "Permission denied for USB device " + usbDeviceString(usbDevice));
                }
            }
            // 处理 USB 设备断开连接事件
            else if ("android.hardware.usb.action.USB_DEVICE_DETACHED".equals(action)) {
                if (usbDevice != null) {
                    try {
                        // 通过反射获取 Device 类的 Class 对象
                        Class<?> deviceClass = Device.class;
                        // 通过反射获取 Device 类的 cachedDevice 字段
                        Field cachedDeviceField = deviceClass.getDeclaredField("cachedDevice");
                        // 设置字段可访问，即使它是私有的
                        cachedDeviceField.setAccessible(true);
                        // 获取当前缓存的设备对象
                        Device cachedDevice = (Device) cachedDeviceField.get(null);
                        if (cachedDevice != null) {
                            // 通过反射获取 Device 类的 usbDevice 字段
                            Field usbDeviceField = Device.class.getDeclaredField("usbDevice");
                            // 设置字段可访问，即使它是私有的
                            usbDeviceField.setAccessible(true);
                            // 获取当前缓存设备的 USB 设备对象
                            UsbDevice cachedUsbDevice = (UsbDevice) usbDeviceField.get(cachedDevice);
                            if (usbDevice.getProductId() == cachedUsbDevice.getProductId() &&
                                    usbDevice.getVendorId() == cachedUsbDevice.getVendorId()) {
                                Log.i("Device", "Received USB Device disconnect event for currently connected FLIR ONE device, shutting it down");
                                // 关闭当前连接的设备
                                cachedDevice.close();
                                try {
                                    // 通过反射获取 Device 类的 Class 对象
                                    Class<?> deviceClass1 = Device.class;
                                    // 通过反射获取 Device 类的 connectToArbitraryDevice 方法，该方法接收一个 Context 参数
                                    java.lang.reflect.Method connectMethod1 = deviceClass1.getDeclaredMethod("connectToArbitraryDevice", Context.class);
                                    // 设置方法可访问，即使它是私有的
                                    connectMethod1.setAccessible(true);
                                    // 调用 connectToArbitraryDevice 方法，尝试连接其他设备
                                    connectMethod1.invoke(null, context);
                                } catch (Exception e) {
                                    // 捕获并记录反射调用过程中出现的异常
                                    Log.e("Device", "Error invoking connectToArbitraryDevice: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            } else {
                                // 记录接收到不相关的 USB 设备断开连接事件的日志
                                Log.i("Device", "Received irrelevant USB device disconnect event. Event device = " + usbDeviceString(usbDevice) + ", " +
                                        "Cached device = " + usbDeviceString(cachedUsbDevice));
                            }
                        }
                    } catch (Exception e) {
                        // 捕获并记录反射获取字段过程中出现的异常
                        Log.e("Device", "Error accessing cachedDevice: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    /**
     * 该方法用于安全地启动设备发现过程。
     * 根据 Android 版本的不同，会注册不同的广播过滤器，并通过反射调用 Device 类的 startDiscovery 方法来开始设备发现。
     *
     * @param context 上下文对象，提供应用程序的全局信息，如资源访问、组件管理等。
     * @param delegate 委托对象，用于处理设备发现相关的回调。
     */
    public static void safeStartDiscovery(Context context, Delegate delegate) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // 创建用于接收 USB 设备断开连接事件的 IntentFilter
                IntentFilter filter1 = new IntentFilter("android.hardware.usb.action.USB_DEVICE_DETACHED");
                // 创建用于接收 USB 权限授予事件的 IntentFilter
                IntentFilter filter2 = new IntentFilter("com.android.example.USB_PERMISSION");
                // 创建用于接收 USB 设备连接事件的 IntentFilter
                IntentFilter filter3 = new IntentFilter("android.hardware.usb.action.USB_DEVICE_ATTACHED");

                // 注册广播接收器，设置为不导出，以增强安全性
                context.registerReceiver(newUsbReceiver, filter1, Context.RECEIVER_NOT_EXPORTED);
                context.registerReceiver(newUsbReceiver, filter2, Context.RECEIVER_NOT_EXPORTED);
                context.registerReceiver(newUsbReceiver, filter3, Context.RECEIVER_NOT_EXPORTED);

                // 通过反射获取 Device 类的 Class 对象
                Class<?> deviceClass = Device.class;
                // 通过反射获取 Device 类的 startDiscovery 方法，该方法接收 Context 和 Delegate 两个参数
                java.lang.reflect.Method startDiscoveryMethod = deviceClass.getDeclaredMethod("startDiscovery", Context.class, Delegate.class);
                // 设置方法可访问，即使它是私有的
                startDiscoveryMethod.setAccessible(true);
                // 调用 startDiscovery 方法，启动设备发现
                startDiscoveryMethod.invoke(null, context, delegate);
            } else {
                // 通过反射获取 Device 类的 Class 对象
                Class<?> deviceClass = Device.class;
                // 通过反射获取 Device 类的 startDiscovery 方法，该方法接收 Context 和 Delegate 两个参数
                java.lang.reflect.Method startDiscoveryMethod = deviceClass.getDeclaredMethod("startDiscovery", Context.class, Delegate.class);
                // 设置方法可访问，即使它是私有的
                startDiscoveryMethod.setAccessible(true);
                // 调用 startDiscovery 方法，启动设备发现
                startDiscoveryMethod.invoke(null, context, delegate);
            }
        } catch (Exception e) {
            // 捕获并记录启动设备发现过程中出现的异常
            Log.e(TAG, "Error in safeStartDiscovery: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 由于 Device 类的构造函数有异常抛出，这里简单实现一个空的构造函数
    protected CustomDevice() throws Exception {
        super();
    }

    /**
     * 将 UsbDevice 对象转换为字符串表示，方便日志记录
     *
     * @param usbDevice UsbDevice 对象
     * @return 表示 UsbDevice 的字符串
     */
    private static String usbDeviceString(UsbDevice usbDevice) {
        return usbDevice == null? "(null)" : "(ProductID = 0x" + Integer.toHexString(usbDevice.getProductId()) + ", " +
                "VendorID = 0x" + Integer.toHexString(usbDevice.getVendorId()) + ")";
    }
}
