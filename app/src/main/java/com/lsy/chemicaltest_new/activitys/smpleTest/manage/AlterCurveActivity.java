package com.lsy.chemicaltest_new.activitys.smpleTest.manage;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;


import android.content.Context;
import android.os.Bundle;
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
import com.lsy.chemicaltest_new.utils.PermissionManager;

import java.util.ArrayList;
import java.util.List;

public class AlterCurveActivity extends BaseActivity {
    private static final String TAG = "AlterCurveActivity";

    private ActivityAlterCurveBinding mBinding;
    private Context mContext;
    private AlterCurveViewModel mViewModel;
    StandardCurveFragment mFragment;
    private PermissionManager mPermissionManager;
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
        // 初始化 PermissionManager
        mPermissionManager = new PermissionManager(
                this,
                new PermissionManager.PermissionCallback() {
                    @Override
                    public void onPermissionGranted() {
                        List<StandardCurve> curveList = new ArrayList<>();
                        curveList.add(mViewModel.getOldCurve());
                        // 权限已授予，执行导出操作
                        ExportUtils.exportCurvesToExcel(mContext, curveList);
                    }
                    @Override
                    public void onPermissionDenied() {
                        // 权限被拒绝，可以在这里处理
                    }
                },
                R.string.toast_permission_write_storage_deny,
                R.string.toast_permission_write_storage_deny,
                R.string.permission_dialog_title,
                R.string.permission_dialog_rational_writeStorage
        );
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
            mPermissionManager.checkAndRequestExportPermissions(mContext);// 请求权限并导出
        }
    }

}