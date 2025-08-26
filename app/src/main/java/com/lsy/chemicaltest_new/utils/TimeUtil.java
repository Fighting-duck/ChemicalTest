package com.lsy.chemicaltest_new.utils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
    /***
     * 时间HH:mm:ss  values值*(单位s)转化为时间(hh:mm:ss)
     * value = 3600*hours(0-24) + 60*minutes(0-60) + seconds(0-6o)
     * @param value
     * @return
     */
    @SuppressLint("DefaultLocale")
    public static String timeNumToStr(float value) {
        int hours = (int) (value / 3600);
        int minutes = (int) ((value % 3600) / 60);
        int seconds = (int) (value % 60);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * 获取当前日期+时间 YYYY-MM-DD HH:mm:ss
     *
     * @return 返回当天的日期+时间
     */
    @SuppressLint("SimpleDateFormat")
    public static String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    /**
     * 根据给定的毫秒时间戳（HH:mm:ss 值），计算自当天午夜以来的总秒数。
     *
     * @param currentTime 毫秒时间戳
     * @return 自当天午夜以来的总秒数 3600 * hours + 60 * minutes + seconds
     */
    @SuppressLint("SimpleDateFormat")
    public static float timeStrToNum(long currentTime) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date(currentTime);
        String time = format.format(date).trim();
        String[] split1 = time.split(":");
        int hours = Integer.parseInt(split1[0]);
        int minutes = Integer.parseInt(split1[1]);
        int seconds = Integer.parseInt(split1[2]);
        return 3600 * hours + 60 * minutes + seconds;
    }
}
