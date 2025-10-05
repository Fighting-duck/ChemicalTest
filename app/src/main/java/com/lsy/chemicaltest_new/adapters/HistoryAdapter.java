package com.lsy.chemicaltest_new.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.lsy.chemicaltest_new.MyApplication;
import com.lsy.chemicaltest_new.R;
import com.lsy.chemicaltest_new.domain.ColoTestResult;
import com.lsy.chemicaltest_new.domain.ElecTestResult;
import com.lsy.chemicaltest_new.domain.History_multiple;
import com.lsy.chemicaltest_new.domain.Temperature_Elec;
import com.lsy.chemicaltest_new.domain.ThermalTestResult;
import com.lsy.chemicaltest_new.utils.MultiSelectHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryAdapter extends ListAdapter<History_multiple,HistoryAdapter.ViewHolder> {
    // 监听器
    public interface OnEditClickListener {
        void onEditClick(int position);
    }
    private final MultiSelectHelper multiSelectHelper = new MultiSelectHelper();//多选模式辅助类
    private static final String TAG = "HistoryAdapter";
    private final Context mContext;
    private OnEditClickListener editClickListener;

    // 设置监听器
    public void setEditClickListener(OnEditClickListener listener) {
        this.editClickListener = listener;
    }
    public void setOnMultiSelectListener(MultiSelectHelper.OnMultiSelectListener listener) {
        multiSelectHelper.setMultiSelectListener(listener);
    }

    private static final DiffUtil.ItemCallback<History_multiple> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<History_multiple>() {
                @Override
                public boolean areItemsTheSame(@NonNull History_multiple oldItem, @NonNull History_multiple newItem) {
                    return oldItem == newItem;
                }

                @Override
                public boolean areContentsTheSame(@NonNull History_multiple oldItem, @NonNull History_multiple newItem) {
                    return oldItem.equals(newItem);
                }
            };
    public HistoryAdapter(Context  context){
        super(DIFF_CALLBACK);
        mContext = context;
    }
    //创建内部ViewHolder
    static class ViewHolder extends RecyclerView.ViewHolder{
        final TextView tv_name, tv_time, tv_CO_elec, tv_CO_colo,tv_CO_thermal,tv_credibility;
        final RadioButton radio_btn;
        final LinearLayout ll_main;

        @SuppressLint("CutPasteId")
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            tv_name =  itemView.findViewById(R.id.tv_name);
            tv_time = itemView.findViewById(R.id.tv_time);
            tv_CO_elec = itemView.findViewById(R.id.tv_CO_elec);
            tv_CO_colo = itemView.findViewById(R.id.tv_CO_colo);
            tv_CO_thermal = itemView.findViewById(R.id.tv_CO_thermal);
            tv_credibility = itemView.findViewById(R.id.tv_credibility);
            radio_btn = itemView.findViewById(R.id.radio_btn);
            ll_main = itemView.findViewById(R.id.ll_main);
        }
        @SuppressLint("SetTextI18n")
        void bind(History_multiple history_multiple) {
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
                tv_CO_elec.setText(CO_elec + " " + x_axis_unit);
            }

            if (coloTestResult!=null){
                String CO_colo = coloTestResult.getDetectionCo()+"";
                String x_axis_unit = "";
                if (coloTestResult.getStandard_curve_id()!=null)
                    x_axis_unit = getX_axis_unit(coloTestResult.getStandard_curve_id());
                tv_CO_colo.setText(CO_colo + " " + x_axis_unit);
            }
            if (thermalTestResult!=null){
                String CO_thermal = thermalTestResult.getDetectionCo()+"";
                String x_axis_unit = "";
                if (thermalTestResult.getStandard_curve_id()!=null)
                    x_axis_unit = getX_axis_unit(thermalTestResult.getStandard_curve_id());
                tv_CO_thermal.setText(CO_thermal + " " + x_axis_unit);
            }
            else  if (temperature_elec!=null){
                String CO_temp = temperature_elec.getDetectionCo()+"";
                String x_axis_unit = "";
                if (temperature_elec.getStandard_curve_id()!=null)
                    x_axis_unit = getX_axis_unit(temperature_elec.getStandard_curve_id());
                tv_CO_thermal.setText(CO_temp + " " + x_axis_unit);
            }
            String credibility = history_multiple.getCredibility()+"";
            tv_name.setText(name);
            tv_time.setText(dateTime);
            tv_credibility.setText(credibility);
        }
        // 根据曲线id查询x_axis_unit
        public String getX_axis_unit(int curve_id){
            return MyApplication.DATABASE_INSTANCE.getStandardCurveDao().find_XUint_ById(curve_id);
        }
        @SuppressLint("UseCompatLoadingForDrawables")
        void updateSelectionStyle(boolean isMultiSelectMode, boolean isSelected, Context  context) {
            // 更新背景样式
            if (isMultiSelectMode && isSelected){
                ll_main.setBackground(context.getDrawable(R.drawable.item_selected_background));
            }
            else {
                ll_main.setBackground(context.getDrawable(R.drawable.item_unselected_background));
            }
            // 缩放动画
            float scale = isMultiSelectMode && isSelected ? 0.94f : 1.0f;
            ll_main.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .setDuration(200) // 动画时长（毫秒）
                    .start();
            radio_btn.setChecked(isMultiSelectMode && isSelected);// 多选模式下单选按钮可见
        }
    }

    @NonNull
    @Override
    //调用时机：初始化时、滚动时、数据变化时、布局变化时
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    //调用时机：视图可见时、数据变化时、ViewHolder 被回收和重用时、布局变化时
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        History_multiple item = getItem(position);
        if (item == null) return;

        holder.bind(item);
        // 更新多选模式样式
        holder.updateSelectionStyle(multiSelectHelper.isMultiSelectMode(),
                multiSelectHelper.getSelectedPositions().contains(position),mContext);
        // 根据多选模式控制单选按钮显隐
        holder.radio_btn.setVisibility(
                multiSelectHelper.isMultiSelectMode() ? View.VISIBLE : View.GONE
        );
        // 设置点击监听器
        setClickListeners(holder, position);
    }
    private void setClickListeners(HistoryAdapter.ViewHolder holder, int position) {
        // 长按开启多选模式
        holder.ll_main.setOnLongClickListener(v -> {
            selectItem(position);
            return true;
        });
        // 点击进入编辑模式
        holder.ll_main.setOnClickListener(v -> {
            if (multiSelectHelper.isMultiSelectMode()) {
                holder.radio_btn.setChecked(true);
                selectItem( position);
            }else {
                if (editClickListener != null) editClickListener.onEditClick(position);
            }
        });
        // 单选按钮点击
        holder.radio_btn.setOnClickListener(v -> {
            if (multiSelectHelper.isMultiSelectMode()) {
               selectItem(position);
            }
        });
    }
    private void selectItem(int position) {
        // 1. 选中当前项
        multiSelectHelper.toggleSelection(position);
        // 2. 通知刷新所有可见项（使单选按钮显示）
        notifyItemRangeChanged(0, getItemCount());
    }
    /**
     * 退出多选模式
     */
    @SuppressLint("NotifyDataSetChanged")
    public void exitMultiSelectMode() {
        multiSelectHelper.clearSelection();
        notifyDataSetChanged();
    }
    /**
     * 判断是否处于多选模式
     */
    public boolean isMultiSelectMode() {
        return multiSelectHelper.isMultiSelectMode();
    }
    /**
     * 获取选中的项
     */
    public List<History_multiple> getSelectedItems() {
        List<History_multiple> selectedItems = new ArrayList<>();
        for (Integer pos : multiSelectHelper.getSelectedPositions()) {
            selectedItems.add(getItem(pos));
        }
        return selectedItems;
    }
    /**
     * 删除选中项
     */
    public void deleteSelectedItems() {
        List<History_multiple> newList = new ArrayList<>(getCurrentList());// 创建一个新的列表,所有标准曲线
        List<Integer> sortedPositions = new ArrayList<>(multiSelectHelper.getSelectedPositions());
        Collections.sort(sortedPositions, Collections.reverseOrder());// 倒序排序
        for (int pos : sortedPositions) {
            if (pos >= 0 && pos < newList.size()) {
                newList.remove(pos);
            }
        }
        submitList(newList);
        multiSelectHelper.clearSelection();
    }
}

