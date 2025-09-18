package com.lsy.chemicaltest_new.activitys.smpleTest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Context;
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
import com.lsy.chemicaltest_new.databinding.ActivityAlterCurveBinding;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.fragments.StandardCurveFragment;
import com.lsy.chemicaltest_new.models.AlterCurveViewModel;
import com.lsy.chemicaltest_new.utils.ExportUtils;
import com.lsy.chemicaltest_new.utils.StorageUtils;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class AlterCurveActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks{
    private static final String TAG = "AlterCurveActivity";
    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 100;
    private static final int MY_PERMISSIONS_REQUEST_MANAGE_STORAGE = 10;
    private ActivityAlterCurveBinding mBinding;
    private Context mContext;
    private AlterCurveViewModel mViewModel;
    StandardCurveFragment mFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityAlterCurveBinding.inflate(getLayoutInflater());
        mContext = this;
        /*mViewModel = MyApplication.INSTANCE.getAlterCurveViewModel();*/
        mViewModel = new ViewModelProvider(this).get(AlterCurveViewModel.class);
        mViewModel.setContext(this);
        setContentView(mBinding.getRoot());

        //添加另一个布局
        mFragment = new StandardCurveFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fcv_CurveFragment, mFragment);
        transaction.commit();

        initUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        StandardCurve curve = DataRepository.getInstance().getStandardCurve();
        if (curve != null){
            mViewModel.setOldCurve(new StandardCurve(curve));// 保存旧曲线
            Log.d(TAG, "onResume oldCurve: " + curve);
            mFragment.fillCurve(curve,false);
        }
    }

    @Override
    protected void onDestroy() {
        mBinding = null; // 显式释放
        super.onDestroy();
    }

    private void initUI() {
        mViewModel.getLiveData_toast().observe(this,toast->{
            if (toast != null){
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
                mViewModel.setToast(null);
            }
        });
        mViewModel.getLiveData_alterState().observe(this,alterState -> {
            switch (alterState){
                case LOADING:
                    mBinding.ivSave.setEnabled(false);
                    mViewModel.setToast(getString(R.string.toast_curve_saving));
                    break;
                case SUCCESS:
                case EMPTY:
                case ERROR:
                    mBinding.ivSave.setEnabled(true);
                    break;
            }
        });
        mBinding.ivSave.setOnClickListener(this::onClick);
        mBinding.ivBack.setOnClickListener(this::onClick);
        mBinding.btnExport.setOnClickListener(this::onClick);
    }

    private void onClick(View view){
        if (view.getId() == mBinding.ivSave.getId()){
            StandardCurve newCurve = mFragment.getCurve();
            Log.d(TAG, "onClick: " + newCurve);
            if (newCurve != null){
                mViewModel.AlterStandardCurve(newCurve);
            }
        }
        else if (view.getId() == mBinding.ivBack.getId()){
            finish();
        }
        else if (view.getId() == mBinding.btnExport.getId()){
            if (StorageUtils.hasEnoughSpace(50)){
                requestPermissionsAndExport();
            }
            else
                Toast.makeText(mContext, getString(R.string.toast_insufficientSspace), Toast.LENGTH_SHORT).show();
        }
    }
    //请求权限并导出
    private void requestPermissionsAndExport() {
        // 检查是否已拥有权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 10 及以上版本，使用分区存储
            if (Environment.isExternalStorageManager()) {
                // 已授予管理所有文件的权限
                ExportUtils.exportCurve(this,mViewModel.getOldCurve());
            } else {
                // 请求管理所有文件的权限
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, MY_PERMISSIONS_REQUEST_MANAGE_STORAGE);
            }
        } else {
            // Android 10 以下版本，使用传统存储权限
            if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // 权限已授予，执行导出操作
                ExportUtils.exportCurve(this,mViewModel.getOldCurve());
            } else {
                // 请求权限
                EasyPermissions.requestPermissions(
                        this,
                        getString(R.string.toast_permission_write_storage),  // 权限请求的解释说明
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
                    ExportUtils.exportCurve(this,mViewModel.getOldCurve());
                } else {
                    // 权限被拒绝，提示用户
                    Toast.makeText(this, getString(R.string.toast_permission_write_storage_deny), Toast.LENGTH_SHORT).show();
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
            ExportUtils.exportCurve(this,mViewModel.getOldCurve());  // 执行导出操作
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