package com.lsy.chemicaltest_new.implement;

import android.util.Log;

import androidx.room.Transaction;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.database.AppDatabase;
import com.lsy.chemicaltest_new.domain.Expression;
import com.lsy.chemicaltest_new.domain.History_multiple;
import com.lsy.chemicaltest_new.domain.Point;
import com.lsy.chemicaltest_new.domain.Sample;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.interfaces.StandardCurve_Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class StandardCurveDataImpl implements StandardCurve_Data {
    private static final String TAG = "StandardCurveDataImpl";
    private static StandardCurveDataImpl instance;
    public static StandardCurveDataImpl getInstance() {
        if (instance == null) {
            instance = new StandardCurveDataImpl();
        }
        return instance;
    }
    private StandardCurveDataImpl() {
    }
    @Override
    public List<Long> savePoints(List<Point> pointList) {
        long[] ids = MyApplication.DATABASE_INSTANCE.getPointDao().insertAllWithRollback(pointList);

        // 将基本类型long数组转换为包装类型Long列表
        List<Long> result = new ArrayList<>(ids.length);
        for (long id : ids) {
            result.add(id); // 自动装箱从long转换为Long
        }

        return result;
    }

    /***
     * 在数据库中修改曲线的point
     * @return 所有point的id
     */
    @Override
    @Transaction
    public List<Long> alterPoints(List<Point> oldList, List<Point> newList) {
        Log.d(TAG, "alterPoints: "+newList);
        if (newList == null) {
            return null;
        }
        // 事务开始：手动控制事务（因ViewModel中的@Transaction可能无法触发Room事务）
        AppDatabase db = MyApplication.DATABASE_INSTANCE;
        db.beginTransaction();
        try {
            // 1.先删除point
            db.getPointDao().delete(oldList.toArray(new Point[0]));//new Point[0] 的作用:动态创建数组
            // 2.再增加point(使用批量插入优化)
            long[] ids = db.getPointDao().insertAllWithRollback(newList);
            db.setTransactionSuccessful();// 标记事务成功
            // 3.将基本类型long数组转换为包装类型Long列表
            List<Long> result = new ArrayList<>(ids.length);
            for (long id : ids) {
                result.add(id); // 自动装箱从long转换为Long
            }
            return result;
        }catch (Exception e){
            revertPoints(db, oldList, newList); // 事务回滚
            throw e; // 向上抛出供外层处理
        }finally {
            db.endTransaction();
        }
    }
    /**
     * 事务回滚：删除未提交的新数据并恢复旧点
     */
    private void revertPoints(AppDatabase db, List<Point> oldList, List<Point> newList) {
        try {
            // 1. 删除可能部分插入的新点
            if (newList != null && !newList.isEmpty()) {
                db.getPointDao().delete(newList.toArray(new Point[0]));
            }

            // 2. 重新插入旧点（通过备份）
            if (oldList != null && !oldList.isEmpty()) {
                db.getPointDao().insertAllWithRollback(oldList); // 需要确保DAO支持批量插入
            }
        } catch (Exception e) {
            Log.e(TAG, "回滚失败: " + e.getMessage());
        }
    }

    @Override
    public void completeCurve(StandardCurve curve) {
        if (curve == null) return;
        // 并行加载关联数据
        try {
            // 1.加载样本数据
            CompletableFuture<Sample> sampleFuture = CompletableFuture.supplyAsync(
                    () -> MyApplication.DATABASE_INSTANCE.getSampleDao().findById(curve.getSample_id()),
                    MyApplication.DB_EXECUTOR
            );
            // 2.加载点数据
            CompletableFuture<List<Point>> pointsFuture = CompletableFuture.supplyAsync(
                    () -> getPointList(curve.getPoint_set()),
                    MyApplication.DB_EXECUTOR
            );
            // 3.加载曲线公式
            CompletableFuture<Expression> expressionFuture = CompletableFuture.supplyAsync(
                    () -> Expression.buildExpression(curve.getExpression()),
                    MyApplication.DB_EXECUTOR
            );

            // 等待所有结果（带超时）
            curve.setSample(sampleFuture.get(1, TimeUnit.SECONDS));
            curve.setPointList(pointsFuture.get(1, TimeUnit.SECONDS));
            curve.setFormula(expressionFuture.get(1, TimeUnit.SECONDS));

        } catch (Exception e) {
            Log.w(TAG, "Complete curve failed: " + e.getMessage());
        }

    }
    @Override
    public List<Point> getPointList(String point_list) {
        //1.字符串转化为List
        // 去除首尾的方括号
        String trimmedInput = point_list.substring(1, point_list.length() - 1);
        // 按逗号分隔字符串
        String[] items = trimmedInput.split(",");
        // 将每个分隔后的字符串转换为整数，并收集到列表中
        List<Integer> points_ids = new ArrayList<>();
        for (String item : items) {
            // 去除每个元素周围的空白字符
            String trimmedItem = item.trim();
            // 将字符串转换为整数
            Integer number = Integer.parseInt(trimmedItem);
            // 添加到结果列表中
            points_ids.add(number);
        }
        //2.循环读取数据库得到point
        List<Point> points = new ArrayList<>();
        for (Integer point_id : points_ids) {
            // 查询数据库
            Point point = MyApplication.DATABASE_INSTANCE.getPointDao().findById(point_id);
            points.add(point);
        }
        //3.返回pointList
        return points;
    }
    @Override
    public List<StandardCurve> deleteCurves(List<StandardCurve> curves,List<StandardCurve> deleteCurves) {
        for (StandardCurve curve : deleteCurves){
            //1. 查看该曲线是否在检测实验中用过
            List<History_multiple> histories = MyApplication.DATABASE_INSTANCE.getHistory_multipleDao().findByCurveId(curve.getId());
            if (histories!=null && !histories.isEmpty()){
                // 2.用过则将validity设为0 curve.setValidity(0);
                Log.d("deleteCurve", "update curveId=" + curve+" 's validity=0.");
                MyApplication.DATABASE_INSTANCE.getStandardCurveDao().updateValidity(curve.getId(),0);
            }
            else {
                // 2.没用过则删除
                Log.d("deleteCurve", "delete curveId=" + curve);
                // 硬删除（带事务）
                MyApplication.DATABASE_INSTANCE.runInTransaction(() -> {
                    // 先删除关联点
                    List<Point> points = curve.getPointList();
                    if (points != null && !points.isEmpty()) {
                        MyApplication.DATABASE_INSTANCE.getPointDao()
                                .delete(points.toArray(new Point[0]));
                    }
                    // 再删除曲线
                    MyApplication.DATABASE_INSTANCE.getStandardCurveDao()
                            .deleteById(curve.getId());
                });
            }
            // 3.更新UI数据（主线程）
            if (curves!=null && !curves.isEmpty()){
                curves.removeIf(curveToRemove -> curveToRemove.getId() == curve.getId());
            }
        }
        return curves;
    }

}
