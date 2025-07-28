package com.lsy.chemicaltest_new.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.lsy.chemicaltest_new.domain.ThermalTestResult;

@Dao
public interface ThermalTestResultDao {
    @Insert
    public Long add(ThermalTestResult result);
    @Delete
    public void delete(ThermalTestResult result);
    @Query("select * from sample_test_thermal_table where id=:id")
    public ThermalTestResult findById(long id);
    @Query("delete from sample_test_thermal_table where id=:id")
    void deleteById(Integer id);
}
