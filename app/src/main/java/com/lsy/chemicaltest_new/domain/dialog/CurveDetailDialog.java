package com.lsy.chemicaltest_new.domain.dialog;

import static com.blankj.utilcode.util.SnackbarUtils.dismiss;
import static com.lsy.chemicaltest_new.utils.DynamicStringUtils.getString;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.domain.Point;
import com.lsy.chemicaltest_new.domain.Sample;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.utils.CombinedChartUtils;

import java.util.List;

public class CurveDetailDialog extends Dialog {
    private CombinedData mCombinedData = new CombinedData();//联合图数据

    @SuppressLint("SetTextI18n")
    public CurveDetailDialog(Context context, StandardCurve standardCurve) {
        super(context);
        setContentView(R.layout.curve_details_dialog_layout);

        // 获取布局中的控件
        TextView tv_type = findViewById(R.id.tv_type);
        TextView tv_sample = findViewById(R.id.tv_sample);
        TextView tv_name = findViewById(R.id.tv_name);
        TextView tv_x_unit = findViewById(R.id.tv_x_unit);
        TextView tv_y_unit = findViewById(R.id.tv_y_unit);
        TextView tv_min_x = findViewById(R.id.tv_min_x);
        TextView tv_max_x = findViewById(R.id.tv_max_x);
        TextView tv_min_CORR = findViewById(R.id.tv_min_CORR);
        TextView tv_corr = findViewById(R.id.tv_corr);
        TextView tv_expression = findViewById(R.id.tv_expression);
        TextView tv_description = findViewById(R.id.tv_description);
        CombinedChart chart = findViewById(R.id.cc_chart);
        chart.setData(mCombinedData);
        if (standardCurve != null){
            tv_type.setText(StandardCurve.getCurveType(standardCurve.getType()));
            tv_sample.setText(standardCurve.getSample().getName());
            tv_name.setText(standardCurve.getName());
            tv_x_unit.setText(standardCurve.getX_axis_unit());
            tv_y_unit.setText(standardCurve.getY_axis_unit());
            tv_min_x.setText(String.valueOf(standardCurve.getMin_CO()));
            tv_max_x.setText(String.valueOf(standardCurve.getMax_CO()));
            tv_min_CORR.setText(String.valueOf(standardCurve.getMinCorr()));
            tv_corr.setText(String.valueOf(standardCurve.getCORR()));
            tv_expression.setText(standardCurve.getFormula().toString());
            tv_description.setText(standardCurve.getDescription());
            CombinedChartUtils.setChart(chart);
            CombinedChartUtils.buildChart(context,chart,standardCurve.getPointList(),standardCurve.getType(),getString(R.string.unit_lg_c)+standardCurve.getX_axis_unit());
        }

    }
}
