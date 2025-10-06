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
import com.lsy.chemicaltest_new.interfaces.DeleteCallback;
import com.lsy.chemicaltest_new.models.HistoryPreviewViewModel;
import com.lsy.chemicaltest_new.utils.ExportUtils;
import com.lsy.chemicaltest_new.databinding.ActivityHistoryPreviewBinding;
import com.lsy.chemicaltest_new.utils.PermissionManager;
import com.lsy.chemicaltest_new.utils.StorageUtils;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class HistoryPreviewActivity extends BaseActivity{
    private static final String TAG = "HistoryPreviewActivity";
    private ActivityHistoryPreviewBinding mBinding;
    private Context mContext;
    private HistoryPreviewViewModel mViewModel;
    private ComprehensiveTestResultFragment mFragment;

    private PermissionManager mPermissionManager;


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
        // 初始化 PermissionManager
        mPermissionManager = new PermissionManager(
                this,
                new PermissionManager.PermissionCallback() {
                    @Override
                    public void onPermissionGranted() {
                        List<History_multiple> historyMultiples = new ArrayList<>();
                        historyMultiples.add(mViewModel.getHistory());
                        // 权限已授予，执行导出操作
                        ExportUtils.exportHistoriesToExcel(mContext,historyMultiples);
                    }
                    @Override
                    public void onPermissionDenied() {
                        // 权限被拒绝，可以在这里处理
                    }
                },
                R.string.permission_dialog_title,
                R.string.permission_dialog_rational_writeStorage
        );
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
                    mViewModel.deleteHistory(new DeleteCallback() {
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
            mPermissionManager.checkAndRequestExportPermissions();// 请求权限并导出
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