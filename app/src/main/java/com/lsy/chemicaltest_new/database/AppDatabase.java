package com.lsy.chemicaltest_new.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.lsy.chemicaltest_new.dao.BleDeviceInfoDao;
import com.lsy.chemicaltest_new.dao.ColoTestResultDao;
import com.lsy.chemicaltest_new.dao.CurveSettingDao;
import com.lsy.chemicaltest_new.dao.ElecTemperatureDao;
import com.lsy.chemicaltest_new.dao.ElecTestResultDao;
import com.lsy.chemicaltest_new.dao.ExperimenterDao;
import com.lsy.chemicaltest_new.dao.History_multipleDao;
import com.lsy.chemicaltest_new.dao.PointDao;
import com.lsy.chemicaltest_new.dao.SampleDao;
import com.lsy.chemicaltest_new.dao.StandardCurveDao;
import com.lsy.chemicaltest_new.dao.ThermalTestResultDao;
import com.lsy.chemicaltest_new.domain.BleDeviceInfo;
import com.lsy.chemicaltest_new.domain.ColoTestResult;
import com.lsy.chemicaltest_new.domain.CurveSetting;
import com.lsy.chemicaltest_new.domain.ElecTestResult;
import com.lsy.chemicaltest_new.domain.Experimenter;
import com.lsy.chemicaltest_new.domain.History_multiple;
import com.lsy.chemicaltest_new.domain.Point;
import com.lsy.chemicaltest_new.domain.Sample;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.domain.Temperature_Elec;
import com.lsy.chemicaltest_new.domain.ThermalTestResult;


@Database(
        entities = {Experimenter.class, StandardCurve.class, Point.class,
                Sample.class, ElecTestResult.class, BleDeviceInfo.class, ThermalTestResult.class,
                ColoTestResult.class, History_multiple.class, Temperature_Elec.class, CurveSetting.class},
        version = 2,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase mAppDatabase = null;
    private static final String DB_NAME = "chemical.db";
    // TODO 在实例化 AppDatabase 对象时应遵循单例设计模式。每个 RoomDatabase 实例的成本相当高，几乎不需要在单个进程中访问多个实例。
    public static AppDatabase getInstance(Context context) {
        if (mAppDatabase == null) {
            synchronized (AppDatabase.class) {
                if (mAppDatabase == null) {
                    mAppDatabase = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DB_NAME)
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    // 手动启用外键约束
                                    db.execSQL("PRAGMA foreign_keys = ON;");
                                }
                            })
                            // 默认不允许在主线程中连接数据库（建议测试）
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return mAppDatabase;
    }
    public abstract ExperimenterDao getExperimenterDao();
    public abstract StandardCurveDao getStandardCurveDao();
    public abstract PointDao getPointDao();
    public abstract SampleDao getSampleDao();
    public abstract ElecTestResultDao getElecTestResultDao();
    public abstract BleDeviceInfoDao getBleDeviceInfoDao();
    public abstract ThermalTestResultDao getThermalTestResultDao();
    public abstract ColoTestResultDao getColoTestResultDao();
    public abstract History_multipleDao getHistory_multipleDao();
    public abstract ElecTemperatureDao getElecTemperatureDao();
    public abstract CurveSettingDao getCurveSettingDao();

    public String getDbName() {
        return DB_NAME;
    }
}
