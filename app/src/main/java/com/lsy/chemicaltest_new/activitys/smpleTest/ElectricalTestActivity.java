package com.lsy.chemicaltest_new.activitys.smpleTest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.activitys.BaseActivity;
import com.lsy.chemicaltest_new.adapters.TestValueAdapter;
import com.lsy.chemicaltest_new.database.DataRepository;
import com.lsy.chemicaltest_new.databinding.ActivityElectricalTestBinding;
import com.lsy.chemicaltest_new.domain.BleDeviceInfo;
import com.lsy.chemicaltest_new.domain.ElecTestResult;
import com.lsy.chemicaltest_new.domain.TestValue;
import com.lsy.chemicaltest_new.domain.dialog.CurveDetailDialog;
import com.lsy.chemicaltest_new.fragments.ConnectMultimeterFragment;
import com.lsy.chemicaltest_new.fragments.SelectCurveFragment;
import com.lsy.chemicaltest_new.models.ElecViewModel;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ElectricalTestActivity extends BaseActivity{
    private static final String TAG = "ElectricalTestActivity";
    private ActivityElectricalTestBinding mBinding;
    private Context mContext;
    private ElecViewModel mViewModel;
    private TestValueAdapter mTestValueAdapter;
    private ActivityResultLauncher<Intent> mMeasureValueActivityLauncher;

    private static final ConnectMultimeterFragment.ShowModel SHOW_MODEL_ELEC = ConnectMultimeterFragment.ShowModel.ELEC;//显示状态为电信号模式

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityElectricalTestBinding.inflate(getLayoutInflater());
        mContext = this;
        setContentView(mBinding.getRoot());
        mViewModel = new ViewModelProvider(this).get(ElecViewModel.class);

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
                                if (testValue != null)
                                    mViewModel.setMaxValue(testValue);
                            }
                            if (data.hasExtra(MeasureValueByMultimeterActivity.RETURN_ELEC_VALUE_LIST)){
                                List<TestValue> testValueList = data.getParcelableArrayListExtra(MeasureValueByMultimeterActivity.RETURN_ELEC_VALUE_LIST);
                                if (testValueList != null)
                                    mViewModel.setTestValueList(testValueList);
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreData();
    }

    private void restoreData() {
        ElecTestResult elecTestResult = DataRepository.getInstance().getElecTestResult();
        if (elecTestResult!=null){
            mViewModel.setStandardCurve_Elec(elecTestResult.getStandardCurve());
            List<TestValue> testValueList = elecTestResult.getTestValueList();
            if (!testValueList.isEmpty()){
                mViewModel.setTestValueList(testValueList);
            }
            mViewModel.setDiseaseAnal_Elec(elecTestResult.getDiseaseAnal());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @SuppressLint("SetTextI18n")
    private void initUI() {
        //recycleView
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext,1,GridLayoutManager.HORIZONTAL, false);
        mTestValueAdapter = new TestValueAdapter();
        mBinding.rvTestValue.setLayoutManager(gridLayoutManager);
        mBinding.rvTestValue.setAdapter(mTestValueAdapter);
        DividerItemDecoration decoration = new DividerItemDecoration(mContext, DividerItemDecoration.HORIZONTAL);
        mBinding.rvTestValue.addItemDecoration(decoration);

        //设置监听器
        mBinding.ivBack.setOnClickListener(this::onCLick);
        mBinding.ivSave.setOnClickListener(this::onCLick);
        mBinding.btnGetCurrentFormMultimeter.setOnClickListener(this::onCLick);
        mBinding.btnStartAnalElec.setOnClickListener(this::onCLick);
        mBinding.btnSelectCurve.setOnClickListener(this::onCLick);
        mBinding.tvCurve.setOnClickListener(this::onCLick);
        mBinding.ivNotice.setOnClickListener(this::onCLick);

        //设置观察者
        mViewModel.getLiveData_toast().observe(this, toast -> {
            if (toast != null){
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
                mViewModel.setToast(null);
            }
        });
        mViewModel.getLiveData_StandardCurve().observe(this, curve -> {
            if (curve!= null){
                mBinding.tvCurve.setText(curve.getName());
            }
            else
                mBinding.tvCurve.setText(R.string.default_no);
        });
        //开始测试
        mViewModel.getLiveData_TestValueList().observe(this, values -> {
            if (values!=null && values.size()==14){
                mBinding.tvStartTime.setText(values.get(0).getTestTime());
                mBinding.tvEndTime.setText(values.get(13).getTestTime());
                mTestValueAdapter.update(values);
            }
        });
        // 最大值
        mViewModel.getLiveData_MaxValue().observe(this, maxValue -> {
            if (maxValue!=null){
                mBinding.tvMaxValue4.setText(maxValue.toString());
                mViewModel.calculateCO();//使用电流计算浓度
            }
            else
                mBinding.tvMaxValue4.setText(getString(R.string.default_no));
        });
        mViewModel.getLiveData_COElec().observe(this, CO -> {
            if (CO != null){
                String unit = mViewModel.getUnit_elec();
                mBinding.tvConcentration.setText(CO +" "+unit);
                mBinding.btnStartAnalElec.setEnabled(true);
            }
            else{
                mBinding.tvConcentration.setText(getString(R.string.default_no));
                mBinding.btnStartAnalElec.setEnabled(false);
                mBinding.ivNotice.setVisibility(View.GONE);
            }
        });
        mViewModel.getLiveData_DiseaseAnalElec().observe(this, result -> {
            if (result!= null)  // 病害分析结果不为空时，显示
                 mBinding.tvDiseaseAnalysisElec.setText(result);
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
        mViewModel.getLiveData_BleDeviceInfo().observe(this, bleDeviceInfo -> {
            if (bleDeviceInfo!=null){
                mBinding.tvCurrentGear.setText(bleDeviceInfo.getGear());
                mBinding.tvMileage.setText(bleDeviceInfo.getMileage());
            }
        });
        mViewModel.getLiveData_ElecTestResult().observe(this, elecTestResult -> {
            if (elecTestResult!=null)
                Log.d(TAG, "elecTestResult:"+elecTestResult.toString());
        });
        mViewModel.getLiveData_confidenceInterval().observe(this, confidenceInterval -> {
            if (confidenceInterval.length == 2){
                mBinding.tvConfidenceInterval.setText(Arrays.toString(confidenceInterval));
            }
        });
    }


    @SuppressLint("NewApi")
    private void onCLick(View view) {
        int id = view.getId();
        if (id ==mBinding.ivBack.getId()){
            finish();
            mViewModel.clearAll();
        }
        else if (id==mBinding.btnSelectCurve.getId()){
            showSelectDialog("select_curve_electrical");
        }
        else if (id==mBinding.tvCurve.getId()){
            if (mViewModel.getLiveData_StandardCurve().getValue()!=null) {
                CurveDetailDialog curveDetailDialog = new CurveDetailDialog(mContext,mViewModel.getLiveData_StandardCurve().getValue());
                curveDetailDialog.show();
            }
        }
        else if (id==mBinding.btnGetCurrentFormMultimeter.getId()){
            Intent intent = new Intent(this, MeasureValueByMultimeterActivity.class);
            intent.putExtra(MeasureValueByMultimeterActivity.GET_SHOW_MODE, SHOW_MODEL_ELEC);
            mMeasureValueActivityLauncher.launch(intent);
        }
        else if (id==mBinding.btnStartAnalElec.getId()){
            mViewModel.diseaseAnalElec();//病害分析
        }
        else if (id==mBinding.ivSave.getId()){
            //保存
            mViewModel.save();
            finish();
        }
        else if (id==mBinding.ivNotice.getId()){
            if (mViewModel.getLiveData_notice().getValue()!=null)
                mViewModel.setToast(mViewModel.getLiveData_notice().getValue());
        }
    }
    //  显示选择曲线对话框
    private void showSelectDialog(String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SelectCurveFragment existingFragment = (SelectCurveFragment) fragmentManager.findFragmentByTag(tag);
        if (existingFragment == null) {
            SelectCurveFragment dialogFragment = getSelectCurveFragment(tag);
            dialogFragment.show(fragmentManager, tag);
        }else {
            if (!existingFragment.isVisible()) {
                existingFragment.show(fragmentManager, tag);
            }
        }
    }
    // 获取选择曲线对话框
    @NonNull
    private SelectCurveFragment getSelectCurveFragment(String tag) {
        SelectCurveFragment dialogFragment;
        if(Objects.equals(tag, "select_curve_electrical"))
            dialogFragment = new SelectCurveFragment(1);
        else
            dialogFragment = new SelectCurveFragment(3);
        dialogFragment.setOnSelectCurveListener(selectCurve -> {
            if (Objects.equals(dialogFragment.getTag(), "select_curve_electrical"))
                mViewModel.setElecCurveAndCalculateCO(selectCurve);
        });
        return dialogFragment;
    }
}