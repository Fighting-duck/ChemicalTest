package com.lsy.chemicaltest_new.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.activitys.smpleTest.ColorimetricActivity;
import com.lsy.chemicaltest_new.activitys.smpleTest.CurveManageActivity;
import com.lsy.chemicaltest_new.activitys.smpleTest.ElectricalTestActivity;
import com.lsy.chemicaltest_new.activitys.smpleTest.SamplesManageActivity;
import com.lsy.chemicaltest_new.activitys.smpleTest.ThermalActivity;
import com.lsy.chemicaltest_new.database.DataRepository;
import com.lsy.chemicaltest_new.databinding.FragmentSamplesTestBinding;
import com.lsy.chemicaltest_new.domain.ColoTestResult;
import com.lsy.chemicaltest_new.domain.ElecTestResult;
import com.lsy.chemicaltest_new.domain.Temperature_Elec;
import com.lsy.chemicaltest_new.domain.ThermalTestResult;
import com.lsy.chemicaltest_new.models.SampleTestViewModel;

public class SampleTestFragment extends Fragment {
    private static final String TAG = "SampleTestFragment";
    private FragmentSamplesTestBinding mBinding;
    private Context mContext;
    private SampleTestViewModel mViewModel;
    private  ComprehensiveTestResultFragment mFragment;
    ElecTestResult elecTestResult = null;
    Temperature_Elec temperatureElec = null;
    ColoTestResult coloTestResult = null;
    ThermalTestResult thermalTestResult = null;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        mBinding = FragmentSamplesTestBinding.inflate(inflater, container, false);
        mContext = getContext();

