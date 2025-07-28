package com.lsy.chemicaltest_new.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.lsy.chemicaltest_new.domain.ColoTestResult;

@Dao
public interface ColoTestResultDao {
    @Insert
    public Long add(ColoTestResult result);
    @Delete
    public void delete(ColoTestResult result);
    @Query("select * from sample_test_colo_table where id=:id")
    public ColoTestResult findById(long id);
    @Query("delete from sample_test_colo_table where id=:id")
    void deleteById(Integer id);
}
