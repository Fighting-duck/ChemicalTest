package com.lsy.chemicaltest_new.utils;

import android.content.Context;

import androidx.annotation.StringRes;

import java.lang.ref.WeakReference;

public class DynamicStringUtils {
    private static WeakReference<Context> contextRef; // 避免内存泄漏

    public static void init(Context context) {
        contextRef = new WeakReference<>(context.getApplicationContext());
    }

    public static String getString(@StringRes int id) {
        if (contextRef == null || contextRef.get() == null) {
            throw new IllegalStateException("Call DynamicStringUtils.init() first!");
        }
        return contextRef.get().getString(id);
    }
}
