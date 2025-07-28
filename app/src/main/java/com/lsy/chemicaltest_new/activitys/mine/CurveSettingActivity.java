package com.lsy.chemicaltest_new.activitys.mine;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.activitys.BaseActivity;
import com.lsy.chemicaltest_new.database.SharePreferencesManager;
import com.lsy.chemicaltest_new.databinding.ActivityCurveSettingBinding;
import com.lsy.chemicaltest_new.domain.CurveSetting;
import com.lsy.chemicaltest_new.models.CurveSettingViewModel;

import java.util.List;

public class CurveSettingActivity extends BaseActivity {
    public static final String TAG = "CurveSettingActivity";
    private ActivityCurveSettingBinding mBinding;
    public CurveSettingViewModel mViewModel;
    private Context mContext;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityCurveSettingBinding.inflate(getLayoutInflater());
        mContext = this;
        setContentView(mBinding.getRoot());
        mViewModel = new ViewModelProvider(this).get(CurveSettingViewModel.class);
        initUI();
    }

    private void initUI() {
        mBinding.ivSave.setOnClickListener(this::onCLick);
        mBinding.ivBack.setOnClickListener(this::onCLick);

        mViewModel.getLiveData_curveSetting().observe(this, curveSetting -> {
            if (curveSetting != null) {
                mBinding.edtXAxisUnit.setText(curveSetting.getX_axis_unit());
                mBinding.edtMinCO.setText(String.valueOf(curveSetting.getMin_CO()));
                mBinding.edtMaxCO.setText(String.valueOf(curveSetting.getMax_CO()));
                mBinding.edtMinCorr.setText(String.valueOf(curveSetting.getMinCorr()));
            }
        });
        mViewModel.getmLiveData_toast().observe(this, s -> {
            if (s != null) {
                Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
            }
        });

        List<CurveSetting> curveSetting = MyApplication.DATABASE_INSTANCE.getCurveSettingDao().findAll();
        if (!curveSetting.isEmpty())
            mViewModel.setCurveSetting(curveSetting.get(0));
    }

    private void onCLick(View view) {
        int id = view.getId();
        if (id == mBinding.ivSave.getId()) {
            Log.d(TAG, "onCLick: ");
            String xUnit = mBinding.edtXAxisUnit.getText().toString();
            String minCO = mBinding.edtMinCO.getText().toString();
            String maxCO = mBinding.edtMaxCO.getText().toString();
            String minCorr = mBinding.edtMinCorr.getText().toString();
            if (xUnit.isEmpty() || minCO.isEmpty() || maxCO.isEmpty() || minCorr.isEmpty()) {
                mViewModel.setToast(getString(R.string.toast_fillComplete));
                return;
            }
            Float f_minCO = Float.parseFloat(minCO);
            Float f_maxCO = Float.parseFloat(maxCO);
            float f_minCorr = Float.parseFloat(minCorr);
            if (f_minCO>f_maxCO) {
                mViewModel.setToast(getString(R.string.toast_curve_valueConflict));
                return;
            }
            if (f_minCorr<0){
                mViewModel.setToast(getString(R.string.toast_curve_limitMin));
                return;
            }
            CurveSetting curveSetting = new CurveSetting();
            curveSetting.setId(1);
            curveSetting.setX_axis_unit(xUnit);
            curveSetting.setMin_CO(f_minCO);
            curveSetting.setMax_CO(f_maxCO);
            curveSetting.setMinCorr(f_minCorr);
            mViewModel.save(curveSetting, new CurveSettingViewModel.SaveCurveSettingCallback() {
                @Override
                public void onSaveSuccess() {
                    mViewModel.setToast(getString(R.string.toast_update_success));
                }

                @Override
                public void onSaveFailed() {
                    mViewModel.setToast(getString(R.string.toast_update_fail));
                }
            });
        }
        else if (id == mBinding.ivBack.getId()) {
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}