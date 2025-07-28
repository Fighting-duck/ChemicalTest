package com.lsy.chemicaltest_new.activitys.mine;

import static com.lsy.chemicaltest_new.database.SharePreferencesManager.LANGUAGE_CODE_CH;
import static com.lsy.chemicaltest_new.database.SharePreferencesManager.LANGUAGE_CODE_EN;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.activitys.BaseActivity;
import com.lsy.chemicaltest_new.activitys.BottomNavigationActivity;
import com.lsy.chemicaltest_new.databinding.ActivityUseLanguageBinding;
import com.lsy.chemicaltest_new.database.SharePreferencesManager;
import com.lsy.chemicaltest_new.models.UseLanguageViewModel;

public class UseLanguageActivity extends BaseActivity {
    private static final String TAG = "UseLanguageActivity";
    private Context mContext;
    private ActivityUseLanguageBinding mBinding;

    private UseLanguageViewModel mViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(UseLanguageViewModel.class);
        mContext = this;
        mBinding = ActivityUseLanguageBinding.inflate(getLayoutInflater()) ;
        setContentView(mBinding.getRoot());
        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String language = new SharePreferencesManager(mContext).getLanguage();
        if (language.equals(LANGUAGE_CODE_CH)){
            mViewModel.setLanguageState(UseLanguageViewModel.LanguageState.CH);
        }
        else if (language.equals(LANGUAGE_CODE_EN)){
            mViewModel.setLanguageState(UseLanguageViewModel.LanguageState.EN);
        }
    }

    private void initUI() {
        mBinding.ivBack.setOnClickListener(this::onClick);
        mBinding.flSelectCH.setOnClickListener(this::onClick);
        mBinding.flSelectEN.setOnClickListener(this::onClick);

        mViewModel.getLiveData_toast().observe(this, toast -> {
            if (toast != null) {
                Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
                mViewModel.setToast(null);
            }
        });
        mViewModel.getLiveData_languageState().observe(this, languageState -> {
            if (languageState != null) {
                switch (languageState) {
                    case CH:
                        mBinding.flSelectCH.setSelected(true);
                        mBinding.flSelectEN.setSelected(false);
                        mBinding.ivSelectCH.setVisibility(View.VISIBLE);
                        mBinding.ivSelectEN.setVisibility(View.GONE);
                        break;
                    case EN:
                        mBinding.flSelectCH.setSelected(false);
                        mBinding.flSelectEN.setSelected(true);
                        mBinding.ivSelectCH.setVisibility(View.GONE);
                        mBinding.ivSelectEN.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
    }

    private void onClick(View view) {
        if (view.getId() == mBinding.ivBack.getId()){
            finish();
        }
        else if (view.getId() == mBinding.flSelectCH.getId()) {
            mViewModel.setLanguageState(UseLanguageViewModel.LanguageState.CH);
            switchLanguage(LANGUAGE_CODE_CH);
        }
        else if (view.getId() == mBinding.flSelectEN.getId()) {
            mViewModel.setLanguageState(UseLanguageViewModel.LanguageState.EN);
            switchLanguage(LANGUAGE_CODE_EN);
        }
    }

    /***
     * 切换语言
     * @param languageCode 语言代码
     */
    private void switchLanguage(String languageCode) {
        SharePreferencesManager sharePreferencesManager = new SharePreferencesManager(this);
        sharePreferencesManager.setLanguage(languageCode);  // 保存语言设置

        // 更新应用配置
/*        Context context = ContextWrapper.wrap(this, languageCode);
        getResources().updateConfiguration(
                context.getResources().getConfiguration(),
                context.getResources().getDisplayMetrics()
        );*/

        // 重启当前 Activity 以应用语言
        /*new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        },300);*/
        // 3. 暴力重启整个应用（确保所有 Activity 重建） 延迟重启避免 ANR
       /* new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(this, BottomNavigationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
            },300);*/
        // 2. 弹窗提示用户重启
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_restart_title)
                .setMessage(R.string.dialog_restart_message)
                .setPositiveButton(R.string.dialog_restart_positive, (dialog, which) -> {
                    // 3. 彻底重启应用
                    Intent intent = new Intent(this, BottomNavigationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finishAffinity(); // 关闭所有 Activity
                })
                .setNegativeButton(R.string.dialog_restart_negative, null)
                .show();


    }

}