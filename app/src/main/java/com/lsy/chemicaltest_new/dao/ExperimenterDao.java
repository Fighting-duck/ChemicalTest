package com.lsy.chemicaltest_new.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.lsy.chemicaltest_new.domain.Experimenter;

import java.util.List;

@Dao
public interface ExperimenterDao {
    @Delete
    void delete(Experimenter... experimenters);
    @Query("DELETE  FROM experimenter_table")
    void findAll();
    @Query("SELECT * FROM experimenter_table")
    List<Experimenter> getAll();
    @Query("SELECT * FROM experimenter_table ORDER BY ID DESC")
    List<Experimenter> getAllUsersDesc();

    @Query("SELECT * FROM experimenter_table WHERE id = :id")
    Experimenter findById(Integer id);
    @Query("SELECT COUNT(*) FROM experimenter_table")
    Integer getCount();
    @Insert
    void insert(Experimenter experimenter);
    @Update
    void update(Experimenter experimenter);
}
