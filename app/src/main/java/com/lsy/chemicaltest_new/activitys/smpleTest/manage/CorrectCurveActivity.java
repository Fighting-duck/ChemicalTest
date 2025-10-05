package com.lsy.chemicaltest_new.activitys.smpleTest.manage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.mikephil.charting.data.CombinedData;
import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.activitys.smpleTest.test.MeasureValueByMultimeterActivity;
import com.lsy.chemicaltest_new.adapters.CorrectCurveAdapter;
import com.lsy.chemicaltest_new.database.DataRepository;
import com.lsy.chemicaltest_new.databinding.ActivityCorrectCurveBinding;
import com.lsy.chemicaltest_new.domain.Expression;
import com.lsy.chemicaltest_new.domain.RGB;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.domain.TestValue;
import com.lsy.chemicaltest_new.domain.dialog.PhotoPickerBottomSheet;
import com.lsy.chemicaltest_new.fragments.ConnectMultimeterFragment;
import com.lsy.chemicaltest_new.models.CorrectCurveViewModel;
import com.lsy.chemicaltest_new.utils.CombinedChartUtils;
import com.lsy.chemicaltest_new.utils.ImageProcessor;
import com.lsy.chemicaltest_new.utils.NumberUtils;
import com.lsy.chemicaltest_new.utils.PhotoUtil;

import java.util.List;

