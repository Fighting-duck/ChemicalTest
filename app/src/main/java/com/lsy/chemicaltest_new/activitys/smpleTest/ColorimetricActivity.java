package com.lsy.chemicaltest_new.activitys.smpleTest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.activitys.BaseActivity;
import com.lsy.chemicaltest_new.database.DataRepository;
import com.lsy.chemicaltest_new.databinding.ActivityColorimetricBinding;
import com.lsy.chemicaltest_new.domain.ColoTestResult;
import com.lsy.chemicaltest_new.domain.HSV;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.domain.dialog.CurveDetailDialog;
import com.lsy.chemicaltest_new.fragments.SelectCurveFragment;
import com.lsy.chemicaltest_new.models.ColoViewModel;
import com.lsy.chemicaltest_new.utils.ImageProcessor;
import com.lsy.chemicaltest_new.utils.PhotoUtil;

import java.util.List;


public class ColorimetricActivity extends BaseActivity {
    private static final String TAG = "ColorimetricActivity";
    private ActivityColorimetricBinding mBinding;
    private Context mContext;
    private ColoViewModel mViewModel;

    private ImageProcessor mImageProcessor;
    private ActivityResultLauncher<Intent> bitmapResultLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityColorimetricBinding.inflate(getLayoutInflater());
        mContext = this;
        setContentView(mBinding.getRoot());
        mViewModel = new ViewModelProvider(this).get(ColoViewModel.class);
        mViewModel.setContext(this);
        // 初始化Activity结果监听 处理图片裁剪返回结果
        bitmapResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> mImageProcessor.handleActivityResult(result.getResultCode(), result.getData())
        );
        mImageProcessor = new ImageProcessor(this);
        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ColoTestResult testResult = DataRepository.getInstance().getColoTestResult();
        if (testResult != null){
            mViewModel.setStandardCurve(testResult.getStandardCurve());
            mViewModel.setOriginalImage(testResult.getOriginalImage());
            mViewModel.setCropImage(testResult.getCropImage());
            mViewModel.setRGB(testResult.getRGB());
            mViewModel.setHSV(testResult.getHsv());
            mViewModel.setCo(testResult.getDetectionCo());
            mBinding.tvDiseaseAnalysis.setText(testResult.getDiseaseAnal());
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @SuppressLint("SetTextI18n")
    private void initUI() {
        mBinding.swIsSaveOriginalImage.setChecked(MyApplication.INSTANCE.getIsSaveColoOriginalImage());

        mBinding.ivBack.setOnClickListener(this::onCLick);
        mBinding.ivTackPhoto.setOnClickListener(this::onCLick);
        mBinding.btnGetPhoto.setOnClickListener(this::onCLick);
        mBinding.ivOriginalImage.setOnClickListener(this::onCLick);
        mBinding.ivSelectArea.setOnClickListener(this::onCLick);
        mBinding.ivSave.setOnClickListener(this::onCLick);
        mBinding.btnAlterCropArea.setOnClickListener(this::onCLick);
        mBinding.btnStartAnal.setOnClickListener(this::onCLick);
        mBinding.btnSelectCurve.setOnClickListener(this::onCLick);
        mBinding.tvCurve.setOnClickListener(this::onCLick);
        mBinding.swIsSaveOriginalImage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            MyApplication.INSTANCE.setIsSaveColoOriginalImage(isChecked);
        });
        // 观察ViewModel中ColoTestResult的变化
        mViewModel.getLiveData_toast().observe(this, toast -> {
            if (toast != null){
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
                mViewModel.setToast(null);
            }
        });
        mViewModel.getLiveData_curve().observe(this,curve -> {
            if (curve == null) {
                mBinding.tvCurve.setText(R.string.default_no);
                mBinding.btnStartAnal.setEnabled(false);
            }
            else {
                mBinding.tvCurve.setText(curve.getName());
                mBinding.btnStartAnal.setEnabled(true);
            }
        });
        mViewModel.getLiveData_originalImage().observe(this, bitmap -> {
            if (bitmap == null) return;
            //显示图片
            Glide.with(mContext)
                    .load(bitmap)
                    .placeholder(R.drawable.icon_loading)
                    .error(R.drawable.icon_error)
                    .into(mBinding.ivOriginalImage);
            //等价于
            //mBinding.ivOriginalImage.setImageBitmap(bitmap);
        });
        mViewModel.getLiveData_cropImage().observe(this, bitmap -> {
            if (bitmap == null) return;
            //显示裁剪图片
            Glide.with(mContext)
                    .load(bitmap)
                    .placeholder(R.drawable.icon_loading)
                    .error(R.drawable.icon_error)
                    .into(mBinding.ivSelectArea);
            //等价于
            //mBinding.ivSelectArea.setImageBitmap(bitmap);
            // 提交任务到后台线程
            MyApplication.DB_EXECUTOR.execute(() -> {
                // 执行耗时操作（如网络请求、文件读写、图像处理）
                //获取RGB值
                Integer color = PhotoUtil.getAverageRGB(bitmap,20);
                //获取HSV值
                List<Float> hsv = PhotoUtil.getAverageHSV(bitmap,20);

                // 需要更新 UI 时，切回主线程
                runOnUiThread(() -> {
                    if (color != null)
                        mViewModel.setColor(color);
                    if (hsv != null)
                        mViewModel.setHSV(new HSV(hsv.get(0),hsv.get(1),hsv.get(2)));
                });
            });
        });
        //mViewModel.setColor(color);
        mViewModel.getLiveData_correctedColor().observe(this,color -> {
            if (color == null) return;
            drawColor(color);
        });
        mViewModel.getLiveData_rgb().observe(this, rgb -> {
            if (rgb == null) return;
            mBinding.tvRed.setText(String.valueOf(rgb.getRed()));
            mBinding.tvGreen.setText(String.valueOf(rgb.getGreen()));
            mBinding.tvBlue.setText(String.valueOf(rgb.getBlue()));
        });
        mViewModel.getLiveData_hsv().observe(this, hsv -> {
            if (hsv == null) return;
            mBinding.tvHue.setText(String.valueOf(hsv.getHue()));
            mBinding.tvSaturation.setText(String.valueOf(hsv.getSaturation()));
            mBinding.tvValue.setText(String.valueOf(hsv.getValue()));
        });
        mViewModel.getLiveData_CO().observe(this, co -> {
            if (co == null) return;
            String unit = mViewModel.getUnit();
            mBinding.tvCORGB.setText(co+" "+unit);
        });
        mViewModel.getLiveData_diseaseAnal().observe(this,result->{
            if (result == null) return;
            mBinding.tvDiseaseAnalysis.setText(result);
        });
        mViewModel.getLiveData_coloTestResult().observe(this, result ->{
            if (result!=null){
                Log.d(TAG, "update coloTestResult: "+result);
            }
        });
    }

    private void onCLick(View view) {
        int id = view.getId();
        if (id ==mBinding.ivBack.getId()){
            finish();
        }
        else if (id==mBinding.btnSelectCurve.getId()){
            showSelectDialog("select_curve");
        }
        else if (id==mBinding.tvCurve.getId()){
            if (mViewModel.getLiveData_curve().getValue()!=null) {
                CurveDetailDialog curveDetailDialog = new CurveDetailDialog(mContext,mViewModel.getLiveData_curve().getValue());
                curveDetailDialog.show();
            }
        }
        // 拍照
        else if (id==mBinding.ivTackPhoto.getId()) {
            mImageProcessor.takePhoto( new ImageProcessor.ImageProcessingCallback() {
                @Override
                public void onImageSelected(Bitmap bitmap) {
                    mViewModel.setOriginalImage(bitmap);
                    mImageProcessor.startCrop(bitmap, bitmapResultLauncher, new ImageProcessor.ImageProcessingCallback() {
                        @Override
                        public void onImageSelected(Bitmap bitmap) {
                            mViewModel.setCropImage(bitmap);
                        }

                        @Override
                        public void onError(String message) {
                            Log.e(TAG, "crop photo onImageSelected: " + message);
                            mViewModel.setToast(message);
                        }
                    });
                }

                @Override
                public void onError(String message) {
                    Log.e(TAG, "take photo onImageSelected: " + message);
                    mViewModel.setToast(message);
                }
            });
        }
        // 从相册获取图片
        else if (id == mBinding.btnGetPhoto.getId()) {
            mImageProcessor.pickFromGallery(new ImageProcessor.ImageProcessingCallback() {
                @Override
                public void onImageSelected(Bitmap bitmap) {
                    mViewModel.setOriginalImage(bitmap);
                    mImageProcessor.startCrop(bitmap, bitmapResultLauncher, new ImageProcessor.ImageProcessingCallback() {
                        @Override
                        public void onImageSelected(Bitmap bitmap) {
                            mViewModel.setCropImage(bitmap);
                        }

                        @Override
                        public void onError(String message) {
                            Log.e(TAG, "crop photo onImageSelected: " + message);
                            mViewModel.setToast(message);
                        }
                    });
                }

                @Override
                public void onError(String message) {
                    Log.e(TAG, "select photo onImageSelected: " + message);
                    mViewModel.setToast(message);
                }
            });
        }
        // 修改裁剪区域
        else if (id == mBinding.btnAlterCropArea.getId()) {
            BitmapDrawable drawable = ((BitmapDrawable)(mBinding.ivOriginalImage).getDrawable());
            if (drawable == null) {
                mViewModel.setToast(getString(R.string.toast_no_image));
                return;
            }
            mImageProcessor.startCrop(drawable.getBitmap(), bitmapResultLauncher, new ImageProcessor.ImageProcessingCallback() {
                @Override
                public void onImageSelected(Bitmap bitmap) {
                    mViewModel.setCropImage(bitmap);
                }

                @Override
                public void onError(String message) {
                    Log.e(TAG, "crop photo onImageSelected: " + message);
                    mViewModel.setToast(message);
                }
            });
        }
        else if (id == mBinding.ivOriginalImage.getId()) {
            BitmapDrawable drawable = ((BitmapDrawable)(mBinding.ivOriginalImage).getDrawable());
            PhotoUtil.viewLargeImage(mContext,drawable);
        }
        else if (id == mBinding.ivSelectArea.getId()) {
            BitmapDrawable drawable = ((BitmapDrawable)(mBinding.ivSelectArea).getDrawable());
            PhotoUtil.viewLargeImage(mContext,drawable);
        }
        else if (id == mBinding.btnStartAnal.getId()){
            mViewModel.diseaseAnal();
        }
        else if (id==mBinding.ivSave.getId()){
            if (mViewModel.save()) finish();
        }
    }
    //显示选择标准曲线弹框
    private void showSelectDialog(String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SelectCurveFragment existingFragment = (SelectCurveFragment) fragmentManager.findFragmentByTag(tag);
        if (existingFragment == null) {
            SelectCurveFragment dialogFragment = getSelectCurveFragment();
            dialogFragment.show(fragmentManager, tag);
        }else {
            if (!existingFragment.isVisible()) {
                existingFragment.show(fragmentManager, tag);
            }
        }
    }

    @NonNull
    private SelectCurveFragment getSelectCurveFragment() {
        SelectCurveFragment dialogFragment = new SelectCurveFragment(2);
        dialogFragment.setOnSelectCurveListener(new SelectCurveFragment.OnFragmentChangeListener() {
            @Override
            public void onSelectCurve(StandardCurve selectCurve) {
                mViewModel.setStandardCurveAndCalculateCO(selectCurve);
            }
        });
        return dialogFragment;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
    /***
     * 使用了String.format来格式化字符串，%06X表示输出一个至少6位的十六进制数，如果不足6位则在前面补零。
     * 0xFFFFFFFF & color用于确保颜色值是32位的，这样可以正确处理ARGB格式的颜色值。
     * @param color
     * @return
     */
    public void drawColor(int color) {
        int red = Color.red(color);//提取红色分量
        int green = Color.green(color);//提取绿色分量
        int blue = Color.blue(color);//提取蓝色分量
        String color_str =  ("#" + red + green + blue).toUpperCase();

        mBinding.tvColor.setBackgroundColor(color);
        mBinding.tvColorStr.setText(color_str);
        mBinding.tvRed.setText(String.valueOf(red));
        mBinding.tvGreen.setText(String.valueOf(green));
        mBinding.tvBlue.setText(String.valueOf(blue));
    }

}