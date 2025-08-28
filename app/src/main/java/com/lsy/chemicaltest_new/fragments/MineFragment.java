package com.lsy.chemicaltest_new.fragments;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.lsy.chemicaltest_new.BuildConfig;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.activitys.BottomNavigationActivity;
import com.lsy.chemicaltest_new.activitys.mine.AboutAppActivity;
import com.lsy.chemicaltest_new.activitys.mine.CurveSettingActivity;
import com.lsy.chemicaltest_new.activitys.mine.UseLanguageActivity;
import com.lsy.chemicaltest_new.database.SharePreferencesManager;
import com.lsy.chemicaltest_new.databinding.FragmentMineBinding;
import com.lsy.chemicaltest_new.domain.Experimenter;
import com.lsy.chemicaltest_new.domain.dialog.PhotoPickerBottomSheet;
import com.lsy.chemicaltest_new.domain.imageView.GestureImageView;
import com.lsy.chemicaltest_new.models.MineViewModel;
import com.lsy.chemicaltest_new.utils.AvatarWithBlurBackground;
import com.lsy.chemicaltest_new.utils.ImageProcessor;
import com.lsy.chemicaltest_new.utils.PhotoUtil;


public class MineFragment extends Fragment {

    private static final String TAG = "MineFragment";
    private FragmentMineBinding mBinding;
    private Context mContext;
    private MineViewModel mViewModel;
    private BottomNavigationActivity mActivity;
    private AvatarWithBlurBackground mAvatarHelper;
    private SharePreferencesManager mSharePreferencesManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentMineBinding.inflate(inflater, container, false);
        mContext = this.getContext();
        mViewModel = new ViewModelProvider(this).get(MineViewModel.class);
        mAvatarHelper = new AvatarWithBlurBackground(mContext, mBinding.ivHeadBackground);
        mSharePreferencesManager = new SharePreferencesManager(mContext);
        initUI();
        return mBinding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity  = (BottomNavigationActivity) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        String userName = mSharePreferencesManager.getUserName();
        String avatarPath = mSharePreferencesManager.getAvatarPath();
        Experimenter experimenter = new Experimenter();
        experimenter.setName(userName);
        experimenter.setImagePath(avatarPath);
        if (avatarPath != null && !avatarPath.isEmpty())
            experimenter.setImage(PhotoUtil.loadBitmapFromPath(avatarPath));
        else
            experimenter.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.icon_head));
        mViewModel.setExperimenter(experimenter);
        Log.d(TAG, "onResume 实验员信息 ：" + userName + " " + avatarPath);
    }


    @SuppressLint("SetTextI18n")
    private void initUI() {
        String appName = getString(R.string.app_name);
        mBinding.tvVersion.setText(appName+ " V" + BuildConfig.VERSION_NAME);
        mBinding.flSetting.setOnClickListener(this::onClick);
        mBinding.tvExperimenterName.setOnClickListener(this::onClick);
        mBinding.flUseLanguage.setOnClickListener(this::onClick);
        mBinding.flAppDetails.setOnClickListener(this::onClick);
        mBinding.ivHeadPicture.setOnGestureListener(new GestureImageView.OnGestureListener() {
            @Override
            public void onSingleTap(View v) {
                // 执行单击操作 打开图库选择图片或是拍照
                PhotoPickerBottomSheet.show(mContext, new PhotoPickerBottomSheet.OnPhotoPickerListener() {
                    @Override
                    public void onCameraSelected() {
                        //拍照
                       mActivity.takePhoto(new ImageProcessor.ImageProcessingCallback() {
                           @Override
                           public void onImageSelected(Bitmap bitmap) {
                               mViewModel.updateHeadImage(bitmap,mContext);
                           }

                           @Override
                           public void onError(String message) {
                               Log.e(TAG, "take photo onImageSelected: " + message);
                               mViewModel.setToast(message);
                           }
                       });
                    }

                    @Override
                    public void onGallerySelected() {
                        // 处理相册逻辑
                        mActivity.pickFromGallery(new ImageProcessor.ImageProcessingCallback() {
                            @Override
                            public void onImageSelected(Bitmap bitmap) {
                                mViewModel.updateHeadImage(bitmap,mContext);
                            }

                            @Override
                            public void onError(String message) {
                                Log.e(TAG, "crop photo onImageSelected: " + message);
                                mViewModel.setToast(message);
                            }
                        });
                    }
                });
            }

            @Override
            public boolean onDoubleTap(View v) {
                Bitmap bitmap = mViewModel.getImage();
                // 双击事件处理
                if (bitmap != null){
                    PhotoUtil.viewLargeImage(mContext, bitmap);
                }
                else
                    mViewModel.setToast(getString(R.string.toast_no_image));

                return false;
            }

            @Override
            public void onLongPress(View v) {

            }
        });

        mViewModel.getLiveData_toast().observe(getViewLifecycleOwner(), message -> {
            if (message != null)
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        });
        mViewModel.getLiveData_experimenter().observe(getViewLifecycleOwner(), experimenter -> {
            if (experimenter != null) {
                mBinding.tvExperimenterName.setText(experimenter.getName()); // 显示实验人名
                setImage(experimenter.getImage());// 显示头像
                mAvatarHelper.loadAvatar(experimenter.getImage()); //  加载模糊头像背景
            }
        });

    }

    public void setImage(Bitmap bitmap) {
        Glide.with(mContext)
                .load(bitmap)
                .placeholder(R.drawable.icon_loading)
                .error(R.drawable.icon_error)
                .transform(new CircleCrop())
                .into(mBinding.ivHeadPicture);
    }

    private void onClick(View view) {
        int id = view.getId();
        if (id == mBinding.flSetting.getId()) {
            Intent intent = new Intent(mContext, CurveSettingActivity.class);
            mContext.startActivity(intent);
        }
        else if (id == mBinding.tvExperimenterName.getId()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(getString(R.string.mine_experimenter));

            // 创建一个 EditText 并添加到对话框中
            final EditText input = new EditText(mContext);
            builder.setView(input);

            builder.setPositiveButton(getString(R.string.dialog_positive), (dialog, which) -> {
                // 获取编辑框中的文本
                String inputText = input.getText().toString();
                mViewModel.setExperimenterName(inputText,mContext);
            });

            builder.setNegativeButton(getString(R.string.dialog_negative), (dialog, which) -> dialog.cancel());

            Dialog dialog = builder.create();
            dialog.show();
        }
        else if (id == mBinding.flUseLanguage.getId()) {
            Intent intent = new Intent(mContext, UseLanguageActivity.class);
            mContext.startActivity(intent);
        }
        else if (id == mBinding.flAppDetails.getId()) {
            Intent intent = new Intent(mContext, AboutAppActivity.class);
            mContext.startActivity(intent);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}