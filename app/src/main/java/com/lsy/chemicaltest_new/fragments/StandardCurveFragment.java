package com.lsy.chemicaltest_new.fragments;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.CombinedData;
import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.activitys.smpleTest.MeasureValueByMultimeterActivity;
import com.lsy.chemicaltest_new.activitys.smpleTest.SamplesManageActivity;
import com.lsy.chemicaltest_new.adapters.PointListAdapter;
import com.lsy.chemicaltest_new.adapters.PointsAdapter;
import com.lsy.chemicaltest_new.database.DataRepository;
import com.lsy.chemicaltest_new.databinding.FragmentStandardCurveBinding;
import com.lsy.chemicaltest_new.domain.CurveSetting;
import com.lsy.chemicaltest_new.domain.Expression;
import com.lsy.chemicaltest_new.domain.RGB;
import com.lsy.chemicaltest_new.domain.Sample;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.domain.TestValue;
import com.lsy.chemicaltest_new.domain.dialog.PhotoPickerBottomSheet;
import com.lsy.chemicaltest_new.models.StandardCurveViewModel;
import com.lsy.chemicaltest_new.utils.CombinedChartUtils;
import com.lsy.chemicaltest_new.utils.ImageProcessor;
import com.lsy.chemicaltest_new.utils.NumberUtils;
import com.lsy.chemicaltest_new.utils.PhotoUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class StandardCurveFragment extends Fragment {
    private static final String TAG = "StandardCurveFragment";
    private FragmentStandardCurveBinding mBinding;
    private Context mContext;
    private StandardCurveViewModel mViewModel;
    private ArrayAdapter<String> mSpinner_TypeAdapter;//曲线类型列表适配器
    private ArrayAdapter<String> mSpinner_SampleAdapter;//样本列表适配器
    private PointsAdapter mPointsAdapter;//(x,y)点列表RecyclerView适配器
    private PointListAdapter mPointListAdapter;//图表中点列表适配器
    private CombinedData mCombinedData;//联合图数据
    private Boolean mIsAutoCalculate = true;//是否自动计算
    private CurveSetting mCurveSetting;//曲线设置
    private ImageProcessor mImageProcessor;
    private ActivityResultLauncher<Intent> mMeasureValueActivityLauncher;
    private ActivityResultLauncher<Intent> bitmapResultLauncher;
    private CombinedChartUtils.LinearRegressionResult mRegressionResult;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageProcessor = new ImageProcessor(getActivity());
        /**ActivityLauncher**/
        mMeasureValueActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // 3. 处理返回结果：result是SecondActivity关闭后返回的数据
                    if (result.getResultCode() == RESULT_OK) { // 确保结果正常返回
                        Intent data = result.getData();
                        if (data != null) {
                            //判断是否包含key "result_float"
                            if (data.hasExtra(MeasureValueByMultimeterActivity.RETURN_TEST_VALUE)){
                                // 从Intent中获取float数据（key为"result_float"，与SecondActivity对应）
                                TestValue testValue = data.getParcelableExtra(MeasureValueByMultimeterActivity.RETURN_TEST_VALUE);
                                if (testValue != null){
                                    String prompt = "";
                                    if (Objects.equals(testValue.getUnit(), getString(R.string.unit_degree)))
                                         prompt = getString(R.string.curve_GetTemperature);
                                    else prompt = getString(R.string.curve_GetCurrent);
                                    controlResultFromActivity(testValue.getValue(),prompt);
                                }
                            }
                        }
                    }
                }
        );
        // 初始化Activity结果监听 处理图片裁剪返回结果
        bitmapResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> mImageProcessor.handleActivityResult(result.getResultCode(), result.getData())
        );
    }
    /**
     * 对万用表测量值 界面 或 拍照/图库 截取图片界面 传过来的电流、温度、b值进行处理
     * @param floatResult 测量值
     */
    public void controlResultFromActivity(float floatResult,String prompt){
        mPointsAdapter.alter_YValue(3,(double)floatResult);

        mViewModel.setToast(prompt+"："+floatResult);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentStandardCurveBinding.inflate(getLayoutInflater());
        mContext = getContext();
        mViewModel = new ViewModelProvider(this).get(StandardCurveViewModel.class);
        //mViewModel.setContext(mContext);
        return mBinding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        initUI();
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void initData() {
        // 检查 HashMap 是否为空
        if (!StandardCurve.TYPE.isEmpty()) {
            Map.Entry<Integer,String > entry = StandardCurve.TYPE.entrySet().iterator().next();
            mViewModel.setType(entry.getKey()); // 获取第一个 key
        }
        //获取标准曲线默认资源
        List<CurveSetting> curveSetting = MyApplication.DATABASE_INSTANCE.getCurveSettingDao().findAll();
        if (!curveSetting.isEmpty()){
            mCurveSetting = curveSetting.get(0);
            Log.d(TAG, "curveSetting is not empty:"+mCurveSetting);
            mViewModel.set_curve_xUnit(mCurveSetting.getX_axis_unit());
            mViewModel.set_curve_xMin(mCurveSetting.getMin_CO());
            mViewModel.set_curve_xMax(mCurveSetting.getMax_CO());
            mViewModel.set_curve_minCORR(mCurveSetting.getMinCorr());
        }

        // 设置初始值
        mViewModel.set_curve_yUnit(getResources().getString(R.string.unit_mA));

        // 更新样本列表
        mViewModel.updateSampleList();
    }

    @SuppressLint("SetTextI18n")
    private void initUI() {
        /**mSpinner_TypeAdapter**/
        String[] typeList = getResources().getStringArray(R.array.standardCurve_type);
        mSpinner_TypeAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item,typeList);
        mSpinner_TypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.spTypeList.setAdapter(mSpinner_TypeAdapter);
        /***mSpinner_SampleAdapter***/
        List<String> sampleList = new ArrayList<>();
        mSpinner_SampleAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item,sampleList);
        mSpinner_SampleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.spSampleList.setAdapter(mSpinner_SampleAdapter);
        /**CombinedChart**/
        CombinedChartUtils.setChart(mBinding.ccChart);
        mCombinedData = new CombinedData();// 初始化 CombinedData 对象
        // 设置联合图表数据到 CombinedChart
        mBinding.ccChart.setData(mCombinedData);

        /**RecycleView**/
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext,2,GridLayoutManager.HORIZONTAL, false);
        mPointsAdapter = new PointsAdapter(mContext, mViewModel,2);
        mBinding.rvPoints.setLayoutManager(gridLayoutManager);
        mBinding.rvPoints.setAdapter(mPointsAdapter);

        GridLayoutManager gridLayoutManager_2 = new GridLayoutManager(mContext,1,GridLayoutManager.HORIZONTAL, false);
        mPointListAdapter = new PointListAdapter(mContext, mViewModel);
        mBinding.rvPointList.setLayoutManager(gridLayoutManager_2);
        mBinding.rvPointList.setAdapter(mPointListAdapter);
        DividerItemDecoration decoration = new DividerItemDecoration(mContext, DividerItemDecoration.HORIZONTAL);
        mBinding.rvPointList.addItemDecoration(decoration);

        /**Listener**/
        mBinding.edtYNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                String str_yNumber = s.toString();
                if (!str_yNumber.isEmpty()) {
                    mPointsAdapter.alter_YNum(Integer.parseInt(str_yNumber));
                }
            }
        });
        mBinding.edtDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                String description = s.toString();
                mViewModel.setDescription(description);
            }
        });
        mBinding.edtName.setOnFocusChangeListener(focusListener);
        mBinding.edtYNumber.setOnFocusChangeListener(focusListener);
        mBinding.edtXUnit.setOnFocusChangeListener(focusListener);
        mBinding.edtYUnit.setOnFocusChangeListener(focusListener);
        mBinding.edtMinX.setOnFocusChangeListener(focusListener);
        mBinding.edtMaxX.setOnFocusChangeListener(focusListener);
        mBinding.edtMinCORR.setOnFocusChangeListener(focusListener);
        mBinding.btnPointAdd.setOnClickListener(this::onClick);
        mBinding.btnSampleAdd.setOnClickListener(this::onClick);
        mBinding.btnVerifyStandardSample.setOnClickListener(this::onClick);
        mBinding.ivNoticeCorr.setOnClickListener(this::onClick);
        mBinding.edtY.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                String str_y = s.toString();

                // 只有一个负号，设置为默认值
                if (str_y.equals("-")) {
                    mBinding.tvX.setText(""); // 清空之前的计算结果
                    mBinding.tvLgX2.setText("");
                    mBinding.tvConfidenceInterval.setText("");
                    return;
                }

                if (!str_y.isEmpty()) {
                    Float y = Float.parseFloat(str_y);
                    Float result = mViewModel.calculateCo_toY(y);//x
                    Log.d(TAG, "y:"+str_y+" x:"+result);
                    if (result != null && mRegressionResult != null){
                        mBinding.tvX.setText(String.valueOf(NumberUtils.roundCurve_lgX_avgY(result)));
                        mBinding.tvLgX2.setText(String.valueOf(NumberUtils.roundCurve_lgX_avgY((float) Math.log10(result))));
                        float[] x_interval = mRegressionResult.inversePredictInterval(y);//[x1,x2]
                        mBinding.tvConfidenceInterval.setText("["+
                                NumberUtils.roundCurve_lgX_avgY(x_interval[0])+
                                " ， "+
                                NumberUtils.roundCurve_lgX_avgY(x_interval[1])+
                                "]");
                    }
                    else
                        mBinding.tvX.setText("");
                }else mBinding.tvX.setText("");
            }
        });
        mBinding.spTypeList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //String selectedItem = (String) parent.getItemAtPosition(position);
                // 处理用户选择的选项
                mViewModel.setType(position+1);
                Log.d(TAG, "sample type: " + position+1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mBinding.spSampleList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                // 处理用户选择的选项
                mViewModel.setCurveSample(position);
                Log.d(TAG, "sample name: "+ position+ "," + selectedItem);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 用户未选择任何选项时的处理
            }
        });

        /**ViewModel**/
        mViewModel.getLiveData_toast().observe(getViewLifecycleOwner(), toast -> {
            if (toast != null){
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
                mViewModel.setToast(null);
            }
        });
        //注册观察者,注意这个必须得注册，否则ViewModel中的MediatorLiveData就不处于onActive()状态。
        mViewModel.getMediatorLiveData_Curve().observe(getViewLifecycleOwner(), standardCurve -> {
            if (standardCurve == null) return;
            Log.d(TAG, "current standardCurve:"+standardCurve.toString());
        });
        mViewModel.getLiveData_sampleList().observe(getViewLifecycleOwner(), samples -> {
            if (samples == null) return;
            // 获取样本名称列表并更新 Spinner 列表
            List<String> sampleNameList = new ArrayList<>();
            for (Sample sample : samples) {
                sampleNameList.add(sample.getName());
            }
            mSpinner_SampleAdapter.clear(); // 清空旧数据
            mSpinner_SampleAdapter.addAll(sampleNameList); // 添加新数据
            mSpinner_SampleAdapter.notifyDataSetChanged(); // 通知适配器数据已更新
        });
        mViewModel.getLiveData_sample().observe(getViewLifecycleOwner(), samplePosition -> {
            if (samplePosition == null) return;
            // 填充曲线
            mBinding.spSampleList.setSelection(Math.max(0, samplePosition));// 确保 selection 不会小于 0
        });
        mViewModel.getLiveData_curveType().observe(this, curveType -> {
            if(curveType == null) return;
            mBinding.spTypeList.setSelection(Math.max(0, curveType - 1));// 确保 selection 不会小于 0
            //设置获取标样数据按钮的文字
            switch (curveType){
                case 1:
                    mBinding.btnVerifyStandardSample.setText(getString(R.string.curve_GetCurrent));
                    break;
                case 2:
                    mBinding.btnVerifyStandardSample.setText(getString(R.string.curve_GetB));
                    break;
                case 3:
                    mBinding.btnVerifyStandardSample.setText(getString(R.string.curve_GetTemperature));
                    break;
            }
        });
        mViewModel.getLiveData_curveName().observe(getViewLifecycleOwner(), curveName ->{
            if (curveName == null || curveName.isEmpty()) {
                mViewModel.setToast(getString(R.string.toast_curve_nameEmpty));
                mViewModel.restoreCurveName();
                return;
            }
            Log.d(TAG, "current curveName:"+curveName);
            mBinding.edtName.setText(curveName);
        });
        mViewModel.getLiveData_xUnit().observe(getViewLifecycleOwner(), xUnit -> {
            if (xUnit == null || xUnit.isEmpty()) {
                mViewModel.setToast(getString(R.string.toast_curve_notEmpty));
                mViewModel.restoreXUnit();
                return;
            }
            mBinding.edtXUnit.setText(xUnit);
            /**描述**/
            Description description = mBinding.ccChart.getDescription();
            description.setEnabled(true);//是否可用
            description.setText(getString(R.string.unit_lg_c) + xUnit);
            description.setTextColor(Color.BLACK);//字体颜色
            description.setTextSize(12f);//字体大小
            mBinding.ccChart.notifyDataSetChanged();
            mBinding.ccChart.invalidate();// 刷新图表
        });
        //设置y轴标签
        mViewModel.getLiveData_yUnit().observe(getViewLifecycleOwner(), yUnit -> {
            if (yUnit == null || yUnit.isEmpty()) {
                mViewModel.setToast(getString(R.string.toast_curve_notEmpty));
                mViewModel.restoreYUnit();
                return;
            }
            mBinding.edtYUnit.setText(yUnit);
        });
        mViewModel.getLiveData_xMin().observe(getViewLifecycleOwner(), xMin -> {
            if(xMin == null) {
                mViewModel.setToast("");
                mViewModel.restoreXMin();
                return;
            }
            mBinding.edtMinX.setText(String.valueOf(xMin));
        });
        mViewModel.getLiveData_xMax().observe(getViewLifecycleOwner(), xMax -> {
            if(xMax == null) {
                mViewModel.setToast(getString(R.string.toast_curve_notEmpty));
                mViewModel.restoreXMax();
                return;
            }
            mBinding.edtMaxX.setText(String.valueOf(xMax));
        });
        mViewModel.getLiveData_minCORR().observe(getViewLifecycleOwner(), CORR -> {
            if(CORR == null) {
                mViewModel.setToast(getString(R.string.toast_curve_notEmpty));
                mViewModel.restoreMinCORR();
                return;
            }
            mViewModel.noticeCorr();
            mBinding.edtMinCORR.setText(String.valueOf(CORR));
        });
        mViewModel.getLiveData_Expression().observe(getViewLifecycleOwner(), expression ->{
            if (expression == null)
                mBinding.tvExpression.setText("");
            else
                mBinding.tvExpression.setText(expression.toString());//显示曲线方程
        });
        mViewModel.getLiveData_CORR().observe(getViewLifecycleOwner(), corr ->{
            if(corr == null)
                mBinding.tvCorr.setText("");
           else {
                mBinding.tvCorr.setText(corr+" %");//显示相关系数
                mViewModel.noticeCorr();
            }
        });
        mViewModel.getLiveData_pointList().observe(getViewLifecycleOwner(), pointList -> {
            String x_unit = getString(R.string.unit_lg_c) + mViewModel.getLiveData_xUnit().getValue();//x轴单位
            mRegressionResult = CombinedChartUtils.buildChart(mContext, mBinding.ccChart,pointList, mViewModel.getCurveType(), x_unit);
            Log.d(TAG, "regressionResult:"+mRegressionResult.toString());
            if (pointList.size()>=2 && mIsAutoCalculate){
                mViewModel.set_curve_CORR(NumberUtils.roundCurve_k_b_r(mRegressionResult.correlationCoefficient()*100));//设置相关系数
                mViewModel.setCurve_Mse(NumberUtils.roundCurve_k_b_r(mRegressionResult.getMse()));
                mViewModel.setExpression(new Expression(NumberUtils.roundCurve_k_b_r(mRegressionResult.getK()),
                        NumberUtils.roundCurve_k_b_r(mRegressionResult.getB())));//设置曲线方程
            }
            mPointListAdapter.update(pointList);
        });
        mViewModel.getLiveData_entry().observe(getViewLifecycleOwner(), point -> {
            if (point == null) return;
            mBinding.tvXAverage.setText(String.valueOf(point.getX_value()));
            mBinding.tvYAverage.setText(String.valueOf(point.getY_value()));
        });
        mViewModel.getLiveData_description().observe(getViewLifecycleOwner(), description -> {
            if (description == null) return;
            //通过判断新值与当前 EditText 文本是否相同来避免循环
            String currentText = mBinding.edtDescription.getText().toString();
            if (!currentText.equals(description)) {
                mBinding.edtDescription.setText(description);
            }
        });
        mViewModel.getLiveData_CO_noticeCorr().observe(getViewLifecycleOwner(), noticeCorr -> {
            if (noticeCorr != null){
                mBinding.ivNoticeCorr.setVisibility(View.VISIBLE);
                if (noticeCorr.equals(getString(R.string.toast_normal))){
                    mBinding.ivNoticeCorr.setImageResource(R.drawable.icon_notice_normal);
                }
                else {
                    mBinding.ivNoticeCorr.setImageResource(R.drawable.icon_notice_abnormal);
                }
            }
        });
        mViewModel.getLiveData_mse().observe(getViewLifecycleOwner(), mse -> {
            if (mse == null)
                mBinding.tvMse.setText(getString(R.string.default_no));
            else
                mBinding.tvMse.setText(String.valueOf(mse));
        });
    }

    //focusListener
    View.OnFocusChangeListener focusListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (hasFocus) {
                // 处理获得焦点的逻辑
            } else {
                // 处理失去焦点的逻辑
                // v 是一个 EditText
                if (view instanceof EditText){
                    String str_text = ((EditText) view).getText().toString();
                    if (view.getId() == mBinding.edtName.getId()){
                        mViewModel.set_curve_name(str_text);
                    }
                    if (view.getId() == mBinding.edtXUnit.getId()){
                        mViewModel.set_curve_xUnit(str_text);
                    }
                    else if(view.getId() == mBinding.edtYUnit.getId()){
                        mViewModel.set_curve_yUnit(str_text);
                    }
                    else if(view.getId() == mBinding.edtMinX.getId()){
                        if (!str_text.isEmpty()){
                            Float xMin = Float.valueOf(str_text);
                            Float value = mViewModel.set_curve_xMin(xMin);
                            if (!value.equals(xMin)){//不相等表示设置失败
                                mViewModel.setToast(getString(R.string.toast_curve_cant_greaterMax));
                                ((EditText) view).setText(String.valueOf(value)); //还原值
                            }
                        }
                        else mViewModel.set_curve_xMin(null);

                    }
                    else if(view.getId() == mBinding.edtMaxX.getId()){
                       if (!str_text.isEmpty()){
                           Float xMax = Float.valueOf(str_text);
                           Float value = mViewModel.set_curve_xMax(xMax);
                           if (!value.equals(xMax)){//不相等表示设置失败
                               mViewModel.setToast(getString(R.string.toast_curve_cant_lessMin));
                               ((EditText) view).setText(String.valueOf(value));//还原值
                           }
                       }
                       else mViewModel.set_curve_xMax(null);
                    }
                    else if(view.getId() == mBinding.edtMinCORR.getId()){
                        if (!str_text.isEmpty())
                            mViewModel.set_curve_minCORR( Float.valueOf(str_text));
                        else mViewModel.set_curve_minCORR(null);
                    }
                    else if(view.getId() == mBinding.edtYNumber.getId()){
                        if (str_text.isEmpty())
                            ((EditText) view).setText(getString(R.string.default_point_number));
                        else {
                            float yNum = Float.parseFloat(str_text);
                            if (yNum < 1){
                                mViewModel.setToast(getString(R.string.toast_curve_cant_lessOne));
                                ((EditText) view).setText(getString(R.string.default_point_number));
                            }

                        }
                    }
                }
            }
        }
    };
    private void onClick(View view){
        if (view.getId() == mBinding.btnPointAdd.getId()){
            mViewModel.addPoint();
            //收起键盘
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0,null);// 隐藏键盘
        }
        else if (view.getId() == mBinding.btnSampleAdd.getId()){
            Intent intent = new Intent(mContext, SamplesManageActivity.class);
            startActivity(intent);
        }
        else if (view.getId() ==mBinding.ivNoticeCorr.getId()){
            String noticeCorr = mViewModel.getNoticeCorr();
            if (noticeCorr != null){
                mViewModel.setToast(noticeCorr);
            }
        }
        else if (view.getId() == mBinding.btnVerifyStandardSample.getId()){
            /* 验证标准样品 */
            if (mViewModel.getCurveType() == null) return;
            // 将y值数量设为1
            mBinding.edtYNumber.setText("1");
            //保存当前直线信息
            StandardCurve standardCurve = mViewModel.getCurve();
            DataRepository.getInstance().setStandardCurve(standardCurve);
            Log.d(TAG, "onClick: " + standardCurve.toString());
            //跳转到测量界面
            if (mViewModel.getCurveType() == 1){ //从标样中获取电流
                // 通过 电信号检测 获取标准样品电流值
                Intent intent = new Intent(mContext, MeasureValueByMultimeterActivity.class);
                intent.putExtra(MeasureValueByMultimeterActivity.GET_SHOW_MODE, ConnectMultimeterFragment.ShowModel.ELEC);
                mMeasureValueActivityLauncher.launch(intent);
            }
            else if (mViewModel.getCurveType() == 2){//从标样中获取B值
                // 通过 比色图像分析 获取标准样品B值
                // 打开图库或是拍照 选择图片,裁剪图片，获取B值
                PhotoPickerBottomSheet.show(mContext, new PhotoPickerBottomSheet.OnPhotoPickerListener() {
                    @Override
                    public void onCameraSelected() {
                        //拍照
                        mImageProcessor.takePhoto(new ImageProcessor.ImageProcessingCallback() {
                            @Override
                            public void onImageSelected(Bitmap bitmap) {
                                mImageProcessor.startCrop(bitmap, bitmapResultLauncher, new ImageProcessor.ImageProcessingCallback() {
                                    @Override
                                    public void onImageSelected(Bitmap bitmap) {
                                        controlBitmap(bitmap);
                                    }

                                    @Override
                                    public void onError(String message) {
                                        Log.e(TAG, "crop photo onImageSelected: " + message);
                                        mViewModel.setToast(getString(R.string.toast_takePhoto_cropFail));
                                    }
                                });
                            }

                            @Override
                            public void onError(String message) {
                                Log.e(TAG, "take photo onImageSelected: " + message);
                                mViewModel.setToast(getString(R.string.toast_takePhoto_fail));
                            }
                        });
                    }

                    @Override
                    public void onGallerySelected() {
                        // 处理相册逻辑
                        mImageProcessor.pickFromGallery(new ImageProcessor.ImageProcessingCallback() {
                            @Override
                            public void onImageSelected(Bitmap bitmap) {
                                mImageProcessor.startCrop(bitmap, bitmapResultLauncher, new ImageProcessor.ImageProcessingCallback() {
                                    @Override
                                    public void onImageSelected(Bitmap bitmap) {
                                        controlBitmap(bitmap);
                                    }

                                    @Override
                                    public void onError(String message) {
                                        Log.e(TAG, "crop photo onImageSelected: " + message);
                                        mViewModel.setToast(getString(R.string.toast_takePhoto_selectImage_fail));
                                    }
                                });
                            }

                            @Override
                            public void onError(String message) {
                                Log.e(TAG, "crop photo onImageSelected: " + message);
                                mViewModel.setToast(getString(R.string.toast_takePhoto_selectImage_fail));
                            }
                        });
                    }
                });

            }
            else if (mViewModel.getCurveType() == 3){//从标样中获取温度
                // 通过 光热图像分析 获取标准样品温度值
                Intent intent = new Intent(mContext, MeasureValueByMultimeterActivity.class);
                intent.putExtra(MeasureValueByMultimeterActivity.GET_SHOW_MODE, ConnectMultimeterFragment.ShowModel.TEMPERATURE);
                mMeasureValueActivityLauncher.launch(intent);
            }
            else return;
        }
    }
    /**
     * 对用户从拍照/图库取得的图片进行处理，即获取标准样品B值
     * @param bitmap  图片
     */
    public void controlBitmap(Bitmap bitmap){
        //获取标准样品B值
        List<Integer> colors = PhotoUtil.getDistinctColors(
                bitmap,
                5,           // 最多返回5种颜色
                10f,       // 色相差<10°视为相似色
                0.1f,        // 最小饱和度=0.1
                0f         // 最小亮度=0
        );
        if (!colors.isEmpty() && colors.size()>1){
            mViewModel.setToast(getString(R.string.toast_color_too_much));
            return;
        }
        if (!colors.isEmpty()){
            Integer color = colors.get(0);//获取主要颜色
            RGB rgb = RGB.fromColor( color);
            String prompt = getString(R.string.curve_GetB);
            controlResultFromActivity(rgb.getBlue(),prompt);
        }
    }

   public void fillCurve(StandardCurve curve , Boolean isAutoCalculate){
       //mIsAutoCalculate = isAutoCalculate;
       Log.d(TAG, "fillCurve: "+ curve);
       mViewModel.setCurve(curve);
       //mIsAutoCalculate = true;
   }

   public StandardCurve getCurve(){
        StandardCurve curve = mViewModel.getCurve();
       return new StandardCurve(curve);
   }
}