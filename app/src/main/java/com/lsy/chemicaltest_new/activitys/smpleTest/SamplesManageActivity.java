package com.lsy.chemicaltest_new.activitys.smpleTest;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.activitys.BaseActivity;
import com.lsy.chemicaltest_new.adapters.SampleAdapter;
import com.lsy.chemicaltest_new.databinding.ActivitySamplesManageBinding;
import com.lsy.chemicaltest_new.domain.Sample;
import com.lsy.chemicaltest_new.domain.dialog.PhotoPickerBottomSheet;
import com.lsy.chemicaltest_new.domain.imageView.GestureImageView;
import com.lsy.chemicaltest_new.models.SampleManageViewModel;
import com.lsy.chemicaltest_new.utils.ImageProcessor;
import com.lsy.chemicaltest_new.utils.PhotoUtil;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SamplesManageActivity extends BaseActivity {
    public static final String TAG = "SamplesManageActivity";
    private static final String KEY_DATA = "key_data";
    private ActivitySamplesManageBinding mBinding;
    private Context mContext;
    private SampleManageViewModel mViewModel;
    private SampleAdapter mAdapter;
    ExecutorService mCachedThreadPool = Executors.newCachedThreadPool();//java线程池
    private ImageProcessor mImageProcessor;
    private ActivityResultLauncher<Intent> bitmapResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mBinding = ActivitySamplesManageBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(this).get(SampleManageViewModel.class);
        mViewModel.setContext(this);
        mImageProcessor = new ImageProcessor(this);
        // 初始化Activity结果监听 处理图片裁剪返回结果
        bitmapResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> mImageProcessor.handleActivityResult(result.getResultCode(), result.getData())
        );
        setContentView(mBinding.getRoot());
        initUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewModel.showAllSamples(new SampleManageViewModel.UpdateCallback() {
            @Override
            public void onUpdateCompleted(List<Sample> samples) {

            }

            @Override
            public void onUpdateFailed(Exception e) {
                e.printStackTrace();
                mViewModel.setToast(getString(R.string.toast_sample_getSamples_fail));
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCachedThreadPool.shutdown();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initUI() {
        //初始化recycleView列表
        mBinding.rvSampleList.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new SampleAdapter();
        mBinding.rvSampleList.setAdapter(mAdapter);

        mViewModel.getLiveData_toast().observe(this, toast -> {
            if (toast != null){
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
                mViewModel.setToast(null);
            }
        });
        mViewModel.getLiveData_samples().observe(this, samples -> {
            if (samples != null && !samples.isEmpty()) {
                mBinding.tvSampleTotalNum.setText(String.valueOf(samples.size()));
                mBinding.emptyTextView.setVisibility(View.GONE);
                mBinding.rvSampleList.setVisibility(View.VISIBLE);
                mAdapter.submitList(samples);
                mAdapter.notifyDataSetChanged();
            }
            else {
                mBinding.emptyTextView.setVisibility(View.VISIBLE);
                mBinding.rvSampleList.setVisibility(View.GONE);
                mBinding.tvSampleTotalNum.setText(getString(R.string.default_number));
            }
        });
        mViewModel.getLiveData_Image().observe(this, image -> {
            if (image != null) {
               mBinding.ivImage.setImageBitmap(image);
            }
            else
                mBinding.ivImage.setImageResource(R.drawable.icon_no_image);
        });
        mViewModel.getLiveData_currentState().observe(this, integer -> {
            switch (integer) {
                case 1:
                    mBinding.btnAddSample.setText(getString(R.string.sample_add));
                    break;
                case 2:
                    mBinding.btnAddSample.setText(getString(R.string.sample_edit));
                    break;
                default:
                    break;
            }
        });
        mViewModel.getLiveData_editSample().observe(this, sample -> {
            if (sample != null) {
                mBinding.llBackAdd.setVisibility(View.VISIBLE);
                mBinding.edtSampleName.setText(sample.getName());
                mBinding.edtDescription.setText(sample.getDescription());
                mViewModel.setImage(sample.getImage());
            }
            else {
                mBinding.llBackAdd.setVisibility(View.GONE);
                mBinding.edtSampleName.setText("");
                mBinding.edtDescription.setText("");
                mViewModel.setImage(null);
            }
        });
        mViewModel.getLiveData_saveState().observe(this, saveState -> {
            switch (saveState){
                case SUCCESS:
                case ERROR:
                    mBinding.edtSampleName.setText("");
                    mBinding.edtDescription.setText("");
                    mBinding.btnAddSample.setText(getString(R.string.sample_add));
                    mBinding.btnAddSample.setEnabled(true);
                    break;
                case LOADING:
                    mBinding.btnAddSample.setText(getString(R.string.sample_adding));
                    mBinding.btnAddSample.setEnabled(false);
                    break;
                case EMPTY:
                    mBinding.btnAddSample.setText(getString(R.string.sample_add));
                    break;
            }
        });
        mViewModel.getLiveData_editState().observe(this, editState -> {
            switch (editState){
                case SUCCESS:
                case ERROR:
                    mBinding.btnAddSample.setText(getString(R.string.sample_edit));
                    mBinding.btnAddSample.setEnabled(true);
                    break;
                case LOADING:
                    mBinding.btnAddSample.setText(getString(R.string.sample_editing));
                    mBinding.btnAddSample.setEnabled(false);
                    break;
                case EMPTY:
                    mBinding.btnAddSample.setText(getString(R.string.sample_add));
                    break;
            }
        });

        mBinding.btnAddSample.setOnClickListener(this::onClick);
        mBinding.ivBack.setOnClickListener(this::onClick);
        mBinding.ivBackAdd.setOnClickListener(this::onClick);
        mBinding.ivImage.setOnGestureListener(new GestureImageView.OnGestureListener() {
            @Override
            public void onSingleTap(View v) {
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
                                        mViewModel.setImage(bitmap);
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
                                        mViewModel.setImage(bitmap);
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
            }

            @Override
            public boolean onDoubleTap(View v) {
                // 双击事件处理
                if (mViewModel.getImage() != null) {
                    BitmapDrawable drawable = ((BitmapDrawable) (mBinding.ivImage).getDrawable());
                    PhotoUtil.viewLargeImage(mContext, drawable);
                } else
                    mViewModel.setToast(getString(R.string.toast_no_image));
                return false;
            }

            @Override
            public void onLongPress(View v) {
                // 长按事件处理  弹框通知是否删除
                if (mViewModel.getImage() != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(getString(R.string.sample_deleteDialog_title))
                           .setPositiveButton(getString(R.string.dialog_positive), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mViewModel.setImage(null);
                                    dialog.dismiss();
                                }
                            })
                           .setNegativeButton(getString(R.string.dialog_positive), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }
            }
        });
        mAdapter.setOnEditClickListener(position -> {
            if (mAdapter != null) {
                Sample sample = mAdapter.getCurrentList().get(position);
                // 处理编辑逻辑
                mViewModel.setCurrentState(2, sample);//修改当前状态为修改
            }
        });
        mAdapter.setOnDeleteClickListener(position -> {
            if (mAdapter != null){
                //弹框提示是否删除
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(mContext);
                builder.setTitle(getString(R.string.dialog_deleteSample_title));
                builder.setMessage(getString(R.string.dialog_deleteSample_message));
                builder.setPositiveButton(getString(R.string.dialog_positive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mViewModel.deleteSample(position);
                    }
                });
                builder.setNegativeButton(getString(R.string.dialog_negative), null);
                builder.create().show();
            }
        });
        mAdapter.setOnImageClickListener(position -> {
            if (mAdapter != null) {
                Sample sample = mAdapter.getCurrentList().get(position);
                PhotoUtil.viewLargeImage(mContext, sample.getImage());
            }
        });
        //设置下拉刷新布局的进度圆圈颜色
        mBinding.srlRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light,
                android.R.color.holo_orange_light, android.R.color.holo_green_light);
        //给refreshLayout设置下拉刷新监听器
        mBinding.srlRefreshLayout.setOnRefreshListener(() -> {
            mViewModel.showAllSamples(new SampleManageViewModel.UpdateCallback() {
                @Override
                public void onUpdateCompleted(List<Sample> samples) {
                    mBinding.srlRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onUpdateFailed(Exception e) {
                    mBinding.srlRefreshLayout.setRefreshing(false);
                    mViewModel.setToast(getString(R.string.toast_update_fail));
                }
            });
        });
    }
    private void onClick(View view){
        if (view.getId() == mBinding.ivBack.getId()){
            finish();
        }
        else if (view.getId() == mBinding.btnAddSample.getId()){
            String sampleName = mBinding.edtSampleName.getText().toString();
            if (sampleName.isEmpty()) {
                mViewModel.setToast(getString(R.string.toast_sample_name_empty));
                return;
            }
            // 样品名称
            Sample sample = new Sample(sampleName);
            // 样品描述
            String description = mBinding.edtDescription.getText().toString();
            if (!description.isEmpty()) sample.setDescription(description);
            else sample.setDescription("");
            // 样品图片
            sample.setImage(mViewModel.getImage());
            //根据当前状态判断是添加还是编辑
            Integer currentState = mViewModel.getLiveData_currentState().getValue();
            if (currentState !=null){
                if (currentState == 1) {
                    //保存
                    mViewModel.saveSample(mContext, sample);
                }
                else if (currentState == 2) {
                    Sample editSample = mViewModel.getLiveData_editSample().getValue();
                    if (editSample != null) {
                        //编辑
                        mViewModel.editSample(mContext, sample, editSample);
                    }
                }
            }
        }
        else if (view.getId() == mBinding.ivBackAdd.getId()){
            mViewModel.setCurrentState(1,null);//修改当前状态为修改
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //mCameraHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //mCameraHelper.onActivityResult(requestCode, resultCode, data);
    }

    //数据保存与恢复
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: ");
        super.onSaveInstanceState(outState);
    }
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState: ");
        super.onRestoreInstanceState(savedInstanceState);
        mViewModel.showAllSamples(new SampleManageViewModel.UpdateCallback() {
            @Override
            public void onUpdateCompleted(List<Sample> samples) {

            }

            @Override
            public void onUpdateFailed(Exception e) {
                mViewModel.setToast(getString(R.string.toast_sample_getSamples_fail));
            }
        });
    }
}