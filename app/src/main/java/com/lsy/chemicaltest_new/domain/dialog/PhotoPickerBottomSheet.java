package com.lsy.chemicaltest_new.domain.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.lsy.chemicaltest_new.R;

public class PhotoPickerBottomSheet {

    public interface OnPhotoPickerListener {
        void onCameraSelected();
        void onGallerySelected();
    }

    public static void show(Context context, OnPhotoPickerListener listener) {
        // 创建对话框
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context,R.style.BottomSheetDialogTheme);

        // 加载布局
        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_photo_picker, null);
        bottomSheetDialog.setContentView(view);

        // 设置选项点击事件
        view.findViewById(R.id.tv_gallery).setOnClickListener(v -> {
            if (listener != null) {
                listener.onGallerySelected();
            }
            bottomSheetDialog.dismiss();
        });

        view.findViewById(R.id.tv_camera).setOnClickListener(v -> {
            if (listener != null) {
                listener.onCameraSelected();
            }
            bottomSheetDialog.dismiss();
        });

        view.findViewById(R.id.tv_cancel).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });

        // 显示对话框
        bottomSheetDialog.show();

        // 设置对话框行为
        FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }
    }
}