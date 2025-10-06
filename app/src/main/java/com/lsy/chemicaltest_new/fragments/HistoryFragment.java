package com.lsy.chemicaltest_new.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.activitys.history.HistoryPreviewActivity;
import com.lsy.chemicaltest_new.adapters.HistoryAdapter;
import com.lsy.chemicaltest_new.database.DataRepository;
import com.lsy.chemicaltest_new.databinding.FragmentHistoryBinding;
import com.lsy.chemicaltest_new.domain.History_multiple;
import com.lsy.chemicaltest_new.implement.MultiHistoryDataImpl;
import com.lsy.chemicaltest_new.interfaces.DeleteCallback;
import com.lsy.chemicaltest_new.interfaces.UpdateCallback;
import com.lsy.chemicaltest_new.models.HistoryViewModel;
import com.lsy.chemicaltest_new.utils.ExportUtils;
import com.lsy.chemicaltest_new.utils.PermissionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HistoryFragment extends Fragment {
    private static final String TAG = "HistoryFragment";
    private FragmentHistoryBinding mBinding;
    private Context mContext;
    private HistoryViewModel mViewModel;
    private HistoryAdapter mHistoryAdapter;
    private ArrayAdapter<String> mSpinnerAdapter_data;
    private ArrayAdapter<String> mSpinnerAdapter_sample;
    private List<String> mDateList = new ArrayList<>();
    private List<String> mDSampleList = new ArrayList<>();
    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();//java线程池
    private PermissionManager mPermissionManager;
    private final Handler handler = new Handler();
    private final Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            String filter = mBinding.etSearchCurve.getText().toString();
            if (filter.isEmpty()){
                mViewModel.updateHistoriesByDateAndSample(new UpdateCallback() {
                    @Override
                    public void onUpdateSuccess() {
                        mViewModel.setToast(getString(R.string.toast_refresh_success));
                    }

                    @Override
                    public void onUpdateFailure(Exception e) {
                        e.printStackTrace();
                        mViewModel.setToast(getString(R.string.toast_refresh_fail));
                    }
                });
            }
            else mViewModel.updateHistories(filter);
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        //HistoryViewModel dashboardViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        mBinding = FragmentHistoryBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        mContext = getContext();
        initUI();
        return mBinding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if(MyApplication.INSTANCE.isUpdateHistory()){
            //初始化历史记录 和 日期列表
            mViewModel.updateHistoriesByDateAndSample(new UpdateCallback() {
                @Override
                public void onUpdateSuccess() {

                }

                @Override
                public void onUpdateFailure(Exception e) {
                    e.printStackTrace();
                    mViewModel.setToast(getString(R.string.toast_history_get_fail));
                }
            });
            mViewModel.updateUniqueDateList();
            MyApplication.INSTANCE.setUpdateHistory(false);
        }

        if (MyApplication.INSTANCE.isUpdateSample()){
            // 初始化样品列表
            mViewModel.updateSampleList();
            MyApplication.INSTANCE.setUpdateSample(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        mContext = null;
        cachedThreadPool.shutdown();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
        mBinding = null;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initUI() {
        // 初始化 PermissionManager
        mPermissionManager = new PermissionManager(
                getActivity(),
                new PermissionManager.PermissionCallback() {
                    @Override
                    public void onPermissionGranted() {
                        List<History_multiple> historyList = mHistoryAdapter.getSelectedItems();
                        if (!historyList.isEmpty()){
                            for (History_multiple history : mHistoryAdapter.getSelectedItems()){
                                //填充曲线数据 曲线名+曲线公式
                                MultiHistoryDataImpl.getInstance().fillPreviewHistory_curve(history, new UpdateCallback() {
                                    @Override
                                    public void onUpdateSuccess() {

                                    }

                                    @Override
                                    public void onUpdateFailure(Exception e) {

                                    }
                                });
                            }
                            // 权限已授予，执行导出操作
                            ExportUtils.exportHistoriesToExcel(mContext, mHistoryAdapter.getSelectedItems());
                        }
                    }
                    @Override
                    public void onPermissionDenied() {
                        // 权限被拒绝，可以在这里处理
                    }
                },
                R.string.permission_dialog_title,
                R.string.permission_dialog_rational_writeStorage
        );
        // 初始化历史记录列表
        mHistoryAdapter = new HistoryAdapter(mContext);
        mBinding.rvHistory.setLayoutManager(new LinearLayoutManager(mContext));
        mBinding.rvHistory.setAdapter(mHistoryAdapter);
        mHistoryAdapter.setEditClickListener(position -> {
            History_multiple history_multiple = mHistoryAdapter.getCurrentList().get(position);
            //跳转到历史预览界面
            Intent intent = new Intent(mContext, HistoryPreviewActivity.class);
            MultiHistoryDataImpl.getInstance().fillPreviewHistory_curve(history_multiple, new UpdateCallback() {
                @Override
                public void onUpdateSuccess() {
                    Log.d(TAG, "预览历史："+history_multiple.toString());
                    DataRepository.getInstance().setHistory_multiple(history_multiple);
                    mContext.startActivity(intent);
                }

                @Override
                public void onUpdateFailure(Exception e) {
                    mViewModel.setToast(getString(R.string.toast_curve_loadData_fail));
                }
            });
        });
        mHistoryAdapter.setOnMultiSelectListener((isMultiSelectMode,selectedCount) -> {
            if (isMultiSelectMode){
                Log.d(TAG+" MultiSelect", "打开多选模式");
                mBinding.llHistoryMultiSelect.setVisibility(View.VISIBLE);
                mBinding.tvBack.setVisibility(View.VISIBLE);
            }
            else {
                Log.d(TAG+" MultiSelect", "关闭多选模式");
                mBinding.llHistoryMultiSelect.setVisibility(View.GONE);
                mBinding.tvBack.setVisibility(View.GONE);
            }
        });
        //初始化时间下拉框列表   给下拉框创建适配器
        mSpinnerAdapter_data = new ArrayAdapter<String>(mContext, R.layout.spinner_selected_item, mDateList);
        mSpinnerAdapter_data.setDropDownViewResource(R.layout.spinner_item);
        mBinding.spinnerTime.setAdapter(mSpinnerAdapter_data);
        //初始化样品下拉框列表   给下拉框创建适配器
        mSpinnerAdapter_sample = new ArrayAdapter<String>(mContext, R.layout.spinner_selected_item, mDSampleList);
        mSpinnerAdapter_sample.setDropDownViewResource(R.layout.spinner_item);
        mBinding.spinnerSample.setAdapter(mSpinnerAdapter_sample);
        //设置下拉刷新布局的进度圆圈颜色
        mBinding.srlRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light,
                android.R.color.holo_orange_light, android.R.color.holo_green_light);
        //给refreshLayout设置下拉刷新监听器
        mBinding.srlRefreshLayout.setOnRefreshListener(() -> {
            mViewModel.updateHistoriesByDateAndSample(new UpdateCallback() {
                @Override
                public void onUpdateSuccess() {
                    mBinding.srlRefreshLayout.setRefreshing(false);
                    mViewModel.setToast(getString(R.string.toast_update_success));
                }

                @Override
                public void onUpdateFailure(Exception e) {
                    e.printStackTrace();
                    mViewModel.setToast(getString(R.string.toast_update_fail));
                }
            });
        });
        //给搜索框添加实时监听器
        // 时间列表加上监听器
        mBinding.spinnerTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String date = (String) parent.getItemAtPosition(position);
                Log.d(TAG, "onItemSelected: " + date);
                if (date.equals(getString(R.string.history_unlimitedTime))){
                    mViewModel.setCurrentDate(null);
                }
                else
                    mViewModel.setCurrentDate(date);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // 样品列表加上监听器
        mBinding.spinnerSample.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sampleName = (String) parent.getItemAtPosition(position);
                Log.d(TAG, "onItemSelected: " + sampleName);
                if (sampleName.equals(getString(R.string.history_unlimitedSamples)))
                    mViewModel.setCurrentSampleId(null);
                else{
                    Integer sampleId = mViewModel.getSampleIdByName(sampleName);
                    mViewModel.setCurrentSampleId(sampleId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //搜索框
        mBinding.etSearchCurve.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                handler.removeCallbacks(searchRunnable); // 移除之前的回调
                handler.postDelayed(searchRunnable, 300); // 延迟 300 毫秒触发
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        //排序类型
        mBinding.ivSortType.setOnClickListener(view -> {
            if (mViewModel.getIsAscend().getValue()!=null){
                if (mViewModel.getIsAscend().getValue()){//升序
                    mViewModel.setIsAscend(false);
                    mBinding.ivSortType.setImageResource(R.drawable.icon_down);
                }
                else{
                    mViewModel.setIsAscend(true);
                    mBinding.ivSortType.setImageResource(R.drawable.icon_up);
                }
            }
            else {
                mViewModel.setIsAscend(false);
                mBinding.ivSortType.setImageResource(R.drawable.icon_down);
            }
        });

        mViewModel.getLiveData_toast().observe(getViewLifecycleOwner(), toast -> {
            if (toast != null){
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
                mViewModel.setToast(null);
            }
        });
        mBinding.btnDelete.setOnClickListener(this::onClick);
        mBinding.btnExport.setOnClickListener(this::onClick);
        mBinding.tvBack.setOnClickListener(this::onClick);

        mViewModel.getLiveData_histories().observe(getViewLifecycleOwner(),history_multiples -> {
            if (history_multiples != null && !history_multiples.isEmpty()) {
                mBinding.tvHistoryTotalNUm.setText(String.valueOf(history_multiples.size()));//历史总数
                mBinding.emptyTextView.setVisibility(View.GONE);
                mBinding.rvHistory.setVisibility(View.VISIBLE);
                mHistoryAdapter.submitList(history_multiples);//列表显示数据
                mHistoryAdapter.notifyDataSetChanged();
            }
            else {
                Log.e(TAG, "No data available");
                mBinding.emptyTextView.setVisibility(View.VISIBLE);
                mBinding.rvHistory.setVisibility(View.GONE);
                mBinding.tvHistoryTotalNUm.setText(getString(R.string.default_number));
            }
        });
        mViewModel.getLiveData_uniqueDates().observe(getViewLifecycleOwner(),uniqueDates->{
            if (uniqueDates == null) return;
            // 清空旧数据并添加新数据
            mDateList.clear();
            mDateList.addAll(uniqueDates);
            mSpinnerAdapter_data.notifyDataSetChanged();
        });
        mViewModel.getLiveData_samples().observe(getViewLifecycleOwner(), samples -> {
            if (samples==null) return;
            // 清空旧数据并添加新数据
            mDSampleList.clear();
            mDSampleList.addAll(samples);
            mSpinnerAdapter_sample.notifyDataSetChanged();
        });
        mViewModel.getLiveData_currentDate().observe(getViewLifecycleOwner(), data -> {
            Log.d(TAG, "currentDate: " + data);
            mViewModel.updateHistoriesByDateAndSample(new UpdateCallback() {
                @Override
                public void onUpdateSuccess() {
                    //mViewModel.setToast(getString(R.string.toast_update_success));
                }

                @Override
                public void onUpdateFailure(Exception e) {
                    e.printStackTrace();
                    //mViewModel.setToast(getString(R.string.toast_update_fail));
                }
            });
        });
        mViewModel.getLiveData_currentSampleId().observe(getViewLifecycleOwner(), sample -> {
            Log.d(TAG, "currentSampleId: " + sample);
            mViewModel.updateHistoriesByDateAndSample(new UpdateCallback() {
                @Override
                public void onUpdateSuccess() {
                    //mViewModel.setToast(getString(R.string.toast_update_success));
                }

                @Override
                public void onUpdateFailure(Exception e) {
                    e.printStackTrace();
                    //mViewModel.setToast(getString(R.string.toast_update_fail));
                }
            });
        });
        mViewModel.getIsAscend().observe(getViewLifecycleOwner(), isAscend -> {
            if (isAscend == null) return;
            mViewModel.reverseHistory();
        });
    }

    private void onClick(View view) {
        int id = view.getId();
        if (id == mBinding.btnDelete.getId()) {
            //弹框提示是否删除
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(getString(R.string.dialog_deleteCurve_title));
            builder.setMessage(getString(R.string.dialog_deleteCurve_message));
            builder.setPositiveButton(getString(R.string.dialog_positive), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    List<History_multiple> deleteHistories = mHistoryAdapter.getSelectedItems();
                    mHistoryAdapter.deleteSelectedItems();
                    if (deleteHistories.isEmpty()) return;
                    mViewModel.deleteHistories(deleteHistories, new DeleteCallback() {
                        @Override
                        public void onDeleteSuccess() {
                            mViewModel.setToast(getString(R.string.toast_delete_success));
                        }

                        @Override
                        public void onDeleteFailure(Exception e) {
                            e.printStackTrace();
                            mViewModel.setToast(getString(R.string.toast_delete_fail));
                        }
                    });
                }
            });
            builder.setNegativeButton(getString(R.string.dialog_negative), null);
            builder.create().show();
        }
        else if (id == mBinding.btnExport.getId()){
            mPermissionManager.checkAndRequestExportPermissions();// 请求权限并导出
        }
        else if (id == mBinding.tvBack.getId()) {
            mHistoryAdapter.exitMultiSelectMode();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPermissionManager.handleActivityResult(requestCode);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPermissionManager.handleRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}