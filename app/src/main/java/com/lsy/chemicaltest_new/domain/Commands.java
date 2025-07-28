package com.lsy.chemicaltest_new.domain;

public class Commands {
    public static final byte CMD_MAX_MIN = 0x41; // Max/Min（最大值最小值测量按钮）
    public static final byte CMD_EXIT_MAX_MIN = 0x42; // Exit Max/Min（退出最大值最小值测量按钮）
    public static final byte CMD_INRUSH = 0x43; // INRUSH（进入浪涌电流测试按钮）
    public static final byte CMD_EXIT_INRUSH = 0x44; // Exit INRUSH（退出浪涌电流测试按钮）
    public static final byte CMD_ZERO = 0x45; // ZERO（直流电流清零按钮）
    public static final byte CMD_MANUAL_RANGE = 0x46; // Manual Range（手动量程按钮）
    public static final byte CMD_AUTO_RANGE = 0x47; // Auto Range（自动量程按钮）
    public static final byte CMD_REL = 0x48; // REL（相对值按钮）
    public static final byte CMD_HZ_PERCENT = 0x49; // Hz %（频率/占空比按钮）
    public static final byte CMD_HOLD = 0x4A; // Hold（数据保持按钮）
    public static final byte CMD_LIGHT = 0x4B; // Light（背光按钮）
    public static final byte CMD_SELECT = 0x4C; // Select（功能选择按钮）
    public static final byte CMD_PEAK_MAX_MIN = 0x4D; // PEAK Max/Min（进入峰值最大值/最小值按钮）
    public static final byte CMD_EXIT_PEAK_MAX_MIN = 0x4E; // Exit PEAK Max/Min（退出峰值按钮）
    public static final byte CMD_FLIGHT = 0x4F; // Flight（手电筒按钮）
    public static final byte CMD_ENABLE_DISPLAY_VALUE = 0x5E; // 使能发送显示值
    public static final byte CMD_ENABLE_MODEL = 0x5F; // 使能发送产品型号
}
