package com.lsy.chemicaltest_new.activitys.smpleTest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import com.flir.flironesdk.Device;
import com.flir.flironesdk.Frame;
import com.flir.flironesdk.FrameProcessor;
import com.flir.flironesdk.RenderedImage;
import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.activitys.BaseActivity;
import com.lsy.chemicaltest_new.database.DataRepository;
import com.lsy.chemicaltest_new.databinding.ActivityTakeThermalPhotoBinding;
import com.lsy.chemicaltest_new.models.ThermalViewModel;
import com.lsy.chemicaltest_new.utils.CustomDevice;
import com.lsy.chemicaltest_new.utils.FrameSaver;
import com.lsy.chemicaltest_new.utils.PhotoUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class TakeThermalPhotoActivity extends BaseActivity implements Device.Delegate, FrameProcessor.Delegate, Device.StreamDelegate, Device.PowerUpdateDelegate  {
    private ActivityTakeThermalPhotoBinding mBinding;
    private Context mContext;

    private static final String TAG = TakeThermalPhotoActivity.class.getSimpleName();
    private volatile Device flirOneDevice;
    private ImageView thermalImageView;//热图像视图
    private Bitmap thermalBitmap = null;//热位图
    private int deviceRotation= 0;//设备方向
    private FrameProcessor frameProcessor;//帧处理器
    private ScaleGestureDetector mScaleDetector;//SCALE 手势检测器
    private List<RenderedImage.ImageType> imageTypes = new ArrayList<RenderedImage.ImageType>();//图片类型

    private volatile boolean imageCaptureRequested = false;//是否点击拍照
    private volatile Socket streamSocket = null;
    private OrientationEventListener orientationEventListener;
    private String lastSavedPath;//保存地址
    private Device.TuningState currentTuningState = Device.TuningState.Unknown;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityTakeThermalPhotoBinding.inflate(getLayoutInflater());
        mContext = getApplicationContext();
        setContentView(mBinding.getRoot());

        //默认的图像类型defaultImageType
        imageTypes.add(RenderedImage.ImageType.BlendedMSXRGBA8888Image);//defaultImageType
        imageTypes.add(RenderedImage.ImageType.VisualJPEGImage);//otherImageType
        //创建帧处理器
        frameProcessor = new FrameProcessor(this, this, EnumSet.of(imageTypes.get(0)));
        initUI();

    }
    private void initUI(){
        //监听设备方向的变化
        orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                deviceRotation = orientation;
            }
        };
        //监听缩放手势
        mScaleDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            //缩放手势结束时，会调用这个方法
            public void onScaleEnd(ScaleGestureDetector detector) {
            }
            @Override
            //缩放手势开始时，会调用这个方法
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }
            @Override
            //缩放手势进行中时，会调用这个方法
            public boolean onScale(ScaleGestureDetector detector) {
                Log.d("缩放", "缩放手势进行中时: " + detector.getScaleFactor());
                frameProcessor.setMSXDistance(detector.getScaleFactor());
                return false;
            }
        });
        //设置了一个触摸监听器
        findViewById(R.id.fullscreen_content).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //将触摸事件传递给mScaleDetector对象进行处理
                mScaleDetector.onTouchEvent(event);
                return true;
            }
        });
        mBinding.btnTakePhoto.setOnClickListener(this::onClick);
        mBinding.btnAdjust.setOnClickListener(this::onClick);
        mBinding.btnTransform.setOnClickListener(this::onClick);
        mBinding.ivBack.setOnClickListener(this::onClick);
    }

    private void onClick(View view) {
        int id = view.getId();
        if (id == mBinding.ivBack.getId()){
            onBackPressed();
        }
        else if(id == mBinding.btnTakePhoto.getId()){
            this.imageCaptureRequested = true;
        }
        else if(id == mBinding.btnAdjust.getId()){
            if (flirOneDevice != null){
                flirOneDevice.performTuning();
            }
        }
        else if(id == mBinding.btnTransform.getId()){
            if (frameProcessor ==null){
                return;
            }
            if (frameProcessor.getImageTypes().equals(EnumSet.of(imageTypes.get(1))) ){
                frameProcessor.setImageTypes(EnumSet.of(imageTypes.get(0)));
            }
            else
                frameProcessor.setImageTypes(EnumSet.of(imageTypes.get(1)));
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        thermalImageView = mBinding.imageView;
        /*try {
            //开始发现USB设备，并为mUsbReceiver注册三个广播接收器，用于监听USB设备断开、USB权限请求和USB设备连接事件
            //Device.startDiscovery(this, this);
            Device.startDiscovery();
        }catch(IllegalStateException e){
            // it's okay if we've already started discovery
        }*/
        try {
            CustomDevice.safeStartDiscovery(this, this);
        } catch (Exception e) {
            Log.e(TAG, "Somehow we've started discovery twice");
            e.printStackTrace();
        }
    }
    @Override
    public void onRestart(){
        super.onRestart();
       /* try {
            //开始发现USB设备，并为mUsbReceiver注册三个广播接收器，用于监听USB设备断开、USB权限请求和USB设备连接事件
            Device.startDiscovery(this, this);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Somehow we've started discovery twice");
            e.printStackTrace();
        }*/
        try {
            CustomDevice.safeStartDiscovery(this, this);
        } catch (Exception e) {
            Log.e(TAG, "Somehow we've started discovery twice");
            e.printStackTrace();
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        // 注销我们的 USB 接收器, 否则将从其他应用程序窃取事件
        Log.e(TAG, "onStop, stopping discovery!");
        Device.stopDiscovery();
    }
    @Override
    //处理设备调谐状态的变化
    public void onTuningStateChanged(Device.TuningState tuningState) {
        Log.i(TAG, "Tuning state changed changed!");

        currentTuningState = tuningState;
        //进行中状态
        if (tuningState == Device.TuningState.InProgress){
            runOnUiThread(new Thread(){
                @Override
                public void run() {
                    super.run();
                    //对热成像图片设置一个过滤器，使其变暗
                    thermalImageView.setColorFilter(Color.DKGRAY, PorterDuff.Mode.DARKEN);
                    //加载图案、进行中可见
                    mBinding.pbTuning.setVisibility(View.VISIBLE);
                    mBinding.tvTuning.setVisibility(View.VISIBLE);
                }
            });
        }
        else {
            runOnUiThread(new Thread() {
                @Override
                public void run() {
                    super.run();
                    //移除热成像图片过滤器
                    thermalImageView.clearColorFilter();
                    //加载图案、进行中不可见
                    mBinding.pbTuning.setVisibility(View.GONE);
                    mBinding.tvTuning.setVisibility(View.GONE);
                }
            });
        }
    }
    @Override
    //处理设备自动调谐状态的变化
    public void onAutomaticTuningChanged(boolean b) {

    }
    @Override
    //USB设备连接
    public void onDeviceConnected(Device device) {
        Log.i(TAG, "设备已连接！！");

        flirOneDevice = device;
        flirOneDevice.setPowerUpdateDelegate(this);
        flirOneDevice.startFrameStream(this);
        orientationEventListener.enable();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBinding.tvDisconnect.setVisibility(View.GONE);
            }
        });
    }
    @Override
    //设备断开连接
    public void onDeviceDisconnected(Device device) {
        Log.i(TAG, "Device disconnected!");
        runOnUiThread(new Runnable() {
            @Override
            //将一切复原
            public void run() {
                //将thermalImageView的位图设置为1x1像素的透明位图
                thermalImageView.setImageBitmap(Bitmap.createBitmap(1,1, Bitmap.Config.ALPHA_8));
                //移除热成像过滤器
                thermalImageView.clearColorFilter();
                mBinding.pbTuning.setVisibility(View.GONE);
                mBinding.tvTuning.setVisibility(View.GONE);
                mBinding.tvDisconnect.setVisibility(View.VISIBLE);
            }
        });
        flirOneDevice = null;
        orientationEventListener.disable();
    }
    @Override
    //收到电池充电状态时
    public void onBatteryChargingStateReceived(Device.BatteryChargingState batteryChargingState) {

    }
    @Override
    //接收的电池电量
    public void onBatteryPercentageReceived(byte b) {

    }
    @Override
    //处理接收到的帧
    public void onFrameReceived(Frame frame) {
        Log.v(TAG, "Frame received!");
        //若状态不是“进行中”，则处理接收到的帧数据
        if (currentTuningState != Device.TuningState.InProgress){
            frameProcessor.processFrame(frame);
        }
    }
    //更新热图像视图
    private void updateThermalImageView(final Bitmap frame){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                thermalImageView.setImageBitmap(frame);
            }
        });
    }
    @Override
    //每次生成渲染的帧时都会调用
    public void onFrameProcessed(RenderedImage renderedImage) {
        Log.v(TAG, "Frame processed!");
        long startTime = System.nanoTime();
        //若“渲染的帧”类型为“可视 JPEG 图像”
        if (renderedImage.imageType() == RenderedImage.ImageType.VisualJPEGImage){
            //从渲染后的帧的像素数据中解码出位图
            final Bitmap visBitmap = BitmapFactory.decodeByteArray(renderedImage.pixelData(),0, renderedImage.pixelData().length);

            // 将视图旋转90度，并更新热成像图像视图
            android.graphics.Matrix mtx = new android.graphics.Matrix();
            mtx.postRotate(90);
            final Bitmap rotatedVisBitmap = Bitmap.createBitmap(visBitmap, 0, 0, visBitmap.getWidth(), visBitmap.getHeight(), mtx, true);
            updateThermalImageView(rotatedVisBitmap);
        }
        else {
            //若热成像图像视图为空，或者渲染后的帧的宽度和高度是否与thermalBitmap的宽度和高度不同
            if (thermalBitmap == null || (renderedImage.width() != thermalBitmap.getWidth() || renderedImage.height() != thermalBitmap.getHeight())) {
                Log.d("THERMALBMP", "Creating thermalBitmap with dimensions: " + renderedImage.width() + "x" + renderedImage.height() );
                //则创建一个新的thermalBitmap
                thermalBitmap = Bitmap.createBitmap(renderedImage.width(), renderedImage.height(), Bitmap.Config.ARGB_8888);

            }
            //若渲染后的帧类型为“热辐射开尔文图像”类型
            if (renderedImage.imageType() == RenderedImage.ImageType.ThermalRadiometricKelvinImage){
                /**
                 * 下面是一个显示 9 个温度波段颜色的简单示例:
                 * 低于 0°C is black
                 * 0-10°C is dark blue
                 * 10-20°C is light blue
                 * 20-36°C is green
                 * 36-40°C is dark red (human body)
                 * 40-50°C is bright red
                 * 50-60°C is orange
                 * 60-100°C is yellow
                 * 大于 100°C is white
                 */
                short[] shortPixels = new short[renderedImage.pixelData().length / 2];
                byte[] argbPixels = new byte[renderedImage.width() * renderedImage.height() * 4];
                // Thermal data is little endian.
                ByteBuffer.wrap(renderedImage.pixelData()).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortPixels);
                final byte aPixValue = (byte)255;
                for (int p = 0; p < shortPixels.length; p++) {
                    int destP = p * 4;
                    int tempInC = (shortPixels[p]-27315)/100;
                    byte rPixValue;
                    byte gPixValue;
                    byte bPixValue;
                    if (tempInC < 0){
                        rPixValue = gPixValue = bPixValue = 0;
                    }else if (tempInC < 10){
                        rPixValue = gPixValue = 0;
                        bPixValue = 127;
                    }else if (tempInC < 20){
                        rPixValue = gPixValue = 0;
                        bPixValue = (byte)255;
                    }else if (tempInC < 36){
                        rPixValue = bPixValue = 0;
                        gPixValue = (byte)160;
                    }else if (tempInC < 40){
                        bPixValue = gPixValue = 0;
                        rPixValue = 127;
                    }else if (tempInC < 50){
                        bPixValue = gPixValue = 0;
                        rPixValue = (byte)255;
                    }else if (tempInC < 60){
                        rPixValue = (byte)255;
                        gPixValue = (byte)166;
                        bPixValue = 0;
                    }else if (tempInC < 100){
                        rPixValue = gPixValue = (byte)255;
                        bPixValue = 0;
                    }else{
                        bPixValue = rPixValue = gPixValue = (byte)255;
                    }
                    // alpha always high
                    argbPixels[destP + 3] = aPixValue;
                    // red pixel
                    argbPixels[destP] = rPixValue;
                    argbPixels[destP + 1] = gPixValue;
                    argbPixels[destP + 2] = bPixValue;
                }
                thermalBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(argbPixels));

            }
            //若渲染后的帧类型为“热线性通量 14 位图像”类型
            else if(renderedImage.imageType() == RenderedImage.ImageType.ThermalLinearFlux14BitImage) {
                /**
                 * 以下是如何将自定义伪彩色应用于 14 位灰度图像的示例
                 * 此示例通过线性映射将 768 色的黑色>绿色>水绿色>白色
                 * RGB 值。尝试使用不同的颜色映射方法。
                 *
                 * 此示例以线性方式规格化场景。如果要将颜色映射到温度，
                 * 使用 Radiometic Kelvin 图像类型，并且不要应用缩放，如下所示。
                 */

                short[] shortPixels = new short[renderedImage.pixelData().length / 2];
                byte[] argbPixels = new byte[renderedImage.width() * renderedImage.height() * 4];
                // Thermal data is little endian.
                ByteBuffer.wrap(renderedImage.pixelData()).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortPixels);
                int minValue = 65535;
                int maxValue = 0;
                for (int p = 0; p < shortPixels.length; p++) {
                    minValue = Math.min(minValue, shortPixels[p]);
                    maxValue = Math.max(maxValue, shortPixels[p]);
                }
                int range = (maxValue - minValue);
                float scale = ((float) 767 / (float) range);
                for (int p = 0; p < shortPixels.length; p++) {
                    int destP = p * 4;

                    short pixelValue = (short)((shortPixels[p] - minValue) * scale);
                    byte redValue = 0;
                    byte greenValue;
                    byte blueValue = 0;
                    if (pixelValue < 256){
                        greenValue = (byte)pixelValue;
                    }else if (pixelValue < 512){
                        greenValue = (byte)255;
                        blueValue = (byte)(pixelValue - 256);
                    }else{
                        greenValue = (byte)255;
                        blueValue = (byte)255;
                        redValue = (byte)(pixelValue -512);
                    }


                    // alpha always high
                    argbPixels[destP + 3] = (byte) 255;
                    // red pixel
                    argbPixels[destP] = redValue;
                    argbPixels[destP + 1] = greenValue;
                    argbPixels[destP + 2] = blueValue;
                }

                thermalBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(argbPixels));
            }
            //若渲染后的帧类型为其他
            else {
                Log.e(TAG, "接收到的帧：width: "+renderedImage.width()+", height: "+renderedImage.height());
                //将热成像图像中的像素数据复制到一个Bitmap对象中
                thermalBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(renderedImage.pixelData()));
            }

            updateThermalImageView(thermalBitmap);

            /*
             在接收到新的图像帧时进行处理，并根据请求保存图像
             */
            //保存图像
            if (this.imageCaptureRequested) {
                imageCaptureRequested = false;
                saveImage(renderedImage,thermalBitmap);
            }
            //计算图像处理的时间，并将其记录到日志中。
            long endTime = System.nanoTime();
            long duration = (endTime - startTime);
            Log.d(TAG, "图像处理的时间Duration: "+(duration/1000000)+"ms");
            //检查streamSocket是否为null且已连接
            if (streamSocket != null && streamSocket.isConnected()){
                try {
                    // 通过另一个线程中的套接字发送 PNG 文件
                    final OutputStream outputStream = streamSocket.getOutputStream();
                    // 制作一个输出流，以便我们可以获取 PNG 的大小
                    final ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
                    //将thermalBitmap压缩为WEBP格式，并将压缩后的数据写入bufferStream
                    thermalBitmap.compress(Bitmap.CompressFormat.WEBP, 100, bufferStream);
                    bufferStream.flush();
                    (new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                /*
                                 * 标头是 6 个字节，表示图像数据的长度和设备旋转，
                                 * 这可以通过添加字节来扩展，以获得更多的元数据，例如图像格式(image format)
                                 */
                                byte[] headerBytes = ByteBuffer.allocate((Integer.SIZE + Short.SIZE) / 8).putInt(bufferStream.size()).putShort((short)deviceRotation).array();
                                synchronized (streamSocket) {
                                    outputStream.write(headerBytes);
                                    bufferStream.writeTo(outputStream);
                                    outputStream.flush();
                                }
                                bufferStream.close();


                            } catch (IOException ex) {
                                Log.e(TAG, "Error sending frame: " + ex.toString());
                            }
                        }
                    }).start();
                } catch (Exception ex){
                    Log.e(TAG, "Error creating PNG: "+ex.getMessage());

                }

            }

        }
    }
    private void saveImage(RenderedImage renderedImage,Bitmap bitmap){
        //断开连接
        int returnBitmapId = DataRepository.getInstance().cacheBitmap(bitmap);//缓存图片
        Intent returnIntent = new Intent();
        returnIntent.putExtra("RETURN_BITMAP_ID", returnBitmapId);
        setResult(RESULT_OK, returnIntent);
        finish();
        /*//创建并启动一个新的线程来执行图像保存的操作
        new Thread(new Runnable() {
            public void run() {
                //创建并启动一个新的线程来执行图像保存的操作
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        thermalImageView.animate().setDuration(50).scaleY(0).withEndAction((new Runnable() {
                            public void run() {
                                thermalImageView.animate().setDuration(50).scaleY(1);
                            }
                        }));

                    }
                });
                lastSavedPath = FrameSaver.saveFrame(mContext, renderedImage.getFrame());
                //将最后保存的路径添加到SavedDisplay列表中，并将其添加到MyApplication的displays列表中
                MyApplication.INSTANCE.saveDisplay(renderedImage, lastSavedPath);
                Log.d(TAG, "保存路径："+lastSavedPath+"-----------------------------------------");
            }
            @Override
            protected void finalize() throws Throwable {
                super.finalize();
                //断开连接
                mThermalViewModel.setThermalBitmap(bitmap);
                finish();
            }
        }).start();*/
    }
}