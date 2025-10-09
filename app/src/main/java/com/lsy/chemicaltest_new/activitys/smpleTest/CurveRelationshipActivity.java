package com.lsy.chemicaltest_new.activitys.smpleTest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.databinding.ActivityCurveRelationshipBinding;
import com.lsy.chemicaltest_new.domain.Sample;
import com.lsy.chemicaltest_new.models.CurveRelationshipViewModel;

public class CurveRelationshipActivity extends AppCompatActivity {
    private static final String TAG = "CurveRelationshipActivity";
    private ActivityCurveRelationshipBinding mBinding;
    private CurveRelationshipViewModel mViewModel;
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityCurveRelationshipBinding.inflate(getLayoutInflater());
        mViewModel = new ViewModelProvider(this).get(CurveRelationshipViewModel.class);
        mContext = this;
        int sampleID = getIntent().getIntExtra("sampleID", -1);
        if (sampleID != -1){
            mViewModel.setSampleID(sampleID);
            Log.d(TAG, "onCreate: sampleID = " + sampleID);
        }
        setContentView(mBinding.getRoot());
        initUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @SuppressLint("DefaultLocale")
    private void initUI() {
        mViewModel.getLiveData_sampleID().observe(this, sampleID -> {
            if (sampleID == null) return;
            Sample sample = MyApplication.DATABASE_INSTANCE.getSampleDao().findById(sampleID);
            if (sample != null){
                mBinding.tvSampleName.setText(sample.getName());
            }
        });
        mViewModel.getLiveData_sensorData().observe(this, sensorDataList -> {
            if (sensorDataList == null) return;
            Log.d(TAG, "initUI: sensorDataList.size() = " + sensorDataList.size());
            Log.d(TAG, "initUI: sensorDataList = " + sensorDataList);
        });
        mViewModel.getLiveData_predictor().observe(this, predictor -> {
            if (predictor == null) return;
            Log.d(TAG, "initUI: predictor = " + predictor);
            double[] coefficients = predictor.getCoefficients();
            //if (predictor.isModelValid() && coefficients != null && coefficients.length >= 3) {
            if (coefficients != null && coefficients.length >= 3) {
                mBinding.tvB0.setText(String.format("B0: %.2f", coefficients[0]));
                mBinding.tvB1.setText(String.format("B1: %.2f", coefficients[1]));
                mBinding.tvB2.setText(String.format("B2: %.2f", coefficients[2]));
            }
        });
    }
}