package com.lsy.chemicaltest_new.dao;

import android.database.sqlite.SQLiteException;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.lsy.chemicaltest_new.domain.Point;

import java.util.List;

@Dao
public interface PointDao {
    @Insert
    //获取主键id
    Long add(Point point);
    @Insert
    long[] insertAll(Point... points);// 批量插入接口
    @Transaction
    default long[] insertAllWithRollback(List<Point> points) {
        long[] ids = insertAll(points.toArray(new Point[0]));
        // 验证插入数量
        if (ids.length != points.size()) {
            throw new SQLiteException("插入点数量不匹配");
        }
        return ids;
    }
    @Delete
    void delete(Point... points);
    @Query("select * from point_table")
    List<Point> findAll();
    @Query("SELECT * FROM point_table WHERE id = :id")
    Point findById(int id);

}