public class CorrectCurveActivity extends AppCompatActivity {
    private static final String TAG = "CorrectCurveActivity";
    private ActivityCorrectCurveBinding mBinding;
    private CorrectCurveViewModel mViewModel;
    private CorrectCurveAdapter mAdapter;
    private Context mContext;
    private ImageProcessor mImageProcessor;
    private ActivityResultLauncher<Intent> mMeasureValueActivityLauncher;
    private ActivityResultLauncher<Intent> bitmapResultLauncher;
    private CombinedData mOldCombinedData;//联合图数据
    private CombinedData mNewCombinedData;//联合图数据
    private Expression mNewExpression = null;
    private Float mNewR = null;
    private Integer mPosition = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityCorrectCurveBinding.inflate(getLayoutInflater());
        mContext = this;
        setContentView(mBinding.getRoot());
        mViewModel = new ViewModelProvider(this).get(CorrectCurveViewModel.class);
        /**ActivityLauncher**/
        mMeasureValueActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // 处理返回结果：result是SecondActivity关闭后返回的数据
                    if (result.getResultCode() == RESULT_OK) { // 确保结果正常返回
                        Intent data = result.getData();
                        if (data != null) {
                            //判断是否包含key "result_float"
                            if (data.hasExtra(MeasureValueByMultimeterActivity.RETURN_TEST_VALUE)){
                                // 从Intent中获取float数据（key为"result_float"，与SecondActivity对应）
                                TestValue testValue = data.getParcelableExtra(MeasureValueByMultimeterActivity.RETURN_TEST_VALUE);
                                if (testValue != null){
                                    Log.d(TAG, "testValue: "+ mPosition+"   "+ testValue.getValue());
                                    mViewModel.setTestValue(mPosition,testValue);
                                }
                            }
                        }
                    }
                }
        );
        mImageProcessor = new ImageProcessor(this);
        // 初始化Activity结果监听 处理图片裁剪返回结果
        bitmapResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> mImageProcessor.handleActivityResult(result.getResultCode(), result.getData())
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPosition==null){
            StandardCurve curve = DataRepository.getInstance().getStandardCurve();
            if (curve != null){
                mViewModel.setStandardCurve(new StandardCurve(curve));// 保存旧曲线
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void initUI() {
        // 图表
        CombinedChartUtils.setChart(mBinding.ccNewChart);
        CombinedChartUtils.setChart(mBinding.ccOldChart);
        mNewCombinedData = new CombinedData();// 初始化 CombinedData 对象
        mOldCombinedData = new CombinedData();
        // 设置联合图表数据到 CombinedChart
        mBinding.ccNewChart.setData(mNewCombinedData);
        mBinding.ccOldChart.setData(mOldCombinedData);
        // 适配器
        mAdapter = new CorrectCurveAdapter(mContext);
        mBinding.rvCurveList.setLayoutManager(new LinearLayoutManager(mContext));
        mBinding.rvCurveList.setAdapter(mAdapter);
        // 监听器
        mAdapter.setOnTestClickListener(position -> {
            mPosition = position;
            Integer type = mViewModel.getCurveType();
            if (type == 2){// 比色图像颜色提取
                // 执行单击操作 打开图库选择图片或是拍照
                PhotoPickerBottomSheet.show(mContext, new PhotoPickerBottomSheet.OnPhotoPickerListener() {
                    @Override
                    public void onCameraSelected() {
                        //拍照
                        mImageProcessor.takePhoto(new ImageProcessor.ImageProcessingCallback() {
                            @Override
                            public void onImageSelected(Bitmap bitmap) {
                                mImageProcessor.startCrop(bitmap, bitmapResultLauncher, new ImageProcessor.ImageProcessingCallback() {
                                    @Override
                                    public void onImageSelected(Bitmap bitmap) {
                                        handleImage(bitmap,mPosition);
                                    }

                                    @Override
                                    public void onError(String message) {
                                        Log.e(TAG, "crop photo onImageSelected: " + message);
                                        mViewModel.setToast(getString(R.string.toast_takePhoto_cropFail));
                                    }
                                });
                            }

                            @Override
                            public void onError(String message) {
                                Log.e(TAG, "take photo onImageSelected: " + message);
                                mViewModel.setToast(getString(R.string.toast_takePhoto_fail));
                            }
                        });
                    }

                    @Override
                    public void onGallerySelected() {
                        // 处理相册逻辑
                        mImageProcessor.pickFromGallery(new ImageProcessor.ImageProcessingCallback() {
                            @Override
                            public void onImageSelected(Bitmap bitmap) {
                                mImageProcessor.startCrop(bitmap, bitmapResultLauncher, new ImageProcessor.ImageProcessingCallback() {
                                    @Override
                                    public void onImageSelected(Bitmap bitmap) {
                                        handleImage(bitmap,mPosition);
                                    }

                                    @Override
                                    public void onError(String message) {
                                        Log.e(TAG, "crop photo onImageSelected: " + message);
                                        mViewModel.setToast(getString(R.string.toast_takePhoto_cropFail));
                                    }
                                });
                            }

                            @Override
                            public void onError(String message) {
                                Log.e(TAG, "crop photo onImageSelected: " + message);
                                mViewModel.setToast(getString(R.string.toast_takePhoto_selectImage_fail));
                            }
                        });
                    }
                });
            }else {
                Intent intent = new Intent(this, MeasureValueByMultimeterActivity.class);
                intent.putExtra(MeasureValueByMultimeterActivity.GET_SHOW_MODE, ConnectMultimeterFragment.ShowModel.ELEC);//测量电流值
                mMeasureValueActivityLauncher.launch(intent);
            }
        });
        mAdapter.setOnLongClickListener(position -> {
            // 弹框提示是否删除
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(getString(R.string.dialog_deletePoint_title));
            builder.setMessage(getString(R.string.dialog_deletePoint_message));
            builder.setPositiveButton(getString(R.string.dialog_positive), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mViewModel.deleteItem(position);
                }
            });
            builder.setNegativeButton(getString(R.string.dialog_negative), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mAdapter.notifyAllItemRangeChanged();
                }
            });

            // 创建对话框并设置外部点击事件
            AlertDialog dialog = builder.create();
            // 设置点击外部是否可取消
            dialog.setCanceledOnTouchOutside(true);
            // 设置取消监听（包括外部点击和返回键取消）
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    // 处理点击空白区域的逻辑，这里示例和取消按钮做相同处理
                    mAdapter.notifyAllItemRangeChanged();
                }
            });
            dialog.show();
        });
        mAdapter.setOnDeleteClickListener(position -> {
            mViewModel.deleteTestValue(position);
        });
        mBinding.ivBack.setOnClickListener(this::onClick);
        mBinding.ivSave.setOnClickListener(this::onClick);
        // 设置观察者
        mViewModel.getLiveData_prompt().observe(this,toast->{
            if (toast != null){
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
                mViewModel.setToast(null);
            }
        });
        mViewModel.getLiveData_standardCurve().observe(this, curve -> {
            if (curve != null){
                mBinding.tvCurveName.setText(curve.getName());
            }
        });
        mViewModel.getLiveData_pointList().observe(this, pointList -> {
            if (pointList != null) {
                CombinedChartUtils.LinearRegressionResult linearRegressionResult =
                        CombinedChartUtils.buildChart(mContext, mBinding.ccOldChart,pointList, mViewModel.getCurveType(), mViewModel.getUnit_x());
                float R = NumberUtils.roundCurve_k_b_r(linearRegressionResult.correlationCoefficient());
                float R_2 = NumberUtils.roundCurve_k_b_r(linearRegressionResult.coefficientOfDetermination());
                float mse = NumberUtils.roundCurve_k_b_r(linearRegressionResult.getMse());
                Float k = NumberUtils.roundCurve_k_b_r(linearRegressionResult.getK());
                Float b = NumberUtils.roundCurve_k_b_r(linearRegressionResult.getB());
                mBinding.tvOldK.setText(k + "");
                mBinding.tvOldB.setText(b + "");
                mBinding.tvOldR.setText(R + "");
                mBinding.tvOldR2.setText(R_2 + "");
                mBinding.tvOldMse.setText(mse + "");
                mBinding.tvOldExpression.setText(new Expression(k,b).toString());
            }
        });
        mViewModel.getLiveData_correctedPointList().observe(this, pointList -> {
            if (pointList != null) {
                CombinedChartUtils.LinearRegressionResult linearRegressionResult =
                        CombinedChartUtils.buildChart(mContext, mBinding.ccNewChart,pointList, mViewModel.getCurveType(), mViewModel.getUnit_x());
                float R = NumberUtils.roundCurve_k_b_r(linearRegressionResult.correlationCoefficient());
                float R_2 = NumberUtils.roundCurve_k_b_r(linearRegressionResult.coefficientOfDetermination());
                float mse = NumberUtils.roundCurve_k_b_r(linearRegressionResult.getMse());
                Float k = NumberUtils.roundCurve_k_b_r(linearRegressionResult.getK());
                Float b = NumberUtils.roundCurve_k_b_r(linearRegressionResult.getB());
                mNewR = R;
                mNewExpression = new Expression(k,b);
                mBinding.tvNewK.setText(k + "");
                mBinding.tvNewB.setText(b + "");
                mBinding.tvNewR.setText(R + "");
                mBinding.tvNewR2.setText(R_2 + "");
                mBinding.tvNewMse.setText(mse + "");
                mBinding.tvNewExpression.setText(mNewExpression.toString());
            }
        });
        mViewModel.getLiveData_CurveItems().observe(this, curveItems -> {
            if (curveItems != null){
                mAdapter.submitList(curveItems);
                mAdapter.notifyDataSetChanged();
            }
        });


    }

    public void handleImage(Bitmap bitmap,Integer  position){
        MyApplication.DB_EXECUTOR.execute(() -> {
            // 执行耗时操作（如网络请求、文件读写、图像处理）
            //获取RGB值
            //Integer color = PhotoUtil.getAverageRGB(bitmap,20);

            List<Integer> colors = PhotoUtil.getDistinctColors(
                    bitmap,
                    5,           // 最多返回5种颜色
                    10f,       // 色相差<10°视为相似色
                    0.1f,        // 最小饱和度=0.1
                    0f         // 最小亮度=0
            );
            if (!colors.isEmpty() && colors.size()>1){
                mViewModel.setToast(getString(R.string.toast_color_too_much));
            }
            if (!colors.isEmpty()){
                /*float[] hsv = new float[3];
                Color.colorToHSV(colors.get(0), hsv);*/
                RGB rgb = RGB.fromColor(colors.get(0));
                TestValue testValue = mViewModel.createTestValue(rgb.getBlue());
                runOnUiThread(() -> {
                    mViewModel.setTestValue(position, testValue);
                    //mViewModel.setHSV(new HSV(hsv[0],hsv[1],hsv[2]));
                });
            }
        });
    }

    private void onClick(View view) {
        int id = view.getId();
        if (id == mBinding.ivBack.getId()) {
            finish();
        }
        else if (id == mBinding.ivSave.getId()) {
            // 保存曲线
            mViewModel.saveCurve(mNewExpression,mNewR);
        }
    }
}