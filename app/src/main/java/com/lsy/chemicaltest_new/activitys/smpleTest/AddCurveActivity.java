package com.lsy.chemicaltest_new.activitys.smpleTest;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.lsy.chemicaltest_new.R;


import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.lsy.chemicaltest_new.activitys.BaseActivity;
import com.lsy.chemicaltest_new.databinding.ActivityAddCurveBinding;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.fragments.StandardCurveFragment;
import com.lsy.chemicaltest_new.models.AddCurveViewModel;
import com.lsy.chemicaltest_new.models.CurveManageViewModel;
import com.lsy.chemicaltest_new.utils.ExportUtils;

import java.io.IOException;


public class AddCurveActivity extends BaseActivity {
    private static final String TAG = "AddCurveActivity";
    private ActivityAddCurveBinding mBinding;
    private Context mContext;
    private AddCurveViewModel mViewModel;
    private StandardCurveFragment mFragment;
    private ActivityResultLauncher<Intent> mFromAlumn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityAddCurveBinding.inflate(getLayoutInflater());
        mContext = this;
        setContentView(mBinding.getRoot());
        mViewModel = new ViewModelProvider(this).get(AddCurveViewModel.class);
        mViewModel.setContext(this);
        initUI();
        //添加另一个布局
        mFragment = new StandardCurveFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fcv_CurveFragment, mFragment);
        transaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mBinding = null;
        super.onDestroy();
    }

    @SuppressLint("SetTextI18n")
    private void initUI() {
        /**ViewModel**/
        mViewModel.getLiveData_toast().observe(this, toast -> {
            if (toast != null){
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
                mViewModel.setToast(null);
            }
        });
        mViewModel.getLiveData_saveState().observe(this, saveState -> {
            switch (saveState){
                case SUCCESS:
                    mBinding.ivSave.setEnabled(true);
                    finish();
                    break;
                case LOADING:
                    mBinding.ivSave.setEnabled(false);
                    mViewModel.setToast(getString(R.string.toast_curve_saving));
                    break;
                case ERROR:
                case EMPTY:
                    mBinding.ivSave.setEnabled(true);
                    break;
            }
        });
        //选择文件
        mFromAlumn = registerForActivityResult( new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                try {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedFileUri = result.getData().getData();
                        if (selectedFileUri != null) {
                            Log.d(TAG, "selectedFileUri: " + selectedFileUri.toString());
                            // 读取文件内容
                            try {
                                StandardCurve curve = ExportUtils.readCurveExcel(mContext,selectedFileUri);
                                Log.d(TAG, "curve: " + curve);
                                if (curve != null)
                                    mFragment.fillCurve(curve,true);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                    mViewModel.setToast(getString(R.string.toast_curve_accessFile_deny));
                    Log.e(TAG, "Import failed", e);
                } catch (Exception e) {
                    e.printStackTrace();
                    mViewModel.setToast(getString(R.string.toast_curve_fileFormat_error));
                    Log.e(TAG, "Import failed", e);
                }

            }
        });

        mBinding.ivSave.setOnClickListener(this::onClick);
        mBinding.ivBack.setOnClickListener(this::onClick);
        mBinding.btnImport.setOnClickListener(this::onClick);
    }

    private void onClick(View view){
        if (view.getId() == mBinding.ivSave.getId()){
            StandardCurve newCurve = mFragment.getCurve();
            Log.d(TAG, "onClick: " + newCurve);
            // 验证曲线
            if (newCurve == null) {
                mViewModel.setToast(getString(R.string.toast_curve_empty));
            }
            else if (newCurve.getSample()==null || newCurve.getSample_id()==null){
                mViewModel.setToast(getString(R.string.toast_curve_selectSample));
            }
            else if (newCurve.getName() == null){
                mViewModel.setToast(getString(R.string.toast_curve_inputCurveName));
            }
            else{
                // 保存曲线
                mViewModel.saveStandardCurve(newCurve);
            }
        }
        else if (view.getId() == mBinding.ivBack.getId()){
            finish();
        }
        else if (view.getId() == mBinding.btnImport.getId()){
            //选择文档（excel）
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            //指定文件扩展名过滤器
            //application/vnd.ms-excel 是旧版 Excel 文件（.xls）的 MIME 类型。 application/vnd.openxmlformats-officedocument.spreadsheetml.sheet 是新版 Excel 文件（.xlsx）的 MIME 类型。
            //intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"});
            mFromAlumn.launch(intent);
        }
    }
}