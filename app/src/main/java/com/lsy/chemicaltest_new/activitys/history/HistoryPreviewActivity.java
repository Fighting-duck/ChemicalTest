package com.lsy.chemicaltest_new.activitys.history;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.activitys.BaseActivity;
import com.lsy.chemicaltest_new.database.DataRepository;
import com.lsy.chemicaltest_new.domain.History_multiple;
import com.lsy.chemicaltest_new.fragments.ComprehensiveTestResultFragment;
import com.lsy.chemicaltest_new.models.HistoryPreviewViewModel;
import com.lsy.chemicaltest_new.utils.ExportUtils;
import com.lsy.chemicaltest_new.databinding.ActivityHistoryPreviewBinding;
import com.lsy.chemicaltest_new.utils.StorageUtils;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class HistoryPreviewActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks{
    private static final String TAG = "HistoryPreviewActivity";
    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 100;
    private static final int MY_PERMISSIONS_REQUEST_MANAGE_STORAGE = 10;
    private ActivityHistoryPreviewBinding mBinding;
    private Context mContext;
    private HistoryPreviewViewModel mViewModel;
    private ComprehensiveTestResultFragment mFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityHistoryPreviewBinding.inflate(getLayoutInflater());
        mContext = this;
        mViewModel = new ViewModelProvider(this).get(HistoryPreviewViewModel.class);
        setContentView(mBinding.getRoot());
        // 添加另一个布局
        mFragment = new ComprehensiveTestResultFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fcv_historyFragment, mFragment);
        transaction.commit();
        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        History_multiple history_multiple = DataRepository.getInstance().getHistory_multiple();
        if (history_multiple != null){
            mViewModel.setHistory(history_multiple);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding = null;
        mViewModel.clearAll();
    }
    @SuppressLint("SetTextI18n")
    private void initUI() {

        mBinding.ivBack.setOnClickListener(view -> finish());
        mBinding.btnDelete.setOnClickListener(this::onClick);
        mBinding.ivExport.setOnClickListener(this::onClick);

        mViewModel.getLiveData_toast().observe(this, toast ->{
            if (toast != null){
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
                mViewModel.setToast(null);
            }
        });
        mViewModel.getLiveData_history().observe(this, history_multiple -> {
            if (history_multiple==null) return;
            Log.d(TAG, "new History:"+history_multiple);
            mBinding.tvName.setText(history_multiple.getHistoryName());
            mBinding.tvDateTime.setText(history_multiple.getSaveTime());//历史记录时间
            if (history_multiple.getCredibility()!=null){
                mBinding.tvCredibilityAnalysis.setText(String.valueOf(history_multiple.getCredibility()));//可信度分析结果
            }
            mBinding.tvDescription.setText(history_multiple.getRemarks());
            mFragment.updateData(history_multiple);
        });
    }

    private void onClick(View view) {
        if (view.getId() == mBinding.btnDelete.getId()) {
            //弹框提示是否删除
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(getString(R.string.historyPreview_dialog_title));
            builder.setMessage(getString(R.string.historyPreview_dialog_message));
            builder.setPositiveButton(getString(R.string.dialog_positive), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mBinding.btnDelete.setEnabled(false);
                    mViewModel.deleteHistory(new HistoryPreviewViewModel.DeleteHistoryCallback() {
                        @Override
                        public void onDeleteSuccess() {
                            mViewModel.setToast(getString(R.string.toast_delete_success));
                            runOnUiThread(HistoryPreviewActivity.this::finish);
                        }

                        @Override
                        public void onDeleteFailure(Exception e) {
                            e.printStackTrace();
                            mViewModel.setToast(getString(R.string.toast_delete_fail));
                            runOnUiThread(()->{
                                mBinding.btnDelete.setEnabled(true);
                            });
                        }
                    });

                }
            });
            builder.setNegativeButton(getString(R.string.dialog_negative), null);
            builder.create().show();
        }
        else if (view.getId() == mBinding.ivExport.getId()) {
            if (StorageUtils.hasEnoughSpace(50)){
                requestPermissionsAndExport();
            }
            else
                mViewModel.setToast(getString(R.string.toast_insufficientSspace));
        }
    }
    //请求权限并导出
    private void requestPermissionsAndExport() {
        // 检查是否已拥有权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 10 及以上版本，使用分区存储
            if (Environment.isExternalStorageManager()) {
                // 已授予管理所有文件的权限
                ExportUtils.exportHistoryToExcel(this,mViewModel.getLiveData_history().getValue());
            } else {
                // 请求管理所有文件的权限
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, MY_PERMISSIONS_REQUEST_MANAGE_STORAGE);
            }
        } else {
            // Android 10 以下版本，使用传统存储权限
            if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // 权限已授予，执行导出操作
                ExportUtils.exportHistoryToExcel(this,mViewModel.getLiveData_history().getValue());
            } else {
                // 请求权限
                EasyPermissions.requestPermissions(
                        this,
                        getString(R.string.toast_permission_write_storage),  // 权限被拒绝,  // 权限请求的解释说明
                        MY_PERMISSIONS_REQUEST_WRITE_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                );
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_PERMISSIONS_REQUEST_MANAGE_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // 权限已授予，执行导出操作
                    ExportUtils.exportHistoryToExcel(this,mViewModel.getLiveData_history().getValue());
                } else {
                    // 权限被拒绝，提示用户
                    Toast.makeText(this,
                            getString(R.string.toast_permission_write_storage_deny),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 使用 EasyPermissions 处理权限请求的结果
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        // 权限被授予
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_STORAGE) {
            ExportUtils.exportHistoryToExcel(this,mViewModel.getLiveData_history().getValue());  // 执行导出操作
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
    // 权限被拒绝
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_STORAGE) {
            Toast.makeText(this, getString(R.string.toast_permission_write_storage_deny), Toast.LENGTH_SHORT).show();
            // 如果用户永久拒绝了权限，可以提示用户手动开启权限
            if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                new AppSettingsDialog.Builder(this)
                        .setTitle(getString(R.string.permission_dialog_title))
                        .setRationale(getString(R.string.permission_dialog_rational_writeStorage))
                        .setPositiveButton(getString(R.string.permission_dialog_positive))
                        .setNegativeButton(getString(R.string.permission_dialog_negative))
                        .build()
                        .show();
            }
        }
    }
}