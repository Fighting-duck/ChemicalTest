package com.lsy.chemicaltest_new.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.activitys.history.HistoryPreviewActivity;
import com.lsy.chemicaltest_new.domain.ColoTestResult;
import com.lsy.chemicaltest_new.domain.ElecTestResult;
import com.lsy.chemicaltest_new.domain.History_multiple;
import com.lsy.chemicaltest_new.domain.StandardCurve;
import com.lsy.chemicaltest_new.domain.Temperature_Elec;
import com.lsy.chemicaltest_new.domain.ThermalTestResult;
import com.lsy.chemicaltest_new.models.HistoryPreviewViewModel;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    public interface OnViewClickListener {
        void onClick(History_multiple  history_multiple);
    }
    private OnViewClickListener onViewClickListener;
    public void setOnViewClickListener(OnViewClickListener listener) {
        this.onViewClickListener = listener;
    }
    private static final String TAG = "HistoryAdapter";
    private List<History_multiple> mHistoryList= new ArrayList<History_multiple>();

    public HistoryAdapter(){
    }
    //创建内部ViewHolder
    static class ViewHolder extends RecyclerView.ViewHolder{
        View view;
        TextView tv_name;
        TextView tv_time;
        TextView tv_CO_elec;
        TextView tv_CO_colo;
        TextView tv_CO_thermal;
        TextView tv_credibility;

        @SuppressLint("CutPasteId")
        public ViewHolder(@NonNull View view){
            super(view);
            this.view = view;
            tv_name =  view.findViewById(R.id.tv_name);
            tv_time = view.findViewById(R.id.tv_time);
            tv_CO_elec = view.findViewById(R.id.tv_CO_elec);
            tv_CO_colo = view.findViewById(R.id.tv_CO_colo);
            tv_CO_thermal = view.findViewById(R.id.tv_CO_thermal);
            tv_credibility = view.findViewById(R.id.tv_credibility);
        }
    }

    @NonNull
    @Override
    //调用时机：初始化时、滚动时、数据变化时、布局变化时
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //定义一个构造
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        //创建布局
        View view  = layoutInflater.inflate(R.layout.item_history,parent,false);
        //创建ViewHolder实例
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    //调用时机：视图可见时、数据变化时、ViewHolder 被回收和重用时、布局变化时
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        History_multiple history_multiple = mHistoryList.get(position);
        String name = history_multiple.getHistoryName();
        String dateTime = history_multiple.getSaveTime();
        ElecTestResult elecTestResult = history_multiple.getElecTestResult();
        Temperature_Elec temperature_elec = history_multiple.getTemperature_elec();
        ColoTestResult coloTestResult = history_multiple.getColoTestResult();
        ThermalTestResult thermalTestResult = history_multiple.getThermalTestResult();

        if (elecTestResult!=null){
            String CO_elec = elecTestResult.getDetectionCo()+"";
            String x_axis_unit = "";
            if (elecTestResult.getStandard_curve_id()!=null)
                x_axis_unit = getX_axis_unit(elecTestResult.getStandard_curve_id());
            holder.tv_CO_elec.setText(CO_elec + " " + x_axis_unit);
        }

        if (coloTestResult!=null){
            String CO_colo = coloTestResult.getDetectionCo()+"";
            String x_axis_unit = "";
            if (coloTestResult.getStandard_curve_id()!=null)
                x_axis_unit = getX_axis_unit(coloTestResult.getStandard_curve_id());
            holder.tv_CO_colo.setText(CO_colo + " " + x_axis_unit);
        }
        if (thermalTestResult!=null){
            String CO_thermal = thermalTestResult.getDetectionCo()+"";
            String x_axis_unit = "";
            if (thermalTestResult.getStandard_curve_id()!=null)
                x_axis_unit = getX_axis_unit(thermalTestResult.getStandard_curve_id());
            holder.tv_CO_thermal.setText(CO_thermal + " " + x_axis_unit);
        }
        else  if (temperature_elec!=null){
            String CO_temp = temperature_elec.getDetectionCo()+"";
            String x_axis_unit = "";
            if (temperature_elec.getStandard_curve_id()!=null)
                x_axis_unit = getX_axis_unit(temperature_elec.getStandard_curve_id());
            holder.tv_CO_thermal.setText(CO_temp + " " + x_axis_unit);
        }
        String credibility = history_multiple.getCredibility()+"";
        holder.tv_name.setText(name);
        holder.tv_time.setText(dateTime);
        holder.tv_credibility.setText(credibility);

        holder.view.setOnClickListener(view1 -> {
            if (onViewClickListener != null) {
                onViewClickListener.onClick(mHistoryList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mHistoryList == null)
            return 0;
        return mHistoryList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void update(List<History_multiple> history_multiples) {
        mHistoryList = history_multiples;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear(){
        mHistoryList.clear();
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void remove(int position){
        mHistoryList.remove(position);
        notifyDataSetChanged();
    }

    // 根据曲线id查询x_axis_unit
    public String getX_axis_unit(int curve_id){
        return MyApplication.DATABASE_INSTANCE.getStandardCurveDao().find_XUint_ById(curve_id);
    }
}

