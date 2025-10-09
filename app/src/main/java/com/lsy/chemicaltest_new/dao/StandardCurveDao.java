package com.lsy.chemicaltest_new.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lsy.chemicaltest_new.domain.StandardCurve;

import java.util.List;

@Dao
public interface StandardCurveDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    Long add(StandardCurve standardCurve);
    @Delete
    void delete(StandardCurve standardCurve);
    @Update
    void update(StandardCurve standardCurve);
    @Query("SELECT * FROM standard_curve_table")
    List<StandardCurve> getAll();
    @Query("SELECT * FROM standard_curve_table where validity=1")
    List<StandardCurve> getAll_Available();
    @Query("DELETE FROM standard_curve_table")
    void deleteAll();
    @Query("DELETE FROM standard_curve_table WHERE id=:id")
    void deleteById(Integer id);
    @Query("SELECT * FROM standard_curve_table WHERE id = :id")
    StandardCurve findById(long id);
    @Query("SELECT * FROM standard_curve_table WHERE sample_id = :sampleId")
    List<StandardCurve> findBySampleId(int sampleId);

    /***
     * 使曲线不可用
     * @return
     */
    @Query("Update standard_curve_table set validity=:validity where id=:id")
    void updateValidity(Integer id,Integer validity);
    @Query("SELECT * FROM standard_curve_table WHERE name = :name and validity=1")
    StandardCurve findByName(String name);
    @Query("SELECT * FROM standard_curve_table WHERE name = :sampleId")
    List<StandardCurve> findBy_sampleId(int sampleId);
    @Query("DELETE FROM standard_curve_table WHERE id = :id")
    void deleteById(long id);
    @Query("SELECT sc.* FROM standard_curve_table sc " +
            "JOIN sample_table st ON sc.sample_id = st.id " +
            "WHERE (sc.name LIKE '%' || :filter || '%' OR st.name LIKE '%' || :filter || '%') " +
            "AND sc.validity = 1")
    List<StandardCurve> findByFuzzy(String filter);
    @Query("SELECT sc.x_axis_unit FROM standard_curve_table sc WHERE sc.id = :curveId")
    String find_XUint_ById(int curveId);
}
