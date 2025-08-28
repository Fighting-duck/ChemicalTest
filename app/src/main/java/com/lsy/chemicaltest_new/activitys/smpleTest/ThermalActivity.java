package com.lsy.chemicaltest_new.activitys.smpleTest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.activitys.BaseActivity;
import com.lsy.chemicaltest_new.database.DataRepository;
import com.lsy.chemicaltest_new.databinding.ActivityThermalBinding;
import com.lsy.chemicaltest_new.domain.BleDeviceInfo;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.domain.Temperature_Elec;
import com.lsy.chemicaltest_new.domain.TestValue;
import com.lsy.chemicaltest_new.domain.dialog.CurveDetailDialog;
import com.lsy.chemicaltest_new.fragments.ConnectMultimeterFragment;
import com.lsy.chemicaltest_new.fragments.SelectCurveFragment;
import com.lsy.chemicaltest_new.models.ThermalViewModel;
import com.lsy.chemicaltest_new.utils.PhotoUtil;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


public class ThermalActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks {
    private static final String TAG = "ThermalActivity";
    private static final String[] USB_PERMISSIONS = {Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_CODE_USB_PERMISSIONS = 100;
    private ActivityThermalBinding mBinding;
    private Context mContext;
    private ThermalViewModel mViewModel;
    private ActivityResultLauncher<Intent> bitmapResultLauncher;
    private ActivityResultLauncher<Intent> mMeasureValueActivityLauncher;
    private static final ConnectMultimeterFragment.ShowModel SHOW_MODEL_TEMPERATURE = ConnectMultimeterFragment.ShowModel.TEMPERATURE;//显示状态为温度模式


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mBinding = ActivityThermalBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mContext = this;
        mViewModel = new ViewModelProvider(this).get(ThermalViewModel.class);
        // 初始化Activity结果监听
        bitmapResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> handleBitmapResult(result.getResultCode(), result.getData())
        );
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
                                if (testValue != null)
                                    mViewModel.setTemperature(testValue.getValue());
                            }
                            if (data.hasExtra(MeasureValueByMultimeterActivity.RETURN_BLE_DEVICE_INFO)){
                                BleDeviceInfo bleDeviceInfo = data.getParcelableExtra(MeasureValueByMultimeterActivity.RETURN_BLE_DEVICE_INFO);
                                if (bleDeviceInfo != null)
                                    mViewModel.setBleDeviceInfo_Elec(bleDeviceInfo);
                            }
                        }
                    }
                }
        );
        initUI();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        restoreData();
    }
    //恢复数据
    private void restoreData() {
        Temperature_Elec temperature_elec = DataRepository.getInstance().getTemperature_Elec();
        if (temperature_elec!=null){
            BleDeviceInfo bleDeviceInfo = temperature_elec.getBleDeviceInfo();
            StandardCurve standardCurve = temperature_elec.getStandardCurve();
            Float temperature = temperature_elec.getTemperature();
            Float detectionCo = temperature_elec.getDetectionCo();
            String diseaseAnal = temperature_elec.getDiseaseAnal();
            if (bleDeviceInfo!=null)
                mViewModel.setBleDeviceInfo_Elec(bleDeviceInfo);
            if (standardCurve!=null)
                mViewModel.setStandardCurve(standardCurve);
            if (temperature!=null)
                mViewModel.setTemperature(temperature);
            if (detectionCo!=null)
                mViewModel.setCO(detectionCo);
            if (diseaseAnal!=null)
                mViewModel.setDiseaseAnal(diseaseAnal);
        }
    }

    // 处理返回的Bitmap结果
    private void handleBitmapResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            // 从Intent中提取缓存ID
            int bitmapId = data.getIntExtra("RETURN_BITMAP_ID", -1);

            // 从全局缓存获取Bitmap（并自动移除缓存）
            Bitmap returnedBitmap = DataRepository.getInstance().getAndRemoveBitmap(bitmapId);

            if (returnedBitmap != null) {
                mViewModel.setThermalBitmap(returnedBitmap);
            } else {
                mViewModel.setToast(getString(R.string.toast_thermal_getCrop_fail));
            }
        }
    }

    private void initUI() {
        // 监听器
        mBinding.ivBack.setOnClickListener(this::onCLick);
        mBinding.btnGetTemperatureFormMultimeter.setOnClickListener(this::onCLick);
        mBinding.ivTackPhoto.setOnClickListener(this::onCLick);
        mBinding.ivThermalImage.setOnClickListener(this::onCLick);
        mBinding.ivSave.setOnClickListener(this::onCLick);
        mBinding.btnSelectElecCurve.setOnClickListener(this::onCLick);
        mBinding.tvCurve.setOnClickListener(this::onCLick);
        mBinding.btnStartAnal.setOnClickListener(this::onCLick);
        mBinding.ivNotice.setOnClickListener(this::onCLick);

        mViewModel.getLiveData_toast().observe(this, toast -> {
            if (toast != null){
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
                mViewModel.setToast(null);
            }
        });
        mViewModel.getLiveData_Curve().observe(this, curve -> {
            if (curve == null) {
                mBinding.tvCurve.setText(R.string.default_no);
            }
            else {
                mBinding.tvCurve.setText(curve.getName());
            }
        });
        mViewModel.getLiveData_ThermalBitmap().observe(this,bitmap -> {
            if (bitmap== null) return;
            mBinding.ivThermalImage.setImageBitmap(bitmap);
        });
        mViewModel.getLiveData_CentralTemperature().observe(this, temperature -> {
            if(temperature == null) return;
            mBinding.tvCenterTemperature.setText(String.valueOf(temperature));
        });
        mViewModel.getLiveData_CO().observe(this,CO->{
            if(CO == null) {
                mBinding.tvCO.setText(getString(R.string.default_no));
                mBinding.btnStartAnal.setEnabled( false);
                mBinding.ivNotice.setVisibility(View.GONE);
            }
            else {
                mBinding.tvCO.setText(String.valueOf(CO));
                mBinding.btnStartAnal.setEnabled(true);
                mBinding.ivNotice.setVisibility(View.VISIBLE);
            }

        });
        mViewModel.getLiveData_diseaseAnal().observe(this,diseaseAnal ->{
            if(diseaseAnal == null) return;
            mBinding.tvDiseaseAnalysis.setText(diseaseAnal);
        });
        mViewModel.getLiveData_BleDeviceInfo().observe(this, bleDeviceInfo ->{
            if (bleDeviceInfo != null) {
                mBinding.tvCurrentGear.setText(bleDeviceInfo.getGear());
                mBinding.tvMileage.setText(bleDeviceInfo.getMileage());
            }

        });
        mViewModel.getLiveData_notice().observe(this, result -> {
            if (result != null){
                mBinding.ivNotice.setVisibility(View.VISIBLE);
                if (result.equals(getString(R.string.toast_normal))){
                    mBinding.ivNotice.setImageResource(R.drawable.icon_notice_normal);
                }
                else {
                    mBinding.ivNotice.setImageResource(R.drawable.icon_notice_abnormal);
                }
            }
        });
        mViewModel.getLiveData_ThermalTestResult().observe(this, testResult->{

        });
        mViewModel.getLiveData_Temperature_Elec().observe(this, testResult->{

        });
    }

    private void onCLick(View view) {
        int id = view.getId();
        if (id ==mBinding.ivBack.getId()){
            if (!mViewModel.isFinishTest()){
                mViewModel.clearAll();
            }
            finish();
        }
        else if (id ==mBinding.btnGetTemperatureFormMultimeter.getId()) {
            Intent intent = new Intent(this, MeasureValueByMultimeterActivity.class);
            intent.putExtra(MeasureValueByMultimeterActivity.GET_SHOW_MODE, SHOW_MODEL_TEMPERATURE);
            mMeasureValueActivityLauncher.launch(intent);
        }
        else if (id ==mBinding.btnSelectElecCurve.getId()) {
            showSelectDialog("select_curve");
        }
        else if (id==mBinding.tvCurve.getId()){
            if (mViewModel.getLiveData_Curve().getValue()!=null) {
                CurveDetailDialog curveDetailDialog = new CurveDetailDialog(mContext,mViewModel.getLiveData_Curve().getValue());
                curveDetailDialog.show();
            }
        }
        else if (id==mBinding.ivTackPhoto.getId()){
            checkAndRequestUSBPermissions();
        }
        else if (id==mBinding.ivThermalImage.getId()){
            BitmapDrawable drawable = ((BitmapDrawable)(mBinding.ivThermalImage).getDrawable());
            PhotoUtil.viewLargeImage(mContext,drawable);
        }
        else if (id==mBinding.ivSave.getId()){
            //保存
            if (mViewModel.save()) finish();
        }
        else if (id==mBinding.btnStartAnal.getId()){
            mViewModel.startDiseaseAnal();
        }
        else if (id==mBinding.ivNotice.getId()){
            if (mViewModel.getLiveData_notice().getValue()!=null)
                mViewModel.setToast(mViewModel.getLiveData_notice().getValue());
        }
    }
    private void checkAndRequestUSBPermissions() {
        if (EasyPermissions.hasPermissions(this, USB_PERMISSIONS)) {
            startTakeThermalPhotoActivity();
        } else {
            EasyPermissions.requestPermissions(this,
                    getString(R.string.toast_permission_usb),
                    REQUEST_CODE_USB_PERMISSIONS,
                    USB_PERMISSIONS);
        }
    }
    private void startTakeThermalPhotoActivity() {
        Intent intent = new Intent(this, TakeThermalPhotoActivity.class);
        bitmapResultLauncher.launch(intent);//启动发送方Activity
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
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
        SelectCurveFragment dialogFragment = new SelectCurveFragment(3);
        dialogFragment.setOnSelectCurveListener(new SelectCurveFragment.OnFragmentChangeListener() {
            @Override
            public void onSelectCurve(StandardCurve selectCurve) {
                mViewModel.setStandardCurve(selectCurve);
            }
        });
        return dialogFragment;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_CODE_USB_PERMISSIONS) {
            startTakeThermalPhotoActivity();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_CODE_USB_PERMISSIONS) {
            // 构建包含具体被拒绝权限的日志信息
            StringBuilder deniedPermissionsLog = new StringBuilder("用户拒绝了以下权限: ");
            for (String permission : perms) {
                deniedPermissionsLog.append(permission).append(", ");
            }
            if (deniedPermissionsLog.length() > 0) {
                // 移除最后多余的逗号和空格
                deniedPermissionsLog.delete(deniedPermissionsLog.length() - 2, deniedPermissionsLog.length());
            }
            Log.w(TAG, deniedPermissionsLog.toString());

            // 构建包含具体被拒绝权限的提示信息
            StringBuilder deniedPermissionsToast = new StringBuilder(getString(R.string.toast_permission_denyPermissions));
            for (String permission : perms) {
                deniedPermissionsToast.append(extractPermissionName(permission)).append(", ");
            }
            if (deniedPermissionsToast.length() > 0) {
                // 移除最后多余的逗号和空格
                deniedPermissionsToast.delete(deniedPermissionsToast.length() - 2, deniedPermissionsToast.length());
            }
            mViewModel.setToast(deniedPermissionsToast.toString());

            // 检查是否永久拒绝
            if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                new AppSettingsDialog.Builder(this)
                        .setTitle(getString(R.string.permission_dialog_title_needPermission))
                        .setRationale(getString(R.string.permission_dialog_toSetting))
                        .build()
                        .show();
            }
        }
    }
    /**
     * 从完整的权限字符串中提取权限名称
     * 例如将 android.permission.READ_EXTERNAL_STORAGE 转换为 READ_EXTERNAL_STORAGE
     */
    private String extractPermissionName(String fullPermission) {
        int lastDotIndex = fullPermission.lastIndexOf('.');
        if (lastDotIndex != -1 && lastDotIndex < fullPermission.length() - 1) {
            return fullPermission.substring(lastDotIndex + 1);
        }
        return fullPermission;
    }
}