package com.lsy.chemicaltest_new.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.database.SharePreferencesManager;
import com.lsy.chemicaltest_new.domain.ColoTestResult;
import com.lsy.chemicaltest_new.domain.Experimenter;
import com.lsy.chemicaltest_new.utils.LiveDataUtils;
import com.lsy.chemicaltest_new.utils.PhotoUtil;

import java.util.concurrent.Executors;

public class MineViewModel extends ViewModel {
    private static final String TAG = "MineViewModel";
    MutableLiveData<Experimenter> mLiveData_experimenter = new MutableLiveData<>();
    MutableLiveData<String> mLiveData_toast = new MutableLiveData<>();
    Context mContext;
    public void setContext(Context context){
        mContext = context;
    }
    public MutableLiveData<Experimenter> getLiveData_experimenter() {
        return mLiveData_experimenter;
    }
    public MutableLiveData<String> getLiveData_toast() {
        return mLiveData_toast;
    }

    public void setExperimenter(Experimenter experimenter) {
        // 先释放旧的Bitmap
        Experimenter current = mLiveData_experimenter.getValue();
        if (current != null && current.getImage() != null) {
            current.getImage().recycle();
        }
        mLiveData_experimenter.setValue(experimenter);
    }
    public void setToast(String prompt) {
        LiveDataUtils.safeUpdate(mLiveData_toast,prompt);
    }

    public void updateHeadImage(Bitmap rotatedBitmap) {
        Experimenter experimenter = mLiveData_experimenter.getValue();
        if (experimenter == null){
            experimenter = new Experimenter();
        }
        experimenter.setImage(rotatedBitmap);
        // 删除旧图
        if (experimenter.getImagePath() != null && !experimenter.getImagePath().isEmpty())
            PhotoUtil.deleteImage(experimenter.getImagePath());
        // 保存新图
        String imagePath = PhotoUtil.saveBitmapToFile(mContext, rotatedBitmap, "experimenter_");
        experimenter.setImagePath(imagePath);
        SharePreferencesManager sharePreferencesManager = new SharePreferencesManager(mContext);
        sharePreferencesManager.setAvatarPath(imagePath);
        // 更新LiveData
        mLiveData_experimenter.setValue(experimenter);
    }

    public void setExperimenterName(String inputText) {
        Experimenter experimenter = mLiveData_experimenter.getValue();
        if (experimenter == null){
            experimenter = new Experimenter();
        }
        experimenter.setName(inputText);
        // 保存
        SharePreferencesManager sharePreferencesManager = new SharePreferencesManager(mContext);
        sharePreferencesManager.setUserName(inputText);
        // 更新
        mLiveData_experimenter.setValue(experimenter);
    }
//    public interface SaveExperimenterCallback {
//        void onSaveSuccess();
//        void onSaveFailure();
//    }
//    public void saveExperimenter(SaveExperimenterCallback callback) {
//        // 保存图片
//        Experimenter experimenter = mLiveData_experimenter.getValue();
//        Log.d(TAG, "saveExperimenter: " + experimenter);
//        if (experimenter != null && experimenter.getImage() != null){
//            Executors.newSingleThreadExecutor().execute(() -> {
//                // 删除旧图
//                if (experimenter.getImagePath() != null){
//                    PhotoUtil.deleteImage(experimenter.getImagePath());
//                }
//                // 保存图片
//                String imagePath = PhotoUtil.saveBitmapToFile(MyApplication.INSTANCE, experimenter.getImage(), "experimenter_");
//                experimenter.setImagePath(imagePath);
//                Log.d(TAG, "saveExperimenter: " + imagePath);
//                // 保存到数据库
//                /*MyApplication.DATABASE_INSTANCE.getExperimenterDao().update(experimenter);*/
//
//                callback.onSaveSuccess();
//            });
//        }
//        else {
//            callback.onSaveFailure();
//        }
//    }

    public Bitmap getImage() {
        Experimenter experimenter = mLiveData_experimenter.getValue();
        if (experimenter == null){
            return null;
        }
        else return experimenter.getImage();
    }
}