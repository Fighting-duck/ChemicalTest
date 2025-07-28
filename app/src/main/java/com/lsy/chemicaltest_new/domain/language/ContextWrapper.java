package com.lsy.chemicaltest_new.domain.language;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import java.util.Locale;

public class ContextWrapper extends android.content.ContextWrapper {

    public ContextWrapper(Context base) {
        super(base);
    }

    // 包装 Context，设置新语言
    public static ContextWrapper wrap(Context context, String languageCode) {
        Resources res = context.getResources();
        Configuration config = res.getConfiguration();

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale); // API 24+
        } else {
            config.locale = locale; // 旧 API
        }

        // 更新资源配置
        res.updateConfiguration(config, res.getDisplayMetrics());

        return new ContextWrapper(context);
    }
}
