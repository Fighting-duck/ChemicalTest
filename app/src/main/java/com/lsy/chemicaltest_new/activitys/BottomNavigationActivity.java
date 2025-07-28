package com.lsy.chemicaltest_new.activitys;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.databinding.ActivityBoottomNavigationBinding;
import com.lsy.chemicaltest_new.fragments.HistoryFragment;
import com.lsy.chemicaltest_new.fragments.SampleTestFragment;
import com.lsy.chemicaltest_new.fragments.MineFragment;
import com.lsy.chemicaltest_new.utils.ImageProcessor;

import java.util.ArrayList;

public class BottomNavigationActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private ActivityBoottomNavigationBinding mBinding;
    private FragmentPagerAdapter mPagerAdapter;
    private ImageProcessor mImageProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityBoottomNavigationBinding.inflate(getLayoutInflater());
        mImageProcessor = new ImageProcessor(this);
        setContentView(mBinding.getRoot());

        addUiListener();
        initPageAdapter();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        if (mImageProcessor != null) {
            mImageProcessor.release(); // 需在 ImageProcessor 中添加 release() 方法
        }
        mBinding.vpMain.removeOnPageChangeListener(onPageChangeListener);
        mBinding = null;
        super.onDestroy();
    }


    //初始化页面监听器
    private void initPageAdapter() {
        //Fragment列表，将fragment放入列表中，放入mPagerAdapter
        final ArrayList<Fragment> fgLists = new ArrayList<>(5);
        fgLists.add(new SampleTestFragment());
        fgLists.add(new HistoryFragment());
        fgLists.add(new MineFragment());
        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fgLists.get(position);
            }

            @Override
            public int getCount() {
                return fgLists.size();
            }
        };

        mBinding.vpMain.setAdapter(mPagerAdapter);
        //设置预加载页面数量的方法
        mBinding.vpMain.setOffscreenPageLimit(1);
    }

    //底部导航栏监听器
    @SuppressLint("NonConstantResourceId")
    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            // 清除之前的选中状态
            Menu menu = mBinding.navView.getMenu();
            for (int i = 0; i < menu.size(); i++) {
                MenuItem menuItem = menu.getItem(i);
                menuItem.setChecked(false);
            }
            int id = item.getItemId();
            if (id == R.id.navigation_sampleTest) {
                mBinding.vpMain.setCurrentItem(0);
                item.setChecked(true);
            } else if (id == R.id.navigation_history) {
                mBinding.vpMain.setCurrentItem(1);
                item.setChecked(true);
            } else if (id == R.id.navigation_useGuide) {
                mBinding.vpMain.setCurrentItem(2);
                item.setChecked(true);
            }
            return false;
        }
    };

    //添加组件监听器
    private void addUiListener() {
        mBinding.navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mBinding.vpMain.addOnPageChangeListener(onPageChangeListener);
    }

    //页面改变监听器
    private final ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            //在滑动中

        }

        @Override
        public void onPageSelected(int position) {
            //写滑动页面后做的事，使每一个fragment与一个page相对应
            switch (position) {
                case 0:
                    mBinding.navView.setSelectedItemId(R.id.navigation_sampleTest);
                    break;
                case 1:
                    mBinding.navView.setSelectedItemId(R.id.navigation_history);
                    break;
                case 2:
                    mBinding.navView.setSelectedItemId(R.id.navigation_useGuide);
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

    };

    public void takePhoto(ImageProcessor.ImageProcessingCallback callback) {
        mImageProcessor.takePhoto(callback);
    }
    public void pickFromGallery(ImageProcessor.ImageProcessingCallback callback) {
        mImageProcessor.pickFromGallery(callback);
    }
}