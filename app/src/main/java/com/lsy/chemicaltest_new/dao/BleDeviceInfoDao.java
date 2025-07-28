package com.lsy.chemicaltest_new.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.lsy.chemicaltest_new.domain.BleDeviceInfo;

@Dao
public interface BleDeviceInfoDao {
    @Insert
    Long add(BleDeviceInfo bleDeviceInfo);
    @Delete
    void delete(BleDeviceInfo bleDeviceInfo);
    @Query("SELECT * FROM BleDeviceInfo WHERE id=:id")
    BleDeviceInfo findById(int id);
    @Query("SELECT * FROM BleDeviceInfo WHERE gear=:gear")
    BleDeviceInfo findByGear(String gear);
}
