package com.lsy.chemicaltest_new.models;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.room.Transaction;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.domain.Sample;
import com.lsy.chemicaltest_new.utils.LiveDataUtils;
import com.lsy.chemicaltest_new.utils.PhotoUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SampleManageViewModel extends ViewModel {
    private static final String TAG = "SampleManageViewModel";
    // 添加任务管理
    private final ConcurrentHashMap<UUID, Future<?>> pendingTasks = new ConcurrentHashMap<>();
    // 保存数据状态枚举
    public enum SaveState {
        LOADING,    // 加载中
        SUCCESS,    // 成功
        ERROR,       // 错误
        EMPTY
    }
    // 修改数据状态枚举
    public enum EditState {
        LOADING,    // 加载中
        SUCCESS,    // 成功
        ERROR,       // 错误
        EMPTY
    }
     MutableLiveData<List<Sample>> mLiveData_samples = new MutableLiveData<>();
     MutableLiveData<String> mLiveData_toast = new MutableLiveData<>();
     MutableLiveData<Sample> mLiveData_editSample = new MutableLiveData<>();//要修改的样品信息
     MutableLiveData<Integer> mLiveData_currentState = new MutableLiveData<>(1);  //当前状态，1表示添加状态，2表示编辑状态
     MutableLiveData<Bitmap> mLiveData_Image = new MutableLiveData<>(null);
     MutableLiveData<SaveState> mLiveData_saveState = new MutableLiveData<>(SaveState.EMPTY);
     MutableLiveData<EditState> mLiveData_editState = new MutableLiveData<>(EditState.EMPTY);
    Context mContext;
    public void setContext(Context context){
        mContext = context;
    }
    public MutableLiveData<List<Sample>> getLiveData_samples(){
        return mLiveData_samples;
    }
    public MutableLiveData<String> getLiveData_toast(){
        return mLiveData_toast;
    }
    public MutableLiveData<Sample> getLiveData_editSample(){
        return mLiveData_editSample;
    }
    public MutableLiveData<Integer> getLiveData_currentState(){
        return mLiveData_currentState;
    }
    public MutableLiveData<Bitmap> getLiveData_Image(){
        return mLiveData_Image;
    }
    public MutableLiveData<SaveState> getLiveData_saveState(){
        return mLiveData_saveState;
    }
    public MutableLiveData<EditState> getLiveData_editState(){
        return mLiveData_editState;
    }
    public void setCurrentState(int state,Sample sample){
        mLiveData_currentState.setValue(state);
        mLiveData_editSample.setValue(sample);
    }

    public void setToast(String prompt){
        LiveDataUtils.safeUpdate(mLiveData_toast,prompt);
    }
    private void executeTask(Runnable task,String prompt) {
        UUID taskId = UUID.randomUUID();
        Future<?> future = MyApplication.DB_EXECUTOR.submit(() -> {
            try {
                task.run();
            } catch (Exception e){
                setToast(prompt);
                e.printStackTrace();
                Log.e(TAG,e+"");
            } finally{
                pendingTasks.remove(taskId);
            }
        });
        pendingTasks.put(taskId, future);
    }
    @Override
    protected void onCleared() {
        super.onCleared();
        // 取消所有未完成的任务
        pendingTasks.forEach((id, future) -> {
            if (!future.isDone()) {
                future.cancel(true);
            }
        });
        pendingTasks.clear();
    }

    public void setImage(Bitmap bitmap){
        LiveDataUtils.safeUpdate(mLiveData_Image, bitmap);
    }
    public Bitmap getImage(){
       return mLiveData_Image.getValue();
    }
    /**
     * 保存样本
     * @param context 上下文
     * @param sample 样本
     */
    @Transaction
    public void saveSample(@NonNull Context context,@NonNull Sample sample) {
        //参数校验
        Objects.requireNonNull(context, "Context cannot be null");
        Objects.requireNonNull(sample, "Sample cannot be null");
        // 通知保存开始
        mLiveData_saveState.setValue(SaveState.LOADING);
        MyApplication.DB_EXECUTOR.submit(() -> {
            try {
                // 1.先检查是否已经存在
                Sample existingSample = checkSampleNameExists(sample.getName());
                if (existingSample!=null){
                    mLiveData_saveState.postValue(SaveState.ERROR);
                    setToast(context.getString(R.string.toast_sample_nameRepeat));
                    return;
                }
                // 2.查看是否有图 有图则构造图片路径
                if (sample.getImage() != null){
                    String imagePath = PhotoUtil.saveBitmapToFile(context, sample.getImage(), "sample_");
                    sample.setImagePath(imagePath);
                }
                // 3.不存在插入数据库
                Long id = MyApplication.DATABASE_INSTANCE.getSampleDao().add(sample);
                if (id == -1){
                    throw new RuntimeException("Failed to insert sample into database");
                }
                // 4. 更新LiveData
                synchronized (this) {   //同步块，确保同一时间只有一个线程能执行块内代码
                    sample.setId(Math.toIntExact(id));
                    List<Sample> currentList = new ArrayList<>(mLiveData_samples.getValue() != null ?
                            mLiveData_samples.getValue() : new ArrayList<>());
                    currentList.add(sample);
                    LiveDataUtils.safeUpdate(mLiveData_samples, currentList);
                }
                LiveDataUtils.safeUpdate(mLiveData_Image,null);
                //完成
                mLiveData_saveState.postValue(SaveState.SUCCESS);
                setToast(context.getString(R.string.toast_save_success));
            }catch (Exception e){
                setToast(context.getString(R.string.toast_save_fail_exception));
                mLiveData_saveState.postValue(SaveState.ERROR);
            }
        });

    }

    /**
     * 检查名称是否重复
     * @param name 样品名
     * @return 样品对象
     * @throws Exception
     */
    private Sample checkSampleNameExists(String name) throws Exception {
        Callable<Sample> checkTask = () ->
                MyApplication.DATABASE_INSTANCE.getSampleDao().findBy_name(name);

        Future<Sample> future = MyApplication.DB_EXECUTOR.submit(checkTask);
        try {
            return future.get(3, TimeUnit.SECONDS); // 3秒超时
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new TimeoutException("检查名称是否存在超时");
        }
    }
    public interface UpdateCallback {
        void onUpdateCompleted(List<Sample> samples);
        void onUpdateFailed(Exception e);
    }
    @Transaction
    public void showAllSamples(@NonNull UpdateCallback callback){
        Objects.requireNonNull(callback, "Callback cannot be null");

        MyApplication.DB_EXECUTOR.execute(() -> {
            try{
                List<Sample> sampleList = MyApplication.DATABASE_INSTANCE.getSampleDao().getAll_Available();
                if(sampleList == null){
                    callback.onUpdateCompleted(null);
                    return;
                }
                for (Sample sample:sampleList){
                    if (sample.getImagePath() != null){
                        sample.setImage(PhotoUtil.loadBitmapFromPath(sample.getImagePath()));
                    }
                }
                LiveDataUtils.safeUpdate(mLiveData_samples,sampleList);
                callback.onUpdateCompleted(sampleList);
            }catch (Exception e){
                callback.onUpdateFailed(e);
            }

        });
    }

    /***
     * 删除指定索引的样本
     * @param index 索引
     */
    @Transaction
    public void deleteSample(int index) {
        executeTask(() -> {
            List<Sample> currentList = mLiveData_samples.getValue();
            if (currentList == null || index < 0 || index >= currentList.size()) {
                setToast(mContext.getString(R.string.toast_delete_fail));
                return;
            }

            Sample sample = currentList.get(index);

            if (!deleteSampleFromDatabase(sample)) {
                setToast(mContext.getString(R.string.toast_sample_deleteFail_existCurve));
                return;
            }

            if (!deleteSampleImage(sample.getImagePath())) {
                setToast(mContext.getString(R.string.toast_delete_fail_image));
                return;
            }

            currentList.remove(index);
            LiveDataUtils.safeUpdate(mLiveData_samples, currentList);
            setToast(mContext.getString(R.string.toast_delete_success));
        },mContext.getString(R.string.toast_delete_fail_exception));
    }

    private boolean deleteSampleFromDatabase(Sample sample) {
        try {
            // 删除样本记录（依赖关系会自动删除）
            return MyApplication.DATABASE_INSTANCE.getSampleDao().delete(sample.getId()) > 0;
        } catch (SQLiteConstraintException e) {
            // 如果捕获到外键约束异常，说明可能存在其他表的依赖关系
            e.printStackTrace();
            return false;
        }
    }

    private boolean deleteSampleImage(String imagePath) {
        if (imagePath == null) {
            return true;
        }
        return PhotoUtil.deleteImage(imagePath);
    }
    @Transaction
    public void editSample(@NonNull Context context,@NonNull Sample newSample,@NonNull Sample oldSample) {
        //参数检查
        Objects.requireNonNull(context, "Context cannot be null");
        Objects.requireNonNull(newSample, "New Sample cannot be null");
        Objects.requireNonNull(oldSample, "Old Sample cannot be null");
        final UUID taskId = UUID.randomUUID();
        mLiveData_editState.postValue(EditState.LOADING);//修改中
        Future<?> future = MyApplication.DB_EXECUTOR.submit(()->{
            try{
                // 1.先检查样品名是否已经存在
                Sample  sampleExists = MyApplication.DATABASE_INSTANCE.getSampleDao().findByName(newSample.getName());
                if (sampleExists!=null && !sampleExists.getName().equals(oldSample.getName())){
                    setToast(mContext.getString(R.string.toast_sample_nameRepeat));
                    mLiveData_editState.postValue(EditState.ERROR);//修改失败
                    return;
                }
                else newSample.setId(oldSample.getId());
                // 2.删除oldSample的图片，保存newSample的图片
                if (oldSample.getImagePath() != null){
                    //删除原始图片
                    Boolean isDelete = PhotoUtil.deleteImage(oldSample.getImagePath());
                    Log.d(TAG, "editSample isDelete: "+isDelete);
                }
                if (newSample.getImage() != null){
                    //保存现在的图片
                    String imagePath = PhotoUtil.saveBitmapToFile(context, newSample.getImage(), "sample_");
                    newSample.setImagePath(imagePath);
                }
                // 2.不存在 修改数据库
                int id = MyApplication.DATABASE_INSTANCE.getSampleDao().update(newSample);
                if (id == -1){
                    throw new Exception("修改失败");
                }
                // 3.更新列表
                MyApplication.INSTANCE.setUpdateSample(true);
                showAllSamples(new UpdateCallback() {
                    @Override
                    public void onUpdateCompleted(List<Sample> samples) {

                    }

                    @Override
                    public void onUpdateFailed(Exception e) {
                        setToast(mContext.getString(R.string.toast_sample_refreshFail_sampleList));
                    }
                });
                setToast(mContext.getString(R.string.toast_update_success));
                mLiveData_editState.postValue(EditState.SUCCESS);//修改成功
                pendingTasks.remove(taskId);
            }catch (Exception e){
                pendingTasks.remove(taskId);
                setToast(mContext.getString(R.string.toast_update_fail));
                mLiveData_editState.postValue(EditState.ERROR);//修改失败
            }
        });
        pendingTasks.put(taskId, future);
    }
}