        return mBinding.getRoot();
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        createComprehensiveTestResultFragment();
        mViewModel = new ViewModelProvider(this).get(SampleTestViewModel.class);//在 Fragment 中初始化 ViewModel
        mViewModel.setContext(getContext());
        initUI();
    }


    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        updateResult();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        // 清理Fragment引用
        if (mFragment != null && mFragment.isAdded()) {
            try {
                getChildFragmentManager()
                        .beginTransaction()
                        .remove(mFragment)
                        .commitAllowingStateLoss();
            } catch (Exception e) {
                Log.w(TAG, "Error removing fragment", e);
            }
        }
        super.onDestroyView();
        mBinding = null;
    }

    public void createComprehensiveTestResultFragment(){
        // 检查容器是否存在
        View container = mBinding.getRoot().findViewById(R.id.fcv_ComprehensiveTestResultFragment);
        if (container == null) {
            Log.e(TAG, "Fragment container not found!");
            return;
        }
        //添加另一个布局
        mFragment = new ComprehensiveTestResultFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.fcv_ComprehensiveTestResultFragment, mFragment);
        // 不调用 addToBackStack(null);
        transaction.commit();
    }

    private void updateResult(){
        MyApplication.DB_EXECUTOR.execute(() -> {
            try {
                elecTestResult = DataRepository.getInstance().getElecTestResult();
                temperatureElec = DataRepository.getInstance().getTemperature_Elec();
                coloTestResult = DataRepository.getInstance().getColoTestResult();
                thermalTestResult = DataRepository.getInstance().getThermalTestResult();

                // 在主线程更新UI
                requireActivity().runOnUiThread(() -> {
                    mViewModel.setLiveData_history(elecTestResult, temperatureElec, coloTestResult, thermalTestResult);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error updating results", e);
                mViewModel.setToast(getString(R.string.toast_sample_refresh_fail));
            }
        });
    }
    @SuppressLint("SetTextI18n")
    private void initUI() {
        mBinding.llElectrical.setOnClickListener(this::onClick);
        mBinding.llColorimetric.setOnClickListener(this::onClick);
        mBinding.llThermal.setOnClickListener(this::onClick);
        mBinding.btnCurveManage.setOnClickListener(this::onClick);
        mBinding.btnSaveAll.setOnClickListener(this::onClick);
        mBinding.btnCleanAll.setOnClickListener(this::onClick);
        mBinding.btnSamplesManage.setOnClickListener(this::onClick);
        mBinding.btnCredibilityAnalysis.setOnClickListener(this::onClick);

        mViewModel.getLiveData_toast().observe(getViewLifecycleOwner(), toast -> {
            if (toast != null){
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
                mViewModel.setToast(null);
            }
        });
        mViewModel.getLiveData_history().observe(getViewLifecycleOwner(), history -> {
            if (history != null){
                Log.d(TAG, "new History:"+history);
                mFragment.updateData(history);
            }
        });
        mViewModel.getLiveData_saveState().observe(getViewLifecycleOwner(), state -> {
            Log.d(TAG, "State: " + state + ", hashCode: " + state.hashCode());
            switch (state){
                case LOADING:
                    mBinding.btnSaveAll.setClickable(false);
                    mBinding.pbSaveAll.setVisibility(View.VISIBLE);//加载progressBar
                    Log.d(TAG, "LOADING");
                    break;
                case SUCCESS:
                    mBinding.btnSaveAll.setClickable(true);
                    mBinding.pbSaveAll.setVisibility(View.GONE);//隐藏progressBar
                    Log.d(TAG, "SUCCESS");
                    mViewModel.setToast(getString(R.string.toast_add_success));
                    break;
                case EMPTY:
                case ERROR:
                    mBinding.btnSaveAll.setClickable(true);
                    mBinding.pbSaveAll.setVisibility(View.GONE);//隐藏progressBar
                    Log.d(TAG, "EMPTY or ERROR");
                    break;
            }
        });
        mViewModel.getLiveData_credibility().observe(getViewLifecycleOwner(), cred -> {
            if (cred == null)
                mBinding.tvCredibility.setText(getString(R.string.default_no));
            else
                mBinding.tvCredibility.setText(String.valueOf(cred));
        });
    }
    private void onClick(View view){
        if (view.getId() == mBinding.llElectrical.getId()){
            Intent intent = new Intent(mContext, ElectricalTestActivity.class);
            mContext.startActivity(intent);
        }
        else if(view.getId() == mBinding.llColorimetric.getId()){
            Intent intent = new Intent(mContext, ColorimetricActivity.class);
            mContext.startActivity(intent);
        }
        else if(view.getId() == mBinding.llThermal.getId()){
            Intent intent = new Intent(mContext, ThermalActivity.class);
            mContext.startActivity(intent);
        }
        else if(view.getId() == mBinding.btnCurveManage.getId()){
            Intent intent = new Intent(mContext, CurveManageActivity.class);
            mContext.startActivity(intent);
        }
        else if(view.getId() == mBinding.btnSamplesManage.getId()){
            Intent intent = new Intent(mContext, SamplesManageActivity.class);
            mContext.startActivity(intent);
        }
        else if (view.getId() == mBinding.btnCredibilityAnalysis.getId()){
            if (mViewModel.analyzeCredibility()==null){
                mViewModel.setToast(getString(R.string.toast_credibilityAnalysis_false));
            }
        }
        else if (view.getId() == mBinding.btnSaveAll.getId()) {
            if (elecTestResult == null && temperatureElec == null && coloTestResult == null && thermalTestResult == null){
                mViewModel.setToast(getString(R.string.toast_test_all_empty));
                return;
            }
            new AlertDialog.Builder(mContext)
                    .setTitle(getString(R.string.dialog_save_title))
                    .setMessage(getString(R.string.dialog_save_message))
                    .setPositiveButton(getString(R.string.dialog_positive), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            String description = mBinding.edtDescription.getText().toString();
                            // 保存所有数据
                            mViewModel.saveAll(mContext, description,
                                    elecTestResult, temperatureElec, coloTestResult, thermalTestResult);
                        }
                    })
                    .setNegativeButton(getString(R.string.dialog_negative), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(R.drawable.icon_notice)
                    .show();
        }
        else if (view.getId() == mBinding.btnCleanAll.getId()){
            clearAll();
            // 显示提示信息
            mViewModel.setToast(getString(R.string.toast_test_clear_success));
        }
    }
    public void clearAll(){
        //清空所有数据
        DataRepository.getInstance().clearAll();//清空数据仓库
        elecTestResult = null;
        temperatureElec = null;
        coloTestResult = null;
        thermalTestResult = null;

        mViewModel.setCredibility(null);
        mBinding.edtDescription.setText("");
        mViewModel.setHistoryMultiple(null);

        mFragment.restoreAll();
    }
}