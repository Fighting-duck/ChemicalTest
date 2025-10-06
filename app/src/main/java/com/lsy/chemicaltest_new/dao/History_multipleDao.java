package com.lsy.chemicaltest_new.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.lsy.chemicaltest_new.domain.ColoTestResult;
import com.lsy.chemicaltest_new.domain.ElecTestResult;
import com.lsy.chemicaltest_new.domain.History_multiple;

import java.util.List;

@Dao
public interface History_multipleDao {
    @Insert
    Long add(History_multiple history_multiple);
    @Delete
    void delete(History_multiple history_multiple);  // 删除某一项数据
    @Query("DELETE FROM history_table")
    void deleteAll(); // 删除所有数据
    @Query("SELECT * FROM history_table")
    List<History_multiple> getAll(); // 查询所有数据
    @Query("SELECT * FROM history_table WHERE id = :id")
    History_multiple findById(int id); // 查询某一项数据

    @Query("SELECT * FROM history_table " +
            "WHERE saveTime LIKE '%' || :date || '%' " +
            "AND sample_ids LIKE '%' || :sampleId || '%'")
    List<History_multiple> findAllByDateAndSampleId(String date, String sampleId);
    @Query("SELECT * FROM history_table " +
            "WHERE saveTime LIKE '%' || :date || '%' ")
    List<History_multiple> findAllByDate(String date);
    @Query("SELECT * FROM history_table " +
            "WHERE sample_ids LIKE '%' || :sampleId || '%'")
    List<History_multiple> findAllBySampleId( String sampleId);
    @Query("SELECT saveTime FROM history_table")
    List<String> getAllDate(); // 查询所有日期
    /***
     * 查看是否用过曲线
     * @param curveId 曲线id
     * @return 返回值为空，则没有用过，反之有
     */
    @Query("SELECT * FROM history_table " +
            "WHERE curve_ids LIKE :curveId || ',%' " +  // 匹配开头
            "OR curve_ids LIKE '%,' || :curveId || ',%' " +  // 匹配中间
            "OR curve_ids LIKE '%,' || :curveId " +  // 匹配结尾
            "OR curve_ids = :curveId")  // 仅包含当前ID
    List<History_multiple> findByCurveId(Integer curveId);
}
