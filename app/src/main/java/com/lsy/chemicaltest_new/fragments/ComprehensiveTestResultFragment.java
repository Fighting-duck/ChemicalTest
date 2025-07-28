package com.lsy.chemicaltest_new.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.adapters.RecordAdapter;
import com.lsy.chemicaltest_new.databinding.FragmentComprehensiveTestResultBinding;
import com.lsy.chemicaltest_new.domain.BleDeviceInfo;
import com.lsy.chemicaltest_new.domain.ColoTestResult;
import com.lsy.chemicaltest_new.domain.ElecTestResult;
import com.lsy.chemicaltest_new.domain.Expression;
import com.lsy.chemicaltest_new.domain.HSV;
import com.lsy.chemicaltest_new.domain.History_multiple;
import com.lsy.chemicaltest_new.domain.Point;
import com.lsy.chemicaltest_new.domain.RGB;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.domain.Temperature_Elec;
import com.lsy.chemicaltest_new.domain.ThermalTestResult;
import com.lsy.chemicaltest_new.domain.dialog.CurveDetailDialog;
import com.lsy.chemicaltest_new.models.ComprehensiveTestResultViewModel;
import com.lsy.chemicaltest_new.utils.PhotoUtil;

import java.util.ArrayList;
import java.util.List;

public class ComprehensiveTestResultFragment extends Fragment {
    private final String TAG = "ComprehensiveTestResultFrag";
    private FragmentComprehensiveTestResultBinding mBinding;
    private Context mContext;
    private ComprehensiveTestResultViewModel mViewModel;
    private RecordAdapter mRecordAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        mBinding = FragmentComprehensiveTestResultBinding.inflate(inflater, container, false);
        mContext = getContext();
        initUI();
        return mBinding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ComprehensiveTestResultViewModel.class);
        mViewModel.setContext(getContext());
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle args = getArguments();
        if (args != null) {
            Object obj = args.getSerializable("history");
            if (obj instanceof History_multiple) {
                History_multiple history = (History_multiple) obj;
                mViewModel.setHistory(history);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // 不保存任何状态
        super.onSaveInstanceState(outState);
    }

    private void initUI() {

        mRecordAdapter = new RecordAdapter();
        mBinding.rvRecords.setLayoutManager(new LinearLayoutManager(mContext));
        mBinding.rvRecords.setAdapter(mRecordAdapter);

        mBinding.ivOriginalImage.setOnClickListener(this::onClick);
        mBinding.ivCropImage.setOnClickListener(this::onClick);
        mBinding.ivThermalImage.setOnClickListener(this::onClick);
        mBinding.tvStandardCurveElec.setOnClickListener(this::onClick);
        mBinding.tvStandardCurveThermal.setOnClickListener(this::onClick);
        mBinding.tvStandardCurveColo.setOnClickListener(this::onClick);
        mBinding.ivColoAbnormalJudgment.setOnClickListener(this::onClick);
        mBinding.ivElecAbnormalJudgment.setOnClickListener(this::onClick);
        mBinding.ivThermalAbnormalJudgment.setOnClickListener(this::onClick);

        mViewModel.getLiveData_toast().observe(getViewLifecycleOwner(), toast -> {
            if (toast != null){
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
                mViewModel.setToast(null);
            }
        });
        mViewModel.getLiveData_history().observe(getViewLifecycleOwner(), history -> {
            if (history==null) return;
            try {
                Log.d(TAG, "new History:"+history);
                //电信号检测
                setElecResult(history.getElecTestResult());
                //比色图像分析
                setColoResult(history.getColoTestResult());
                //热成像分析  万用表测温度分析
                setDegree(history.getThermalTestResult(),history.getTemperature_elec());
            }catch (Exception e){
                mViewModel.setToast(e.getMessage());
                Log.e(TAG, "Exception:", e);
            }
        });
        mViewModel.getLiveData_elec_isNormal().observe(getViewLifecycleOwner(), isNormal -> {
            if (isNormal==null) {
                mBinding.ivElecAbnormalJudgment.setVisibility(View.GONE);
                return;
            }
            mBinding.ivElecAbnormalJudgment.setVisibility(View.VISIBLE);
            if (isNormal){
                mBinding.ivElecAbnormalJudgment.setImageResource(R.drawable.icon_notice_normal);
            }
            else {
                mBinding.ivElecAbnormalJudgment.setImageResource(R.drawable.icon_notice_abnormal);
            }
        });
        mViewModel.getLiveData_colo_isNormal().observe(getViewLifecycleOwner(), isNormal -> {
            if (isNormal==null) {
                mBinding.ivColoAbnormalJudgment.setVisibility(View.GONE);
                return;
            }
            mBinding.ivColoAbnormalJudgment.setVisibility(View.VISIBLE);
            if (isNormal){
                mBinding.ivColoAbnormalJudgment.setImageResource(R.drawable.icon_notice_normal);
            }
            else {
                mBinding.ivColoAbnormalJudgment.setImageResource(R.drawable.icon_notice_abnormal);
            }
        });
        mViewModel.getLiveData_thermal_isNormal().observe(getViewLifecycleOwner(), isNormal -> {
            if (isNormal==null) {
                mBinding.ivThermalAbnormalJudgment.setVisibility(View.GONE);
                return;
            }
            mBinding.ivThermalAbnormalJudgment.setVisibility(View.VISIBLE);
            if (isNormal){
                mBinding.ivThermalAbnormalJudgment.setImageResource(R.drawable.icon_notice_normal);
            }
            else {
                mBinding.ivThermalAbnormalJudgment.setImageResource(R.drawable.icon_notice_abnormal);
            }
        });
    }
    // ============恢复默认状态==============
    public void restoreElecSection(){
        mBinding.llResultsElec.setVisibility(View.GONE);
        String defaultText = getString(R.string.default_no);
        String defaultText2 = getString(R.string.default_no_text);
        //电信号检测
        mBinding.tvStandardCurveElec.setClickable(false);
        mBinding.tvStandardCurveElec.setText(defaultText);
        mBinding.tvCOElecUnit.setText(defaultText);
        mBinding.tvDataTimeElec.setText(defaultText);
        mBinding.tvDeviceInfo.setText(defaultText);
        mBinding.tvElecUnit.setText(defaultText);
        mRecordAdapter.clear();
        mBinding.tvMaxValue4.setText(defaultText);
        mBinding.tvCOElec.setText(defaultText);
        mBinding.tvDiseaseAnalysisElectrical.setText(defaultText2);
    }
    public void restoreColoSection(){
        mBinding.llResultsColo.setVisibility(View.GONE);
        String defaultText = getString(R.string.default_no);
        String defaultText2 = getString(R.string.default_no_text);
        //比色图像分析
        mBinding.tvStandardCurveColo.setClickable(false);
        mBinding.tvStandardCurveColo.setText(defaultText);
        mBinding.tvCOColoUnit.setText(defaultText);
        mBinding.tvDataTimeColo.setText(defaultText);
        mBinding.ivOriginalImage.setImageResource(R.drawable.icon_no_image);
        mBinding.ivCropImage.setImageResource(R.drawable.icon_no_image);
        mBinding.tvCorrectedColor.setText(defaultText);
        mBinding.tvRed.setText(defaultText);
        mBinding.tvGreen.setText(defaultText);
        mBinding.tvBlue.setText(defaultText);
        mBinding.tvShowColor.setBackgroundColor(Color.TRANSPARENT);//背景设为透明
        mBinding.tvHue.setText(defaultText);
        mBinding.tvSaturation.setText(defaultText);
        mBinding.tvValue.setText(defaultText);
        mBinding.tvCOColo.setText(defaultText);
        mBinding.tvDiseaseAnalysisColorimetric.setText(defaultText2);
    }
    public void restoreThermalSection(){
        mBinding.llResultsThermal.setVisibility(View.GONE);
        String defaultText = getString(R.string.default_no);
        String defaultText2 = getString(R.string.default_no_text);
        //热成像分析
        mBinding.rlImageThermal.setVisibility(View.GONE);
        mBinding.llDeviceInfoDegree.setVisibility(View.VISIBLE);
        mBinding.tvStandardCurveThermal.setClickable(true);
        mBinding.tvStandardCurveThermal.setText(defaultText);
        mBinding.tvCOThermalUnit.setText(defaultText);
        mBinding.tvDataTimeThermal.setText(defaultText);
        mBinding.ivThermalImage.setImageResource(R.drawable.icon_no_image);
        mBinding.tvCenterTemperature.setText(defaultText);
        mBinding.tvCOThermal.setText(defaultText);
        mBinding.tvDiseaseAnalysisThermal.setText(defaultText2);
    }

    public void restoreAll() {
        mViewModel.setHistory(null);
        restoreElecSection();
        restoreColoSection();
        restoreThermalSection();
    }
    public String showBleDeviceInfo(BleDeviceInfo bleDeviceInfo){
        if (bleDeviceInfo!=null){
            return getString(R.string.text_gear_0)+"("+bleDeviceInfo.getGear()+")"+
                    "  "+getString(R.string.text_mileage_0)+"("+bleDeviceInfo.getMileage()+")";
        }
        else
            return null;
    }

    private void setElecResult(ElecTestResult elecTestResult){
        Log.d(TAG, "setElecResult:"+elecTestResult);
        if (elecTestResult!=null){
            mBinding.llResultsElec.setVisibility(View.VISIBLE);
            Float co = elecTestResult.getDetectionCo();
            StandardCurve standardCurve = elecTestResult.getStandardCurve();
            if (standardCurve!=null){
                mBinding.tvStandardCurveElec.setClickable(true);
                mBinding.tvStandardCurveElec.setText(standardCurve.getName());
                mBinding.tvCOElecUnit.setText(standardCurve.getX_axis_unit());
                Float min_CO = standardCurve.getMin_CO();
                Float max_CO = standardCurve.getMax_CO();
                mViewModel.isNormal(co,min_CO,max_CO,1);
            }
            if (elecTestResult.getDateTime()!=null)
                mBinding.tvDataTimeElec.setText(elecTestResult.getDateTime());
            if (elecTestResult.getBleDeviceInfo()!=null){

                mBinding.tvDeviceInfo.setText(
                        showBleDeviceInfo(elecTestResult.getBleDeviceInfo())
                );
                mBinding.tvElecUnit.setText(elecTestResult.getBleDeviceInfo().getUnit());
            }
            if (elecTestResult.getTestValueList()!=null)
                mRecordAdapter.update(elecTestResult.getTestValueList());
            if (elecTestResult.getMaxValue_4()!=null)
                mBinding.tvMaxValue4.setText(String.valueOf(elecTestResult.getMaxValue_4()));
            if (co!=null)
                mBinding.tvCOElec.setText(String.valueOf(co));
            if (elecTestResult.getDiseaseAnal()!=null)
                mBinding.tvDiseaseAnalysisElectrical.setText(elecTestResult.getDiseaseAnal());
        }
        else
            restoreElecSection();
    }
    private void setColoResult(ColoTestResult coloTestResult) {
        Log.d(TAG, "setColoResult:"+coloTestResult);
        if (coloTestResult!=null){
            mBinding.llResultsColo.setVisibility(View.VISIBLE);
            Float co = coloTestResult.getDetectionCo();
            StandardCurve standardCurve = coloTestResult.getStandardCurve();
            if (standardCurve!=null){
                mBinding.tvStandardCurveColo.setClickable(true);
                mBinding.tvStandardCurveColo.setText(standardCurve.getName());
                mBinding.tvCOColoUnit.setText(standardCurve.getX_axis_unit());
                Float min_CO = standardCurve.getMin_CO();
                Float max_CO = standardCurve.getMax_CO();
                mViewModel.isNormal(co,min_CO,max_CO,2);
            }
            if (coloTestResult.getDateTime()!=null)
                mBinding.tvDataTimeColo.setText(coloTestResult.getDateTime());
            Bitmap bitmap = coloTestResult.getOriginalImage();
            if (bitmap!=null && !bitmap.isRecycled()){
                mBinding.ivOriginalImage.setImageBitmap(bitmap);
            }
            else{
                mBinding.ivOriginalImage.setImageResource(R.drawable.icon_no_image);
            }
            Bitmap bitmap2 = coloTestResult.getCropImage();
            if (bitmap2!=null && !bitmap2.isRecycled()){
                mBinding.ivCropImage.setImageBitmap(bitmap2);
            }
            else{
                mBinding.ivCropImage.setImageResource(R.drawable.icon_no_image);
            }
            RGB rgb = coloTestResult.getRGB();
            if (rgb!=null){
                mBinding.tvCorrectedColor.setText(rgb.toHex());
                mBinding.tvRed.setText(String.valueOf(rgb.getRed()));
                mBinding.tvGreen.setText(String.valueOf(rgb.getGreen()));
                mBinding.tvBlue.setText(String.valueOf(rgb.getBlue()));
            }
            if (coloTestResult.getCorrectedColor()!=null)
                mBinding.tvShowColor.setBackgroundColor(coloTestResult.getCorrectedColor());
            HSV hsv = coloTestResult.getHsv();
            if (hsv!=null){
                mBinding.tvHue.setText(String.valueOf(hsv.getHue()));
                mBinding.tvSaturation.setText(String.valueOf(hsv.getSaturation()));
                mBinding.tvValue.setText(String.valueOf(hsv.getValue()));
            }
            if (coloTestResult.getDetectionCo()!=null)
                mBinding.tvCOColo.setText(String.valueOf(coloTestResult.getDetectionCo()));
            if (coloTestResult.getDiseaseAnal()!=null)
                mBinding.tvDiseaseAnalysisColorimetric.setText(coloTestResult.getDiseaseAnal());

        }
        else
            restoreColoSection();
    }
    private void setDegree(ThermalTestResult thermalTestResult,Temperature_Elec temperature_elec){
        Log.d(TAG, "setDegree:"+thermalTestResult);
        Log.d(TAG, "setDegree:"+temperature_elec);
        Float co = null;
        StandardCurve standardCurve = null;
        if (thermalTestResult!=null) {
            mBinding.llResultsThermal.setVisibility(View.VISIBLE);
            mBinding.llDeviceInfoDegree.setVisibility(View.GONE);
            mBinding.rlImageThermal.setVisibility(View.VISIBLE);
            co = thermalTestResult.getDetectionCo();
            standardCurve = thermalTestResult.getStandardCurve();
            if (standardCurve!=null){
                mBinding.tvStandardCurveThermal.setClickable(true);
                mBinding.tvStandardCurveThermal.setText(standardCurve.getName());
                mBinding.tvCOThermalUnit.setText(standardCurve.getX_axis_unit());
            }
            if (thermalTestResult.getDateTime()!=null)
                mBinding.tvDataTimeThermal.setText(thermalTestResult.getDateTime());
            if (thermalTestResult.getThermalBitmap()!=null){
                mBinding.ivThermalImage.setImageBitmap(thermalTestResult.getThermalBitmap());
            }
            if (thermalTestResult.getCentralTemperature()!=null)
                mBinding.tvCenterTemperature.setText(String.valueOf(thermalTestResult.getCentralTemperature()));
            if (co!=null)
                mBinding.tvCOThermal.setText(String.valueOf(co));
            if (thermalTestResult.getDiseaseAnal()!=null)
                mBinding.tvDiseaseAnalysisThermal.setText(thermalTestResult.getDiseaseAnal());
        }
        else if (temperature_elec!=null){
            mBinding.llResultsThermal.setVisibility(View.VISIBLE);
            mBinding.rlImageThermal.setVisibility(View.GONE);
            mBinding.llDeviceInfoDegree.setVisibility(View.VISIBLE);
            co = temperature_elec.getDetectionCo();
            standardCurve = temperature_elec.getStandardCurve();
            if (standardCurve!=null){
                mBinding.tvStandardCurveThermal.setClickable(true);
                mBinding.tvStandardCurveThermal.setText(standardCurve.getName());
                mBinding.tvCOThermalUnit.setText(standardCurve.getX_axis_unit());
            }
            if (temperature_elec.getDateTime()!=null)
                mBinding.tvDataTimeThermal.setText(temperature_elec.getDateTime());
            if (temperature_elec.getBleDeviceInfo()!=null)
                mBinding.tvDeviceInfoDegree.setText(showBleDeviceInfo(temperature_elec.getBleDeviceInfo()));
            if (temperature_elec.getTemperature()!=null)
                mBinding.tvCenterTemperature.setText(String.valueOf(temperature_elec.getTemperature()));
            if (co!=null)
                mBinding.tvCOThermal.setText(String.valueOf(co));
            if (temperature_elec.getDiseaseAnal()!=null)
                mBinding.tvDiseaseAnalysisThermal.setText(temperature_elec.getDiseaseAnal());
        }
        else
            restoreThermalSection();

        if (standardCurve!=null){
            Float min_CO = standardCurve.getMin_CO();
            Float max_CO = standardCurve.getMax_CO();
            mViewModel.isNormal(co,min_CO,max_CO,3);
        }
    }


    private void onClick(View view) {
        History_multiple history_multiple = mViewModel.getLiveData_history().getValue();
        if(view.getId() == mBinding.ivOriginalImage.getId()){
            if (history_multiple==null) {
                PhotoUtil.viewLargeImage(mContext,(Bitmap) null);
                return;
            }
            ColoTestResult coloTestResult = history_multiple.getColoTestResult();
            if (coloTestResult!=null){
                PhotoUtil.viewLargeImage(mContext,coloTestResult.getOriginalImage());
            }

        }
        else if(view.getId() == mBinding.ivCropImage.getId()){
            if (history_multiple==null) {
                PhotoUtil.viewLargeImage(mContext,(Bitmap) null);
                return;
            }
            ColoTestResult coloTestResult = history_multiple.getColoTestResult();
            if (coloTestResult!=null){
                PhotoUtil.viewLargeImage(mContext,coloTestResult.getCropImage());
            }
        }
        else if(view.getId() == mBinding.ivThermalImage.getId()){
            if (history_multiple==null) {
                PhotoUtil.viewLargeImage(mContext,(Bitmap) null);
                return;
            }
            ThermalTestResult thermalTestResult = history_multiple.getThermalTestResult();
            if (thermalTestResult!=null){
                PhotoUtil.viewLargeImage(mContext,thermalTestResult.getThermalBitmap());
            }
        }
        else if(view.getId() == mBinding.tvStandardCurveElec.getId()){
            if (history_multiple==null) {
                Log.e(TAG,"onClick: history_multiple is null");
                return;
            }
            ElecTestResult elecTestResult = history_multiple.getElecTestResult();
            if (elecTestResult.getStandardCurve()!=null){
                CurveDetailDialog curveDetailDialog = new CurveDetailDialog(mContext,elecTestResult.getStandardCurve());
                curveDetailDialog.show();
            }
            else mBinding.tvStandardCurveElec.setClickable(false);
        }
        else if(view.getId() == mBinding.tvStandardCurveColo.getId()){
            if (history_multiple==null) {
                Log.e(TAG,"onClick: history_multiple is null");
                return;
            }
            ColoTestResult coloTestResult = history_multiple.getColoTestResult();
            if (coloTestResult!=null){
                CurveDetailDialog curveDetailDialog = new CurveDetailDialog(mContext,coloTestResult.getStandardCurve());
                curveDetailDialog.show();
            }
            else mBinding.tvStandardCurveColo.setClickable(false);
        }
        else if(view.getId() == mBinding.tvStandardCurveThermal.getId()) {
            if (history_multiple==null) {
                Log.e(TAG,"onClick: history_multiple is null");
                return;
            }
            ThermalTestResult thermalTestResult = history_multiple.getThermalTestResult();
            Temperature_Elec temperature_elec = history_multiple.getTemperature_elec();
            if (thermalTestResult!=null){
                CurveDetailDialog curveDetailDialog = new CurveDetailDialog(mContext, thermalTestResult.getStandardCurve());
                curveDetailDialog.show();
            }
            else if (temperature_elec!=null){
                CurveDetailDialog curveDetailDialog = new CurveDetailDialog(mContext, temperature_elec.getStandardCurve());
                curveDetailDialog.show();
            }
            else mBinding.tvStandardCurveThermal.setClickable(false);
        }
        else if(view.getId() == mBinding.ivElecAbnormalJudgment.getId()) {
            mViewModel.toastIsNormal(1);
        }
        else if(view.getId() == mBinding.ivColoAbnormalJudgment.getId()) {
            mViewModel.toastIsNormal(2);
        }
        else if(view.getId() == mBinding.ivThermalAbnormalJudgment.getId()) {
            mViewModel.toastIsNormal(3);
        }

    }

    public void updateData(History_multiple history){
        if (history!=null){
            Log.d(TAG, "updateData: "+history);
            if (mViewModel!=null) mViewModel.setHistory(history);
        }

    }
}