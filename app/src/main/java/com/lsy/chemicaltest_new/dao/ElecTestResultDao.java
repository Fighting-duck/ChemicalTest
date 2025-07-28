package com.lsy.chemicaltest_new.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.lsy.chemicaltest_new.domain.ElecTestResult;

@Dao
public interface ElecTestResultDao {
    @Insert
    Long add(ElecTestResult elecTestResult);
    @Delete
    void delete(ElecTestResult elecTestResult);
    @Query("delete from sample_test_elec_table where id=:id")
    void deleteById(Integer id);
    @Query("select * from sample_test_elec_table where id=:id")
    ElecTestResult findById(int id);
}
