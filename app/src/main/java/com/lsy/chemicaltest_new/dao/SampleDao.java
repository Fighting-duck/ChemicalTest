package com.lsy.chemicaltest_new.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.lsy.chemicaltest_new.domain.Sample;
import com.lsy.chemicaltest_new.domain.StandardCurve;

import java.util.List;

@Dao
public interface SampleDao {
    @Insert
    Long add(Sample sample);
    @Delete
    void delete(Sample... samples);
    @Update
    int update(Sample sample);
    @Query("select * from sample_table")
    List<Sample> getAll();
    @Query("select * from sample_table where validity=1")
    List<Sample> getAll_Available();
    @Query("SELECT * FROM sample_table WHERE id =:id")
    Sample findById(Integer id);
    @Query("UPDATE sample_table SET validity =:validity WHERE id =:id")
    int updateValidity(Integer id,int validity);
    @Query("Delete from sample_table WHERE id =:id")
    int delete(Integer id);
    @Query("SELECT * FROM sample_table WHERE name =:name")
    Sample findBy_name(String name);
    /**
     * 查询除 filterName 之外，名字为 name 的样品列表
     * @param name 要查询的样品名字
     * @param filterName 需要排除的样品名字
     * @return 符合条件的样品列表
     */
    @Query("SELECT * FROM sample_table WHERE name = :name AND name != :filterName")
    List<Sample> findByNameExcludingFilter(String name,String filterName);

    @Query("select name from sample_table where validity=1")
    List<String> getAll_AvailableName();

    @Query("select id from sample_table where name=:name")
    Integer getSampleByName(String name);

    @Query("select id from sample_table where name like '%' || :sampleName || '%'")
    List<Integer> getSampleIdLikeName(String sampleName);

    @Query("select * from sample_table where name=:name")
    Sample findByName(String name);
}
