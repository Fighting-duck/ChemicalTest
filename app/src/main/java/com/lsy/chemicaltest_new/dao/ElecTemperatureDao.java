package com.lsy.chemicaltest_new.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.lsy.chemicaltest_new.domain.Temperature_Elec;

import java.util.List;

@Dao
public interface ElecTemperatureDao {
    @Insert
    Long add(Temperature_Elec temperature_elec);
    @Delete
    void delete(Temperature_Elec temperature_elec);
    @Query("delete from temperature_elec_table where id=:id"                                                  )
    void deleteById(Integer id);
    @Query("select * from temperature_elec_table where id=:id")
    Temperature_Elec findById(int id);
    @Query("select * from temperature_elec_table")
    List<Temperature_Elec> getAll();
}
