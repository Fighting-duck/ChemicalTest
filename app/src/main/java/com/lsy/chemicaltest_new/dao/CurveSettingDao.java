package com.lsy.chemicaltest_new.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.lsy.chemicaltest_new.domain.CurveSetting;

import java.util.List;

@Dao
public interface CurveSettingDao {
    @Insert
    void insert(CurveSetting curveSetting);
    @Query("SELECT * FROM curve_setting_table WHERE id = :id")
    CurveSetting getById(int id);
    @Query("SELECT COUNT(*) FROM curve_setting_table")
    Integer getCount();
    @Update
    void update(CurveSetting curveSetting);
    @Query("SELECT * FROM curve_setting_table")
    List<CurveSetting> findAll();

}
