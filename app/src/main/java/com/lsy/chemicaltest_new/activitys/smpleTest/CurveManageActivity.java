package com.lsy.chemicaltest_new.activitys.smpleTest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.activitys.BaseActivity;
import com.lsy.chemicaltest_new.adapters.CurveAdapter;
import com.lsy.chemicaltest_new.database.DataRepository;
import com.lsy.chemicaltest_new.databinding.ActivityCurveManageBinding;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.models.CurveManageViewModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/***
 * 主要负责：
 * 显示标准曲线列表
 * 提供添加、编辑和删除曲线的功能
 * 支持曲线搜索和刷新
 * 处理曲线数据的保存和恢复
 */
public class CurveManageActivity extends BaseActivity {
    private static final String TAG = "CurveManageActivity";
    private static final String KEY_DATA = "key_data";
    private ActivityCurveManageBinding mBinding;
    private Context mContext;
    private CurveManageViewModel mViewModel;
    private CurveAdapter mAdapter;
    private final Handler handler = new Handler();
    private final Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            String filter = mBinding.etSearchCurve.getText().toString();
            if (filter.isEmpty()) filter = null;
            mViewModel.updateCurves(filter, new CurveManageViewModel.UpdateCallback() {
                @Override
                public void onUpdateCompleted() {
                }

                @Override
                public void onEmptyCurve() {
                    mViewModel.setToast(getString(R.string.toast_curve_empty));
                }

                @Override
                public void onUpdateFailed(Exception e) {
                    e.printStackTrace();
                    mViewModel.setToast(getString(R.string.toast_update_fail));
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityCurveManageBinding.inflate(getLayoutInflater());
        mContext = this;
        mViewModel = new ViewModelProvider(this).get(CurveManageViewModel.class);
        setContentView(mBinding.getRoot());
        initUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
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
    protected void onResume() {
        super.onResume();
        mViewModel.updateCurves(null, new CurveManageViewModel.UpdateCallback() {
            @Override
            public void onUpdateCompleted() {
            }

            @Override
            public void onEmptyCurve() {
                mViewModel.setToast(getString(R.string.toast_curve_empty));
            }

            @Override
            public void onUpdateFailed(Exception e) {
                e.printStackTrace();
                mViewModel.setToast(getString(R.string.toast_update_fail));
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initUI() {
        //初始化recycleView列表
        mBinding.rvCurveList.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new CurveAdapter();
        mBinding.rvCurveList.setAdapter(mAdapter);

        mAdapter.setOnEditClickListener(position -> {
            StandardCurve curve = mAdapter.getCurrentList().get(position);
            //MyApplication.INSTANCE.getAlterCurveViewModel().setOldCurve(curve);
            DataRepository.getInstance().setStandardCurve(curve);//临时保存到数据仓库
            //跳转到修改界面
            Intent intent = new Intent(mContext, AlterCurveActivity.class);
            mContext.startActivity(intent);
        });
        mAdapter.setOnDeleteClickListener(new CurveAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(int position) {
                StandardCurve curve = mAdapter.getCurrentList().get(position);
                //弹框提示是否删除
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(getString(R.string.dialog_deleteCurve_title));
                builder.setMessage(getString(R.string.dialog_deleteCurve_message));
                builder.setPositiveButton(getString(R.string.dialog_positive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mViewModel.deleteCurve(curve, new CurveManageViewModel.DeleteCallback() {
                            @Override
                            public void onDeleteCompleted() {
                                mViewModel.setToast(getString(R.string.toast_delete_success));
                            }

                            @Override
                            public void onDeleteFailed(Exception e) {
                                e.printStackTrace();
                                mViewModel.setToast(getString(R.string.toast_delete_fail));
                            }
                        });
                    }
                });
                builder.setNegativeButton(getString(R.string.dialog_negative), null);
                builder.create().show();

            }
        });
        mBinding.ivBack.setOnClickListener(view -> finish());
        mBinding.ivAdd.setOnClickListener(v -> {
            DataRepository.getInstance().setStandardCurve(null);//清楚数据仓库中的曲线
            Intent intent = new Intent(mContext, AddCurveActivity.class);
            mContext.startActivity(intent);
        });
        //设置下拉刷新布局的进度圆圈颜色
        mBinding.srlRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light,
                android.R.color.holo_orange_light, android.R.color.holo_green_light);
        //给refreshLayout设置下拉刷新监听器
        mBinding.srlRefreshLayout.setOnRefreshListener(() -> {
            mViewModel.updateCurves(null, new CurveManageViewModel.UpdateCallback() {
                @Override
                public void onUpdateCompleted() {
                    mBinding.srlRefreshLayout.setRefreshing(false);
                    mViewModel.setToast(getString(R.string.toast_update_success));
                }

                @Override
                public void onEmptyCurve() {
                    mViewModel.setToast(getString(R.string.toast_curve_empty));
                }

                @Override
                public void onUpdateFailed(Exception e) {
                    mViewModel.setToast(getString(R.string.toast_update_fail));
                }
            });
        });

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

        mViewModel.getLiveData_toast().observe(this, toast ->{
            if (toast != null){
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
                mViewModel.setToast(null);
            }
        });
        mViewModel.getLiveData_showCurves().observe(this, standardCurves -> {
            if (standardCurves != null && !standardCurves.isEmpty()) {
                mBinding.tvSampleTotalNum.setText(String.valueOf(standardCurves.size()));
                mBinding.emptyTextView.setVisibility(View.GONE);
                mBinding.rvCurveList.setVisibility(View.VISIBLE);
                mAdapter.submitList(standardCurves);
                mAdapter.notifyDataSetChanged();
            }
            else {
                mBinding.emptyTextView.setVisibility(View.VISIBLE);
                mBinding.rvCurveList.setVisibility(View.GONE);
                mBinding.tvSampleTotalNum.setText(getString(R.string.default_number));
            }
        });
    }

    //数据保存与恢复
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: ");
        super.onSaveInstanceState(outState);
        List<StandardCurve> curves = mAdapter.getCurrentList();
        if (!curves.isEmpty()) {
            outState.putParcelableArrayList(KEY_DATA,new ArrayList<>(curves));
            Log.d(TAG, "onSaveInstanceState: Saved curves list with size " + curves.size() + ", data: " + curves);
        } else {
            Log.d(TAG, "onSaveInstanceState: No curves list to save");
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState: ");
        super.onRestoreInstanceState(savedInstanceState);
        // 从 Bundle 中恢复数据
        List<StandardCurve> curves = savedInstanceState.getParcelableArrayList("KEY_DATA");
        if (curves != null) {
            mAdapter.submitList(curves);
        }
    }
}