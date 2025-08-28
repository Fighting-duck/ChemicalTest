package com.lsy.chemicaltest_new.fragments;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.adapters.LimitedArrayAdapter;
import com.lsy.chemicaltest_new.databinding.FragmentSelectCurveBinding;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.models.SelectCurveViewModel;
import com.lsy.chemicaltest_new.utils.CombinedChartUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectCurveFragment extends DialogFragment {
    private String TAG = "SelectCurveFragment";
    private SelectCurveViewModel mViewModel;
    private Context mContext;
    private FragmentSelectCurveBinding mBinding;
    private LimitedArrayAdapter<String> mCurveNameAdapter;
    private ArrayAdapter<String> mCurveTypeAdapter;
    private OnFragmentChangeListener listener;

    private Integer mCurveType =  0;// 0:所有曲线  1:电信号曲线  2:比色曲线  3:光热曲线

    // 定义接口
    public interface OnFragmentChangeListener {
        void onSelectCurve(StandardCurve selectCurve);
    }

    // 设置监听器 监听曲线选择变化
    public void setOnSelectCurveListener(OnFragmentChangeListener listener) {
        this.listener = listener;
    }

    public SelectCurveFragment(){}

    public SelectCurveFragment(Integer curveType) {
        mCurveType = curveType;
    }

    public static SelectCurveFragment newInstance(Integer curveId) {
        SelectCurveFragment fragment = new SelectCurveFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("curveId", curveId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        mBinding = FragmentSelectCurveBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(this).get(SelectCurveViewModel.class);
        mContext = getContext();
        return mBinding.getRoot();
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        initUI();
    }
    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onDestroy");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    private void initUI() {
        // 给下拉框创建适配器
        List<String> curveNames = new ArrayList<>(); // 定义一个空的列表作为初始数据
        mCurveNameAdapter = new LimitedArrayAdapter<String>(mContext, R.layout.spinner_selected_item, curveNames,5);
        mCurveNameAdapter.setDropDownViewResource(R.layout.spinner_item);
        // 获取单个条目高度（这里简单假设条目高度为 50dp）
        int itemHeight = (int) (50 * getResources().getDisplayMetrics().density);
        mBinding.spCurveNames.setAdapter(mCurveNameAdapter);
        List<String> curveTypes = Arrays.asList(getResources().getStringArray(R.array.standardCurve_array));; //从资源文件中获取
        mCurveTypeAdapter = new ArrayAdapter<String>(mContext, R.layout.spinner_selected_item, curveTypes);
        mCurveTypeAdapter.setDropDownViewResource(R.layout.spinner_item);
        mBinding.spCurveType.setAdapter(mCurveTypeAdapter);
        mBinding.spCurveType.setSelection(mCurveType);
        //设置联合图表参数
        CombinedChartUtils.setChart(mBinding.ccChart);

        mViewModel.getLiveData_toast().observe(this, toast -> {
            if (toast != null){
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
                mViewModel.setToast(null);
            }
        });
        mViewModel.getLiveData_showCurves().observe(getViewLifecycleOwner(),standardCurves -> {
            List<String> curveNameList = new ArrayList<String>();
            for (StandardCurve curve : standardCurves){
                curveNameList.add(curve.getName());
            }
            // 更新数据
            mCurveNameAdapter.clear();
            mCurveNameAdapter.addAll(curveNameList);
            mCurveNameAdapter.notifyDataSetChanged();
        });
        mViewModel.getLiveData_selectCurve().observe(getViewLifecycleOwner(), standardCurve -> {
            CombinedChartUtils.buildChart(mContext,mBinding.ccChart,standardCurve.getPointList(), standardCurve.getType(),standardCurve.getX_axis_unit());
            mBinding.tvCorr.setText(String.valueOf(standardCurve.getCORR()));
            mBinding.tvFunction.setText(standardCurve.getFormula().toString());
        });

        mBinding.spCurveNames.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mViewModel.selectStandardCurve(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mBinding.spCurveType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mViewModel.updateShowCurves(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // 设置取消按钮点击事件
        mBinding.btnCancel.setOnClickListener(view -> dismiss());
        // 设置确定按钮点击事件
        mBinding.btnOk.setOnClickListener(view -> {
            listener.onSelectCurve(mViewModel.getShowCurves());
            dismiss();
        });
    }
}