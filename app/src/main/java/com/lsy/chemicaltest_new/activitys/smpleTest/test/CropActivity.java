package com.lsy.chemicaltest_new.activitys.smpleTest.test;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import com.lsy.chemicaltest_new.activitys.BaseActivity;
import com.lsy.chemicaltest_new.databinding.ActivityCropImageBinding;
import com.lsy.chemicaltest_new.utils.ImageProcessor;

public class CropActivity extends BaseActivity {
    private static final String TAG = "CropImageActivity";
    private ActivityCropImageBinding mBinding;
    private Context     mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityCropImageBinding.inflate(getLayoutInflater());
        mContext = this;
        setContentView(mBinding.getRoot());
        int bitmapId = getIntent().getIntExtra("BITMAP_ID", -1);
        Bitmap receivedBitmap = ImageProcessor.BitmapCache.getInstance().getAndRemove(bitmapId);//获取 Bitmap 后自动移除缓存（避免泄漏）
        if (receivedBitmap != null) {
            mBinding.cropImageView.setImageBitmap(receivedBitmap);
        }
    }
    /**
     * 点击裁剪按钮
     */
    public void crop(View view) {
        Bitmap bitmap = mBinding.cropImageView.getCroppedImage();
        int returnBitmapId = ImageProcessor.BitmapCache.getInstance().cache(bitmap);//缓存 Bitmap 后返回 id
        Intent returnIntent = new Intent();
        returnIntent.putExtra("RETURN_BITMAP_ID", returnBitmapId);
        setResult(RESULT_OK, returnIntent);
        finish();
    }
}